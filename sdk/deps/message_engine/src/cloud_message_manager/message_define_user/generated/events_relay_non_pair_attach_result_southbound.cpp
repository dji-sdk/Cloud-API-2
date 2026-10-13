// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_relay_non_pair_attach_result_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsRelayNonPairAttachResultSouthbound::EventsRelayNonPairAttachResultSouthbound() {
    this->topic_ = "thing/product/gnd/events/relay_non_pair_attach_result";
    this->method_ = "relay_non_pair_attach_result";
}

EventsRelayNonPairAttachResultSouthbound::EventsRelayNonPairAttachResultSouthbound(const EventsRelayNonPairAttachResultSouthboundMsg& msg) : EventsRelayNonPairAttachResultSouthbound() {
    this->msg = msg;
}

std::string EventsRelayNonPairAttachResultSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsRelayNonPairAttachResultSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsRelayNonPairAttachResultSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsRelayNonPairAttachResultSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}