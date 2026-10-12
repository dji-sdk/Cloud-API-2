// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesCommandSendSouthboundMsg {
    std::string target;
    std::string command;
    std::string req;
    bool need_ack = false;

    AIGC_JSON_HELPER(target, command, req, need_ack)
};
///------ 用户自定义数据结构 end ------

class ServicesCommandSendSouthbound final : public MessageV1 {
public:
    ServicesCommandSendSouthbound();
    explicit ServicesCommandSendSouthbound(const ServicesCommandSendSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesCommandSendSouthboundMsg msg;
};

}