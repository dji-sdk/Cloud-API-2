// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_device_v1_forward_start_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesDeviceV1ForwardStartSouthbound::ServicesDeviceV1ForwardStartSouthbound() {
    this->topic_ = "thing/product/uav/services/device_v1_forward_start";
    this->method_ = "device_v1_forward_start";
}

ServicesDeviceV1ForwardStartSouthbound::ServicesDeviceV1ForwardStartSouthbound(const ServicesDeviceV1ForwardStartSouthboundMsg& msg) : ServicesDeviceV1ForwardStartSouthbound() {
    this->msg = msg;
}

std::string ServicesDeviceV1ForwardStartSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesDeviceV1ForwardStartSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesDeviceV1ForwardStartSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesDeviceV1ForwardStartSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}