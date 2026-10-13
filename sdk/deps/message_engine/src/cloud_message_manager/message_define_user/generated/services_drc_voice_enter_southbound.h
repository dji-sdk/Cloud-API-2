// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcVoiceEnterConfig {
    std::string app_id;
    std::string channel_name;
    std::string room_id;
    std::string user_id;
    std::string token;
    int expire_time = 0;

    AIGC_JSON_HELPER(app_id, channel_name, room_id, user_id, token, expire_time)
};

struct ServicesDrcVoiceEnterSouthboundMsg {
    std::string provider;
    DrcVoiceEnterConfig config;

    AIGC_JSON_HELPER(provider, config)
};
///------ 用户自定义数据结构 end ------

class ServicesDrcVoiceEnterSouthbound final : public MessageV1 {
public:
    ServicesDrcVoiceEnterSouthbound();
    explicit ServicesDrcVoiceEnterSouthbound(const ServicesDrcVoiceEnterSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesDrcVoiceEnterSouthboundMsg msg;
};

}