// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct OneTakeOffEventsFlyToPointProgressNorthboundMsgPoint {
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    AIGC_JSON_HELPER(latitude, longitude, height)
};

struct EventsTakeoffToPointProgressNorthboundMsg {
    int result = 0;
    std::string fly_to_id;
    std::vector<OneTakeOffEventsFlyToPointProgressNorthboundMsgPoint> planned_path_points;
    double remaining_distance = 0.0;
    double remaining_time = 0.0;
    std::string status;
    int way_point_index = 0;
    std::string track_id;

    AIGC_JSON_HELPER(result, fly_to_id, planned_path_points, remaining_distance, remaining_time, status, way_point_index, track_id)
};
///------ 用户自定义数据结构 end ------

class EventsTakeoffToPointProgressNorthbound final : public MessageV1 {
public:
    EventsTakeoffToPointProgressNorthbound();
    explicit EventsTakeoffToPointProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsTakeoffToPointProgressNorthboundMsg msg;
};

}