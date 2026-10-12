// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcAiIdentifyScoreModeSetSouthboundMsg {
    int index = 0;
    int light_type = 0;
    int score_mode = 0;

    AIGC_JSON_HELPER(index, light_type, score_mode)
};
///------ 用户自定义数据结构 end ------

class DrcDrcAiIdentifyScoreModeSetSouthbound final : public MessageV2 {
public:
    DrcDrcAiIdentifyScoreModeSetSouthbound();
    explicit DrcDrcAiIdentifyScoreModeSetSouthbound(const DrcDrcAiIdentifyScoreModeSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcAiIdentifyScoreModeSetSouthboundMsg msg;
};

}