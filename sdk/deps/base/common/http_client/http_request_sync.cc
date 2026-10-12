// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "http_request_sync.h"
#include "base/common/logger.h"

#include <mutex>

namespace drc_sdk {
namespace common {
namespace network {

// 全局 TLS 配置(进程级)。状态仅经 SetGlobalTlsConfig 变更, mutex 保护
// 与构造函数(任意线程创建请求)的并发读取。
namespace {
std::mutex g_tls_config_mutex;
bool g_verify_ssl = true;   // 安全默认: 开启校验(未调用 SetGlobalTlsConfig 时的兜底); 私有化自签名环境须经 ssl_ca_path 指定 CA
std::string g_ca_path;      // 空 = 系统 CA 信任库
}  // namespace

void HttpSyncRequest::SetGlobalTlsConfig(bool verify_ssl, const std::string &ca_path) {
    {
        std::lock_guard<std::mutex> lock(g_tls_config_mutex);
        g_verify_ssl = verify_ssl;
        g_ca_path = ca_path;
    }
    if (!verify_ssl) {
        LOG_WARN("[HttpSyncRequest] 全局 TLS 证书验证已关闭(verify_ssl=false, 不安全, 仅限受控测试环境)");
    }
    LOG_INFO("[HttpSyncRequest] 全局 TLS 配置: verify_ssl=%s, ca_path=%s",
             verify_ssl ? "true" : "false", ca_path.empty() ? "(系统CA)" : ca_path.c_str());
}

// TLS 验证失败的迁移指引。默认收紧后私有化自签名部署升级即失败,
// ISV 看到的第一个现象就是这里的日志 —— 必须给出原因与出路, 直接决定支持成本。
// 单点覆盖本类全部 5 个 curl_easy_perform 调用点及未来新增点。
static void LogSslFailureGuidance(CURLcode code) {
    if (code == CURLE_PEER_FAILED_VERIFICATION) {
        LOG_ERROR("[HttpSyncRequest] HTTPS 证书验证失败(curl_code=60): 服务器证书无法验证"
                  "(自签名/私有CA/主机名不匹配)。私有化部署请在初始化配置中设置 ssl_ca_path"
                  " 指向您的 CA 证书文件(PEM), 或确认服务器证书链完整且与访问地址匹配");
    } else if (code == CURLE_SSL_CACERT_BADFILE) {
        LOG_ERROR("[HttpSyncRequest] ssl_ca_path 指向的 CA 文件加载失败(curl_code=77),"
                  " 请检查路径与文件权限(须为 PEM 格式证书文件)");
    }
}

static int DebugCallback(CURL *handle, curl_infotype type, char *data, size_t size, void *userptr) {
    std::string text;
    std::string str_data = data;
    switch (type) {
        case CURLINFO_TEXT:
            text = "== Info: ";
        break;
        case CURLINFO_HEADER_OUT:
            text = "=> Send header: ";
        break;
        case CURLINFO_DATA_OUT:
            text = "=> Send data: ";
        break;
        case CURLINFO_SSL_DATA_OUT:
            text = "=> Send SSL data: ";
        str_data.clear();
        break;
        case CURLINFO_HEADER_IN:
            text = "<= Recv header: ";
        break;
        case CURLINFO_DATA_IN:
            text = "<= Recv data: ";
        str_data.clear();
        break;
        case CURLINFO_SSL_DATA_IN:
            text = "<= Recv SSL data: ";
        str_data.clear();
        break;
        default:
            return 0;
    }
    if (!str_data.empty()) {
        LOG_INFO("HttpSyncRequest %s, size = %d, data: %s", text.c_str(), (int)size, str_data.c_str());
    }
    return 0;
}

size_t WriteFunctionAdapterSync(void *ptr, size_t size, size_t nmemb, void *data) {
    auto *handle = reinterpret_cast<HttpSyncRequest *>(data);
    if (handle == nullptr) {
        return size * nmemb;
    }
    return handle->OnCurlWrite(static_cast<char *>(ptr), size, nmemb);
}

void HttpSyncRequest::SetURL(const std::string &url) {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] SetURL failed: curl easy handle is null");
        return;
    }
    curl_easy_setopt(easy_, CURLOPT_URL, url.c_str());
    url_ = url;
}

void HttpSyncRequest::SetHeader(const RequestHeader &header) {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] SetHeader failed: curl easy handle is null");
        return;
    }
    for (const auto& item : header) {
        std::string header_string = item.first;
        if (item.second.empty()) {
            header_string += ";";
        } else {
            header_string += ": " + item.second;
        }

        curl_slist *temp = curl_slist_append(header_list_, header_string.c_str());
        if (temp) {
            header_list_ = temp;
        }
    }
    curl_easy_setopt(easy_, CURLOPT_HTTPHEADER, header_list_);
    // TODO: Todo action 后 free
    // curl_init 需保证线程安全
    // curl_slist_free_all(chunk);
}

