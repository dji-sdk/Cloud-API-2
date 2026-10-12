// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_emergency_stop_notify_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsEmergencyStopNotifySouthbound::EventsEmergencyStopNotifySouthbound() {
    this->topic_ = "thing/product/gnd/events/emergency_stop_notify";
    this->method_ = "emergency_stop_notify";
}

EventsEmergencyStopNotifySouthbound::EventsEmergencyStopNotifySouthbound(const EventsEmergencyStopNotifySouthboundMsg& msg) : EventsEmergencyStopNotifySouthbound() {
    this->msg = msg;
}

std::string EventsEmergencyStopNotifySouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsEmergencyStopNotifySouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsEmergencyStopNotifySouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsEmergencyStopNotifySouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}