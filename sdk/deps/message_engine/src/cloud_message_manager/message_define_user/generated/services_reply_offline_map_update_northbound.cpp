// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_reply_offline_map_update_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesReplyOfflineMapUpdateNorthbound::ServicesReplyOfflineMapUpdateNorthbound() {
    this->topic_ = "thing/product/gnd/services_reply/offline_map_update";
    this->method_ = "offline_map_update";
}

ServicesReplyOfflineMapUpdateNorthbound::ServicesReplyOfflineMapUpdateNorthbound(const std::shared_ptr<MessageV1>& message) : ServicesReplyOfflineMapUpdateNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyOfflineMapUpdateNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string ServicesReplyOfflineMapUpdateNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyOfflineMapUpdateNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesReplyOfflineMapUpdateNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesReplyOfflineMapUpdateNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}