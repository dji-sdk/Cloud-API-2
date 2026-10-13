// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DroneAssistantVideoState {
    int sw = 0;
    int mode = 0;

    AIGC_JSON_HELPER(sw, mode)
};

struct DroneFlashLedPatternInfo {
    int color = 0;
    int period = 0;
    int brightness = 0;

    AIGC_JSON_HELPER(color, period, brightness)
};

struct DroneFlashLed {
    int on = 0;
    std::string pattern_uuid;
    std::string pattern_name;
    std::vector<DroneFlashLedPatternInfo> patterns;

    AIGC_JSON_HELPER(on, pattern_uuid, pattern_name, patterns)
};

struct DrcDrcDroneStatePushNorthboundMsg {
    int stealth_state = 0;
    int night_lights_state = 0;
    int mode_code = 0;
    int rth_altitude = 0;
    int current_commander_flight_mode = 0;
    int is_near_height_limit = 0;
    int is_near_distance_limit = 0;
    int follow_mouse_motion_switch = 0;
    DroneAssistantVideoState assistant_video;
    bool is_in_fixed_speed = false;
    bool is_marker_focuser_activated = false;
    int landing_type = 0;
    int landing_protection_type = 0;
    DroneFlashLed flash_led;

    AIGC_JSON_HELPER(stealth_state, night_lights_state, mode_code, rth_altitude, current_commander_flight_mode, is_near_height_limit, is_near_distance_limit, follow_mouse_motion_switch, assistant_video, is_in_fixed_speed, is_marker_focuser_activated, landing_type, landing_protection_type, flash_led)
};
///------ 用户自定义数据结构 end ------

class DrcDrcDroneStatePushNorthbound final : public MessageV2 {
public:
    DrcDrcDroneStatePushNorthbound();
    explicit DrcDrcDroneStatePushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcDroneStatePushNorthboundMsg msg;
};

}