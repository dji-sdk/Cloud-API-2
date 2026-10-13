// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCountDownActionSetSouthboundMsg {
    int count_down_type = 0;
    int action_type = 0;

    AIGC_JSON_HELPER(count_down_type, action_type)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCountDownActionSetSouthbound final : public MessageV2 {
public:
    DrcDrcCountDownActionSetSouthbound();
    explicit DrcDrcCountDownActionSetSouthbound(const DrcDrcCountDownActionSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCountDownActionSetSouthboundMsg msg;
};

}