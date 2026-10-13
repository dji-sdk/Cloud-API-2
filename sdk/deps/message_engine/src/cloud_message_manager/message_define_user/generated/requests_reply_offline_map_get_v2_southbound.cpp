// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "requests_reply_offline_map_get_v2_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

RequestsReplyOfflineMapGetV2Southbound::RequestsReplyOfflineMapGetV2Southbound() {
    this->topic_ = "thing/product/gnd/requests_reply/offline_map_get_v2";
    this->method_ = "offline_map_get_v2";
}

RequestsReplyOfflineMapGetV2Southbound::RequestsReplyOfflineMapGetV2Southbound(const RequestsReplyOfflineMapGetV2SouthboundMsg& msg) : RequestsReplyOfflineMapGetV2Southbound() {
    this->msg = msg;
}

std::string RequestsReplyOfflineMapGetV2Southbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsReplyOfflineMapGetV2Southbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer RequestsReplyOfflineMapGetV2Southbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("RequestsReplyOfflineMapGetV2Southbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}