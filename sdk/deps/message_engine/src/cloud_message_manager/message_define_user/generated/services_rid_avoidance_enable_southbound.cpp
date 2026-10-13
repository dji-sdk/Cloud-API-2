// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_rid_avoidance_enable_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesRidAvoidanceEnableSouthbound::ServicesRidAvoidanceEnableSouthbound() {
    this->topic_ = "thing/product/uav/services/rid_avoidance_enable";
    this->method_ = "rid_avoidance_enable";
}

ServicesRidAvoidanceEnableSouthbound::ServicesRidAvoidanceEnableSouthbound(const ServicesRidAvoidanceEnableSouthboundMsg& msg) : ServicesRidAvoidanceEnableSouthbound() {
    this->msg = msg;
}

std::string ServicesRidAvoidanceEnableSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesRidAvoidanceEnableSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesRidAvoidanceEnableSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesRidAvoidanceEnableSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}