void HttpSyncRequest::OpenDebugLog() {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] OpenDebugLog failed: curl easy handle is null");
        return;
    }
    curl_easy_setopt(easy_, CURLOPT_VERBOSE, 1L);
    curl_easy_setopt(easy_, CURLOPT_DEBUGFUNCTION, DebugCallback);
}

void HttpSyncRequest::SetSslVerify(bool enable, const std::string& ca_path) {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] SetSslVerify failed: curl easy handle is null");
        return;
    }
    curl_easy_setopt(easy_, CURLOPT_SSL_VERIFYPEER, enable ? 1L : 0L);
    curl_easy_setopt(easy_, CURLOPT_SSL_VERIFYHOST, enable ? 2L : 0L);
    if (!ca_path.empty()) {
        // PEM 证书文件应使用 CURLOPT_CAINFO; 原实现误用 CURLOPT_CAPATH
        // (其语义是 OpenSSL hash 符号链接目录), 指向 PEM 文件会静默加载失败,
        // 开启 VERIFYPEER 后表现为 "unable to get local issuer certificate"。
        curl_easy_setopt(easy_, CURLOPT_CAINFO, ca_path.c_str());
    }
    if (!enable) {
        LOG_INFO("[HttpSyncRequest] 本请求 SSL 验证已关闭(不安全)");
    }
}

CURLcode HttpSyncRequest::Get(uint32_t timeout_ms) {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] Get failed: curl easy handle is null");
        return CURLE_FAILED_INIT;
    }
    curl_easy_setopt(easy_, CURLOPT_TIMEOUT_MS, timeout_ms);
    curl_easy_setopt(easy_, CURLOPT_LOW_SPEED_TIME, timeout_ms / 1000);
    response_buffer_.Clear();
    CURLcode ret = curl_easy_perform(easy_);
    LogSslFailureGuidance(ret);
    return ret;
}

CURLcode HttpSyncRequest::Post(const std::string &data, uint32_t timeout_ms) {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] Post failed: curl easy handle is null");
        return CURLE_FAILED_INIT;
    }
    response_buffer_.Clear();
    data_str_ = data;
    curl_easy_setopt(easy_, CURLOPT_POST, 1);
    curl_easy_setopt(easy_, CURLOPT_POSTFIELDS, data_str_.c_str());
    curl_easy_setopt(easy_, CURLOPT_POSTFIELDSIZE, data_str_.size());
    curl_easy_setopt(easy_, CURLOPT_TIMEOUT_MS, timeout_ms);
    curl_easy_setopt(easy_, CURLOPT_LOW_SPEED_TIME, timeout_ms / 1000);
    CURLcode ret = curl_easy_perform(easy_);
    LogSslFailureGuidance(ret);
    return ret;
}

CURLcode HttpSyncRequest::PostMultipartFiles(const std::vector<std::string> &file_paths, const std::string &field_name, uint32_t timeout_ms) {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] PostMultipartFiles failed: curl easy handle is null");
        return CURLE_FAILED_INIT;
    }
    response_buffer_.Clear();

    curl_mime *mime = curl_mime_init(easy_);
    for (const auto &file_path : file_paths) {
        curl_mimepart *part = curl_mime_addpart(mime);
        curl_mime_name(part, field_name.c_str());
        curl_mime_filedata(part, file_path.c_str());
    }

    curl_easy_setopt(easy_, CURLOPT_MIMEPOST, mime);
    curl_easy_setopt(easy_, CURLOPT_TIMEOUT_MS, timeout_ms);
    curl_easy_setopt(easy_, CURLOPT_LOW_SPEED_TIME, timeout_ms / 1000);

    CURLcode result = curl_easy_perform(easy_);
    LogSslFailureGuidance(result);
    curl_mime_free(mime);
    return result;
}

CURLcode HttpSyncRequest::Put(const std::string &data, uint32_t timeout_ms) {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] Put failed: curl easy handle is null");
        return CURLE_FAILED_INIT;
    }
    response_buffer_.Clear();
    data_str_ = data;
    curl_easy_setopt(easy_, CURLOPT_CUSTOMREQUEST, "PUT");
    curl_easy_setopt(easy_, CURLOPT_POSTFIELDS, data_str_.c_str());
    curl_easy_setopt(easy_, CURLOPT_POSTFIELDSIZE, data_str_.size());
    curl_easy_setopt(easy_, CURLOPT_TIMEOUT_MS, timeout_ms);
    curl_easy_setopt(easy_, CURLOPT_LOW_SPEED_TIME, timeout_ms / 1000);
    CURLcode ret = curl_easy_perform(easy_);
    LogSslFailureGuidance(ret);
    return ret;
}

