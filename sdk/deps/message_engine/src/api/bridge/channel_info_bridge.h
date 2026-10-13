// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"

namespace message_engine {

struct ChannelInfoBridge {
    ChannelProviderType provider_type;
    std::string app_id;
    std::string channel_name;  // Volc/Agora:channel_name_ 与 room_id_相同; Drtc: channel_name_ 独立
    std::string room_id;
    std::string user_id;       // For Volc/Agora
    std::string client_id;     // Only for Drtc
    std::string log_path;      // For Volc/Agora

    std::string payload_index;  // live channel 特有, 用于区分不同 payload
    std::string country_code;   // agora 特有, 用于区分不同 国家码

    std::string url;
    std::string token;         // 认证 token，Join 时必需
};

}