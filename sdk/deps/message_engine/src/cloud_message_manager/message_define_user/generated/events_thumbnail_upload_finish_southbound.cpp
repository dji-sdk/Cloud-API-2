// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_thumbnail_upload_finish_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsThumbnailUploadFinishSouthbound::EventsThumbnailUploadFinishSouthbound() {
    this->topic_ = "thing/product/gnd/events/thumbnail_upload_finish";
    this->method_ = "thumbnail_upload_finish";
}

EventsThumbnailUploadFinishSouthbound::EventsThumbnailUploadFinishSouthbound(const EventsThumbnailUploadFinishSouthboundMsg& msg) : EventsThumbnailUploadFinishSouthbound() {
    this->msg = msg;
}

std::string EventsThumbnailUploadFinishSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsThumbnailUploadFinishSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsThumbnailUploadFinishSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsThumbnailUploadFinishSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}