// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>
#include <list>

namespace message_engine {

enum class PropertyType {
    UNKNOWN = 0,
    RTH_ALTITUDE = 1,
    COMMANDER_FLIGHT_HEIGHT = 2,
    RC_LOST_ACTION = 3,
    COMMANDER_MODE_LOST_ACTION = 4,
    COMMANDER_FLIGHT_MODE = 5, // commander_flight_mode
    CAMERA_WATERMARK_SETTING = 6, // camera_watermark_settings
    RTH_MODE = 7, // camera_watermark_settings
};

/// camera_watermark_settings
struct SetPropertyCameraWatermarkMsg {
    int global_enable = 0;
    int drone_type_enable = 0;
    int manufacturer_enable = 0;
    int drone_sn_enable = 0;
    int datetime_enable = 0;
    int gps_enable = 0;
    int user_custom_string_enable = 0;
    std::string user_custom_string;
    int layout = 0;
    AIGC_JSON_HELPER(global_enable, drone_type_enable, manufacturer_enable, drone_sn_enable,
        datetime_enable, gps_enable, user_custom_string_enable, user_custom_string, layout);
};

struct SetPropertySouthboundMsg {
    std::optional<int> rth_altitude;
    std::optional<double> commander_flight_height;
    std::optional<int> rc_lost_action;
    std::optional<int> commander_mode_lost_action;
    std::optional<int> commander_flight_mode;
    std::optional<int> rth_mode;
    std::optional<SetPropertyCameraWatermarkMsg> camera_watermark_settings;

    [[nodiscard]] PropertyType GetValidProperty() const;
};

class SetPropertySouthbound final : public MessageV1 {
public:
    SetPropertySouthbound();
    explicit SetPropertySouthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    SetPropertySouthboundMsg msg;
};

}