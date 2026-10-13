// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include "WebSocketBase.h"
#include <memory>
#include <string>
#include <thread>

namespace notification_center {
    // C++原生WebSocket实现, 使用 libwebsockets 实现
    class CppWebSocket : public WebSocketBase {
    public:
        CppWebSocket();

        ~CppWebSocket() override;

        void connect(const std::string &url) override;
        void setAuthHeaders(const std::map<std::string, std::string> &headers) override;
        void setTlsConfig(bool verify_ssl, const std::string &ca_path) override;

        void send(const std::string &message) override;

        void close() override;

        bool isOpen() const override;

    private:
#ifdef ENABLE_LIBWEBSOCKETS
        class Impl;
        std::unique_ptr<Impl> pImpl;
#else
        void* pImpl;
#endif
    };
}
