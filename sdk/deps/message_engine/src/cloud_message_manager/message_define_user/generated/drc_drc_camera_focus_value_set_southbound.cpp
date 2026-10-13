// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_camera_focus_value_set_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcCameraFocusValueSetSouthbound::DrcDrcCameraFocusValueSetSouthbound() {
    this->method_ = "drc_camera_focus_value_set";
}

DrcDrcCameraFocusValueSetSouthbound::DrcDrcCameraFocusValueSetSouthbound(const DrcDrcCameraFocusValueSetSouthboundMsg& msg) : DrcDrcCameraFocusValueSetSouthbound() {
    this->msg = msg;
}

std::string DrcDrcCameraFocusValueSetSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcCameraFocusValueSetSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcCameraFocusValueSetSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcCameraFocusValueSetSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}