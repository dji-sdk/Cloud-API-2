// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"
#include <memory>
#include <string>
#include <vector>
#include <unordered_map>
#include "i_mqtt_client.h"

namespace message_engine {

constexpr auto DDSAL_BASE_TRANSPORT_ID = "ddsal_base";
constexpr auto PAHO_MQTT_BASE_TRANSPORT_ID = "paho_mqtt_base";
constexpr auto PAHO_MQTT_DRC_TRANSPORT_ID = "paho_mqtt_drc";

struct AddMqttClientParams {
    std::unordered_map<std::string, std::string> topic_method_map;
    std::vector<TopicInfo> topics_pub;
    std::vector<TopicInfo> topics_sub;
};

class IMqttClientManager {
public:
    virtual ~IMqttClientManager() = default;

    virtual MeErrorCode AddMqttClient(const TransportIdType& transport_id, const AddMqttClientParams& params) = 0;

    virtual MeErrorCode RemoveMqttClient(const TransportIdType& transport_id) = 0;

    virtual std::weak_ptr<IMqttClient> GetMqttClient(const TransportIdType& transport_id) = 0;

    virtual bool IsMqttClientExisted(const TransportIdType& transport_id) = 0;
};

} // namespace message_engine 