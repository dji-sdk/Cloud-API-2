// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesReplySdrDebugSwitchNorthboundMsg {
    int result = 0;

    AIGC_JSON_HELPER(result)
};
///------ 用户自定义数据结构 end ------

class ServicesReplySdrDebugSwitchNorthbound final : public MessageV1 {
public:
    ServicesReplySdrDebugSwitchNorthbound();
    explicit ServicesReplySdrDebugSwitchNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesReplySdrDebugSwitchNorthboundMsg msg;
};

}