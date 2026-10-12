// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_rtk_calibration_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsRtkCalibrationSouthbound::EventsRtkCalibrationSouthbound() {
    this->topic_ = "thing/product/gnd/events/rtk_calibration";
    this->method_ = "rtk_calibration";
}

EventsRtkCalibrationSouthbound::EventsRtkCalibrationSouthbound(const EventsRtkCalibrationSouthboundMsg& msg) : EventsRtkCalibrationSouthbound() {
    this->msg = msg;
}

std::string EventsRtkCalibrationSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsRtkCalibrationSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsRtkCalibrationSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsRtkCalibrationSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}