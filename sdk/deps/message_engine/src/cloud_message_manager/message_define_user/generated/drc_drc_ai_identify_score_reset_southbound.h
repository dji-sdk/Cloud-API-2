// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcAiIdentifyScoreResetSouthboundMsg {
    int index = 0;
    int light_type = 0;

    AIGC_JSON_HELPER(index, light_type)
};
///------ 用户自定义数据结构 end ------

class DrcDrcAiIdentifyScoreResetSouthbound final : public MessageV2 {
public:
    DrcDrcAiIdentifyScoreResetSouthbound();
    explicit DrcDrcAiIdentifyScoreResetSouthbound(const DrcDrcAiIdentifyScoreResetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcAiIdentifyScoreResetSouthboundMsg msg;
};

}