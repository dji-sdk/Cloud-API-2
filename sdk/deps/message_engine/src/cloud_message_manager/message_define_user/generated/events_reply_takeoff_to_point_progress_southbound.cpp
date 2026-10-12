// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_takeoff_to_point_progress_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyTakeoffToPointProgressSouthbound::EventsReplyTakeoffToPointProgressSouthbound() {
    this->topic_ = "thing/product/uav/events_reply/takeoff_to_point_progress";
    this->method_ = "takeoff_to_point_progress";
}

EventsReplyTakeoffToPointProgressSouthbound::EventsReplyTakeoffToPointProgressSouthbound(const EventsReplyTakeoffToPointProgressSouthboundMsg& msg) : EventsReplyTakeoffToPointProgressSouthbound() {
    this->msg = msg;
}

std::string EventsReplyTakeoffToPointProgressSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyTakeoffToPointProgressSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyTakeoffToPointProgressSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyTakeoffToPointProgressSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}