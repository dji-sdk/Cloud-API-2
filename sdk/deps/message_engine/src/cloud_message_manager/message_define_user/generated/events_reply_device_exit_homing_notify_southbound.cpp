// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_device_exit_homing_notify_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyDeviceExitHomingNotifySouthbound::EventsReplyDeviceExitHomingNotifySouthbound() {
    this->topic_ = "thing/product/uav/events_reply/device_exit_homing_notify";
    this->method_ = "device_exit_homing_notify";
}

EventsReplyDeviceExitHomingNotifySouthbound::EventsReplyDeviceExitHomingNotifySouthbound(const EventsReplyDeviceExitHomingNotifySouthboundMsg& msg) : EventsReplyDeviceExitHomingNotifySouthbound() {
    this->msg = msg;
}

std::string EventsReplyDeviceExitHomingNotifySouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyDeviceExitHomingNotifySouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyDeviceExitHomingNotifySouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyDeviceExitHomingNotifySouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}