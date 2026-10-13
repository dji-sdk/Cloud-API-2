// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct InFlightWaylineEventProgress {
    int percent = 0;

    AIGC_JSON_HELPER(percent)
};

struct EventsInFlightWaylineProgressNorthboundMsg {
    std::string in_flight_wayline_id;
    int way_point_index = 0;
    int result = 0;
    int status = 0;
    InFlightWaylineEventProgress progress;

    AIGC_JSON_HELPER(in_flight_wayline_id, way_point_index, result, status, progress)
};
///------ 用户自定义数据结构 end ------

class EventsInFlightWaylineProgressNorthbound final : public MessageV1 {
public:
    EventsInFlightWaylineProgressNorthbound();
    explicit EventsInFlightWaylineProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsInFlightWaylineProgressNorthboundMsg msg;
};

}