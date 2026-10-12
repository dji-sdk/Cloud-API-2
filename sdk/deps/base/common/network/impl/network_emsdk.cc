// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "network_impl.h"
#include "base/common/logger.h"
#include "common/log_safe.h"
#include "network.h"
#include <emscripten/fetch.h>
#include <emscripten/emscripten.h>
// 单线程模式：移除 threading 相关头文件
// #include <emscripten/threading_legacy.h>
// #include <emscripten/threading.h>

namespace drc_sdk {
namespace common {
namespace injector {

class FetchParamCache {
public:
    std::string url;
    std::vector<HttpHeader> headers;
    HttpPostType post_type;
    std::string body;
    drc_sdk::common::injector::HttpCallback callback;
};
/**************** C code for emscripten fetch ****************/
EMSCRIPTEN_KEEPALIVE
static void on_error(emscripten_fetch_t *fetch) {
    LOG_ERROR("[NetworkImpl] onError: %d %s\n", fetch->status, fetch->statusText);
    if (fetch->userData) {
        auto fetch_param_cache = static_cast<FetchParamCache*>(fetch->userData);
        (fetch_param_cache->callback)(NetworkError::UNKNOWN, {}, {}, {});
        delete fetch_param_cache;
    }
    emscripten_fetch_close(fetch);
}

EMSCRIPTEN_KEEPALIVE
static void on_success(emscripten_fetch_t *fetch) {
    LOG_INFO("[NetworkImpl] onPostSuccess URL=%s status=%d numBytes=%llu", fetch->url, fetch->status, fetch->numBytes);
    // LOG_INFO("data: %d %s", (int)fetch->numBytes, fetch->data);
    if (fetch->userData) {
        auto fetch_param_cache = static_cast<FetchParamCache*>(fetch->userData);
        (fetch_param_cache->callback)(NetworkError::SUCCESS, Bytes(fetch->data, fetch->data + fetch->numBytes), {}, {});
        delete fetch_param_cache;
    }
    emscripten_fetch_close(fetch); // Free data associated with the fetch.
}
EMSCRIPTEN_KEEPALIVE
static void emscripten_get_wrapper(void *args) {
    if (args == nullptr) {
        LOG_ERROR("[NetworkImpl] emscripten_get_wrapper args: %p", args);
        return;
    }
    auto fetch_param_cache = static_cast<FetchParamCache*>(args);
    emscripten_fetch_attr_t attr;
    emscripten_fetch_attr_init(&attr);
    strcpy(attr.requestMethod, "GET");
    attr.attributes = EMSCRIPTEN_FETCH_LOAD_TO_MEMORY;
    attr.onsuccess = on_success;
    attr.onerror = on_error;    
    const char* headers_fetch[fetch_param_cache->headers.size() * 2 + 1];
    for (size_t i = 0; i < fetch_param_cache->headers.size(); i++) {
        headers_fetch[i * 2] = fetch_param_cache->headers[i].key.c_str();
        headers_fetch[i * 2 + 1] = fetch_param_cache->headers[i].value.c_str();
    }
    headers_fetch[fetch_param_cache->headers.size() * 2] = NULL;
    attr.requestHeaders = headers_fetch;
    attr.userData = fetch_param_cache;
    emscripten_fetch(&attr, fetch_param_cache->url.c_str());
}

EMSCRIPTEN_KEEPALIVE
static void emscripten_post_wrapper(void *args) {
    if (args == nullptr) {
        LOG_ERROR("[NetworkImpl] emscripten_post_wrapper args: %p", args);
        return;
    }
    auto fetch_param_cache = static_cast<FetchParamCache*>(args);
    emscripten_fetch_attr_t attr;
    emscripten_fetch_attr_init(&attr);
    strcpy(attr.requestMethod, "POST");
    attr.attributes = EMSCRIPTEN_FETCH_LOAD_TO_MEMORY;
    attr.onsuccess = on_success;
    attr.onerror = on_error;
    const char* headers_fetch[fetch_param_cache->headers.size() * 2 + 1];
    for (size_t i = 0; i < fetch_param_cache->headers.size(); i++) {
        headers_fetch[i * 2] = fetch_param_cache->headers[i].key.c_str();
        headers_fetch[i * 2 + 1] = fetch_param_cache->headers[i].value.c_str();
    }
    headers_fetch[fetch_param_cache->headers.size() * 2] = NULL;
    attr.requestHeaders = headers_fetch;

    attr.requestDataSize = fetch_param_cache->body.size();
    attr.requestData = fetch_param_cache->body.c_str();
    attr.userData = fetch_param_cache;
    emscripten_fetch(&attr, fetch_param_cache->url.c_str());
}

EMSCRIPTEN_KEEPALIVE
static void emscripten_put_wrapper(void *args) {
    if (args == nullptr) {
        LOG_ERROR("[NetworkImpl] emscripten_put_wrapper args: %p", args);
        return;
    }
    auto fetch_param_cache = static_cast<FetchParamCache*>(args);
    emscripten_fetch_attr_t attr;
    emscripten_fetch_attr_init(&attr);
    strcpy(attr.requestMethod, "PUT");
    attr.attributes = EMSCRIPTEN_FETCH_LOAD_TO_MEMORY;
    attr.onsuccess = on_success;
    attr.onerror = on_error;
    const char* headers_fetch[fetch_param_cache->headers.size() * 2 + 1];
    for (size_t i = 0; i < fetch_param_cache->headers.size(); i++) {
        headers_fetch[i * 2] = fetch_param_cache->headers[i].key.c_str();
        headers_fetch[i * 2 + 1] = fetch_param_cache->headers[i].value.c_str();
    }
    headers_fetch[fetch_param_cache->headers.size() * 2] = NULL;
    attr.requestHeaders = headers_fetch;
    attr.requestDataSize = fetch_param_cache->body.size();
    attr.requestData = fetch_param_cache->body.c_str();
    attr.userData = fetch_param_cache;
    emscripten_fetch(&attr, fetch_param_cache->url.c_str());
}

EMSCRIPTEN_KEEPALIVE
static void emscripten_delete_wrapper(void *args) {
    if (args == nullptr) {
        LOG_ERROR("[NetworkImpl] emscripten_delete_wrapper args: %p", args);
        return;
    }
    auto fetch_param_cache = static_cast<FetchParamCache*>(args);
    emscripten_fetch_attr_t attr;
    emscripten_fetch_attr_init(&attr);
    strcpy(attr.requestMethod, "DELETE");
    attr.attributes = EMSCRIPTEN_FETCH_LOAD_TO_MEMORY;
    attr.onsuccess = on_success;
    attr.onerror = on_error;
    const char* headers_fetch[fetch_param_cache->headers.size() * 2 + 1];
    for (size_t i = 0; i < fetch_param_cache->headers.size(); i++) {
        headers_fetch[i * 2] = fetch_param_cache->headers[i].key.c_str();
        headers_fetch[i * 2 + 1] = fetch_param_cache->headers[i].value.c_str();
    }
    headers_fetch[fetch_param_cache->headers.size() * 2] = NULL;
    attr.requestHeaders = headers_fetch;
    attr.requestDataSize = fetch_param_cache->body.size();
    attr.requestData = fetch_param_cache->body.c_str();
    attr.userData = fetch_param_cache;
    emscripten_fetch(&attr, fetch_param_cache->url.c_str());
}
/**************** C code for emscripten fetch end ****************/


NetworkImpl::NetworkImpl() {
    LOG_INFO("NetworkImpl::NetworkImpl");
}

NetworkImpl::~NetworkImpl() {
    LOG_INFO("NetworkImpl::~NetworkImpl");
}

EMSCRIPTEN_KEEPALIVE
void NetworkImpl::get(const std::string &url, const std::vector<HttpHeader> &headers,
             drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms) {
    LOG_INFO("[NetworkImpl] get url: %s", url.c_str());
    FetchParamCache* fetch_param_cache = new FetchParamCache();
    fetch_param_cache->url = url;
    fetch_param_cache->headers = headers;
    fetch_param_cache->callback = std::move(callback);
    // 单线程模式：直接调用或使用 emscripten_async_call
    emscripten_async_call(emscripten_get_wrapper, fetch_param_cache, 0);
}

EMSCRIPTEN_KEEPALIVE
void NetworkImpl::post(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
    const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms) {
    LOG_DEBUG("[NetworkImpl] post body: %d %s", body.size(), log_safe(body).c_str());

    FetchParamCache* fetch_param_cache = new FetchParamCache();
    fetch_param_cache->url = url;
    fetch_param_cache->headers = headers;
    fetch_param_cache->post_type = post_type;
    fetch_param_cache->body = body;
    fetch_param_cache->callback = std::move(callback);

    // 单线程模式：直接调用或使用 emscripten_async_call
    emscripten_async_call(emscripten_post_wrapper, fetch_param_cache, 0);
}

EMSCRIPTEN_KEEPALIVE
void NetworkImpl::put_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
    const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms) {
    LOG_DEBUG("[NetworkImpl] put_request body: %d %s", body.size(), log_safe(body).c_str());
    FetchParamCache* fetch_param_cache = new FetchParamCache();
    fetch_param_cache->url = url;
    fetch_param_cache->headers = headers;
    fetch_param_cache->post_type = post_type;
    fetch_param_cache->body = body;
    fetch_param_cache->callback = std::move(callback);
    // 单线程模式：直接调用或使用 emscripten_async_call
    emscripten_async_call(emscripten_put_wrapper, fetch_param_cache, 0);
}

EMSCRIPTEN_KEEPALIVE
void NetworkImpl::delete_request(const std::string &url, const std::vector<HttpHeader> &headers, HttpPostType post_type,
    const std::string &body, drc_sdk::common::injector::HttpCallback &&callback, uint32_t timeout_ms) {
    LOG_DEBUG("[NetworkImpl] delete_request body: %d %s", body.size(), log_safe(body).c_str());
    FetchParamCache* fetch_param_cache = new FetchParamCache();
    fetch_param_cache->url = url;
    fetch_param_cache->headers = headers;
    fetch_param_cache->post_type = post_type;
    fetch_param_cache->body = body;
    fetch_param_cache->callback = std::move(callback);
    // 单线程模式：直接调用或使用 emscripten_async_call
    emscripten_async_call(emscripten_delete_wrapper, fetch_param_cache, 0);
}

void NetworkImpl::form_post(const std::string &url, const std::vector<HttpHeader> &headers,
                   const std::vector<std::string> &body, drc_sdk::common::injector::HttpCallback &&callback) {
    // TODO: implement
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

// --- Stage 3: WithStatus 正式实现，读取 fetch->status 作为 http_code ---

class FetchParamCacheWithStatus {
public:
    std::string url;
    std::vector<HttpHeader> headers;
    HttpPostType post_type;
    std::string body;
    drc_sdk::common::injector::HttpCallbackWithStatus callback;
};

EMSCRIPTEN_KEEPALIVE
static void on_error_with_status(emscripten_fetch_t *fetch) {
    LOG_ERROR("[NetworkImpl] onError(WithStatus): %d %s\n", fetch->status, fetch->statusText);
    if (fetch->userData) {
        auto cache = static_cast<FetchParamCacheWithStatus*>(fetch->userData);
        (cache->callback)(NetworkError::UNKNOWN, static_cast<int>(fetch->status), {}, {}, {});
        delete cache;
    }
    emscripten_fetch_close(fetch);
}

EMSCRIPTEN_KEEPALIVE
static void on_success_with_status(emscripten_fetch_t *fetch) {
    LOG_INFO("[NetworkImpl] onSuccess(WithStatus) URL=%s status=%d numBytes=%llu", fetch->url, fetch->status, fetch->numBytes);
    if (fetch->userData) {
        auto cache = static_cast<FetchParamCacheWithStatus*>(fetch->userData);
        (cache->callback)(NetworkError::SUCCESS, static_cast<int>(fetch->status),
                          Bytes(fetch->data, fetch->data + fetch->numBytes), {}, {});
        delete cache;
    }
    emscripten_fetch_close(fetch);
}

EMSCRIPTEN_KEEPALIVE
static void emscripten_get_wrapper_with_status(void *args) {
    if (args == nullptr) return;
    auto cache = static_cast<FetchParamCacheWithStatus*>(args);
    emscripten_fetch_attr_t attr;
    emscripten_fetch_attr_init(&attr);
    strcpy(attr.requestMethod, "GET");
    attr.attributes = EMSCRIPTEN_FETCH_LOAD_TO_MEMORY;
    attr.onsuccess = on_success_with_status;
    attr.onerror = on_error_with_status;
    const char* headers_fetch[cache->headers.size() * 2 + 1];
    for (size_t i = 0; i < cache->headers.size(); i++) {
        headers_fetch[i * 2] = cache->headers[i].key.c_str();
        headers_fetch[i * 2 + 1] = cache->headers[i].value.c_str();
    }
    headers_fetch[cache->headers.size() * 2] = NULL;
    attr.requestHeaders = headers_fetch;
    attr.userData = cache;
    emscripten_fetch(&attr, cache->url.c_str());
}

EMSCRIPTEN_KEEPALIVE
static void emscripten_post_wrapper_with_status(void *args) {
    if (args == nullptr) return;
    auto cache = static_cast<FetchParamCacheWithStatus*>(args);
    emscripten_fetch_attr_t attr;
    emscripten_fetch_attr_init(&attr);
    strcpy(attr.requestMethod, "POST");
    attr.attributes = EMSCRIPTEN_FETCH_LOAD_TO_MEMORY;
    attr.onsuccess = on_success_with_status;
    attr.onerror = on_error_with_status;
    const char* headers_fetch[cache->headers.size() * 2 + 1];
    for (size_t i = 0; i < cache->headers.size(); i++) {
        headers_fetch[i * 2] = cache->headers[i].key.c_str();
        headers_fetch[i * 2 + 1] = cache->headers[i].value.c_str();
    }
    headers_fetch[cache->headers.size() * 2] = NULL;
    attr.requestHeaders = headers_fetch;
    attr.requestDataSize = cache->body.size();
    attr.requestData = cache->body.c_str();
    attr.userData = cache;
    emscripten_fetch(&attr, cache->url.c_str());
}

EMSCRIPTEN_KEEPALIVE
static void emscripten_put_wrapper_with_status(void *args) {
    if (args == nullptr) return;
    auto cache = static_cast<FetchParamCacheWithStatus*>(args);
    emscripten_fetch_attr_t attr;
    emscripten_fetch_attr_init(&attr);
    strcpy(attr.requestMethod, "PUT");
    attr.attributes = EMSCRIPTEN_FETCH_LOAD_TO_MEMORY;
    attr.onsuccess = on_success_with_status;
    attr.onerror = on_error_with_status;
    const char* headers_fetch[cache->headers.size() * 2 + 1];
    for (size_t i = 0; i < cache->headers.size(); i++) {
        headers_fetch[i * 2] = cache->headers[i].key.c_str();
        headers_fetch[i * 2 + 1] = cache->headers[i].value.c_str();
    }
    headers_fetch[cache->headers.size() * 2] = NULL;
    attr.requestHeaders = headers_fetch;
    attr.requestDataSize = cache->body.size();
    attr.requestData = cache->body.c_str();
    attr.userData = cache;
    emscripten_fetch(&attr, cache->url.c_str());
}

EMSCRIPTEN_KEEPALIVE
static void emscripten_delete_wrapper_with_status(void *args) {
    if (args == nullptr) return;
    auto cache = static_cast<FetchParamCacheWithStatus*>(args);
    emscripten_fetch_attr_t attr;
    emscripten_fetch_attr_init(&attr);
    strcpy(attr.requestMethod, "DELETE");
    attr.attributes = EMSCRIPTEN_FETCH_LOAD_TO_MEMORY;
    attr.onsuccess = on_success_with_status;
    attr.onerror = on_error_with_status;
    const char* headers_fetch[cache->headers.size() * 2 + 1];
    for (size_t i = 0; i < cache->headers.size(); i++) {
        headers_fetch[i * 2] = cache->headers[i].key.c_str();
        headers_fetch[i * 2 + 1] = cache->headers[i].value.c_str();
    }
    headers_fetch[cache->headers.size() * 2] = NULL;
    attr.requestHeaders = headers_fetch;
    attr.requestDataSize = cache->body.size();
    attr.requestData = cache->body.c_str();
    attr.userData = cache;
    emscripten_fetch(&attr, cache->url.c_str());
}

void NetworkImpl::post(const std::string &url, const std::vector<HttpHeader> &headers,
                       HttpPostType post_type, const std::string &body,
                       HttpCallbackWithStatus &&callback, uint32_t timeout_ms) {
    LOG_DEBUG("[NetworkImpl] post(WithStatus) body: %d %s", body.size(), log_safe(body).c_str());
    auto cache = new FetchParamCacheWithStatus();
    cache->url = url;
    cache->headers = headers;
    cache->post_type = post_type;
    cache->body = body;
    cache->callback = std::move(callback);
    emscripten_async_call(emscripten_post_wrapper_with_status, cache, 0);
}

void NetworkImpl::put_request(const std::string &url, const std::vector<HttpHeader> &headers,
                              HttpPostType post_type, const std::string &body,
                              HttpCallbackWithStatus &&callback, uint32_t timeout_ms) {
    LOG_DEBUG("[NetworkImpl] put_request(WithStatus) body: %d %s", body.size(), log_safe(body).c_str());
    auto cache = new FetchParamCacheWithStatus();
    cache->url = url;
    cache->headers = headers;
    cache->post_type = post_type;
    cache->body = body;
    cache->callback = std::move(callback);
    emscripten_async_call(emscripten_put_wrapper_with_status, cache, 0);
}

void NetworkImpl::delete_request(const std::string &url, const std::vector<HttpHeader> &headers,
                                 HttpPostType post_type, const std::string &body,
                                 HttpCallbackWithStatus &&callback, uint32_t timeout_ms) {
    LOG_DEBUG("[NetworkImpl] delete_request(WithStatus) body: %d %s", body.size(), log_safe(body).c_str());
    auto cache = new FetchParamCacheWithStatus();
    cache->url = url;
    cache->headers = headers;
    cache->post_type = post_type;
    cache->body = body;
    cache->callback = std::move(callback);
    emscripten_async_call(emscripten_delete_wrapper_with_status, cache, 0);
}

void NetworkImpl::get(const std::string &url, const std::vector<HttpHeader> &headers,
                      HttpCallbackWithStatus &&callback, uint32_t timeout_ms) {
    LOG_INFO("[NetworkImpl] get(WithStatus) url: %s", url.c_str());
    auto cache = new FetchParamCacheWithStatus();
    cache->url = url;
    cache->headers = headers;
    cache->callback = std::move(callback);
    emscripten_async_call(emscripten_get_wrapper_with_status, cache, 0);
}

} // namespace injector
} // namespace common
} // namespace drc_sdk