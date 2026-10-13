// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_camera_frame_zoom_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcCameraFrameZoomSouthbound::DrcDrcCameraFrameZoomSouthbound() {
    this->method_ = "drc_camera_frame_zoom";
}

DrcDrcCameraFrameZoomSouthbound::DrcDrcCameraFrameZoomSouthbound(const DrcDrcCameraFrameZoomSouthboundMsg& msg) : DrcDrcCameraFrameZoomSouthbound() {
    this->msg = msg;
}

std::string DrcDrcCameraFrameZoomSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcCameraFrameZoomSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcCameraFrameZoomSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcCameraFrameZoomSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}