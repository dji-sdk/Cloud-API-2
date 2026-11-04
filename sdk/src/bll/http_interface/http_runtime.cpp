// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "http_runtime.h"

#include "api/flight_hub_sdk.h"
#include "session/session_manager.h"
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

bool ensure_mesh_dst_header_for_session(
    drc_sdk::common::HttpHeaders& headers,
    int session_id) {
    auto mgr_iface = FlightHubSdk::getSessionManager();
    auto* mgr = mgr_iface ? static_cast<SessionManager*>(mgr_iface.get()) : nullptr;
    if (!mgr) {
        LOG_ERROR("[http] mesh header: SessionManager unavailable, session_id=%d", session_id);
        return false;
    }
    auto session = mgr->getSession(session_id);
    if (!session) {
        LOG_ERROR("[http] mesh header: session_id=%d not found", session_id);
        return false;
    }
    headers["mesh-dst-idc"] = session->getSessionInfo().node_id;
    return true;
}

#ifndef __EMSCRIPTEN__
// 把在途 HTTP 回调安全回跳到 Main executor。
// shutdown() 期间 Executors::Shutdown() 会把 main_ reset 成 nullptr；若此时仍有在途
// HTTP(如 SDK 自动发起的 beacon 上报)回调返回，直接 Executors::Main()->Post(...) 会
// 解引用空 shared_ptr 触发 SIGSEGV(si_addr=0x0)。此处取本地引用并判空，Main 已销毁时
// 静默丢弃晚到回调，避免 native 崩溃。
inline void post_http_callback_to_main(
    drc_sdk::common::HttpCallbackWithStatus&& callback,
    drc_sdk::common::NetworkError err_code, int http_code,
    const std::string& response, const std::vector<std::string>& certs,
    const drc_sdk::common::HttpHeaders& resp_headers) {
    auto main = drc_sdk::common::Executors::Main();
    if (!main) {
        LOG_WARN("[http] drop in-flight callback: Executors::Main() already shut down");
        return;
    }
    main->Post([callback = std::move(callback), err_code, http_code, response, certs, resp_headers]() mutable {
        callback(err_code, http_code, response, certs, resp_headers);
    });
}

// 把 HTTP 任务安全提交到 IO executor。
// shutdown() 期间 Executors::Shutdown() 会把 io_ reset 成 nullptr；而在途回调执行时
// 可能再次发起 HTTP(如业务回调里 SDK 自动补发 beacon 上报)并调 Executors::IO()->Post，
// 直接解引用空 io_ 会触发 SIGSEGV。此处取本地引用并判空，IO 已销毁时静默丢弃新任务。
inline bool submit_http_io_task(std::function<void()>&& task) {
    auto io = drc_sdk::common::Executors::IO();
    if (!io) {
        LOG_WARN("[http] drop http task: Executors::IO() already shut down");
        return false;
    }
    io->Post(std::move(task));
    return true;
}
#endif

void HttpPostInnerWithStatus(const std::string& url, const drc_sdk::common::HttpHeaders& headers,
    const drc_sdk::common::HttpPostType post_type, const std::string& body,
    drc_sdk::common::HttpCallbackWithStatus&& callback, uint32_t timeout_ms) {
    auto request_headers = headers;
    ensure_gateway_auth_headers_if_needed(url, request_headers);
#ifdef __EMSCRIPTEN__
    drc_sdk::common::Network::Instance().Post(url, request_headers, post_type, body, std::move(callback), timeout_ms);
#else
    submit_http_io_task([url, request_headers, post_type, body, callback = std::move(callback), timeout_ms]() mutable {
        drc_sdk::common::HttpCallbackWithStatus callback_in_main =
            [callback = std::move(callback)](drc_sdk::common::NetworkError err_code, int http_code,
                const std::string& response, const std::vector<std::string>& certs,
                const drc_sdk::common::HttpHeaders& resp_headers) mutable {
            // 经安全回跳：Main 已销毁则丢弃，避免空指针解引用崩溃
            post_http_callback_to_main(std::move(callback), err_code, http_code, response, certs, resp_headers);
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
    submit_http_io_task([url, request_headers, post_type, body, callback = std::move(callback), timeout_ms]() mutable {
        drc_sdk::common::HttpCallbackWithStatus callback_in_main =
            [callback = std::move(callback)](drc_sdk::common::NetworkError err_code, int http_code,
                const std::string& response, const std::vector<std::string>& certs,
                const drc_sdk::common::HttpHeaders& resp_headers) mutable {
            // 经安全回跳：Main 已销毁则丢弃，避免空指针解引用崩溃
            post_http_callback_to_main(std::move(callback), err_code, http_code, response, certs, resp_headers);
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
    submit_http_io_task([url, request_headers, post_type, body, callback = std::move(callback), timeout_ms]() mutable {
        drc_sdk::common::HttpCallbackWithStatus callback_in_main =
            [callback = std::move(callback)](drc_sdk::common::NetworkError err_code, int http_code,
                const std::string& response, const std::vector<std::string>& certs,
                const drc_sdk::common::HttpHeaders& resp_headers) mutable {
            // 经安全回跳：Main 已销毁则丢弃，避免空指针解引用崩溃
            post_http_callback_to_main(std::move(callback), err_code, http_code, response, certs, resp_headers);
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
    submit_http_io_task([url, request_headers, callback = std::move(callback), timeout_ms]() mutable {
        drc_sdk::common::HttpCallbackWithStatus callback_in_main =
            [callback = std::move(callback)](drc_sdk::common::NetworkError err_code, int http_code,
                const std::string& response, const std::vector<std::string>& certs,
                const drc_sdk::common::HttpHeaders& resp_headers) mutable {
            // 经安全回跳：Main 已销毁则丢弃，避免空指针解引用崩溃
            post_http_callback_to_main(std::move(callback), err_code, http_code, response, certs, resp_headers);
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
