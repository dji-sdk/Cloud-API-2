// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ReturnHomeTrajectoryPoint {
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    AIGC_JSON_HELPER(latitude, longitude, height)
};

struct MultiDockHomeInfo {
    int plan_status = 0;
    int estimated_battery_consumption = 0;
    double home_distance = 0.0;
    std::string sn;

    AIGC_JSON_HELPER(plan_status, estimated_battery_consumption, home_distance, sn)
};

struct EventsReturnHomeInfoNorthboundMsg {
    int last_point_type = 0;
    std::vector<ReturnHomeTrajectoryPoint> planned_path_points;
    std::string flight_id;
    std::string home_dock_sn;
    MultiDockHomeInfo multi_dock_home_info;

    AIGC_JSON_HELPER(last_point_type, planned_path_points, flight_id, home_dock_sn, multi_dock_home_info)
};
///------ 用户自定义数据结构 end ------

class EventsReturnHomeInfoNorthbound final : public MessageV1 {
public:
    EventsReturnHomeInfoNorthbound();
    explicit EventsReturnHomeInfoNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReturnHomeInfoNorthboundMsg msg;
};

}