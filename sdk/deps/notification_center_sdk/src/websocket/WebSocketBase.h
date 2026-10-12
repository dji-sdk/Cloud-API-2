// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <string>
#include <functional>
#include <list>
#include <map>
#include <mutex>

namespace notification_center {
    // WebSocket 抽象基类。默认实现是 CppWebSocket（libwebsockets）；
    // 未定义 ENABLE_LIBWEBSOCKETS 时可注入自定义实现。
    class WebSocketBase {
    public:
        using MessageCallback = std::function<void(const std::string &)>;
        using OpenCallback = std::function<void()>;
        using CloseCallback = std::function<void()>;
        using ErrorCallback = std::function<void(const std::string &)>;

        virtual ~WebSocketBase() = default;

        virtual void connect(const std::string &url) = 0;

        // 设置握手鉴权头（仅在下一次 connect() 生效）
        // 约定语义：连接建立后不热更新，变更需断开重连。
        virtual void setAuthHeaders(const std::map<std::string, std::string> &headers) {
            (void)headers;
        }

        // 设置 WSS TLS 证书验证配置（仅在下一次 connect() 生效）
        // verify_ssl=true：校验证书链 + 主机名/IP；ca_path 为空时使用系统 CA，非空时使用私有 CA 文件
        // verify_ssl=false：显式跳过验证（不安全，仅限受控测试环境）
        virtual void setTlsConfig(bool verify_ssl, const std::string &ca_path) {
            (void)verify_ssl;
            (void)ca_path;
        }

        virtual void send(const std::string &message) = 0;

        virtual void close() = 0;

        virtual bool isOpen() const = 0;

        void setOnMessage(MessageCallback cb);

        void setOnOpen(OpenCallback cb);

        void setOnClose(CloseCallback cb);

        void setOnError(ErrorCallback cb);

    protected:
        void callOnMessage(const std::string &msg);

        void callOnOpen();

        void callOnClose();

        void callOnError(const std::string &err);

        bool connected{false};

    private:
        MessageCallback message_callbacks_;
        OpenCallback open_callback_;
        CloseCallback close_callback_;
        ErrorCallback error_callback_;

        std::mutex mtx_;
    };
}
