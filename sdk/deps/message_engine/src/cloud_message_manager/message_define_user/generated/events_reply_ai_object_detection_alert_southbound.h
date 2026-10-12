// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct EventsReplyAiObjectDetectionAlertSouthboundMsg {
    int placeholder = 0;

    AIGC_JSON_HELPER(placeholder)
};
///------ 用户自定义数据结构 end ------

class EventsReplyAiObjectDetectionAlertSouthbound final : public MessageV1 {
public:
    EventsReplyAiObjectDetectionAlertSouthbound();
    explicit EventsReplyAiObjectDetectionAlertSouthbound(const EventsReplyAiObjectDetectionAlertSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyAiObjectDetectionAlertSouthboundMsg msg;
};

}