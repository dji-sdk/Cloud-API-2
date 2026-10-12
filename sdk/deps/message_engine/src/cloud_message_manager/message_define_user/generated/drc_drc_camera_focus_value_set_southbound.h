// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCameraFocusValueSetSouthboundMsg {
    std::string payload_index;
    std::string camera_type;
    int focus_value = 0;

    AIGC_JSON_HELPER(payload_index, camera_type, focus_value)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraFocusValueSetSouthbound final : public MessageV2 {
public:
    DrcDrcCameraFocusValueSetSouthbound();
    explicit DrcDrcCameraFocusValueSetSouthbound(const DrcDrcCameraFocusValueSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraFocusValueSetSouthboundMsg msg;
};

}