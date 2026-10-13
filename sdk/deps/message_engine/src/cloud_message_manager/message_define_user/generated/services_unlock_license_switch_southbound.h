// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesUnlockLicenseSwitchSouthboundMsg {
    int license_id = 0;
    bool enable = false;

    AIGC_JSON_HELPER(license_id, enable)
};
///------ 用户自定义数据结构 end ------

class ServicesUnlockLicenseSwitchSouthbound final : public MessageV1 {
public:
    ServicesUnlockLicenseSwitchSouthbound();
    explicit ServicesUnlockLicenseSwitchSouthbound(const ServicesUnlockLicenseSwitchSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesUnlockLicenseSwitchSouthboundMsg msg;
};

}