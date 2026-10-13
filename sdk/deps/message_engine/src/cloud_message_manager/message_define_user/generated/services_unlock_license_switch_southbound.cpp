// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_unlock_license_switch_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesUnlockLicenseSwitchSouthbound::ServicesUnlockLicenseSwitchSouthbound() {
    this->topic_ = "thing/product/gnd/services/unlock_license_switch";
    this->method_ = "unlock_license_switch";
}

ServicesUnlockLicenseSwitchSouthbound::ServicesUnlockLicenseSwitchSouthbound(const ServicesUnlockLicenseSwitchSouthboundMsg& msg) : ServicesUnlockLicenseSwitchSouthbound() {
    this->msg = msg;
}

std::string ServicesUnlockLicenseSwitchSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesUnlockLicenseSwitchSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesUnlockLicenseSwitchSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesUnlockLicenseSwitchSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}