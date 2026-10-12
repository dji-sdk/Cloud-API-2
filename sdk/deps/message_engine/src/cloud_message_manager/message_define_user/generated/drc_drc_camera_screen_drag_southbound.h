// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCameraScreenDragSouthboundMsg {
    bool locked = false;
    std::string payload_index;
    double pitch_speed = 0.0;
    double yaw_speed = 0.0;

    AIGC_JSON_HELPER(locked, payload_index, pitch_speed, yaw_speed)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraScreenDragSouthbound final : public MessageV2 {
public:
    DrcDrcCameraScreenDragSouthbound();
    explicit DrcDrcCameraScreenDragSouthbound(const DrcDrcCameraScreenDragSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraScreenDragSouthboundMsg msg;
};

}