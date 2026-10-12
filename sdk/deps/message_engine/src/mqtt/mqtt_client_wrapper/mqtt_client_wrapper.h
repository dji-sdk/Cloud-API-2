// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include "mqtt_client.h"
#include "api/bridge/i_mqtt_client_bridge.h"

namespace message_engine {


class MqttClientWrapper : public MqttClient {
    using Super = MqttClient;
public:
    explicit MqttClientWrapper(
        const std::shared_ptr<drc_sdk::Executor>& executor,
        std::unordered_map<std::string, std::string> topic_method_map,
        const std::vector<TopicInfo>& topics_pub,
        const std::vector<TopicInfo>& topics_sub,
        std::string log_tag,
        std::shared_ptr<IMqttClientBridge> mqtt_client_bridge);
    MqttClientWrapper() = delete;
    ~MqttClientWrapper() override = default;
public:
    /// MqttClient
    MeErrorCode SetUp() override;
    MeErrorCode CleanUp() override;
    MeErrorCode ConnectParamsConfig(
        const std::string& host,
        const std::string& username,
        const std::string& password,
        const std::string& client_id,
        const std::string& sn) override;

    // API for Multi Mqtt Client, this class no need to implement
    void MultiPathParamsConfig(const MqttMultiPathParams& params) override {};
    void UpdateBestLink(std::string interface_name) override {};
    void UpdateInterfaceMap(std::unordered_map<std::string, std::string> interface_map) override {};
    
    // MqttClient
    void TopicRegister() override;
    void Publish(const std::string &topic, const std::string &payload) override;
private:
    std::shared_ptr<IMqttClientBridge> mqtt_client_bridge_ = nullptr;
    uint32_t connect_state_listen_id_ = 0;
    uint32_t message_listen_id_ = 0;
};

}  // namespace message_engine