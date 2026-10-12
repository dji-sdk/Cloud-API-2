// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"
#include <string>

namespace message_engine {

/**
 * @brief MQTT客户端桥接接口
 * 
 * 用于跨语言互操作的MQTT客户端接口, 接口应当为Mqtt的基础API, 不应当包含业务逻辑
 */
class IMqttClientBridge {
public:
    virtual ~IMqttClientBridge() = default;

    // 消息发布
    virtual MeErrorCode Create() = 0;
    virtual MeErrorCode SetConnectParams(
        const std::string& host,
        const std::string& username,
        const std::string& password,
        const std::string& client_id,
        const std::string& sn) = 0;
    virtual MeErrorCode Connect() = 0;
    virtual MeErrorCode Disconnect() = 0;
    virtual MeErrorCode Destroy() = 0;

    virtual MeErrorCode Publish(const std::string& topic, const std::string& payload) = 0;
    virtual MeErrorCode Subscribe(const std::string& topic) = 0;
    virtual MeErrorCode Unsubscribe(const std::string& topic) = 0;

    virtual MeErrorCode SetConnectStateCallback(const MqttConnectStateCallback& callback) = 0;
    virtual MeErrorCode RemoveConnectStateCallback(uint32_t listen_id) = 0;

    virtual MeErrorCode SetMessageCallback(const MessageCallback& callback) = 0;
    virtual MeErrorCode RemoveMessageCallback(uint32_t listen_id) = 0;
};

} // namespace message_engine 