// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_thumbnail_upload_finish_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyThumbnailUploadFinishNorthbound::EventsReplyThumbnailUploadFinishNorthbound() {
    this->topic_ = "thing/product/gnd/events_reply/thumbnail_upload_finish";
    this->method_ = "thumbnail_upload_finish";
}

EventsReplyThumbnailUploadFinishNorthbound::EventsReplyThumbnailUploadFinishNorthbound(const std::shared_ptr<MessageV1>& message) : EventsReplyThumbnailUploadFinishNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyThumbnailUploadFinishNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string EventsReplyThumbnailUploadFinishNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyThumbnailUploadFinishNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyThumbnailUploadFinishNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyThumbnailUploadFinishNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}