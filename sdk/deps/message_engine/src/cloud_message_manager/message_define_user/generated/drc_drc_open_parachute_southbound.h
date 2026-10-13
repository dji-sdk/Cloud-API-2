// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcOpenParachuteSouthboundMsg {
    int force = 0;

    AIGC_JSON_HELPER(force)
};
///------ 用户自定义数据结构 end ------

class DrcDrcOpenParachuteSouthbound final : public MessageV2 {
public:
    DrcDrcOpenParachuteSouthbound();
    explicit DrcDrcOpenParachuteSouthbound(const DrcDrcOpenParachuteSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcOpenParachuteSouthboundMsg msg;
};

}