// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcSetIsothermEnableSouthboundMsg {
    std::string payload_index;
    int enable = 0;

    AIGC_JSON_HELPER(payload_index, enable)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSetIsothermEnableSouthbound final : public MessageV2 {
public:
    DrcDrcSetIsothermEnableSouthbound();
    explicit DrcDrcSetIsothermEnableSouthbound(const DrcDrcSetIsothermEnableSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSetIsothermEnableSouthboundMsg msg;
};

}