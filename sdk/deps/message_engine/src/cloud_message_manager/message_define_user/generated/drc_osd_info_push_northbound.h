// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcOsdInfoPushNorthboundMsg {
    double attitude_head = 0.0;
    double height = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;
    double speed_x = 0.0;
    double speed_y = 0.0;
    double speed_z = 0.0;
    int ultrasonic_height = 0;
    double horizontal_speed = 0.0;
    double vertical_speed = 0.0;
    double home_distance = 0.0;
    int wind_direction = 0;
    int wind_speed = 0;
    double elevation = 0.0;
    double gimbal_pitch = 0.0;
    double gimbal_yaw = 0.0;
    double gimbal_roll = 0.0;

    AIGC_JSON_HELPER(attitude_head, height, latitude, longitude, speed_x, speed_y, speed_z, ultrasonic_height, horizontal_speed, vertical_speed, home_distance, wind_direction, wind_speed, elevation, gimbal_pitch, gimbal_yaw, gimbal_roll)
};
///------ 用户自定义数据结构 end ------

class DrcOsdInfoPushNorthbound final : public MessageV2 {
public:
    DrcOsdInfoPushNorthbound();
    explicit DrcOsdInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcOsdInfoPushNorthboundMsg msg;
};

}