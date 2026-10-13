// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesAiBoxAppListSouthboundMsg {
    std::string sn;

    AIGC_JSON_HELPER(sn)
};
///------ 用户自定义数据结构 end ------

class ServicesAiBoxAppListSouthbound final : public MessageV1 {
public:
    ServicesAiBoxAppListSouthbound();
    explicit ServicesAiBoxAppListSouthbound(const ServicesAiBoxAppListSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesAiBoxAppListSouthboundMsg msg;
};

}