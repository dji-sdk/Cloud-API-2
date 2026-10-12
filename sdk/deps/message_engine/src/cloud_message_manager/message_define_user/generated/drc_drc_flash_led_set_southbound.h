// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct PatternInfo {
    int color = 0;
    int period = 0;
    int brightness = 0;

    AIGC_JSON_HELPER(color, period, brightness)
};

struct DrcDrcFlashLedSetSouthboundMsg {
    int on = 0;
    std::string pattern_uuid;
    std::string pattern_name;
    std::vector<PatternInfo> patterns;

    AIGC_JSON_HELPER(on, pattern_uuid, pattern_name, patterns)
};
///------ 用户自定义数据结构 end ------

class DrcDrcFlashLedSetSouthbound final : public MessageV2 {
public:
    DrcDrcFlashLedSetSouthbound();
    explicit DrcDrcFlashLedSetSouthbound(const DrcDrcFlashLedSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcFlashLedSetSouthboundMsg msg;
};

}