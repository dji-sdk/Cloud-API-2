// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcSetIrDisplayModeSouthboundMsg {
    std::string payload_index;
    int display_mode = 0;

    AIGC_JSON_HELPER(payload_index, display_mode)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSetIrDisplayModeSouthbound final : public MessageV2 {
public:
    DrcDrcSetIrDisplayModeSouthbound();
    explicit DrcDrcSetIrDisplayModeSouthbound(const DrcDrcSetIrDisplayModeSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSetIrDisplayModeSouthboundMsg msg;
};

}