// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcThermalGainModeSetSouthboundMsg {
    std::string payload_index;
    int thermal_gain_mode = 0;

    AIGC_JSON_HELPER(payload_index, thermal_gain_mode)
};
///------ 用户自定义数据结构 end ------

class DrcDrcThermalGainModeSetSouthbound final : public MessageV2 {
public:
    DrcDrcThermalGainModeSetSouthbound();
    explicit DrcDrcThermalGainModeSetSouthbound(const DrcDrcThermalGainModeSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcThermalGainModeSetSouthboundMsg msg;
};

}