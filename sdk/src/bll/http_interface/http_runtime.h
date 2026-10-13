// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <functional>
#include <string>
#include <vector>
#include <optional>

#include <AIGCJson.hpp>
#include <base/common/logger.h>

#include "api/message.h"
#include "common/network/network.h"
#include "common/log_safe.h"
#include "response_message.h"

namespace flight_hub_sdk {

// FH36-P2-02: 响应内容日志统一经 log_safe()(共享实现 deps/base/common/log_safe.h)
using drc_sdk::common::log_safe;

/// 为 /gateway/ 路径请求补充鉴权头（auth_keys 等）
void ensure_gateway_auth_headers_if_needed(
    const std::string& url,
    drc_sdk::common::HttpHeaders& headers);

std::string generate_url(const std::string& address, const std::string& port, const std::string& api);
CommonResponse convert_response_struct(const std::string& response);
FlightHubError make_http_error(int http_code, const std::string& response);

/// 从 CommonResponse 解析内层 data 为业务 struct
/// @param required  true: 无 data 时返回 false; false: 无 data 时 out 保持默认并返回 true
template<typename T>
bool parse_common_response_data(
    const CommonResponse& common,
    T& out,
    std::string* err = nullptr,
    bool required = true) {
    if (!common.data.has_value()) {
        if (required) {
            if (err) *err = "response data is null or missing";
            return false;
        }
        return true;
    }
    return aigc::JsonHelper::JsonToObject(out, *common.data, {}, err);
}

/// 标准 HTTP 回调分派 helper：封装 network error → code check → data parse → callback 的通用骨架
/// @param log_tag    日志前缀（如 "GetDeviceList"）
/// @param net_err    网络层错误码
/// @param http_code  HTTP 状态码
/// @param response   原始响应体
/// @param out        输出业务 struct（失败时保持不变）
/// @param data_required  data 是否必须（同 parse_common_response_data）
/// @param callback   业务回调（签名: void(const T&, FlightHubError)）
/// 失败时 callback 以 make_http_error(http_code, response) 调用，out 保持默认值。
template<typename T>
void dispatch_common_json_response(
    const char* log_tag,
    drc_sdk::common::NetworkError net_err,
    int http_code,
    const std::string& response,
    T& out,
    bool data_required,
    const std::function<void(const T&, FlightHubError)>& callback) {

    if (net_err != drc_sdk::common::NetworkError::SUCCESS) {
        LOG_ERROR("%s: network error: %d", log_tag, static_cast<int>(net_err));
        callback({}, make_http_error(http_code, response));
        return;
    }

    auto common_response = convert_response_struct(response);
    if (common_response.code != 0) {
        LOG_ERROR("%s: code: %d, message: %s", log_tag,
                  common_response.code, common_response.message.c_str());
        callback({}, make_http_error(http_code, response));
        return;
    }

    std::string err;
    if (!parse_common_response_data(common_response, out, &err, data_required)) {
        LOG_ERROR("%s: parse error: %s, data: %s", log_tag,
                  err.c_str(), common_response.data.has_value() ? log_safe(*common_response.data).c_str() : "");
        callback({}, make_http_error(http_code, response));
        return;
    }

    callback(out, FlightHubError::success());
}

void HttpPostInnerWithStatus(const std::string& url, const drc_sdk::common::HttpHeaders& headers,
    const drc_sdk::common::HttpPostType post_type, const std::string& body,
    drc_sdk::common::HttpCallbackWithStatus&& callback, uint32_t timeout_ms = 10000);

void HttpDeleteInnerWithStatus(const std::string& url, const drc_sdk::common::HttpHeaders& headers,
    const drc_sdk::common::HttpPostType post_type, const std::string& body,
    drc_sdk::common::HttpCallbackWithStatus&& callback, uint32_t timeout_ms = 10000);

void HttpPutInnerWithStatus(const std::string& url, const drc_sdk::common::HttpHeaders& headers,
    const drc_sdk::common::HttpPostType post_type, const std::string& body,
    drc_sdk::common::HttpCallbackWithStatus&& callback, uint32_t timeout_ms = 10000);

void HttpGetInnerWithStatus(const std::string& url, const drc_sdk::common::HttpHeaders& headers,
    drc_sdk::common::HttpCallbackWithStatus&& callback, uint32_t timeout_ms = 10000);

}  // namespace flight_hub_sdk
