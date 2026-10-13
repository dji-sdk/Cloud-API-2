// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "network_impl.h"
#include "base/common/logger.h"
#include "common/log_safe.h"

#ifndef __EMSCRIPTEN__

namespace drc_sdk {
namespace common {
namespace injector {

// FH36-P2-02: 请求/响应体与请求头(含鉴权头)内容日志统一经 log_safe()
using drc_sdk::common::log_safe;


NetworkImpl::NetworkImpl() {
    LOG_INFO("NetworkImpl::NetworkImpl");
}

NetworkImpl::~NetworkImpl() {
    LOG_INFO("NetworkImpl::~NetworkImpl");
}

void NetworkImpl::get(const std::string &url, const std::vector<HttpHeader> &headers,
             drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms) {
    uint64_t request_id = GenerateRequestId();

    std::string headers_str;
    std::string x_request_id;
    drc_sdk::common::network::RequestHeader request_headers;
    for (const auto &header : headers) {
        request_headers[header.key] = header.value;
        if (!headers_str.empty()) headers_str += ", ";
        headers_str += header.key + ": " + header.value;
        if (header.key == "x-request-id") x_request_id = header.value;
    }
    LOG_DEBUG("[NetworkImpl] GET url: %s, x-request-id: %s, headers: [%s]",
             url.c_str(), x_request_id.c_str(), log_safe(headers_str).c_str());

    auto request = std::make_shared<drc_sdk::common::network::HttpSyncRequest>();
    request->SetURL(url);
    request->SetHeader(request_headers);
    // 使用默认的ByteBuffer来累积数据，不设置自定义回调
    // request->OnDataWrite() 不调用，让数据累积到response_buffer_中
    if (const auto curl_code = request->Get(timeout_ms); curl_code != CURLE_OK) {
        LOG_INFO("[NetworkImpl] http curl_code is %d", curl_code);
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(), {}, {});
        return;
    }

    // 获取完整的响应数据（需在状态码检查之前获取，以便错误时透传响应体）
    std::string response_data = request->GetResponseData();
    long status_code = request->StatusCode();
    LOG_DEBUG("[NetworkImpl] GET status_code: %ld, x-request-id: %s, response: %s",
             status_code, x_request_id.c_str(), log_safe(response_data).c_str());
    if (status_code >= 400) {
        LOG_ERROR("[NetworkImpl] http error status_code: %ld, x-request-id: %s, headers: [%s], response: %s",
                  status_code, x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(response_data).c_str());
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(response_data.begin(), response_data.end()), {}, {});
        return;
    }
    requests_.erase(request_id);
    callback(NetworkError::SUCCESS, Bytes(response_data.begin(), response_data.end()), {}, {});
}

void NetworkImpl::post(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
    const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms) {
    uint64_t request_id = GenerateRequestId();

    std::string headers_str;
    std::string x_request_id;
    drc_sdk::common::network::RequestHeader request_headers;
    for (const auto &header : headers) {
        request_headers[header.key] = header.value;
        if (!headers_str.empty()) headers_str += ", ";
        headers_str += header.key + ": " + header.value;
        if (header.key == "x-request-id") x_request_id = header.value;
    }
    LOG_DEBUG("[NetworkImpl] POST url: %s, x-request-id: %s, headers: [%s], body: %s",
             url.c_str(), x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(body).c_str());
    
    // 根据post_type设置Content-Type，如果用户没有手动设置的话
    if (request_headers.find("Content-Type") == request_headers.end()) {
        switch (post_type) {
            case HttpPostType::JSON_DATA:
                request_headers["Content-Type"] = "application/json";
                LOG_INFO("NetworkImpl::post setting Content-Type to application/json");
                break;
            case HttpPostType::X_WWW_FORM_DATA:
                request_headers["Content-Type"] = "application/x-www-form-urlencoded";
                LOG_INFO("NetworkImpl::post setting Content-Type to application/x-www-form-urlencoded");
                break;
            default:
                LOG_WARN("NetworkImpl::post unknown post_type: %d, using default Content-Type", static_cast<int>(post_type));
                break;
        }
    } else {
        LOG_INFO("NetworkImpl::post Content-Type already set by user: %s", request_headers["Content-Type"].c_str());
    }
    auto request = std::make_shared<drc_sdk::common::network::HttpSyncRequest>();
    request->SetURL(url);
    request->SetHeader(request_headers);
    // 使用默认的ByteBuffer来累积数据，不设置自定义回调
    // request->OnDataWrite() 不调用，让数据累积到response_buffer_中
    if (const auto curl_code = request->Post(body, timeout_ms); curl_code != CURLE_OK) {
        LOG_INFO("[NetworkImpl] http curl_code is %d", curl_code);
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(), {}, {});
        return;
    }
    
