// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCameraSettingsResetSouthboundMsg {
    std::string payload_index;

    AIGC_JSON_HELPER(payload_index)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraSettingsResetSouthbound final : public MessageV2 {
public:
    DrcDrcCameraSettingsResetSouthbound();
    explicit DrcDrcCameraSettingsResetSouthbound(const DrcDrcCameraSettingsResetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraSettingsResetSouthboundMsg msg;
};

}