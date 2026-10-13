// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_device_exit_homing_notify_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsDeviceExitHomingNotifyNorthbound::EventsDeviceExitHomingNotifyNorthbound() {
    this->topic_ = "thing/product/uav/events/device_exit_homing_notify";
    this->method_ = "device_exit_homing_notify";
}

EventsDeviceExitHomingNotifyNorthbound::EventsDeviceExitHomingNotifyNorthbound(const std::shared_ptr<MessageV1>& message) : EventsDeviceExitHomingNotifyNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsDeviceExitHomingNotifyNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string EventsDeviceExitHomingNotifyNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsDeviceExitHomingNotifyNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsDeviceExitHomingNotifyNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsDeviceExitHomingNotifyNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}