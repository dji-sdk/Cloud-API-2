// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "WebSocketHolder.h"

#include <utility>

using namespace notification_center;

int64_t WebSocketHolder::setOnMessage(MessageCallback cb) {
    std::lock_guard<std::mutex> lock(mtx_);
    int64_t id = next_listen_id_++;
    message_callbacks_[id] = std::move(cb);
    return id;
}

void WebSocketHolder::cancelOnMessage(int64_t listen_id) {
    std::lock_guard<std::mutex> lock(mtx_);
    message_callbacks_.erase(listen_id);
}

int64_t WebSocketHolder::setOnOpen(OpenCallback cb) {
    std::lock_guard<std::mutex> lock(mtx_);
    int64_t id = next_listen_id_++;
    open_callbacks_[id] = std::move(cb);
    return id;
}

void WebSocketHolder::cancelOnOpen(int64_t listen_id) {
    std::lock_guard<std::mutex> lock(mtx_);
    open_callbacks_.erase(listen_id);
}

int64_t WebSocketHolder::setOnClose(CloseCallback cb) {
    std::lock_guard<std::mutex> lock(mtx_);
    int64_t id = next_listen_id_++;
    close_callbacks_[id] = std::move(cb);
    return id;
}

void WebSocketHolder::cancelOnClose(int64_t listen_id) {
    std::lock_guard<std::mutex> lock(mtx_);
    close_callbacks_.erase(listen_id);
}

int64_t WebSocketHolder::setOnError(ErrorCallback cb) {
    std::lock_guard<std::mutex> lock(mtx_);
    int64_t id = next_listen_id_++;
    error_callbacks_[id] = std::move(cb);
    return id;
}

void WebSocketHolder::cancelOnError(int64_t listen_id) {
    std::lock_guard<std::mutex> lock(mtx_);
    error_callbacks_.erase(listen_id);
}

void WebSocketHolder::setWebSocket(std::shared_ptr<WebSocketBase> ws) {
    std::lock_guard<std::mutex> lock(mtx_);
    ws_ = std::move(ws);
    if (!ws_) return;
    
    // 设置内部回调函数到 WebSocketBase
    ws_->setOnMessage([this](const std::string &message) {
        this->onMessageInternal(message);
    });
    ws_->setOnOpen([this]() {
        this->onOpenInternal();
    });
    ws_->setOnClose([this]() {
        this->onCloseInternal();
    });
    ws_->setOnError([this](const std::string &error) {
        this->onErrorInternal(error);
    });
}

void WebSocketHolder::resetWebSocket() {
    ws_.reset();
}

WebSocketBase *WebSocketHolder::getWebSocket() const {
    if (ws_ == nullptr) {
        return nullptr;
    }
    return ws_.get();
}

void WebSocketHolder::onMessageInternal(const std::string &message) {
    // 复制回调列表，避免锁的范围太大
    std::unordered_map<int64_t, MessageCallback> callbacks_copy;
    {
        std::lock_guard<std::mutex> lock(mtx_);
        callbacks_copy = message_callbacks_;
    }
    
    // 调用所有回调
    for (const auto &kv : callbacks_copy) {
        if (kv.second) {
            kv.second(message);
        }
    }
}

void WebSocketHolder::onOpenInternal() {
    // 复制回调列表，避免锁的范围太大
    std::unordered_map<int64_t, OpenCallback> callbacks_copy;
    {
        std::lock_guard<std::mutex> lock(mtx_);
        callbacks_copy = open_callbacks_;
    }
    
    // 调用所有回调
    for (const auto &kv : callbacks_copy) {
        if (kv.second) {
            kv.second();
        }
    }
}

void WebSocketHolder::onCloseInternal() {
    // 复制回调列表，避免锁的范围太大
    std::unordered_map<int64_t, CloseCallback> callbacks_copy;
    {
        std::lock_guard<std::mutex> lock(mtx_);
        callbacks_copy = close_callbacks_;
    }
    
    // 调用所有回调
    for (const auto &kv : callbacks_copy) {
        if (kv.second) {
            kv.second();
        }
    }
}

void WebSocketHolder::onErrorInternal(const std::string &error) {
    // 复制回调列表，避免锁的范围太大
    std::unordered_map<int64_t, ErrorCallback> callbacks_copy;
    {
        std::lock_guard<std::mutex> lock(mtx_);
        callbacks_copy = error_callbacks_;
    }
    
    // 调用所有回调
    for (const auto &kv : callbacks_copy) {
        if (kv.second) {
            kv.second(error);
        }
    }
}
