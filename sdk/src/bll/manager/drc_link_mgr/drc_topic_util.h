// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include "http_interface/response_message.h"

namespace flight_hub_sdk {

struct DrcMqttTopicInfo {
    bool ok = false;
    /// true：up_topic/down_topic 来自后端；false：缺字段，回退 gateway_sn
    bool from_backend_topics = false;
    std::string up_topic;
    std::string down_topic;
    /// 用于 message_engine 替换 {device_sn}
    std::string topic_sn;
};

/// 从 open_data_channel(mqtt_broker) 响应解析 DRC MQTT topic；缺字段时回退 gateway_sn。
DrcMqttTopicInfo ResolveDrcMqttTopics(
    const OpenDataChannelResponseRtChannelConfig& config,
    const std::string& gateway_sn);

}  // namespace flight_hub_sdk
