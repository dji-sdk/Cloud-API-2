// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_flighttask_recovery_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesFlighttaskRecoverySouthbound::ServicesFlighttaskRecoverySouthbound() {
    this->topic_ = "thing/product/uav/services/flighttask_recovery";
    this->method_ = "flighttask_recovery";
}

ServicesFlighttaskRecoverySouthbound::ServicesFlighttaskRecoverySouthbound(const ServicesFlighttaskRecoverySouthboundMsg& msg) : ServicesFlighttaskRecoverySouthbound() {
    this->msg = msg;
}

std::string ServicesFlighttaskRecoverySouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesFlighttaskRecoverySouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesFlighttaskRecoverySouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesFlighttaskRecoverySouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}