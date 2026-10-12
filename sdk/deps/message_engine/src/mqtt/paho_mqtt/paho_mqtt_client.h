// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include "mqtt_client.h"
#include <MQTTAsync.h>

namespace message_engine {


class PahoMqttClient : public MqttClient {
    using Super = MqttClient;
public:
    explicit PahoMqttClient(const std::shared_ptr<drc_sdk::Executor>& executor, std::unordered_map<std::string, std::string> topic_method_map, 
                            const std::vector<TopicInfo>& topics_pub, const std::vector<TopicInfo>& topics_sub, std::string log_tag);
    PahoMqttClient() = delete;
    ~PahoMqttClient() override;
public:
    /// MqttClient
    MeErrorCode SetUp() override;
    MeErrorCode CleanUp() override;
    MeErrorCode ConnectParamsConfig(const std::string& host, const std::string& username, const std::string& password, const std::string& client_id, const std::string& sn) override;

    // API for Multi Mqtt Client, this class no need to implement
    void MultiPathParamsConfig(const MqttMultiPathParams& params) override {};
    void UpdateBestLink(std::string interface_name) override {};
    void UpdateInterfaceMap(std::unordered_map<std::string, std::string> interface_map) override {};

    // MqttClient
    void TopicRegister() override;
    void Publish(const std::string &topic, const std::string &payload) override;

public:
    int Connect();
private:
    int Create();
    int Disconnect();
private:
    MQTTAsync client_ = nullptr;
    std::vector<TopicInfo> real_subscribe_topics_;
};

}  // namespace message_engine