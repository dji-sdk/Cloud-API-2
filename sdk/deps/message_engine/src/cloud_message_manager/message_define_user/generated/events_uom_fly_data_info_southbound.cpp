// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_uom_fly_data_info_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsUomFlyDataInfoSouthbound::EventsUomFlyDataInfoSouthbound() {
    this->topic_ = "thing/product/gnd/events/uom_fly_data_info";
    this->method_ = "uom_fly_data_info";
}

EventsUomFlyDataInfoSouthbound::EventsUomFlyDataInfoSouthbound(const EventsUomFlyDataInfoSouthboundMsg& msg) : EventsUomFlyDataInfoSouthbound() {
    this->msg = msg;
}

std::string EventsUomFlyDataInfoSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsUomFlyDataInfoSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsUomFlyDataInfoSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsUomFlyDataInfoSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}