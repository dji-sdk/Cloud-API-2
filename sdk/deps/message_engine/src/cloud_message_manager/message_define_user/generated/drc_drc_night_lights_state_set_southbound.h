// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcNightLightsStateSetSouthboundMsg {
    int night_lights_state = 0;

    AIGC_JSON_HELPER(night_lights_state)
};
///------ 用户自定义数据结构 end ------

class DrcDrcNightLightsStateSetSouthbound final : public MessageV2 {
public:
    DrcDrcNightLightsStateSetSouthbound();
    explicit DrcDrcNightLightsStateSetSouthbound(const DrcDrcNightLightsStateSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcNightLightsStateSetSouthboundMsg msg;
};

}