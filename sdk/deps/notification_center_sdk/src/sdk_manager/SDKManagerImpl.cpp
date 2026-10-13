// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "SDKManagerImpl.h"
#include <chrono>
#include <utility>

#include "websocket/CppWebSocket.h"
#include "notification_center_sdk_api/Common.h"
#include "base/common/logger.h"
#include "topic_engine/TopicManager.h"
#include "business_manager/BusinessManagerImpl.h"
#include "common/executors/executors.h"

using namespace notification_center;

SDKManagerImpl::SDKManagerImpl() {
    LOG_INFO("[SDKManagerImpl] ctor");
}

SDKManagerImpl::~SDKManagerImpl() {
    LOG_INFO("[SDKManagerImpl] dtor");
}

void SDKManagerImpl::init(const SdkInitializeInfo &info) {

    drc_sdk::PlogLogger::InitializePlog(static_cast<int>(info.log_level),
                                       info.log_path.c_str(),
                                       info.log_to_console,
                                       info.log_max_file_size,
                                       info.log_max_files);
    LOG_INFO("[SDKManagerImpl] log_to_console: %s, log_file: %s, max_file_size: %zu, max_files: %d",
             info.log_to_console ? "true" : "false",
             info.log_path.c_str(),
             info.log_max_file_size,
             info.log_max_files);

    LOG_INFO("[SDKManagerImpl] ctor: %p", this);

    // 初始化多线程
    initExecutors();

    // 初始化TopicManager和BusinessManager
#ifdef ENABLE_LIBWEBSOCKETS
    LOG_INFO("[SDKManagerImpl] ctor ENABLE_LIBWEBSOCKETS = true");
    auto ws = std::make_shared<CppWebSocket>();
    // 下发 TLS 证书验证配置(verify_ssl 安全默认开启;
    // 私有化自签名部署经 ssl_ca_path 提供私有 CA)
    ws->setTlsConfig(info.verify_ssl, info.ssl_ca_path);
    wsHolder_->setWebSocket(ws);
#else
    LOG_INFO("[SDKManagerImpl] ctor ENABLE_LIBWEBSOCKETS = false");
#endif

    topic_manager_->setWebSocketHolder(wsHolder_);

    business_manager_ = std::make_shared<BusinessManagerImpl>(topic_manager_);

    is_init_ = true;
}

bool SDKManagerImpl::isInit() const {
    return is_init_;
}

void SDKManagerImpl::connect(const std::string &url) {
    if (isInit() == false) {
        LOG_ERROR("[SDKManagerImpl] connect: %s, isInit == false", url.c_str());
        return;
    }
    LOG_INFO("[SDKManagerImpl] connect: %s", url.c_str());
    wsHolder_->getWebSocket()->connect(url);
}

void SDKManagerImpl::setWebSocketAuthHeaders(const std::map<std::string, std::string> &headers) {
    auto ws = wsHolder_->getWebSocket();
    if (!ws) {
        LOG_WARN("[SDKManagerImpl] setWebSocketAuthHeaders: websocket is null, size=%zu", headers.size());
        return;
    }
    ws->setAuthHeaders(headers);
    LOG_INFO("[SDKManagerImpl] setWebSocketAuthHeaders: applied %zu header(s)", headers.size());
}

void SDKManagerImpl::setWebsocketEventListener(const std::shared_ptr<WebsocketEvent> &listener) {
    // Store the listener
    websocket_event_listener_ = listener;

    // Set up C++ lambda callbacks on wsHolder_
    wsHolder_->setOnOpen([this]() {
        if (websocket_event_listener_) {
            websocket_event_listener_->OnOpen();
        }
    });
    wsHolder_->setOnClose([this]() {
        if (websocket_event_listener_) {
            websocket_event_listener_->OnClose();
        }
    });
    wsHolder_->setOnError([this](const std::string &error) {
        if (websocket_event_listener_) {
            websocket_event_listener_->OnError(error);
        }
    });
}

void SDKManagerImpl::setWebSocket(WebSocketBase *ws) {
#ifdef ENABLE_LIBWEBSOCKETS
        LOG_INFO("[SDKManagerImpl] setWebSocket ignored: using built-in libwebsockets");
#else
    LOG_INFO("[SDKManagerImpl] setWebSocket: %p", ws);
    std::shared_ptr<WebSocketBase> sp_ws(ws);
    wsHolder_->setWebSocket(sp_ws);
#endif
}

void SDKManagerImpl::initExecutors() {
    LOG_INFO("[SDKManagerImpl] initExecutors");
    // 初始化 drc_sdk executors
    drc_sdk::common::Executors::Init();
}
