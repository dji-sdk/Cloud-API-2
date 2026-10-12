// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesSetRelayOccupiedStatusSouthboundMsg {
    bool allow_occupied = false;

    AIGC_JSON_HELPER(allow_occupied)
};
///------ 用户自定义数据结构 end ------

class ServicesSetRelayOccupiedStatusSouthbound final : public MessageV1 {
public:
    ServicesSetRelayOccupiedStatusSouthbound();
    explicit ServicesSetRelayOccupiedStatusSouthbound(const ServicesSetRelayOccupiedStatusSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesSetRelayOccupiedStatusSouthboundMsg msg;
};

}