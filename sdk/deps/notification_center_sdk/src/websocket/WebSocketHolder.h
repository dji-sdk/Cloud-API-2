// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <functional>
#include <memory>
#include <mutex>
#include <unordered_map>
#include <atomic>
#include <cstdint>
#include "WebSocketBase.h"

namespace notification_center {
    using MessageCallback = std::function<void(const std::string &)>;
    using OpenCallback = std::function<void()>;
    using CloseCallback = std::function<void()>;
    using ErrorCallback = std::function<void(const std::string &)>;

    class WebSocketHolder {
    public:
        WebSocketHolder() = default;

        ~WebSocketHolder() = default;

        // 支持多个消息回调
        int64_t setOnMessage(MessageCallback cb);
        void cancelOnMessage(int64_t listen_id);

        // 支持多个打开回调
        int64_t setOnOpen(OpenCallback cb);
        void cancelOnOpen(int64_t listen_id);

        // 支持多个关闭回调
        int64_t setOnClose(CloseCallback cb);
        void cancelOnClose(int64_t listen_id);

        // 支持多个错误回调
        int64_t setOnError(ErrorCallback cb);
        void cancelOnError(int64_t listen_id);

        void setWebSocket(std::shared_ptr<WebSocketBase> ws);
        void resetWebSocket();
        WebSocketBase *getWebSocket() const;

    private:
        // 内部回调函数，用于数据分发
        void onOpenInternal();
        void onCloseInternal();
        void onErrorInternal(const std::string &error);
        void onMessageInternal(const std::string &message);

        std::mutex mtx_;
        std::shared_ptr<WebSocketBase> ws_;

        // 支持多个回调
        std::unordered_map<int64_t, MessageCallback> message_callbacks_;
        std::unordered_map<int64_t, OpenCallback> open_callbacks_;
        std::unordered_map<int64_t, CloseCallback> close_callbacks_;
        std::unordered_map<int64_t, ErrorCallback> error_callbacks_;
        
        std::atomic<int64_t> next_listen_id_{1};
    };
}
