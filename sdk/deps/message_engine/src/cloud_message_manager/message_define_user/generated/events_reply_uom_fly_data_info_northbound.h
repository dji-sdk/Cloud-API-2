// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct UomFlyDataInfoEventExt {
    std::string order_id;
    std::string flight_status;
    std::string manufacture_id;
    std::string uas_id;
    double time_stamp = 0.0;
    int coordinate = 0;
    double longitude = 0.0;
    double latitude = 0.0;
    double altitude = 0.0;
    std::string area_code;
    int height_type = 0;
    double height = 0.0;
    bool is_flying = false;
    int VS = 0;
    int GS = 0;
    int course = 0;
    std::string sn;
    std::string uas_model;
    bool is_simulator_open = false;

    AIGC_JSON_HELPER(order_id, flight_status, manufacture_id, uas_id, time_stamp, coordinate, longitude, latitude, altitude, area_code, height_type, height, is_flying, VS, GS, course, sn, uas_model, is_simulator_open)
};

struct UomFlyDataInfoEventProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct UomFlyDataInfoEventOutput {
    UomFlyDataInfoEventExt ext;
    UomFlyDataInfoEventProgress progress;
    std::string status;

    AIGC_JSON_HELPER(ext, progress, status)
};

struct EventsReplyUomFlyDataInfoNorthboundMsg {
    UomFlyDataInfoEventOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsReplyUomFlyDataInfoNorthbound final : public MessageV1 {
public:
    EventsReplyUomFlyDataInfoNorthbound();
    explicit EventsReplyUomFlyDataInfoNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyUomFlyDataInfoNorthboundMsg msg;
};

}