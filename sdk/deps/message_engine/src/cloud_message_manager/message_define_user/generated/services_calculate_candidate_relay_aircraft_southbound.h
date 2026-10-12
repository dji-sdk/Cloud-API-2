// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct CalculateLocation {
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    AIGC_JSON_HELPER(latitude, longitude, height)
};

struct CandidateAircraftInfo {
    double battery = 0.0;
    double extra_use_battery = 0.0;
    CalculateLocation current_location;
    CalculateLocation home_location;

    AIGC_JSON_HELPER(battery, extra_use_battery, current_location, home_location)
};

struct ServicesCalculateCandidateRelayAircraftSouthboundMsg {
    std::string query_id;
    CalculateLocation relay_point;
    std::vector<CandidateAircraftInfo> candidate_aircrafts;

    AIGC_JSON_HELPER(query_id, relay_point, candidate_aircrafts)
};
///------ 用户自定义数据结构 end ------

class ServicesCalculateCandidateRelayAircraftSouthbound final : public MessageV1 {
public:
    ServicesCalculateCandidateRelayAircraftSouthbound();
    explicit ServicesCalculateCandidateRelayAircraftSouthbound(const ServicesCalculateCandidateRelayAircraftSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesCalculateCandidateRelayAircraftSouthboundMsg msg;
};

}