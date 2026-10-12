// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct EventsPoiStatusNotifyNorthboundMsg {
    std::string status;
    int reason = 0;
    double circle_radius = 0.0;
    double circle_speed = 0.0;
    double max_circle_speed = 0.0;

    AIGC_JSON_HELPER(status, reason, circle_radius, circle_speed, max_circle_speed)
};
///------ 用户自定义数据结构 end ------

class EventsPoiStatusNotifyNorthbound final : public MessageV1 {
public:
    EventsPoiStatusNotifyNorthbound();
    explicit EventsPoiStatusNotifyNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsPoiStatusNotifyNorthboundMsg msg;
};

}