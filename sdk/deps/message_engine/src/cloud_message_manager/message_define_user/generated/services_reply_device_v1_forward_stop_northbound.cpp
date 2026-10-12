// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_reply_device_v1_forward_stop_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesReplyDeviceV1ForwardStopNorthbound::ServicesReplyDeviceV1ForwardStopNorthbound() {
    this->topic_ = "thing/product/uav/services_reply/device_v1_forward_stop";
    this->method_ = "device_v1_forward_stop";
}

ServicesReplyDeviceV1ForwardStopNorthbound::ServicesReplyDeviceV1ForwardStopNorthbound(const std::shared_ptr<MessageV1>& message) : ServicesReplyDeviceV1ForwardStopNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyDeviceV1ForwardStopNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string ServicesReplyDeviceV1ForwardStopNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyDeviceV1ForwardStopNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesReplyDeviceV1ForwardStopNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesReplyDeviceV1ForwardStopNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}