// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_cloud_environment_switch_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesCloudEnvironmentSwitchSouthbound::ServicesCloudEnvironmentSwitchSouthbound() {
    this->topic_ = "thing/product/gnd/services/cloud_environment_switch";
    this->method_ = "cloud_environment_switch";
}

ServicesCloudEnvironmentSwitchSouthbound::ServicesCloudEnvironmentSwitchSouthbound(const ServicesCloudEnvironmentSwitchSouthboundMsg& msg) : ServicesCloudEnvironmentSwitchSouthbound() {
    this->msg = msg;
}

std::string ServicesCloudEnvironmentSwitchSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesCloudEnvironmentSwitchSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesCloudEnvironmentSwitchSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesCloudEnvironmentSwitchSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}