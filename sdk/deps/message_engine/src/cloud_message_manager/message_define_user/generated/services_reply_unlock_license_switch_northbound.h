// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesReplyUnlockLicenseSwitchNorthboundMsg {
    int result = 0;
    int license_id = 0;

    AIGC_JSON_HELPER(result, license_id)
};
///------ 用户自定义数据结构 end ------

class ServicesReplyUnlockLicenseSwitchNorthbound final : public MessageV1 {
public:
    ServicesReplyUnlockLicenseSwitchNorthbound();
    explicit ServicesReplyUnlockLicenseSwitchNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesReplyUnlockLicenseSwitchNorthboundMsg msg;
};

}