// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_camera_focal_length_set_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesCameraFocalLengthSetSouthbound::ServicesCameraFocalLengthSetSouthbound() {
    this->topic_ = "thing/product/uav/services/camera_focal_length_set";
    this->method_ = "camera_focal_length_set";
}

ServicesCameraFocalLengthSetSouthbound::ServicesCameraFocalLengthSetSouthbound(const ServicesCameraFocalLengthSetSouthboundMsg& msg) : ServicesCameraFocalLengthSetSouthbound() {
    this->msg = msg;
}

std::string ServicesCameraFocalLengthSetSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesCameraFocalLengthSetSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesCameraFocalLengthSetSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesCameraFocalLengthSetSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}