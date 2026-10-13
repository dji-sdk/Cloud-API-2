// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "injector.h"

// 只在非 Emscripten 环境中包含 http_client（libcurl）；emcc 定义 __EMSCRIPTEN__，不定义 CMake 变量 EMSCRIPTEN 对应的预处理器宏
#ifndef __EMSCRIPTEN__
#include "common/http_client/http_request_sync.h"
#endif


namespace drc_sdk {
namespace common {
namespace injector {


class NetworkImpl : public Network, public AsyncCaptureProtect {
public:
    NetworkImpl();
    ~NetworkImpl();

    void get(const std::string &url, const std::vector<HttpHeader> &headers,
             drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms = 10000) override;

    void post(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
              const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms = 10000) override;

    void put_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms = 10000) override;

    void delete_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms = 10000) override;

    void form_post(const std::string &url, const std::vector<HttpHeader> &headers,
                   const std::vector<std::string> &body, drc_sdk::common::injector::HttpCallback &&callback) override;

    // --- 带 HTTP 状态码的重载版本 ---

    void post(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
              const std::string &body, HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) override;

    void put_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) override;

    void delete_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
                      const std::string &body, HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) override;

    void get(const std::string &url, const std::vector<HttpHeader> &headers,
             HttpCallbackWithStatus &&callback, uint32_t timeout_ms = 10000) override;

    int32_t download(const std::string &url, const std::vector<HttpHeader> &headers, const std::string &path, int32_t max_retry,
                     drc_sdk::common::DownloadProcess &&process, drc_sdk::common::DownloadResult &&complete) override;

    void cancel_download(int32_t handle) override;

    int32_t stream(const std::string &url, const std::vector<HttpHeader> &headers,
                   drc_sdk::common::StreamData &&callback, drc_sdk::common::StreamResult &&complete) override;

    void cancel_stream(int32_t handle) override;

private:
    /// For libcurl
    uint64_t GenerateRequestId();
#ifndef __EMSCRIPTEN__
    std::unordered_map<uint64_t, std::shared_ptr<drc_sdk::common::network::HttpSyncRequest>> requests_;
#endif
};


} // namespace injector
} // namespace common
} // namespace drc_sdk