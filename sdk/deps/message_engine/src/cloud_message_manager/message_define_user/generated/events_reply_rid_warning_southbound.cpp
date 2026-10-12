// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_rid_warning_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyRidWarningSouthbound::EventsReplyRidWarningSouthbound() {
    this->topic_ = "thing/product/uav/events_reply/rid_warning";
    this->method_ = "rid_warning";
}

EventsReplyRidWarningSouthbound::EventsReplyRidWarningSouthbound(const EventsReplyRidWarningSouthboundMsg& msg) : EventsReplyRidWarningSouthbound() {
    this->msg = msg;
}

std::string EventsReplyRidWarningSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyRidWarningSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyRidWarningSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyRidWarningSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}