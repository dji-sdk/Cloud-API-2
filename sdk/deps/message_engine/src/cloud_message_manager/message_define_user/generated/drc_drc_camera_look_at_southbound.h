// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCameraLookAtSouthboundMsg {
    std::string payload_index;
    bool locked = false;
    double height = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;

    AIGC_JSON_HELPER(payload_index, locked, height, latitude, longitude)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraLookAtSouthbound final : public MessageV2 {
public:
    DrcDrcCameraLookAtSouthbound();
    explicit DrcDrcCameraLookAtSouthbound(const DrcDrcCameraLookAtSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraLookAtSouthboundMsg msg;
};

}