// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_aircraft_power_ctrl_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesAircraftPowerCtrlSouthbound::ServicesAircraftPowerCtrlSouthbound() {
    this->topic_ = "thing/product/uav/services/aircraft_power_ctrl";
    this->method_ = "aircraft_power_ctrl";
}

ServicesAircraftPowerCtrlSouthbound::ServicesAircraftPowerCtrlSouthbound(const ServicesAircraftPowerCtrlSouthboundMsg& msg) : ServicesAircraftPowerCtrlSouthbound() {
    this->msg = msg;
}

std::string ServicesAircraftPowerCtrlSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesAircraftPowerCtrlSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesAircraftPowerCtrlSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesAircraftPowerCtrlSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}