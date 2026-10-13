// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_release_terminal_control_area_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReleaseTerminalControlAreaSouthbound::EventsReleaseTerminalControlAreaSouthbound() {
    this->topic_ = "thing/product/gnd/events/release_terminal_control_area";
    this->method_ = "release_terminal_control_area";
}

EventsReleaseTerminalControlAreaSouthbound::EventsReleaseTerminalControlAreaSouthbound(const EventsReleaseTerminalControlAreaSouthboundMsg& msg) : EventsReleaseTerminalControlAreaSouthbound() {
    this->msg = msg;
}

std::string EventsReleaseTerminalControlAreaSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReleaseTerminalControlAreaSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReleaseTerminalControlAreaSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReleaseTerminalControlAreaSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}