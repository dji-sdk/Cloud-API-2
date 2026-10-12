// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct EventsFlyToPointProgressNorthboundMsgPoint {
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    AIGC_JSON_HELPER(latitude, longitude, height)
};

struct EventsFlyToPointProgressNorthboundMsg {
    int result = 0;
    std::string fly_to_id;
    std::vector<EventsFlyToPointProgressNorthboundMsgPoint> planned_path_points;
    double remaining_distance = 0.0;
    double remaining_time = 0.0;
    std::string status;
    int way_point_index = 0;

    AIGC_JSON_HELPER(result, fly_to_id, planned_path_points, remaining_distance, remaining_time, status, way_point_index)
};
///------ 用户自定义数据结构 end ------

class EventsFlyToPointProgressNorthbound final : public MessageV1 {
public:
    EventsFlyToPointProgressNorthbound();
    explicit EventsFlyToPointProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsFlyToPointProgressNorthboundMsg msg;
};

}