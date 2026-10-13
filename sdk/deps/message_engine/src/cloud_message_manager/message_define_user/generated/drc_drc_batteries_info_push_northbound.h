// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcBatteryInfo {
    int capacity_percent = 0;
    std::string firmware_version;
    int high_voltage_storage_days = 0;
    int index = 0;
    int loop_times = 0;
    std::string sn;
    double temperature = 0.0;
    int voltage = 0;

    AIGC_JSON_HELPER(capacity_percent, firmware_version, high_voltage_storage_days, index, loop_times, sn, temperature, voltage)
};

struct DrcDrcBatteriesInfoPushNorthboundMsg {
    std::vector<DrcBatteryInfo> batteries;
    int capacity_percent = 0;
    int landing_power = 0;
    int remain_flight_time = 0;
    int return_home_power = 0;
    int remain_job_time = 0;
    int low_battery_warning_threshold = 0;
    int serious_low_battery_warning_threshold = 0;

    AIGC_JSON_HELPER(batteries, capacity_percent, landing_power, remain_flight_time, return_home_power, remain_job_time, low_battery_warning_threshold, serious_low_battery_warning_threshold)
};
///------ 用户自定义数据结构 end ------

class DrcDrcBatteriesInfoPushNorthbound final : public MessageV2 {
public:
    DrcDrcBatteriesInfoPushNorthbound();
    explicit DrcDrcBatteriesInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcBatteriesInfoPushNorthboundMsg msg;
};

}