// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesRelayPasswdHashSouthboundMsg {
    std::string passwd;

    AIGC_JSON_HELPER(passwd)
};
///------ 用户自定义数据结构 end ------

class ServicesRelayPasswdHashSouthbound final : public MessageV1 {
public:
    ServicesRelayPasswdHashSouthbound();
    explicit ServicesRelayPasswdHashSouthbound(const ServicesRelayPasswdHashSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesRelayPasswdHashSouthboundMsg msg;
};

}