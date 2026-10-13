// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcIrMeteringPointSetSouthboundMsg {
    std::string payload_index;
    double x = 0.0;
    double y = 0.0;

    AIGC_JSON_HELPER(payload_index, x, y)
};
///------ 用户自定义数据结构 end ------

class DrcDrcIrMeteringPointSetSouthbound final : public MessageV2 {
public:
    DrcDrcIrMeteringPointSetSouthbound();
    explicit DrcDrcIrMeteringPointSetSouthbound(const DrcDrcIrMeteringPointSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcIrMeteringPointSetSouthboundMsg msg;
};

}