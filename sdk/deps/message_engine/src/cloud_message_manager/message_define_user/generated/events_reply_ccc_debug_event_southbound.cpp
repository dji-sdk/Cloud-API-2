// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_ccc_debug_event_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyCccDebugEventSouthbound::EventsReplyCccDebugEventSouthbound() {
    this->topic_ = "thing/product/uav/events_reply/ccc_debug_event";
    this->method_ = "ccc_debug_event";
}

EventsReplyCccDebugEventSouthbound::EventsReplyCccDebugEventSouthbound(const EventsReplyCccDebugEventSouthboundMsg& msg) : EventsReplyCccDebugEventSouthbound() {
    this->msg = msg;
}

std::string EventsReplyCccDebugEventSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyCccDebugEventSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyCccDebugEventSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyCccDebugEventSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}