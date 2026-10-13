// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_reply_upload_flighttask_media_prioritize_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound::ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound() {
    this->topic_ = "thing/product/gnd/services_reply/upload_flighttask_media_prioritize";
    this->method_ = "upload_flighttask_media_prioritize";
}

ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound::ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound(const std::shared_ptr<MessageV1>& message) : ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesReplyUploadFlighttaskMediaPrioritizeNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}