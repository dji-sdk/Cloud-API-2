// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_obstacle_avoidance_notify_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsObstacleAvoidanceNotifySouthbound::EventsObstacleAvoidanceNotifySouthbound() {
    this->topic_ = "thing/product/gnd/events/obstacle_avoidance_notify";
    this->method_ = "obstacle_avoidance_notify";
}

EventsObstacleAvoidanceNotifySouthbound::EventsObstacleAvoidanceNotifySouthbound(const EventsObstacleAvoidanceNotifySouthboundMsg& msg) : EventsObstacleAvoidanceNotifySouthbound() {
    this->msg = msg;
}

std::string EventsObstacleAvoidanceNotifySouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsObstacleAvoidanceNotifySouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsObstacleAvoidanceNotifySouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsObstacleAvoidanceNotifySouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}