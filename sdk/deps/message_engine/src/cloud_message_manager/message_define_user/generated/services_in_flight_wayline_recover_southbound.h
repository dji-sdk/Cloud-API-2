// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesInFlightWaylineRecoverSouthboundMsg {
    int placeholder = 0;

    AIGC_JSON_HELPER(placeholder)
};
///------ 用户自定义数据结构 end ------

class ServicesInFlightWaylineRecoverSouthbound final : public MessageV1 {
public:
    ServicesInFlightWaylineRecoverSouthbound();
    explicit ServicesInFlightWaylineRecoverSouthbound(const ServicesInFlightWaylineRecoverSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesInFlightWaylineRecoverSouthboundMsg msg;
};

}