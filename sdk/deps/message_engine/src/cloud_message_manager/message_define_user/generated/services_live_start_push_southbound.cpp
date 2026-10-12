// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_live_start_push_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesLiveStartPushSouthbound::ServicesLiveStartPushSouthbound() {
    this->topic_ = "thing/product/uav/services/live_start_push";
    this->method_ = "live_start_push";
}

ServicesLiveStartPushSouthbound::ServicesLiveStartPushSouthbound(const ServicesLiveStartPushSouthboundMsg& msg) : ServicesLiveStartPushSouthbound() {
    this->msg = msg;
}

std::string ServicesLiveStartPushSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesLiveStartPushSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesLiveStartPushSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesLiveStartPushSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}