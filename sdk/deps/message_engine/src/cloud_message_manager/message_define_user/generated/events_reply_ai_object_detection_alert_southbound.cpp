// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_ai_object_detection_alert_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyAiObjectDetectionAlertSouthbound::EventsReplyAiObjectDetectionAlertSouthbound() {
    this->topic_ = "thing/product/uav/events_reply/ai_object_detection_alert";
    this->method_ = "ai_object_detection_alert";
}

EventsReplyAiObjectDetectionAlertSouthbound::EventsReplyAiObjectDetectionAlertSouthbound(const EventsReplyAiObjectDetectionAlertSouthboundMsg& msg) : EventsReplyAiObjectDetectionAlertSouthbound() {
    this->msg = msg;
}

std::string EventsReplyAiObjectDetectionAlertSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyAiObjectDetectionAlertSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyAiObjectDetectionAlertSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyAiObjectDetectionAlertSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}