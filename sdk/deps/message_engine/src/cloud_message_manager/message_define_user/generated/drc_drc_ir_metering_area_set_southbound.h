// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcIrMeteringAreaSetSouthboundMsg {
    std::string payload_index;
    double x = 0.0;
    double y = 0.0;
    double width = 0.0;
    double height = 0.0;

    AIGC_JSON_HELPER(payload_index, x, y, width, height)
};
///------ 用户自定义数据结构 end ------

class DrcDrcIrMeteringAreaSetSouthbound final : public MessageV2 {
public:
    DrcDrcIrMeteringAreaSetSouthbound();
    explicit DrcDrcIrMeteringAreaSetSouthbound(const DrcDrcIrMeteringAreaSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcIrMeteringAreaSetSouthboundMsg msg;
};

}