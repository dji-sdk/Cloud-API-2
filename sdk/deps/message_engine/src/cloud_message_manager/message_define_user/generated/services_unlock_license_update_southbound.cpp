// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_unlock_license_update_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesUnlockLicenseUpdateSouthbound::ServicesUnlockLicenseUpdateSouthbound() {
    this->topic_ = "thing/product/gnd/services/unlock_license_update";
    this->method_ = "unlock_license_update";
}

ServicesUnlockLicenseUpdateSouthbound::ServicesUnlockLicenseUpdateSouthbound(const ServicesUnlockLicenseUpdateSouthboundMsg& msg) : ServicesUnlockLicenseUpdateSouthbound() {
    this->msg = msg;
}

std::string ServicesUnlockLicenseUpdateSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesUnlockLicenseUpdateSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesUnlockLicenseUpdateSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesUnlockLicenseUpdateSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}