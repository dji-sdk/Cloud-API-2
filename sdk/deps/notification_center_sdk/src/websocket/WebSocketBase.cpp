// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "WebSocketBase.h"

#include "base/common/logger.h"

using namespace notification_center;

void WebSocketBase::setOnMessage(MessageCallback cb) {
    message_callbacks_ = std::move(cb);
}

void WebSocketBase::setOnOpen(OpenCallback cb) {
    open_callback_ = std::move(cb);
}

void WebSocketBase::setOnClose(CloseCallback cb) {
    close_callback_ = std::move(cb);
}

void WebSocketBase::setOnError(ErrorCallback cb) {
    error_callback_ = std::move(cb);
}

void WebSocketBase::callOnMessage(const std::string &message) {
    if (message_callbacks_ == nullptr) {
        LOG_ERROR("[WebSocketBase] callOnMessage but no callback");
        return;
    }
    message_callbacks_(message);
}

void WebSocketBase::callOnOpen() {
    if (open_callback_ == nullptr) {
        LOG_ERROR("[WebSocketBase] callOnOpen but no callback");
        return;
    }
    open_callback_();
}

void WebSocketBase::callOnClose() {
    if (close_callback_ == nullptr) {
        LOG_ERROR("[WebSocketBase] callOnClose but no callback");
        return;
    }
    close_callback_();
}

void WebSocketBase::callOnError(const std::string &error) {
    if (error_callback_ == nullptr) {
        LOG_ERROR("[WebSocketBase] callOnError but no callback");
        return;
    }
    error_callback_(error);
}
