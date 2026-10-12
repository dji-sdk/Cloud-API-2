// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
struct AirSenseAirplaneStateMsg {
    std::string icao;
    int warning_level = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double altitude = 0.0;
    int altitude_type = 0;
    double heading = 0.0;
    double relative_altitude = 0.0;
    int vert_trend = 0;
    int distance = 0;

    AIGC_JSON_HELPER(icao, warning_level, latitude, longitude, altitude, altitude_type, heading, relative_altitude, vert_trend, distance);
};

struct EventsAirsenseWarningNorthboundMsg {
    std::list<AirSenseAirplaneStateMsg> data;

    AIGC_JSON_HELPER(data);
};
///------ 用户自定义数据结构 end ------

class EventsAirsenseWarningNorthbound final : public MessageV1 {
public:
    EventsAirsenseWarningNorthbound();
    explicit EventsAirsenseWarningNorthbound(const EventsAirsenseWarningNorthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsAirsenseWarningNorthboundMsg msg;
};

}