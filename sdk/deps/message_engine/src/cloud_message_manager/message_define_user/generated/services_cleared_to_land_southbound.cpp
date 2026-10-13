// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_cleared_to_land_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesClearedToLandSouthbound::ServicesClearedToLandSouthbound() {
    this->topic_ = "thing/product/gnd/services/cleared_to_land";
    this->method_ = "cleared_to_land";
}

ServicesClearedToLandSouthbound::ServicesClearedToLandSouthbound(const ServicesClearedToLandSouthboundMsg& msg) : ServicesClearedToLandSouthbound() {
    this->msg = msg;
}

std::string ServicesClearedToLandSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesClearedToLandSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesClearedToLandSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesClearedToLandSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}