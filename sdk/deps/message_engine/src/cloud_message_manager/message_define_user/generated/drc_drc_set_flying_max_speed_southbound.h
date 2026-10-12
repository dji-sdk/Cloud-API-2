// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcSetFlyingMaxSpeedSouthboundMsg {
    double speed = 0.0;

    AIGC_JSON_HELPER(speed)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSetFlyingMaxSpeedSouthbound final : public MessageV2 {
public:
    DrcDrcSetFlyingMaxSpeedSouthbound();
    explicit DrcDrcSetFlyingMaxSpeedSouthbound(const DrcDrcSetFlyingMaxSpeedSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSetFlyingMaxSpeedSouthboundMsg msg;
};

}