// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "requests_reply_storage_config_get_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

RequestsReplyStorageConfigGetSouthbound::RequestsReplyStorageConfigGetSouthbound() {
    this->topic_ = "thing/product/uav/requests_reply/storage_config_get";
    this->method_ = "storage_config_get";
}

RequestsReplyStorageConfigGetSouthbound::RequestsReplyStorageConfigGetSouthbound(const RequestsReplyStorageConfigGetSouthboundMsg& msg) : RequestsReplyStorageConfigGetSouthbound() {
    this->msg = msg;
}

std::string RequestsReplyStorageConfigGetSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsReplyStorageConfigGetSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer RequestsReplyStorageConfigGetSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("RequestsReplyStorageConfigGetSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}