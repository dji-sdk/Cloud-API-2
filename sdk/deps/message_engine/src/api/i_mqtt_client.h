// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"
#include <memory>
#include <string>
#include <unordered_map>

namespace message_engine {

struct TopicInfo {
    std::string topic_name;
    std::string type;
    int qos = 0;
};

enum class MQTT_CONNECT_STATUS{
    IDLE = 0,
    PREPARED = 1,
    CONNECTED = 2,
    DISCONNECTED = 3,
};

struct MqttMultiPathParams {
    std::unordered_map<std::string, std::string> interface_map; // key: interface name, value: mqtt host IP address
};

using MqttConnectStateCallback = std::function<void(const MQTT_CONNECT_STATUS& state)>;

class IMqttClient {
public:
    virtual ~IMqttClient() = default;

    // 连接管理
    virtual MeErrorCode SetUp() = 0;
    virtual MeErrorCode CleanUp() = 0;
    virtual MeErrorCode ConnectParamsConfig(const std::string& host, const std::string& username, const std::string& password, const std::string& client_id, const std::string& sn) = 0;

    // Enable Multi Mqtt Client
    virtual void MultiPathParamsConfig(const MqttMultiPathParams& params) = 0;
    virtual void UpdateBestLink(std::string interface_name) = 0;
    virtual void UpdateInterfaceMap(std::unordered_map<std::string, std::string> interface_map) = 0;

    // 连接状态回调
    virtual uint32_t AddConnectStateCallback(const MqttConnectStateCallback& callback) = 0;
    virtual void RemoveConnectStateCallback(uint32_t listen_id) = 0;

};

} // namespace message_engine 