// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_dongle_power_switch_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesDonglePowerSwitchSouthbound::ServicesDonglePowerSwitchSouthbound() {
    this->topic_ = "thing/product/uav/services/dongle_power_switch";
    this->method_ = "dongle_power_switch";
}

ServicesDonglePowerSwitchSouthbound::ServicesDonglePowerSwitchSouthbound(const ServicesDonglePowerSwitchSouthboundMsg& msg) : ServicesDonglePowerSwitchSouthbound() {
    this->msg = msg;
}

std::string ServicesDonglePowerSwitchSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesDonglePowerSwitchSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesDonglePowerSwitchSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesDonglePowerSwitchSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}