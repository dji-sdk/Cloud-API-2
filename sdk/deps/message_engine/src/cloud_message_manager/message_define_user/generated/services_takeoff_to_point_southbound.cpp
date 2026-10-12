// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_takeoff_to_point_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesTakeoffToPointSouthbound::ServicesTakeoffToPointSouthbound() {
    this->topic_ = "thing/product/gnd/services/takeoff_to_point";
    this->method_ = "takeoff_to_point";
}

ServicesTakeoffToPointSouthbound::ServicesTakeoffToPointSouthbound(const ServicesTakeoffToPointSouthboundMsg& msg) : ServicesTakeoffToPointSouthbound() {
    this->msg = msg;
}

std::string ServicesTakeoffToPointSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesTakeoffToPointSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesTakeoffToPointSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesTakeoffToPointSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}