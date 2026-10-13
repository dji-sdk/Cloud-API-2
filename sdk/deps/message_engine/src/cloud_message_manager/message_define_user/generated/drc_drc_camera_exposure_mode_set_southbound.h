// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCameraExposureModeSetSouthboundMsg {
    std::string payload_index;
    std::string camera_type;
    int exposure_mode = 0;

    AIGC_JSON_HELPER(payload_index, camera_type, exposure_mode)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraExposureModeSetSouthbound final : public MessageV2 {
public:
    DrcDrcCameraExposureModeSetSouthbound();
    explicit DrcDrcCameraExposureModeSetSouthbound(const DrcDrcCameraExposureModeSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraExposureModeSetSouthboundMsg msg;
};

}