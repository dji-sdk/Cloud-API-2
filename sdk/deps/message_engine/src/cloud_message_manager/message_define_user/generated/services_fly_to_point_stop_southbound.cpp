// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_fly_to_point_stop_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesFlyToPointStopSouthbound::ServicesFlyToPointStopSouthbound() {
    this->topic_ = "thing/product/uav/services/fly_to_point_stop";
    this->method_ = "fly_to_point_stop";
}

ServicesFlyToPointStopSouthbound::ServicesFlyToPointStopSouthbound(const ServicesFlyToPointStopSouthboundMsg& msg) : ServicesFlyToPointStopSouthbound() {
    this->msg = msg;
}

std::string ServicesFlyToPointStopSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesFlyToPointStopSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesFlyToPointStopSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesFlyToPointStopSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}