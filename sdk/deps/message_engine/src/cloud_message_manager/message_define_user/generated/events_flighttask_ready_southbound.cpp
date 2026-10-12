// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_flighttask_ready_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsFlighttaskReadySouthbound::EventsFlighttaskReadySouthbound() {
    this->topic_ = "thing/product/gnd/events/flighttask_ready";
    this->method_ = "flighttask_ready";
}

EventsFlighttaskReadySouthbound::EventsFlighttaskReadySouthbound(const EventsFlighttaskReadySouthboundMsg& msg) : EventsFlighttaskReadySouthbound() {
    this->msg = msg;
}

std::string EventsFlighttaskReadySouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsFlighttaskReadySouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsFlighttaskReadySouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsFlighttaskReadySouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}