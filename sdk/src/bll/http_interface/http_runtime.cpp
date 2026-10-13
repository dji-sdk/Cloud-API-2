// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "http_runtime.h"

#include "api/flight_hub_sdk.h"
#include <AIGCJson.hpp>
#include <base/common/logger.h>
#include <common/log_safe.h>
#include <set>
#include <sstream>
#ifndef __EMSCRIPTEN__
#include <common/executors/executors.h>
#endif

namespace flight_hub_sdk {

void ensure_gateway_auth_headers_if_needed(
    const std::string& url,
    drc_sdk::common::HttpHeaders& headers) {
    if (url.find("/gateway/") == std::string::npos) {
        return;
    }

    const auto resource_headers = FlightHubSdk::getHttpHeaders();
    if (resource_headers.empty()) {
        return;
    }

    for (const auto& item : resource_headers) {
        if (item.key.empty()) {
            continue;
        }
        if (headers.find(item.key) == headers.end()) {
            headers[item.key] = item.value;
        }
    }

    const auto auth_keys_it = headers.find("auth_keys");
    if (auth_keys_it != headers.end() && !auth_keys_it->second.empty()) {
        return;
    }

    std::set<std::string> auth_key_set;
    for (const auto& item : resource_headers) {
        if (item.key.empty()) {
            continue;
        }
        auth_key_set.insert(item.key);
    }

    if (auth_key_set.empty()) {
        return;
    }

    std::ostringstream auth_keys_oss;
    bool first = true;
    for (const auto& key : auth_key_set) {
        if (!first) {
            auth_keys_oss << ";";
        }
        auth_keys_oss << key;
        first = false;
    }
    headers["auth_keys"] = auth_keys_oss.str();
}

void HttpPostInnerWithStatus(const std::string& url, const drc_sdk::common::HttpHeaders& headers,
    const drc_sdk::common::HttpPostType post_type, const std::string& body,
    drc_sdk::common::HttpCallbackWithStatus&& callback, uint32_t timeout_ms) {
    auto request_headers = headers;
    ensure_gateway_auth_headers_if_needed(url, request_headers);
#ifdef __EMSCRIPTEN__
    drc_sdk::common::Network::Instance().Post(url, request_headers, post_type, body, std::move(callback), timeout_ms);
#else
    drc_sdk::common::Executors::IO()->Post([url, request_headers, post_type, body, callback = std::move(callback), timeout_ms]() mutable {
        drc_sdk::common::HttpCallbackWithStatus callback_in_main =
            [callback = std::move(callback)](drc_sdk::common::NetworkError err_code, int http_code,
                const std::string& response, const std::vector<std::string>& certs,
                const drc_sdk::common::HttpHeaders& resp_headers) {
            drc_sdk::common::Executors::Main()->Post([callback = std::move(callback), err_code, http_code, response, certs, resp_headers]() {
                callback(err_code, http_code, response, certs, resp_headers);
            });
        };
        drc_sdk::common::Network::Instance().Post(url, request_headers, post_type, body, std::move(callback_in_main), timeout_ms);
    });
#endif
}

void HttpDeleteInnerWithStatus(const std::string& url, const drc_sdk::common::HttpHeaders& headers,
    const drc_sdk::common::HttpPostType post_type, const std::string& body,
    drc_sdk::common::HttpCallbackWithStatus&& callback, uint32_t timeout_ms) {
    auto request_headers = headers;
    ensure_gateway_auth_headers_if_needed(url, request_headers);
#ifdef __EMSCRIPTEN__
    drc_sdk::common::Network::Instance().Delete(url, request_headers, post_type, body, std::move(callback), timeout_ms);
#else
    drc_sdk::common::Executors::IO()->Post([url, request_headers, post_type, body, callback = std::move(callback), timeout_ms]() mutable {
        drc_sdk::common::HttpCallbackWithStatus callback_in_main =
            [callback = std::move(callback)](drc_sdk::common::NetworkError err_code, int http_code,
                const std::string& response, const std::vector<std::string>& certs,
                const drc_sdk::common::HttpHeaders& resp_headers) {
            drc_sdk::common::Executors::Main()->Post([callback = std::move(callback), err_code, http_code, response, certs, resp_headers]() {
                callback(err_code, http_code, response, certs, resp_headers);
            });
        };
        drc_sdk::common::Network::Instance().Delete(url, request_headers, post_type, body, std::move(callback_in_main), timeout_ms);
    });
#endif
}

void HttpPutInnerWithStatus(const std::string& url, const drc_sdk::common::HttpHeaders& headers,
    const drc_sdk::common::HttpPostType post_type, const std::string& body,
    drc_sdk::common::HttpCallbackWithStatus&& callback, uint32_t timeout_ms) {
    auto request_headers = headers;
    ensure_gateway_auth_headers_if_needed(url, request_headers);
#ifdef __EMSCRIPTEN__
    drc_sdk::common::Network::Instance().Put(url, request_headers, post_type, body, std::move(callback), timeout_ms);
#else
    drc_sdk::common::Executors::IO()->Post([url, request_headers, post_type, body, callback = std::move(callback), timeout_ms]() mutable {
        drc_sdk::common::HttpCallbackWithStatus callback_in_main =
            [callback = std::move(callback)](drc_sdk::common::NetworkError err_code, int http_code,
                const std::string& response, const std::vector<std::string>& certs,
                const drc_sdk::common::HttpHeaders& resp_headers) {
            drc_sdk::common::Executors::Main()->Post([callback = std::move(callback), err_code, http_code, response, certs, resp_headers]() {
                callback(err_code, http_code, response, certs, resp_headers);
            });
        };
        drc_sdk::common::Network::Instance().Put(url, request_headers, post_type, body, std::move(callback_in_main), timeout_ms);
    });
#endif
}

void HttpGetInnerWithStatus(const std::string& url, const drc_sdk::common::HttpHeaders& headers,
    drc_sdk::common::HttpCallbackWithStatus&& callback, uint32_t timeout_ms) {
    auto request_headers = headers;
    ensure_gateway_auth_headers_if_needed(url, request_headers);
#ifdef __EMSCRIPTEN__
    drc_sdk::common::Network::Instance().Get(url, request_headers, std::move(callback), timeout_ms);
#else
    drc_sdk::common::Executors::IO()->Post([url, request_headers, callback = std::move(callback), timeout_ms]() mutable {
        drc_sdk::common::HttpCallbackWithStatus callback_in_main =
            [callback = std::move(callback)](drc_sdk::common::NetworkError err_code, int http_code,
                const std::string& response, const std::vector<std::string>& certs,
                const drc_sdk::common::HttpHeaders& resp_headers) {
            drc_sdk::common::Executors::Main()->Post([callback = std::move(callback), err_code, http_code, response, certs, resp_headers]() {
                callback(err_code, http_code, response, certs, resp_headers);
            });
        };
        drc_sdk::common::Network::Instance().Get(url, request_headers, std::move(callback_in_main), timeout_ms);
    });
#endif
}

std::string generate_url(const std::string& address, const std::string& port, const std::string& api) {
    if (address.empty() && port.empty() && api.empty()) {
        LOG_ERROR("generate_url: address, port or api is empty");
        return "";
    }
    auto addr = address.back() == '/' ? address.substr(0, address.size() - 1) : address;
    if (port.empty()) {
        return addr + api;
    }
    return addr + ":" + port + api;
}

CommonResponse convert_response_struct(const std::string& response) {
    CommonResponse common_response;
    std::string err;
    aigc::JsonHelper::JsonToObject(common_response, response, {}, &err);
    if (!err.empty()) {
        LOG_ERROR("convert_response_struct: JsonToObject error: %s, response: %s",
                  err.c_str(), drc_sdk::common::log_safe(response).c_str());
        return CommonResponse();
    }
    return common_response;
}

FlightHubError make_http_error(int http_code, const std::string& response) {
    auto parsed = convert_response_struct(response);
    return FlightHubError::from_http(http_code, parsed.code, parsed.message);
}

}  // namespace flight_hub_sdk
