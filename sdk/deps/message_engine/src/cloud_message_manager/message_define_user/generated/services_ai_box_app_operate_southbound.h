// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesAiBoxAppOperateSouthboundMsg {
    std::string app_id;
    std::string sn;
    int operate_type = 0;

    AIGC_JSON_HELPER(app_id, sn, operate_type)
};
///------ 用户自定义数据结构 end ------

class ServicesAiBoxAppOperateSouthbound final : public MessageV1 {
public:
    ServicesAiBoxAppOperateSouthbound();
    explicit ServicesAiBoxAppOperateSouthbound(const ServicesAiBoxAppOperateSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesAiBoxAppOperateSouthboundMsg msg;
};

}