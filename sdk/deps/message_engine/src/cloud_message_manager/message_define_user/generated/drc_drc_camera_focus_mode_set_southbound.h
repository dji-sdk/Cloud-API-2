// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCameraFocusModeSetSouthboundMsg {
    std::string payload_index;
    std::string camera_type;
    int focus_mode = 0;

    AIGC_JSON_HELPER(payload_index, camera_type, focus_mode)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraFocusModeSetSouthbound final : public MessageV2 {
public:
    DrcDrcCameraFocusModeSetSouthbound();
    explicit DrcDrcCameraFocusModeSetSouthbound(const DrcDrcCameraFocusModeSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraFocusModeSetSouthboundMsg msg;
};

}