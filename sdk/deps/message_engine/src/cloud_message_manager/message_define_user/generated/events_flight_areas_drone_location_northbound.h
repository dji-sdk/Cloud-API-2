// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct VfenceDistanceInfoMsg {
    std::string area_id;
    int area_type = 0;
    double area_distance = 0.0;
    bool is_in_area = false;

    AIGC_JSON_HELPER(area_id, area_type, area_distance, is_in_area)
};

struct EventsFlightAreasDroneLocationNorthboundMsg {
    std::vector<VfenceDistanceInfoMsg> drone_locations;

    AIGC_JSON_HELPER(drone_locations)
};
///------ 用户自定义数据结构 end ------

class EventsFlightAreasDroneLocationNorthbound final : public MessageV1 {
public:
    EventsFlightAreasDroneLocationNorthbound();
    explicit EventsFlightAreasDroneLocationNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsFlightAreasDroneLocationNorthboundMsg msg;
};

}