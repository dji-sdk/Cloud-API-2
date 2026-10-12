// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <map>
#include "common.h"
#include "injector.h"
#include "base/common/macros.h"

namespace drc_sdk {
namespace common {

class Network final {
public:
    static Network& Instance();
    DISALLOW_COPY_AND_ASSIGN(Network);
    void Init(std::shared_ptr<drc_sdk::common::injector::Network> network = nullptr);

    /// 设置 HTTPS 通道全局 TLS 证书验证配置(与 WSS 通道共用 FlightHubSdkConfig
    /// 的 verify_ssl/ssl_ca_path 字段, 由 SDK 初始化时统一下发)。
    /// verify_ssl=true: 校验证书链与主机名; ca_path 为空用系统 CA, 非空用指定 PEM 文件
    /// verify_ssl=false: 显式逃生舱(不安全, 仅限受控测试环境, 产生 WARN 日志)
    void SetTlsConfig(bool verify_ssl, const std::string &ca_path);

    /// 发起GET请求，不允许携带消息体
    void Get(const std::string &url, const HttpHeaders &headers,
         drc_sdk::common::HttpCallback &&callback, uint32_t timeout_ms = 10000) const;

    /// 发起POST请求，支持json和x-www-form-urlencoded请求
    void Post(const std::string &url, const HttpHeaders &headers, HttpPostType post_type,
              const std::string &body, drc_sdk::common::HttpCallback &&callback, uint32_t timeout_ms = 10000) const;

    /// 发起PUT请求，支持json和x-www-form-urlencoded请求
    void Put(const std::string &url, const HttpHeaders &headers, HttpPostType post_type,
              const std::string &body, drc_sdk::common::HttpCallback &&callback, uint32_t timeout_ms = 10000) const;

    /// 发起DELETE请求，支持json和x-www-form-urlencoded请求
    void Delete(const std::string &url, const HttpHeaders &headers, HttpPostType post_type,
              const std::string &body, drc_sdk::common::HttpCallback &&callback, uint32_t timeout_ms = 10000) const;

    /// 发起表单POST，请求头MIME为multipart/form-data
    void FormPost(const std::string &url, const HttpHeaders &headers, const std::vector<std::string> &body,
                  drc_sdk::common::HttpCallback &&callback) const;

    // --- 带 HTTP 状态码的重载版本（HttpCallbackWithStatus）---

    void Post(const std::string &url, const HttpHeaders &headers, HttpPostType post_type,
              const std::string &body, drc_sdk::common::HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) const;

    void Put(const std::string &url, const HttpHeaders &headers, HttpPostType post_type,
              const std::string &body, drc_sdk::common::HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) const;

    void Delete(const std::string &url, const HttpHeaders &headers, HttpPostType post_type,
              const std::string &body, drc_sdk::common::HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) const;

    void Get(const std::string &url, const HttpHeaders &headers,
             drc_sdk::common::HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) const;

    /// 发起下载请求，返回句柄
    int32_t Download(const std::string &url, const HttpHeaders &headers, const std::string &path, uint16_t max_retry,
                     drc_sdk::common::DownloadProcess &&process, drc_sdk::common::DownloadResult &&complete) const;

    /// 停止下载，传入句柄
    void CancelDownload(int32_t handle) const;

    /// 发起流请求，返回句柄
    int32_t Stream(const std::string &url, const HttpHeaders &headers,
                   drc_sdk::common::StreamData &&callback, drc_sdk::common::StreamResult &&complete) const;

    /// 停止流请求，传入句柄
    void CancelStream(int32_t handle) const;

private:
    Network() = default;
    std::shared_ptr<drc_sdk::common::injector::Network> network_ = nullptr;

    /// Bytes <---> std::string
    static injector::Bytes ConvertBytes(const std::string &data);
    static std::string ConvertBytes(const injector::Bytes &data);

    /// std::vector<Bytes> <---> std::vector<std::string>
    static std::vector<injector::Bytes> ConvertByteArrays(const std::vector<std::string> &certs);
    static std::vector<std::string> ConvertByteArrays(const std::vector<injector::Bytes> &certs);

    /// HttpHeaders <---> std::vector<HttpHeader>
    static HttpHeaders ConvertHeaders(const std::vector<drc_sdk::common::injector::HttpHeader> &headers);
    static std::vector<drc_sdk::common::injector::HttpHeader> ConvertHeaders(const HttpHeaders &headers);
};

} // namespace common
} // namespace drc_sdk
