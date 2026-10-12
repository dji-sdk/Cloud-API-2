// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_flight_areas_update_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesFlightAreasUpdateSouthbound::ServicesFlightAreasUpdateSouthbound() {
    this->topic_ = "thing/product/gnd/services/flight_areas_update";
    this->method_ = "flight_areas_update";
}

ServicesFlightAreasUpdateSouthbound::ServicesFlightAreasUpdateSouthbound(const ServicesFlightAreasUpdateSouthboundMsg& msg) : ServicesFlightAreasUpdateSouthbound() {
    this->msg = msg;
}

std::string ServicesFlightAreasUpdateSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesFlightAreasUpdateSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesFlightAreasUpdateSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesFlightAreasUpdateSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}