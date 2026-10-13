// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcAiIdentifyScoreSetSouthboundMsg {
    int index = 0;
    int light_type = 0;
    int score = 0;

    AIGC_JSON_HELPER(index, light_type, score)
};
///------ 用户自定义数据结构 end ------

class DrcDrcAiIdentifyScoreSetSouthbound final : public MessageV2 {
public:
    DrcDrcAiIdentifyScoreSetSouthbound();
    explicit DrcDrcAiIdentifyScoreSetSouthbound(const DrcDrcAiIdentifyScoreSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcAiIdentifyScoreSetSouthboundMsg msg;
};

}