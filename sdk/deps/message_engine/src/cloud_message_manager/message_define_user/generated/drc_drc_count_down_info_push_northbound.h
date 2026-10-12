// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCountDownInfoPushNorthboundMsg {
    int count_down_type = 0;
    int count_down_time = 0;
    bool can_cancel = false;

    AIGC_JSON_HELPER(count_down_type, count_down_time, can_cancel)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCountDownInfoPushNorthbound final : public MessageV2 {
public:
    DrcDrcCountDownInfoPushNorthbound();
    explicit DrcDrcCountDownInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCountDownInfoPushNorthboundMsg msg;
};

}