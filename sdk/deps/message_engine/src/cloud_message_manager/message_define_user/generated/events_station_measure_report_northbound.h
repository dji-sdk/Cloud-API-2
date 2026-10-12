// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct MeasureNode {
    std::string flight_id;
    std::string sn;
    int score = 0;

    AIGC_JSON_HELPER(flight_id, sn, score)
};

struct EventsStationMeasureReportNorthboundMsg {
    int seq_num = 0;
    int sdr_topo_role = 0;
    MeasureNode measure_node;
    int result = 0;

    AIGC_JSON_HELPER(seq_num, sdr_topo_role, measure_node, result)
};
///------ 用户自定义数据结构 end ------

class EventsStationMeasureReportNorthbound final : public MessageV1 {
public:
    EventsStationMeasureReportNorthbound();
    explicit EventsStationMeasureReportNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsStationMeasureReportNorthboundMsg msg;
};

}