CURLcode HttpSyncRequest::Delete(const std::string &data, uint32_t timeout_ms) {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] Delete failed: curl easy handle is null");
        return CURLE_FAILED_INIT;
    }
    response_buffer_.Clear();
    data_str_ = data;
    curl_easy_setopt(easy_, CURLOPT_CUSTOMREQUEST, "DELETE");
    curl_easy_setopt(easy_, CURLOPT_POSTFIELDS, data_str_.c_str());
    curl_easy_setopt(easy_, CURLOPT_POSTFIELDSIZE, data_str_.size());
    curl_easy_setopt(easy_, CURLOPT_TIMEOUT_MS, timeout_ms);
    curl_easy_setopt(easy_, CURLOPT_LOW_SPEED_TIME, timeout_ms / 1000);
    CURLcode ret = curl_easy_perform(easy_);
    LogSslFailureGuidance(ret);
    return ret;
}

long HttpSyncRequest::StatusCode() {
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] StatusCode failed: curl easy handle is null");
        return 0;
    }
    curl_easy_getinfo(easy_, CURLINFO_RESPONSE_CODE, &status_code_);
    return status_code_;
}

void HttpSyncRequest::OnDataWrite(WriteFunction callback) {
    write_function_ = std::move(callback);
}

void HttpSyncRequest::Cancel() {}

std::string HttpSyncRequest::GetResponseData() {
    return std::string(response_buffer_.data(), response_buffer_.Size());
}

size_t HttpSyncRequest::OnCurlWrite(char *ptr, size_t size, size_t nmemb) {
    if (write_function_) {
        return write_function_(ptr, size, nmemb);
    }
    response_buffer_.Write(reinterpret_cast<const unsigned char *>(ptr), size * nmemb);
    return size * nmemb;
}

HttpSyncRequest::HttpSyncRequest() {
    easy_ = curl_easy_init();
    if (easy_ == nullptr) {
        LOG_ERROR("[HttpSyncRequest] Failed to initialize curl easy handle");
        return;
    }

    curl_easy_setopt(easy_, CURLOPT_WRITEFUNCTION, WriteFunctionAdapterSync);
    curl_easy_setopt(easy_, CURLOPT_WRITEDATA, this);

    curl_easy_setopt(easy_, CURLOPT_TIMEOUT_MS, 0L);
    curl_easy_setopt(easy_, CURLOPT_CONNECTTIMEOUT_MS, 10000);
    /* abort if slower than 1 bytes/sec during request_timeout ms */
    curl_easy_setopt(easy_, CURLOPT_LOW_SPEED_LIMIT, 1L);
    curl_easy_setopt(easy_, CURLOPT_LOW_SPEED_TIME, 10000 / 1000);

    curl_easy_setopt(easy_, CURLOPT_NOSIGNAL, 1L);

    curl_easy_setopt(easy_, CURLOPT_FOLLOWLOCATION, 1);
    curl_easy_setopt(easy_, CURLOPT_AUTOREFERER, 1L);

    // 安全默认值 —— 启用服务器证书与主机名校验。
    // 原实现无条件关闭(VERIFYPEER/HOST=0), SDK 发出的全部 HTTPS 请求(OpenAPI 调用、
    // STS 临时凭证获取、relay 密码提交、beacon 埋点上报)均不校验服务器证书,
    // 中间人可截获鉴权头/凭证并伪造响应; 类上的 SetSslVerify 实现正确但全仓 0 调用者。
    curl_easy_setopt(easy_, CURLOPT_SSL_VERIFYPEER, 1L);
    curl_easy_setopt(easy_, CURLOPT_SSL_VERIFYHOST, 2L);

    // 应用全局 TLS 配置(SDK 初始化时经 SetGlobalTlsConfig 下发;
    // 未下发时使用全局默认 verify_ssl=true)
    {
        std::lock_guard<std::mutex> lock(g_tls_config_mutex);
        SetSslVerify(g_verify_ssl, g_ca_path);
    }
};
HttpSyncRequest::~HttpSyncRequest() {
    // 释放 curl header list
    if (header_list_ != nullptr) {
        curl_slist_free_all(header_list_);
        header_list_ = nullptr;
    }
    // 释放 curl easy handle，这会自动关闭所有相关的 socket 连接
    if (easy_ != nullptr) {
        curl_easy_cleanup(easy_);
        easy_ = nullptr;
    }
}

}  // namespace network
}  // namespace common
}  // namespace drc_sdk
