// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <memory>
#include <string>
#include <map>

#include "notification_center_sdk_api/SDKManager.h"
#include "business_manager/BusinessManagerImpl.h"
#include "websocket/WebSocketHolder.h"
#include "topic_engine/TopicManager.h"

namespace notification_center {
    // 整个 SDK 的管理器，一条 websocket 链路创建一个 SDKManagerImpl 实例
    // WebSocketHolder: 管理 websocket 连接
    // TopicManager: 在 WebSocketHolder 基础上实现消息收发的匹配，解析消息内容
    // BusinessManager: 业务逻辑处理
    class SDKManagerImpl : public SDKManager {
    public:
        SDKManagerImpl();

        ~SDKManagerImpl() override;

        void init(const SdkInitializeInfo &info) override;

        bool isInit() const override;

        void setWebSocket(WebSocketBase *ws) override; // 注入自定义 WebSocket；已启用 libwebsockets 时忽略
        void setWebSocketAuthHeaders(const std::map<std::string, std::string> &headers) override;
        void setWebsocketEventListener(const std::shared_ptr<WebsocketEvent> &listener) override; // 设置链接事件回调
        void connect(const std::string &url) override; // websocket 连接

        std::shared_ptr<BusinessManager> getBusinessManager() override {
            return business_manager_;
        }

    private:
        std::atomic<bool> is_init_ = false;

        void initExecutors();

        std::shared_ptr<WebsocketEvent> websocket_event_listener_;
        std::shared_ptr<WebSocketHolder> wsHolder_ = std::make_shared<WebSocketHolder>();
        std::shared_ptr<TopicManager> topic_manager_ = std::make_shared<TopicManager>();
        std::shared_ptr<BusinessManager> business_manager_;
    };
}
