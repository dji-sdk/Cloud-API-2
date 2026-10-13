// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ExtInfo {
    int current_waypoint_index = 0;
    std::string flight_id;
    std::string track_id;
    int wayline_id = 0;
    int wayline_mission_state = 0;

    AIGC_JSON_HELPER(current_waypoint_index, flight_id, track_id, wayline_id, wayline_mission_state)
};

struct Progress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct Output {
    ExtInfo ext;
    Progress progress;
    std::string status;

    AIGC_JSON_HELPER(ext, progress, status)
};

struct EventsFlighttaskProgressNorthboundMsg {
    Output output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsFlighttaskProgressNorthbound final : public MessageV1 {
public:
    EventsFlighttaskProgressNorthbound();
    explicit EventsFlighttaskProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsFlighttaskProgressNorthboundMsg msg;
};

}