// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct CandidateAircraftCalculateResult {
    double reach_time = 0.0;
    double reach_energy = 0.0;
    double hover_time = 0.0;
    double hover_energy = 0.0;
    double homing_energy = 0.0;

    AIGC_JSON_HELPER(reach_time, reach_energy, hover_time, hover_energy, homing_energy)
};

struct EventsCandidateAircraftsCalculateResultsNorthboundMsg {
    std::string query_id;
    int result = 0;
    std::string sn;
    std::vector<CandidateAircraftCalculateResult> calculate_results;

    AIGC_JSON_HELPER(query_id, result, sn, calculate_results)
};
///------ 用户自定义数据结构 end ------

class EventsCandidateAircraftsCalculateResultsNorthbound final : public MessageV1 {
public:
    EventsCandidateAircraftsCalculateResultsNorthbound();
    explicit EventsCandidateAircraftsCalculateResultsNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsCandidateAircraftsCalculateResultsNorthboundMsg msg;
};

}