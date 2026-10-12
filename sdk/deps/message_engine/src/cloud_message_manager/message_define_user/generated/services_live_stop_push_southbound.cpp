// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_live_stop_push_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesLiveStopPushSouthbound::ServicesLiveStopPushSouthbound() {
    this->topic_ = "thing/product/uav/services/live_stop_push";
    this->method_ = "live_stop_push";
}

ServicesLiveStopPushSouthbound::ServicesLiveStopPushSouthbound(const ServicesLiveStopPushSouthboundMsg& msg) : ServicesLiveStopPushSouthbound() {
    this->msg = msg;
}

std::string ServicesLiveStopPushSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesLiveStopPushSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesLiveStopPushSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesLiveStopPushSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}