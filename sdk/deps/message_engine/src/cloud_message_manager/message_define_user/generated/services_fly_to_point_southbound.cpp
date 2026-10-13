// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_fly_to_point_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesFlyToPointSouthbound::ServicesFlyToPointSouthbound() {
    this->topic_ = "thing/product/uav/services/fly_to_point";
    this->method_ = "fly_to_point";
}

ServicesFlyToPointSouthbound::ServicesFlyToPointSouthbound(const ServicesFlyToPointSouthboundMsg& msg) : ServicesFlyToPointSouthbound() {
    this->msg = msg;
}

std::string ServicesFlyToPointSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesFlyToPointSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesFlyToPointSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesFlyToPointSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}