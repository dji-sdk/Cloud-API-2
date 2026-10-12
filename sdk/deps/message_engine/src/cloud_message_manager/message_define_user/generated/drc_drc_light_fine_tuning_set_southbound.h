// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcLightFineTuningSetSouthboundMsg {
    int psdk_index = 0;
    int position = 0;
    double value = 0.0;
    bool saved = false;

    AIGC_JSON_HELPER(psdk_index, position, value, saved)
};
///------ 用户自定义数据结构 end ------

class DrcDrcLightFineTuningSetSouthbound final : public MessageV2 {
public:
    DrcDrcLightFineTuningSetSouthbound();
    explicit DrcDrcLightFineTuningSetSouthbound(const DrcDrcLightFineTuningSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcLightFineTuningSetSouthboundMsg msg;
};

}