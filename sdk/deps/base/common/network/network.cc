// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "network.h"
#include "injector.h"
#include "base/common/logger.h"
#include "impl/network_impl.h"

namespace drc_sdk {
namespace common {

Network& Network::Instance() {
    static auto instance = Network();
    return instance;
}

void Network::Init(std::shared_ptr<injector::Network> network) {
    LOG_DEBUG("[Network] injector init occured");
    if (network_ != nullptr) {
        LOG_ERROR("[Network] network already initialized");
        return;
    }
    if (network != nullptr) {
        network_ = std::move(network);
        LOG_INFO("[Network] injector init complete");
    } else {
        network_ = std::make_shared<injector::NetworkImpl>();
    }
}

void Network::SetTlsConfig(bool verify_ssl, const std::string &ca_path) {
#ifndef __EMSCRIPTEN__
    // 经 HttpSyncRequest 构造函数统一应用, 覆盖网络层全部实例化点
    network::HttpSyncRequest::SetGlobalTlsConfig(verify_ssl, ca_path);
#else
    (void)verify_ssl;
    (void)ca_path;
    LOG_WARN("[Network] SetTlsConfig ignored on emscripten build");
#endif
}

void Network::Get(const std::string &url, const HttpHeaders &headers, HttpCallback &&callback, uint32_t timeout_ms) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        callback(NetworkError::NO_NETWORK, "", {}, {});
        return;
    }
    LOG_DEBUG("[Network] http GET request -> `%s` (%d headers)", url.c_str(), headers.size());
    network_->get(url, ConvertHeaders(headers), [callback = std::move(callback)](
        const NetworkError err_code,
        const std::vector<uint8_t> &response,
        const std::vector<std::vector<uint8_t>> &certs,
        const std::vector<injector::HttpHeader> &resp_headers) {
        LOG_DEBUG("[Network] injector http GET callback (err = %d) -> %d bytes (%d headers)", err_code, response.size(), resp_headers.size());
        callback(err_code, ConvertBytes(response), ConvertByteArrays(certs), ConvertHeaders(resp_headers));
    }, timeout_ms);
}

void Network::Post(const std::string &url, const HttpHeaders &headers,
                   const HttpPostType post_type, const std::string &body, HttpCallback &&callback, uint32_t timeout_ms) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        callback(NetworkError::NO_NETWORK, "", {}, {});
        return;
    }
    LOG_DEBUG("[Network] http POST request -> `%s` (%d headers)", url.c_str(), headers.size());
    network_->post(url, ConvertHeaders(headers), post_type, body, [callback = std::move(callback)](
        const NetworkError err_code,
        const std::vector<uint8_t> &response,
        const std::vector<std::vector<uint8_t>> &certs,
        const std::vector<injector::HttpHeader> &resp_headers) {
        LOG_DEBUG("[Network] injector http POST callback (err = %d) -> %d bytes (%d headers)", err_code, response.size(), resp_headers.size());
        callback(err_code, ConvertBytes(response), ConvertByteArrays(certs), ConvertHeaders(resp_headers));
    }, timeout_ms);
}

void Network::Put(const std::string &url, const HttpHeaders &headers, HttpPostType post_type,
    const std::string &body, drc_sdk::common::HttpCallback &&callback, uint32_t timeout_ms) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        callback(NetworkError::NO_NETWORK, "", {}, {});
        return;
    }
    LOG_DEBUG("[Network] http PUT request -> `%s` (%d headers)", url.c_str(), headers.size());
    network_->put_request(url, ConvertHeaders(headers), post_type, body, [callback = std::move(callback)](
        const NetworkError err_code,
        const std::vector<uint8_t> &response,
        const std::vector<std::vector<uint8_t>> &certs,
        const std::vector<injector::HttpHeader> &resp_headers) {
        LOG_DEBUG("[Network] injector http PUT callback (err = %d) -> %d bytes (%d headers)", err_code, response.size(), resp_headers.size());
        callback(err_code, ConvertBytes(response), ConvertByteArrays(certs), ConvertHeaders(resp_headers));
    }, timeout_ms);
}

