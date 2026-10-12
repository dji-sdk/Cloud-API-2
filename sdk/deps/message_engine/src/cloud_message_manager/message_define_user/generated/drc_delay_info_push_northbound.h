// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDelayInfoPushNorthboundMsg {
    int stick_delay = 0;
    int sdr_cmd_delay = 0;

    AIGC_JSON_HELPER(stick_delay, sdr_cmd_delay)
};
///------ 用户自定义数据结构 end ------

class DrcDelayInfoPushNorthbound final : public MessageV2 {
public:
    DrcDelayInfoPushNorthbound();
    explicit DrcDelayInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDelayInfoPushNorthboundMsg msg;
};

}