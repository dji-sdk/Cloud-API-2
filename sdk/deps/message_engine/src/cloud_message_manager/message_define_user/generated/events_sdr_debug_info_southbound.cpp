// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_sdr_debug_info_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsSdrDebugInfoSouthbound::EventsSdrDebugInfoSouthbound() {
    this->topic_ = "thing/product/gnd/events/sdr_debug_info";
    this->method_ = "sdr_debug_info";
}

EventsSdrDebugInfoSouthbound::EventsSdrDebugInfoSouthbound(const EventsSdrDebugInfoSouthboundMsg& msg) : EventsSdrDebugInfoSouthbound() {
    this->msg = msg;
}

std::string EventsSdrDebugInfoSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsSdrDebugInfoSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsSdrDebugInfoSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsSdrDebugInfoSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}