// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RtAgentChannelOpenConfig {
    std::string app_id;
    std::string business_id;
    std::string channel_name;
    std::string room_id;
    std::string user_id;
    std::string client_id;
    std::string token;
    int expire_time = 0;

    AIGC_JSON_HELPER(app_id, business_id, channel_name, room_id, user_id, client_id, token, expire_time)
};

struct RtAgentChannelOpenOutput {
    RtAgentChannelOpenConfig config;
    std::string provider;

    AIGC_JSON_HELPER(config, provider)
};

struct RequestsReplyRtAgentChannelOpenSouthboundMsg {
    int result = 0;
    RtAgentChannelOpenOutput output;

    AIGC_JSON_HELPER(result, output)
};
///------ 用户自定义数据结构 end ------

class RequestsReplyRtAgentChannelOpenSouthbound final : public MessageV1 {
public:
    RequestsReplyRtAgentChannelOpenSouthbound();
    explicit RequestsReplyRtAgentChannelOpenSouthbound(const RequestsReplyRtAgentChannelOpenSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    RequestsReplyRtAgentChannelOpenSouthboundMsg msg;
};

}