    // 检查HTTP状态码
    // 获取完整的响应数据（需在状态码检查之前获取，以便错误时透传响应体）
    std::string response_data = request->GetResponseData();
    long status_code = request->StatusCode();
    LOG_DEBUG("[NetworkImpl] POST status_code: %ld, x-request-id: %s, response: %s",
             status_code, x_request_id.c_str(), log_safe(response_data).c_str());
    if (status_code >= 400) {
        LOG_ERROR("[NetworkImpl] http error status_code: %ld, x-request-id: %s, headers: [%s], response: %s",
                  status_code, x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(response_data).c_str());
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(response_data.begin(), response_data.end()), {}, {});
        return;
    }

    // 获取完整的响应数据
    requests_.erase(request_id);
    callback(NetworkError::SUCCESS, Bytes(response_data.begin(), response_data.end()), {}, {});
}

void NetworkImpl::put_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
    const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms) {
    uint64_t request_id = GenerateRequestId();

    std::string headers_str;
    std::string x_request_id;
    drc_sdk::common::network::RequestHeader request_headers;
    for (const auto &header : headers) {
        request_headers[header.key] = header.value;
        if (!headers_str.empty()) headers_str += ", ";
        headers_str += header.key + ": " + header.value;
        if (header.key == "x-request-id") x_request_id = header.value;
    }
    LOG_DEBUG("[NetworkImpl] PUT url: %s, x-request-id: %s, headers: [%s], body: %s",
             url.c_str(), x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(body).c_str());
    
    // 根据post_type设置Content-Type，如果用户没有手动设置的话
    if (request_headers.find("Content-Type") == request_headers.end()) {
        switch (post_type) {
            case HttpPostType::JSON_DATA:
                request_headers["Content-Type"] = "application/json";
                LOG_INFO("NetworkImpl::put_request setting Content-Type to application/json");
                break;
            case HttpPostType::X_WWW_FORM_DATA:
                request_headers["Content-Type"] = "application/x-www-form-urlencoded";
                LOG_INFO("NetworkImpl::put_request setting Content-Type to application/x-www-form-urlencoded");
                break;
            default:
                LOG_WARN("NetworkImpl::put_request unknown post_type: %d, using default Content-Type", static_cast<int>(post_type));
                break;
        }
    } else {
        LOG_INFO("NetworkImpl::put_request Content-Type already set by user: %s", request_headers["Content-Type"].c_str());
    }
    auto request = std::make_shared<drc_sdk::common::network::HttpSyncRequest>();
    request->SetURL(url);
    request->SetHeader(request_headers);
    // 使用默认的ByteBuffer来累积数据，不设置自定义回调
    // request->OnDataWrite() 不调用，让数据累积到response_buffer_中
    if (const auto curl_code = request->Put(body, timeout_ms); curl_code != CURLE_OK) {
        LOG_INFO("[NetworkImpl] http curl_code is %d", curl_code);
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(), {}, {});
        return;
    }
    // 检查HTTP状态码
    // 获取完整的响应数据（需在状态码检查之前获取，以便错误时透传响应体）
    std::string response_data = request->GetResponseData();
    long status_code = request->StatusCode();
    LOG_DEBUG("[NetworkImpl] PUT status_code: %ld, x-request-id: %s, response: %s",
             status_code, x_request_id.c_str(), log_safe(response_data).c_str());
    if (status_code >= 400) {
        LOG_ERROR("[NetworkImpl] http error status_code: %ld, x-request-id: %s, headers: [%s], response: %s",
                  status_code, x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(response_data).c_str());
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(response_data.begin(), response_data.end()), {}, {});
        return;
    }

    // 获取完整的响应数据
    requests_.erase(request_id);
    callback(NetworkError::SUCCESS, Bytes(response_data.begin(), response_data.end()), {}, {});
}