void Network::Delete(const std::string &url, const HttpHeaders &headers, HttpPostType post_type,
    const std::string &body, drc_sdk::common::HttpCallback &&callback, uint32_t timeout_ms) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        callback(NetworkError::NO_NETWORK, "", {}, {});
        return;
    }
    LOG_DEBUG("[Network] http DELETE request -> `%s` (%d headers)", url.c_str(), headers.size());
    network_->delete_request(url, ConvertHeaders(headers), post_type, body, [callback = std::move(callback)](
        const NetworkError err_code,
        const std::vector<uint8_t> &response,
        const std::vector<std::vector<uint8_t>> &certs,
        const std::vector<injector::HttpHeader> &resp_headers) {
        LOG_DEBUG("[Network] injector http DELETE callback (err = %d) -> %d bytes (%d headers)", err_code, response.size(), resp_headers.size());
        callback(err_code, ConvertBytes(response), ConvertByteArrays(certs), ConvertHeaders(resp_headers));
    }, timeout_ms);
}
void Network::FormPost(const std::string &url, const HttpHeaders &headers, const std::vector<std::string> &body, HttpCallback &&callback) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        callback(NetworkError::NO_NETWORK, "", {}, {});
        return;
    }
    LOG_DEBUG("[Network] http FORM POST request -> `%s` (%d headers)", url.c_str(), headers.size());
    network_->form_post(url, ConvertHeaders(headers), body, [callback = std::move(callback)](
        const NetworkError err_code,
        const std::vector<uint8_t> &response,
        const std::vector<std::vector<uint8_t>> &certs,
        const std::vector<injector::HttpHeader> &resp_headers) {
        LOG_DEBUG("[Network] injector http FORM POST callback (err = %d) -> %d bytes (%d headers)", err_code, response.size(), resp_headers.size());
        callback(err_code, ConvertBytes(response), ConvertByteArrays(certs), ConvertHeaders(resp_headers));
    });
}

int32_t Network::Download(const std::string &url, const HttpHeaders &headers, const std::string &path,
                          const uint16_t max_retry, DownloadProcess &&process, DownloadResult &&complete) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        complete(NetworkError::NO_NETWORK);
        return -1;
    }
    LOG_DEBUG("[Network] http download request -> `%s` (%d headers)", url.c_str(), headers.size());
    return network_->download(url, ConvertHeaders(headers), path, max_retry,
        [process = std::move(process)](const uint64_t size, const float ratio) {
        LOG_DEBUG("[Network] injector download process callback -> %llu bytes, %.2f%%", size, ratio * 100);
        process(size, ratio);
    }, [complete = std::move(complete)](const NetworkError err_code) {
        LOG_DEBUG("[Network] injector download complete callback (err = %d)", err_code);
        complete(err_code);
    });
}

void Network::CancelDownload(const int32_t handle) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        return;
    }
    LOG_DEBUG("[Network] http cancel download -> `%d`", handle);
    network_->cancel_download(handle);
}

int32_t Network::Stream(const std::string &url, const HttpHeaders &headers,
                        drc_sdk::common::StreamData &&callback, drc_sdk::common::StreamResult &&complete) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        complete(NetworkError::NO_NETWORK);
        return -1;
    }
    LOG_DEBUG("[Network] http stream request -> `%s` (%d headers)", url.c_str(), headers.size());
    return network_->stream(url, ConvertHeaders(headers),
        [callback = std::move(callback)](const std::string &data) {
        LOG_DEBUG("[Network] injector stream process callback -> %llu bytes", data.size());
        callback(data);
    }, [complete = std::move(complete)](const NetworkError err_code) {
        LOG_DEBUG("[Network] injector stream complete callback (err = %d)", err_code);
        complete(err_code);
    });
}

