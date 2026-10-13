// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_reply_drc_camera_frame_zoom_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcReplyDrcCameraFrameZoomNorthbound::DrcReplyDrcCameraFrameZoomNorthbound() {
    this->method_ = "drc_camera_frame_zoom";
}

DrcReplyDrcCameraFrameZoomNorthbound::DrcReplyDrcCameraFrameZoomNorthbound(const std::shared_ptr<MessageV2>& message) : DrcReplyDrcCameraFrameZoomNorthbound() {
    this->seq_ = message->GetSeq();
    this->peer_id_ = message->GetPeerId();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcReplyDrcCameraFrameZoomNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string DrcReplyDrcCameraFrameZoomNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcReplyDrcCameraFrameZoomNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcReplyDrcCameraFrameZoomNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcReplyDrcCameraFrameZoomNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}