void NetworkImpl::delete_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
    const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms) {
    uint64_t request_id = GenerateRequestId();

    std::string headers_str;
    std::string x_request_id;
    drc_sdk::common::network::RequestHeader request_headers;
    for (const auto &header : headers) {
        request_headers[header.key] = header.value;
        if (!headers_str.empty()) headers_str += ", ";
        headers_str += header.key + ": " + header.value;
        if (header.key == "x-request-id") x_request_id = header.value;
    }
    LOG_DEBUG("[NetworkImpl] DELETE url: %s, x-request-id: %s, headers: [%s], body: %s",
             url.c_str(), x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(body).c_str());
    auto request = std::make_shared<drc_sdk::common::network::HttpSyncRequest>();
    request->SetURL(url);
    request->SetHeader(request_headers);
    // 使用默认的ByteBuffer来累积数据，不设置自定义回调
    // request->OnDataWrite() 不调用，让数据累积到response_buffer_中
    if (const auto curl_code = request->Delete(body, timeout_ms); curl_code != CURLE_OK) {
        LOG_INFO("[NetworkImpl] http curl_code is %d", curl_code);
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(), {}, {});
        return;
    }
    // 检查HTTP状态码
    // 获取完整的响应数据（需在状态码检查之前获取，以便错误时透传响应体）
    std::string response_data = request->GetResponseData();
    long status_code = request->StatusCode();
    LOG_DEBUG("[NetworkImpl] DELETE status_code: %ld, x-request-id: %s, response: %s",
             status_code, x_request_id.c_str(), log_safe(response_data).c_str());
    if (status_code >= 400) {
        LOG_ERROR("[NetworkImpl] http error status_code: %ld, x-request-id: %s, headers: [%s], response: %s",
                  status_code, x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(response_data).c_str());
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(response_data.begin(), response_data.end()), {}, {});
        return;
    }

    // 获取完整的响应数据
    requests_.erase(request_id);
    callback(NetworkError::SUCCESS, Bytes(response_data.begin(), response_data.end()), {}, {});
}

void NetworkImpl::form_post(const std::string &url, const std::vector<HttpHeader> &headers,
                   const std::vector<std::string> &body, drc_sdk::common::injector::HttpCallback &&callback) {
    uint64_t request_id = GenerateRequestId();

    std::string headers_str;
    std::string x_request_id;
    drc_sdk::common::network::RequestHeader request_headers;
    for (const auto &header : headers) {
        request_headers[header.key] = header.value;
        if (!headers_str.empty()) headers_str += ", ";
        headers_str += header.key + ": " + header.value;
        if (header.key == "x-request-id") x_request_id = header.value;
    }
    LOG_INFO("[NetworkImpl] FORM POST url: %s, x-request-id: %s, file_count: %zu",
             url.c_str(), x_request_id.c_str(), body.size());

    auto request = std::make_shared<drc_sdk::common::network::HttpSyncRequest>();
    request->SetURL(url);
    // 不设置 Content-Type，让 curl_mime 自动生成 multipart boundary
    drc_sdk::common::network::RequestHeader headers_without_ct;
    for (const auto& [k, v] : request_headers) {
        if (k != "Content-Type") {
            headers_without_ct[k] = v;
        }
    }
    request->SetHeader(headers_without_ct);

    if (const auto curl_code = request->PostMultipartFiles(body, "files", 60000); curl_code != CURLE_OK) {
        LOG_ERROR("[NetworkImpl] FORM POST curl_code is %d", curl_code);
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(), {}, {});
        return;
    }

    std::string response_data = request->GetResponseData();
    long status_code = request->StatusCode();
    LOG_DEBUG("[NetworkImpl] FORM POST status_code: %ld, x-request-id: %s, response: %s",
             status_code, x_request_id.c_str(), log_safe(response_data).c_str());
    if (status_code >= 400) {
        LOG_ERROR("[NetworkImpl] FORM POST http error status_code: %ld", status_code);
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, Bytes(response_data.begin(), response_data.end()), {}, {});
        return;
    }

    requests_.erase(request_id);
    callback(NetworkError::SUCCESS, Bytes(response_data.begin(), response_data.end()), {}, {});
}

int32_t NetworkImpl::download(const std::string &url, const std::vector<HttpHeader> &headers, const std::string &path, int32_t max_retry,
    drc_sdk::common::DownloadProcess &&process, drc_sdk::common::DownloadResult &&complete) {
    // TODO: implement
    return 0;
}

