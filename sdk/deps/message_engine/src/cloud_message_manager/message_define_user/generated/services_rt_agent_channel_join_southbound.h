// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RtAgentChannelJoinConfig {
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

struct ServicesRtAgentChannelJoinSouthboundMsg {
    std::string provider;
    std::string target_sn;
    bool enabled = false;
    RtAgentChannelJoinConfig config;

    AIGC_JSON_HELPER(provider, target_sn, enabled, config)
};
///------ 用户自定义数据结构 end ------

class ServicesRtAgentChannelJoinSouthbound final : public MessageV1 {
public:
    ServicesRtAgentChannelJoinSouthbound();
    explicit ServicesRtAgentChannelJoinSouthbound(const ServicesRtAgentChannelJoinSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesRtAgentChannelJoinSouthboundMsg msg;
};

}