// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "requests_reply_rt_agent_channel_open_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

RequestsReplyRtAgentChannelOpenSouthbound::RequestsReplyRtAgentChannelOpenSouthbound() {
    this->topic_ = "thing/product/uav/requests_reply/rt_agent_channel_open";
    this->method_ = "rt_agent_channel_open";
}

RequestsReplyRtAgentChannelOpenSouthbound::RequestsReplyRtAgentChannelOpenSouthbound(const RequestsReplyRtAgentChannelOpenSouthboundMsg& msg) : RequestsReplyRtAgentChannelOpenSouthbound() {
    this->msg = msg;
}

std::string RequestsReplyRtAgentChannelOpenSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsReplyRtAgentChannelOpenSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer RequestsReplyRtAgentChannelOpenSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("RequestsReplyRtAgentChannelOpenSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}