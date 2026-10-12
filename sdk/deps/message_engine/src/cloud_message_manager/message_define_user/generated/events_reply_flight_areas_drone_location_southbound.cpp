// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_flight_areas_drone_location_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyFlightAreasDroneLocationSouthbound::EventsReplyFlightAreasDroneLocationSouthbound() {
    this->topic_ = "thing/product/uav/events_reply/flight_areas_drone_location";
    this->method_ = "flight_areas_drone_location";
}

EventsReplyFlightAreasDroneLocationSouthbound::EventsReplyFlightAreasDroneLocationSouthbound(const EventsReplyFlightAreasDroneLocationSouthboundMsg& msg) : EventsReplyFlightAreasDroneLocationSouthbound() {
    this->msg = msg;
}

std::string EventsReplyFlightAreasDroneLocationSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyFlightAreasDroneLocationSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyFlightAreasDroneLocationSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyFlightAreasDroneLocationSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}