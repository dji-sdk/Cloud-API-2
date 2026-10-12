// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "requests_rt_agent_channel_status_get_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

RequestsRtAgentChannelStatusGetNorthbound::RequestsRtAgentChannelStatusGetNorthbound() {
    this->topic_ = "thing/product/gnd/requests/rt_agent_channel_status_get";
    this->method_ = "rt_agent_channel_status_get";
}

RequestsRtAgentChannelStatusGetNorthbound::RequestsRtAgentChannelStatusGetNorthbound(const std::shared_ptr<MessageV1>& message) : RequestsRtAgentChannelStatusGetNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsRtAgentChannelStatusGetNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string RequestsRtAgentChannelStatusGetNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsRtAgentChannelStatusGetNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer RequestsRtAgentChannelStatusGetNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("RequestsRtAgentChannelStatusGetNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}