// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <vector>
#include <string>
#include "common.h"

namespace drc_sdk {
namespace common {
namespace injector {

struct HttpHeader final {
    std::string key;
    std::string value;
};

using Bytes = std::vector<uint8_t>;

/// GET / POST响应回调 -> std::string使用bytes代替（兼容其他语言UTF-8检查）
using HttpCallback = std::function<void(NetworkError err_code, const Bytes &response,
    const std::vector<Bytes> &certs, const std::vector<drc_sdk::common::injector::HttpHeader> &headers)>;

/// 带 HTTP 原始状态码的回调（重载版），http_code 为 0 表示 curl 层失败（无 HTTP 响应）
using HttpCallbackWithStatus = std::function<void(NetworkError err_code, int http_code,
    const Bytes &response,
    const std::vector<Bytes> &certs,
    const std::vector<drc_sdk::common::injector::HttpHeader> &headers)>;

/// 网络能力实现注入类
class Network {
public:
    virtual ~Network() = default;

    /** 发起GET请求，不允许携带消息体 */
    virtual void get(const std::string &url, const std::vector<HttpHeader> &headers,
                     drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms = 10000) = 0;

    /** 发起POST请求，支持json和x-www-form-urlencoded请求 */
    virtual void post(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms = 10000) = 0;

    /** 发起PUT请求，支持json和x-www-form-urlencoded请求 */
    virtual void put_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms = 10000) = 0;

    /** 发起DELETE请求，支持json和x-www-form-urlencoded请求 */
    virtual void delete_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms = 10000) = 0;

    /** 发起表单POST，请求头MIME为multipart/form-data */
    virtual void form_post(const std::string &url, const std::vector<HttpHeader> &headers,
                           const std::vector<std::string> &body, drc_sdk::common::injector::HttpCallback &&callback) = 0;

    // --- 带 HTTP 状态码的重载版本（HttpCallbackWithStatus），靠 callback 类型与上方区分 ---

    virtual void post(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) = 0;

    virtual void put_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) = 0;

    virtual void delete_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) = 0;

    virtual void get(const std::string &url, const std::vector<HttpHeader> &headers,
                     HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) = 0;

    /** 发起文件下载请求，返回handle，支持设置最大重试次数 */
    virtual int32_t download(const std::string &url, const std::vector<HttpHeader> &headers, const std::string &path, int32_t max_retry,
                             drc_sdk::common::DownloadProcess &&process, drc_sdk::common::DownloadResult &&complete) = 0;

    /** 停止下载，传入handle，触发取消回调 */
    virtual void cancel_download(int32_t handle) = 0;

    /** 发起GET流请求，返回handle，回调流数据 */
    virtual int32_t stream(const std::string &url, const std::vector<HttpHeader> &headers,
                           drc_sdk::common::StreamData &&callback, drc_sdk::common::StreamResult &&complete) = 0;

    /** 停止流请求，传入handle，触发取消回调 */
    virtual void cancel_stream(int32_t handle) = 0;
};

} // namespace injector
} // namespace common
} // namespace drc_sdk