void NetworkImpl::cancel_download(int32_t handle) {
    // TODO: implement
}

int32_t NetworkImpl::stream(const std::string &url, const std::vector<HttpHeader> &headers,
                   drc_sdk::common::StreamData &&callback, drc_sdk::common::StreamResult &&complete) {
    // TODO: implement
    return 0;
}

void NetworkImpl::cancel_stream(int32_t handle) {
    // TODO: implement
}

uint64_t NetworkImpl::GenerateRequestId() {
    static uint64_t request_id = 0;
    return request_id++;
}

// ===== 带 HTTP 状态码的重载实现（HttpCallbackWithStatus）=====

void NetworkImpl::post(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
    const std::string &body, HttpCallbackWithStatus &&callback, uint32_t timeout_ms) {
    uint64_t request_id = GenerateRequestId();

    std::string headers_str;
    std::string x_request_id;
    drc_sdk::common::network::RequestHeader request_headers;
    for (const auto &header : headers) {
        request_headers[header.key] = header.value;
        if (!headers_str.empty()) headers_str += ", ";
        headers_str += header.key + ": " + header.value;
        if (header.key == "x-request-id") x_request_id = header.value;
    }
    LOG_DEBUG("[NetworkImpl] POST(WithStatus) url: %s, x-request-id: %s, headers: [%s], body: %s",
             url.c_str(), x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(body).c_str());

    if (request_headers.find("Content-Type") == request_headers.end()) {
        switch (post_type) {
            case HttpPostType::JSON_DATA:
                request_headers["Content-Type"] = "application/json"; break;
            case HttpPostType::X_WWW_FORM_DATA:
                request_headers["Content-Type"] = "application/x-www-form-urlencoded"; break;
            default: break;
        }
    }
    auto request = std::make_shared<drc_sdk::common::network::HttpSyncRequest>();
    request->SetURL(url);
    request->SetHeader(request_headers);
    if (const auto curl_code = request->Post(body, timeout_ms); curl_code != CURLE_OK) {
        LOG_INFO("[NetworkImpl] POST(WithStatus) curl_code is %d", curl_code);
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, 0, Bytes(), {}, {});
        return;
    }
    std::string response_data = request->GetResponseData();
    int status_code = (int)request->StatusCode();
    LOG_DEBUG("[NetworkImpl] POST(WithStatus) status_code: %d, x-request-id: %s, response: %s",
             status_code, x_request_id.c_str(), log_safe(response_data).c_str());
    requests_.erase(request_id);
    if (status_code >= 400) {
        LOG_ERROR("[NetworkImpl] POST(WithStatus) http error status_code: %d, x-request-id: %s, response: %s",
                  status_code, x_request_id.c_str(), log_safe(response_data).c_str());
        callback(NetworkError::FAILURE, status_code,
                 Bytes(response_data.begin(), response_data.end()), {}, {});
        return;
    }
    callback(NetworkError::SUCCESS, status_code,
             Bytes(response_data.begin(), response_data.end()), {}, {});
}

void NetworkImpl::put_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
    const std::string &body, HttpCallbackWithStatus &&callback, uint32_t timeout_ms) {
    uint64_t request_id = GenerateRequestId();

    std::string headers_str;
    std::string x_request_id;
    drc_sdk::common::network::RequestHeader request_headers;
    for (const auto &header : headers) {
        request_headers[header.key] = header.value;
        if (!headers_str.empty()) headers_str += ", ";
        headers_str += header.key + ": " + header.value;
        if (header.key == "x-request-id") x_request_id = header.value;
    }
    LOG_DEBUG("[NetworkImpl] PUT(WithStatus) url: %s, x-request-id: %s, headers: [%s], body: %s",
             url.c_str(), x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(body).c_str());

    if (request_headers.find("Content-Type") == request_headers.end()) {
        switch (post_type) {
            case HttpPostType::JSON_DATA:
                request_headers["Content-Type"] = "application/json"; break;
            case HttpPostType::X_WWW_FORM_DATA:
                request_headers["Content-Type"] = "application/x-www-form-urlencoded"; break;
            default: break;
        }
    }
    auto request = std::make_shared<drc_sdk::common::network::HttpSyncRequest>();
    request->SetURL(url);
    request->SetHeader(request_headers);
    if (const auto curl_code = request->Put(body, timeout_ms); curl_code != CURLE_OK) {
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, 0, Bytes(), {}, {});
        return;
    }
    std::string response_data = request->GetResponseData();
    int status_code = (int)request->StatusCode();
    LOG_DEBUG("[NetworkImpl] PUT(WithStatus) status_code: %d, x-request-id: %s, response: %s",
             status_code, x_request_id.c_str(), log_safe(response_data).c_str());
    requests_.erase(request_id);
    if (status_code >= 400) {
        callback(NetworkError::FAILURE, status_code,
                 Bytes(response_data.begin(), response_data.end()), {}, {});
        return;
    }
    callback(NetworkError::SUCCESS, status_code,
             Bytes(response_data.begin(), response_data.end()), {}, {});
}

