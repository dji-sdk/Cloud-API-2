// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_device_format_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsDeviceFormatSouthbound::EventsDeviceFormatSouthbound() {
    this->topic_ = "thing/product/gnd/events/device_format";
    this->method_ = "device_format";
}

EventsDeviceFormatSouthbound::EventsDeviceFormatSouthbound(const EventsDeviceFormatSouthboundMsg& msg) : EventsDeviceFormatSouthbound() {
    this->msg = msg;
}

std::string EventsDeviceFormatSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsDeviceFormatSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsDeviceFormatSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsDeviceFormatSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}