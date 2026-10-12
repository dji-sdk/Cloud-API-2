// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcIrMeteringModeSetSouthboundMsg {
    std::string payload_index;
    int mode = 0;

    AIGC_JSON_HELPER(payload_index, mode)
};
///------ 用户自定义数据结构 end ------

class DrcDrcIrMeteringModeSetSouthbound final : public MessageV2 {
public:
    DrcDrcIrMeteringModeSetSouthbound();
    explicit DrcDrcIrMeteringModeSetSouthbound(const DrcDrcIrMeteringModeSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcIrMeteringModeSetSouthboundMsg msg;
};

}