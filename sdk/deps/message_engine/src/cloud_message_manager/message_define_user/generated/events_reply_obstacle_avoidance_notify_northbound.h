// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ObstacleAvoidanceList {
    int id = 0;
    int type = 0;
    double timestamp = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
    int wayline_id = 0;
    int waypoint_index = 0;

    AIGC_JSON_HELPER(id, type, timestamp, latitude, longitude, height, wayline_id, waypoint_index)
};

struct EventsReplyObstacleAvoidanceNotifyNorthboundMsg {
    std::string wayline_uuid;
    std::string flight_id;
    bool is_final_report = false;
    std::vector<ObstacleAvoidanceList> obstacles;

    AIGC_JSON_HELPER(wayline_uuid, flight_id, is_final_report, obstacles)
};
///------ 用户自定义数据结构 end ------

class EventsReplyObstacleAvoidanceNotifyNorthbound final : public MessageV1 {
public:
    EventsReplyObstacleAvoidanceNotifyNorthbound();
    explicit EventsReplyObstacleAvoidanceNotifyNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyObstacleAvoidanceNotifyNorthboundMsg msg;
};

}