// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_flighttask_execute_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesFlighttaskExecuteSouthbound::ServicesFlighttaskExecuteSouthbound() {
    this->topic_ = "thing/product/gnd/services/flighttask_execute";
    this->method_ = "flighttask_execute";
}

ServicesFlighttaskExecuteSouthbound::ServicesFlighttaskExecuteSouthbound(const ServicesFlighttaskExecuteSouthboundMsg& msg) : ServicesFlighttaskExecuteSouthbound() {
    this->msg = msg;
}

std::string ServicesFlighttaskExecuteSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesFlighttaskExecuteSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesFlighttaskExecuteSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesFlighttaskExecuteSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}