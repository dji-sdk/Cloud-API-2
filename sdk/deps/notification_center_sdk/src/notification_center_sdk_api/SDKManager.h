// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <memory>
#include <string>
#include <map>
#include "Common.h"
#include "WebsocketEvent.h"
#include "BusinessManager.h"
#include "APIExport.h"

// 前向声明
namespace notification_center {
    class WebSocketBase;
}

namespace notification_center {
    // SDK管理器的抽象接口
    class NOTIFICATION_CENTER_API SDKManager {
    public:
        virtual ~SDKManager() = default;

        /**
         * 初始化SDK
         * @param info 初始化信息
         */
        virtual void init(const SdkInitializeInfo &info) = 0;

        /**
         * 检查SDK是否已初始化
         * @return 是否已初始化
         */
        virtual bool isInit() const = 0;

        /**
         * 注入自定义 WebSocket。已启用 libwebsockets 时忽略。
         * @param ws WebSocket 实现
         */
        virtual void setWebSocket(WebSocketBase *ws) = 0;
        virtual void setWebSocketAuthHeaders(const std::map<std::string, std::string> &headers) {
            (void)headers;
        }

        /**
         * 设置WebSocket事件监听器
         * @param listener 事件监听器
         */
        virtual void setWebsocketEventListener(const std::shared_ptr<WebsocketEvent> &listener) = 0;

        /**
         * 连接到WebSocket服务器
         * @param url WebSocket服务器URL
         */
        virtual void connect(const std::string &url) = 0;

        /**
         * 获取业务管理器
         * @return 业务管理器实例
         */
        virtual std::shared_ptr<BusinessManager> getBusinessManager() = 0;
    };
}
