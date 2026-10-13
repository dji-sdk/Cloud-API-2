// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct BreakPoint {
    int index = 0;
    double progress = 0.0;
    int state = 0;
    int wayline_id = 0;

    AIGC_JSON_HELPER(index, progress, state, wayline_id)
};

struct ExecutableConditions {
    int storage_capacity = 0;

    AIGC_JSON_HELPER(storage_capacity)
};

struct FileInfo {
    std::string fingerprint;
    std::string url;

    AIGC_JSON_HELPER(fingerprint, url)
};

struct ReadyConditions {
    int battery_capacity = 0;
    double begin_time = 0.0;
    double end_time = 0.0;

    AIGC_JSON_HELPER(battery_capacity, begin_time, end_time)
};

struct SimulateMission {
    int is_enable = 0;
    double latitude = 0.0;
    double doubleitude = 0.0;

    AIGC_JSON_HELPER(is_enable, latitude, doubleitude)
};

struct ServicesFlighttaskPrepareSouthboundMsg {
    std::string flight_id;
    BreakPoint break_point;
    ExecutableConditions executable_conditions;
    double execute_time = 0.0;
    int exit_wayline_when_rc_lost = 0;
    FileInfo file;
    ReadyConditions ready_conditions;
    int rth_altitude = 0;
    SimulateMission simulate_mission;
    int task_type = 0;
    int wayline_precision_type = 0;
    int flight_safety_advance_check = 0;
    int out_of_control_action = 0;

    AIGC_JSON_HELPER(flight_id, break_point, executable_conditions, execute_time, exit_wayline_when_rc_lost, file, ready_conditions, rth_altitude, simulate_mission, task_type, wayline_precision_type, flight_safety_advance_check, out_of_control_action)
};
///------ 用户自定义数据结构 end ------

class ServicesFlighttaskPrepareSouthbound final : public MessageV1 {
public:
    ServicesFlighttaskPrepareSouthbound();
    explicit ServicesFlighttaskPrepareSouthbound(const ServicesFlighttaskPrepareSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesFlighttaskPrepareSouthboundMsg msg;
};

}