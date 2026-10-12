// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcStickControlSouthboundMsg {
    int roll = 0;
    int pitch = 0;
    int throttle = 0;
    int yaw = 0;
    int gimbal_pitch = 0;

    AIGC_JSON_HELPER(roll, pitch, throttle, yaw, gimbal_pitch)
};
///------ 用户自定义数据结构 end ------

class DrcStickControlSouthbound final : public MessageV2 {
public:
    DrcStickControlSouthbound();
    explicit DrcStickControlSouthbound(const DrcStickControlSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcStickControlSouthboundMsg msg;
};

}