void Network::CancelStream(const int32_t handle) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        return;
    }
    LOG_DEBUG("[Network] http cancel stream -> `%d`", handle);
    network_->cancel_download(handle);
}

// ===== 带 HTTP 状态码的重载实现（HttpCallbackWithStatus）=====

void Network::Post(const std::string &url, const HttpHeaders &headers,
                   const HttpPostType post_type, const std::string &body,
                   HttpCallbackWithStatus &&callback, uint32_t timeout_ms) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        callback(NetworkError::NO_NETWORK, 0, "", {}, {});
        return;
    }
    LOG_DEBUG("[Network] http POST(WithStatus) request -> `%s` (%d headers)", url.c_str(), headers.size());
    network_->post(url, ConvertHeaders(headers), post_type, body,
        [callback = std::move(callback)](NetworkError err_code, int http_code,
            const injector::Bytes &response,
            const std::vector<injector::Bytes> &certs,
            const std::vector<injector::HttpHeader> &resp_headers) {
        LOG_DEBUG("[Network] injector http POST(WithStatus) callback (err=%d, http=%d) -> %d bytes",
                  err_code, http_code, response.size());
        callback(err_code, http_code, ConvertBytes(response), ConvertByteArrays(certs), ConvertHeaders(resp_headers));
    }, timeout_ms);
}

void Network::Put(const std::string &url, const HttpHeaders &headers,
                  HttpPostType post_type, const std::string &body,
                  HttpCallbackWithStatus &&callback, uint32_t timeout_ms) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        callback(NetworkError::NO_NETWORK, 0, "", {}, {});
        return;
    }
    LOG_DEBUG("[Network] http PUT(WithStatus) request -> `%s` (%d headers)", url.c_str(), headers.size());
    network_->put_request(url, ConvertHeaders(headers), post_type, body,
        [callback = std::move(callback)](NetworkError err_code, int http_code,
            const injector::Bytes &response,
            const std::vector<injector::Bytes> &certs,
            const std::vector<injector::HttpHeader> &resp_headers) {
        callback(err_code, http_code, ConvertBytes(response), ConvertByteArrays(certs), ConvertHeaders(resp_headers));
    }, timeout_ms);
}

void Network::Delete(const std::string &url, const HttpHeaders &headers,
                     HttpPostType post_type, const std::string &body,
                     HttpCallbackWithStatus &&callback, uint32_t timeout_ms) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        callback(NetworkError::NO_NETWORK, 0, "", {}, {});
        return;
    }
    LOG_DEBUG("[Network] http DELETE(WithStatus) request -> `%s` (%d headers)", url.c_str(), headers.size());
    network_->delete_request(url, ConvertHeaders(headers), post_type, body,
        [callback = std::move(callback)](NetworkError err_code, int http_code,
            const injector::Bytes &response,
            const std::vector<injector::Bytes> &certs,
            const std::vector<injector::HttpHeader> &resp_headers) {
        callback(err_code, http_code, ConvertBytes(response), ConvertByteArrays(certs), ConvertHeaders(resp_headers));
    }, timeout_ms);
}

void Network::Get(const std::string &url, const HttpHeaders &headers,
                  HttpCallbackWithStatus &&callback, uint32_t timeout_ms) const {
    if (network_ == nullptr) {
        LOG_ERROR("[Network] using before initialization");
        callback(NetworkError::NO_NETWORK, 0, "", {}, {});
        return;
    }
    LOG_DEBUG("[Network] http GET(WithStatus) request -> `%s` (%d headers)", url.c_str(), headers.size());
    network_->get(url, ConvertHeaders(headers),
        [callback = std::move(callback)](NetworkError err_code, int http_code,
            const injector::Bytes &response,
            const std::vector<injector::Bytes> &certs,
            const std::vector<injector::HttpHeader> &resp_headers) {
        callback(err_code, http_code, ConvertBytes(response), ConvertByteArrays(certs), ConvertHeaders(resp_headers));
    }, timeout_ms);
}

} // namespace common
} // namespace drc_sdk
