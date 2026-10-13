// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_set_relay_occupied_status_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesSetRelayOccupiedStatusSouthbound::ServicesSetRelayOccupiedStatusSouthbound() {
    this->topic_ = "thing/product/gnd/services/set_relay_occupied_status";
    this->method_ = "set_relay_occupied_status";
}

ServicesSetRelayOccupiedStatusSouthbound::ServicesSetRelayOccupiedStatusSouthbound(const ServicesSetRelayOccupiedStatusSouthboundMsg& msg) : ServicesSetRelayOccupiedStatusSouthbound() {
    this->msg = msg;
}

std::string ServicesSetRelayOccupiedStatusSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesSetRelayOccupiedStatusSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesSetRelayOccupiedStatusSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesSetRelayOccupiedStatusSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}