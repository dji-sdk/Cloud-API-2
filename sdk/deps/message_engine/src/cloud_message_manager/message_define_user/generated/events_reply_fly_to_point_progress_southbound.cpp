// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_fly_to_point_progress_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyFlyToPointProgressSouthbound::EventsReplyFlyToPointProgressSouthbound() {
    this->topic_ = "thing/product/uav/events_reply/fly_to_point_progress";
    this->method_ = "fly_to_point_progress";
}

EventsReplyFlyToPointProgressSouthbound::EventsReplyFlyToPointProgressSouthbound(const EventsReplyFlyToPointProgressSouthboundMsg& msg) : EventsReplyFlyToPointProgressSouthbound() {
    this->msg = msg;
}

std::string EventsReplyFlyToPointProgressSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyFlyToPointProgressSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyFlyToPointProgressSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyFlyToPointProgressSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}