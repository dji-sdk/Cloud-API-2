// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCameraAimSouthboundMsg {
    std::string camera_type;
    bool locked = false;
    std::string payload_index;
    double x = 0.0;
    double y = 0.0;

    AIGC_JSON_HELPER(camera_type, locked, payload_index, x, y)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraAimSouthbound final : public MessageV2 {
public:
    DrcDrcCameraAimSouthbound();
    explicit DrcDrcCameraAimSouthbound(const DrcDrcCameraAimSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraAimSouthboundMsg msg;
};

}