void NetworkImpl::delete_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
    const std::string &body, HttpCallbackWithStatus &&callback, uint32_t timeout_ms) {
    uint64_t request_id = GenerateRequestId();

    std::string headers_str;
    std::string x_request_id;
    drc_sdk::common::network::RequestHeader request_headers;
    for (const auto &header : headers) {
        request_headers[header.key] = header.value;
        if (!headers_str.empty()) headers_str += ", ";
        headers_str += header.key + ": " + header.value;
        if (header.key == "x-request-id") x_request_id = header.value;
    }
    LOG_DEBUG("[NetworkImpl] DELETE(WithStatus) url: %s, x-request-id: %s, headers: [%s], body: %s",
             url.c_str(), x_request_id.c_str(), log_safe(headers_str).c_str(), log_safe(body).c_str());

    auto request = std::make_shared<drc_sdk::common::network::HttpSyncRequest>();
    request->SetURL(url);
    request->SetHeader(request_headers);
    if (const auto curl_code = request->Delete(body, timeout_ms); curl_code != CURLE_OK) {
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, 0, Bytes(), {}, {});
        return;
    }
    std::string response_data = request->GetResponseData();
    int status_code = (int)request->StatusCode();
    LOG_DEBUG("[NetworkImpl] DELETE(WithStatus) status_code: %d, x-request-id: %s, response: %s",
             status_code, x_request_id.c_str(), log_safe(response_data).c_str());
    requests_.erase(request_id);
    if (status_code >= 400) {
        callback(NetworkError::FAILURE, status_code,
                 Bytes(response_data.begin(), response_data.end()), {}, {});
        return;
    }
    callback(NetworkError::SUCCESS, status_code,
             Bytes(response_data.begin(), response_data.end()), {}, {});
}

void NetworkImpl::get(const std::string &url, const std::vector<HttpHeader> &headers,
    HttpCallbackWithStatus &&callback, uint32_t timeout_ms) {
    uint64_t request_id = GenerateRequestId();

    std::string headers_str;
    std::string x_request_id;
    drc_sdk::common::network::RequestHeader request_headers;
    for (const auto &header : headers) {
        request_headers[header.key] = header.value;
        if (!headers_str.empty()) headers_str += ", ";
        headers_str += header.key + ": " + header.value;
        if (header.key == "x-request-id") x_request_id = header.value;
    }
    LOG_DEBUG("[NetworkImpl] GET(WithStatus) url: %s, x-request-id: %s, headers: [%s]",
             url.c_str(), x_request_id.c_str(), log_safe(headers_str).c_str());

    auto request = std::make_shared<drc_sdk::common::network::HttpSyncRequest>();
    request->SetURL(url);
    request->SetHeader(request_headers);
    if (const auto curl_code = request->Get(timeout_ms); curl_code != CURLE_OK) {
        requests_.erase(request_id);
        callback(NetworkError::FAILURE, 0, Bytes(), {}, {});
        return;
    }
    std::string response_data = request->GetResponseData();
    int status_code = (int)request->StatusCode();
    LOG_DEBUG("[NetworkImpl] GET(WithStatus) status_code: %d, x-request-id: %s, response: %s",
             status_code, x_request_id.c_str(), log_safe(response_data).c_str());
    requests_.erase(request_id);
    if (status_code >= 400) {
        callback(NetworkError::FAILURE, status_code,
                 Bytes(response_data.begin(), response_data.end()), {}, {});
        return;
    }
    callback(NetworkError::SUCCESS, status_code,
             Bytes(response_data.begin(), response_data.end()), {}, {});
}

} // namespace injector
} // namespace common
} // namespace drc_sdk

#endif // !__EMSCRIPTEN__