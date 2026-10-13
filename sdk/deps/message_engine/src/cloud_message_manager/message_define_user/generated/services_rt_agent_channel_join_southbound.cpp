// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_rt_agent_channel_join_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesRtAgentChannelJoinSouthbound::ServicesRtAgentChannelJoinSouthbound() {
    this->topic_ = "thing/product/gnd/services/rt_agent_channel_join";
    this->method_ = "rt_agent_channel_join";
}

ServicesRtAgentChannelJoinSouthbound::ServicesRtAgentChannelJoinSouthbound(const ServicesRtAgentChannelJoinSouthboundMsg& msg) : ServicesRtAgentChannelJoinSouthbound() {
    this->msg = msg;
}

std::string ServicesRtAgentChannelJoinSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesRtAgentChannelJoinSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesRtAgentChannelJoinSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesRtAgentChannelJoinSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}