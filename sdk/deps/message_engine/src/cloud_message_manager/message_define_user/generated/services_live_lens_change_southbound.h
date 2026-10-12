// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesLiveLensChangeSouthboundMsg {
    std::string payload_index;
    std::string video_type;
    std::string video_id;

    AIGC_JSON_HELPER(payload_index, video_type, video_id)
};
///------ 用户自定义数据结构 end ------

class ServicesLiveLensChangeSouthbound final : public MessageV1 {
public:
    ServicesLiveLensChangeSouthbound();
    explicit ServicesLiveLensChangeSouthbound(const ServicesLiveLensChangeSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesLiveLensChangeSouthboundMsg msg;
};

}