// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "requests_offline_map_get_v2_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

RequestsOfflineMapGetV2Northbound::RequestsOfflineMapGetV2Northbound() {
    this->topic_ = "thing/product/gnd/requests/offline_map_get_v2";
    this->method_ = "offline_map_get_v2";
}

RequestsOfflineMapGetV2Northbound::RequestsOfflineMapGetV2Northbound(const std::shared_ptr<MessageV1>& message) : RequestsOfflineMapGetV2Northbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsOfflineMapGetV2Northbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string RequestsOfflineMapGetV2Northbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsOfflineMapGetV2Northbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer RequestsOfflineMapGetV2Northbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("RequestsOfflineMapGetV2Northbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}