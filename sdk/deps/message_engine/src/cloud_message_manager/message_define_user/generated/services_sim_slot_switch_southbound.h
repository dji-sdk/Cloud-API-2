// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesSimSlotSwitchSouthboundMsg {
    std::string imei;
    int sim_slot = 0;

    AIGC_JSON_HELPER(imei, sim_slot)
};
///------ 用户自定义数据结构 end ------

class ServicesSimSlotSwitchSouthbound final : public MessageV1 {
public:
    ServicesSimSlotSwitchSouthbound();
    explicit ServicesSimSlotSwitchSouthbound(const ServicesSimSlotSwitchSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesSimSlotSwitchSouthboundMsg msg;
};

}