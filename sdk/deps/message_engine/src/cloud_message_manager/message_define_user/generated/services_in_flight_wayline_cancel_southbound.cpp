// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_in_flight_wayline_cancel_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesInFlightWaylineCancelSouthbound::ServicesInFlightWaylineCancelSouthbound() {
    this->topic_ = "thing/product/uav/services/in_flight_wayline_cancel";
    this->method_ = "in_flight_wayline_cancel";
}

ServicesInFlightWaylineCancelSouthbound::ServicesInFlightWaylineCancelSouthbound(const ServicesInFlightWaylineCancelSouthboundMsg& msg) : ServicesInFlightWaylineCancelSouthbound() {
    this->msg = msg;
}

std::string ServicesInFlightWaylineCancelSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesInFlightWaylineCancelSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesInFlightWaylineCancelSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesInFlightWaylineCancelSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}