// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct EventsObstacleAvoidanceNotifySouthboundMsg {
    int placeholder = 0;

    AIGC_JSON_HELPER(placeholder)
};
///------ 用户自定义数据结构 end ------

class EventsObstacleAvoidanceNotifySouthbound final : public MessageV1 {
public:
    EventsObstacleAvoidanceNotifySouthbound();
    explicit EventsObstacleAvoidanceNotifySouthbound(const EventsObstacleAvoidanceNotifySouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsObstacleAvoidanceNotifySouthboundMsg msg;
};

}