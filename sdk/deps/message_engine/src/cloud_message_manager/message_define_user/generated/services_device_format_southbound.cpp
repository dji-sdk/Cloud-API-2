// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_device_format_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesDeviceFormatSouthbound::ServicesDeviceFormatSouthbound() {
    this->topic_ = "thing/product/gnd/services/device_format";
    this->method_ = "device_format";
}

ServicesDeviceFormatSouthbound::ServicesDeviceFormatSouthbound(const ServicesDeviceFormatSouthboundMsg& msg) : ServicesDeviceFormatSouthbound() {
    this->msg = msg;
}

std::string ServicesDeviceFormatSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesDeviceFormatSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesDeviceFormatSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesDeviceFormatSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}