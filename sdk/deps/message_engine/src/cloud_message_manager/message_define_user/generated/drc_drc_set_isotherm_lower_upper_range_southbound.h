// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcSetIsothermLowerUpperRangeSouthboundMsg {
    std::string payload_index;
    int lower = 0;
    int upper = 0;

    AIGC_JSON_HELPER(payload_index, lower, upper)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSetIsothermLowerUpperRangeSouthbound final : public MessageV2 {
public:
    DrcDrcSetIsothermLowerUpperRangeSouthbound();
    explicit DrcDrcSetIsothermLowerUpperRangeSouthbound(const DrcDrcSetIsothermLowerUpperRangeSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSetIsothermLowerUpperRangeSouthboundMsg msg;
};

}