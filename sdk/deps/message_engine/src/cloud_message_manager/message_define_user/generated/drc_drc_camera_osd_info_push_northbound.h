// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcUpDrcCameraOsdInfoPushNorthboundMsgFovInfo {
    int width = 0;
    int height = 0;
    int image_ratio = 0;
    int focal_length = 0;

    AIGC_JSON_HELPER(width, height, image_ratio, focal_length)
};

struct DrcUpDrcCameraOsdInfoPushNorthboundMsgWideLense {
    int wide_calibrate_farthest_focus_value = 0;
    int wide_calibrate_nearest_focus_value = 0;
    int wide_exposure_mode = 0;
    int wide_exposure_value = 0;
    int wide_focus_mode = 0;
    int wide_focus_state = 0;
    int wide_focus_value = 0;
    int wide_iso = 0;
    int wide_max_focus_value = 0;
    int wide_min_focus_value = 0;
    int wide_shutter_speed = 0;
    int wide_dewarping_state = 0;
    int wide_mechanical_shutter_state = 0;
    int wide_aperture_value = 0;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgFovInfo wide_fov_info;

    AIGC_JSON_HELPER(wide_calibrate_farthest_focus_value, wide_calibrate_nearest_focus_value, wide_exposure_mode, wide_exposure_value, wide_focus_mode, wide_focus_state, wide_focus_value, wide_iso, wide_max_focus_value, wide_min_focus_value, wide_shutter_speed, wide_dewarping_state, wide_mechanical_shutter_state, wide_aperture_value, wide_fov_info)
};

struct DrcUpDrcCameraOsdInfoPushNorthboundMsgZoomLense {
    int zoom_calibrate_farthest_focus_value = 0;
    int zoom_calibrate_nearest_focus_value = 0;
    int zoom_exposure_mode = 0;
    int zoom_exposure_value = 0;
    double zoom_factor = 0.0;
    int zoom_focus_mode = 0;
    int zoom_focus_state = 0;
    int zoom_focus_value = 0;
    int zoom_iso = 0;
    int zoom_max_focus_value = 0;
    int zoom_min_focus_value = 0;
    int zoom_shutter_speed = 0;
    int zoom_aperture_value = 0;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgFovInfo zoom_fov_info;

    AIGC_JSON_HELPER(zoom_calibrate_farthest_focus_value, zoom_calibrate_nearest_focus_value, zoom_exposure_mode, zoom_exposure_value, zoom_factor, zoom_focus_mode, zoom_focus_state, zoom_focus_value, zoom_iso, zoom_max_focus_value, zoom_min_focus_value, zoom_shutter_speed, zoom_aperture_value, zoom_fov_info)
};

struct DrcUpDrcCameraOsdInfoPushNorthboundMsgIrLense {
    bool screen_split_enable = false;
    int ir_zoom_factor = 0;
    int thermal_current_palette_style = 0;
    int thermal_gain_mode = 0;
    double thermal_global_temperature_max = 0.0;
    double thermal_global_temperature_min = 0.0;
    int thermal_isotherm_lower_limit = 0;
    int thermal_isotherm_state = 0;
    int thermal_isotherm_upper_limit = 0;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgFovInfo ir_fov_info;

    AIGC_JSON_HELPER(screen_split_enable, ir_zoom_factor, thermal_current_palette_style, thermal_gain_mode, thermal_global_temperature_max, thermal_global_temperature_min, thermal_isotherm_lower_limit, thermal_isotherm_state, thermal_isotherm_upper_limit, ir_fov_info)
};

struct DrcUpDrcCameraOsdInfoPushNorthboundMsgTemperaturePoint {
    double temperature = 0.0;
    double x = 0.0;
    double y = 0.0;

    AIGC_JSON_HELPER(temperature, x, y)
};

struct DrcUpDrcCameraOsdInfoPushNorthboundMsgIrMeteringArea {
    double aver_temperature = 0.0;
    double height = 0.0;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgTemperaturePoint max_temperature_point;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgTemperaturePoint min_temperature_point;
    double width = 0.0;
    double x = 0.0;
    double y = 0.0;

    AIGC_JSON_HELPER(aver_temperature, height, max_temperature_point, min_temperature_point, width, x, y)
};

struct DrcUpDrcCameraOsdInfoPushNorthboundMsgIrThermometry {
    DrcUpDrcCameraOsdInfoPushNorthboundMsgIrMeteringArea ir_metering_area;
    int ir_metering_mode = 0;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgTemperaturePoint ir_metering_point;

    AIGC_JSON_HELPER(ir_metering_area, ir_metering_mode, ir_metering_point)
};

struct DrcUpDrcCameraOsdInfoPushNorthboundMsgMeasureTarget {
    double measure_target_distance = 0.0;
    double measure_target_latitude = 0.0;
    double measure_target_longitude = 0.0;
    double measure_target_altitude = 0.0;
    int measure_target_error_state = 0;

    AIGC_JSON_HELPER(measure_target_distance, measure_target_latitude, measure_target_longitude, measure_target_altitude, measure_target_error_state)
};

struct DrcUpDrcCameraOsdInfoPushNorthboundMsgLiveViewWorldRegion {
    double bottom = 0.0;
    double left = 0.0;
    double right = 0.0;
    double top = 0.0;

    AIGC_JSON_HELPER(bottom, left, right, top)
};

struct DrcUpDrcCameraOsdInfoPushNorthboundMsgLiveView {
    DrcUpDrcCameraOsdInfoPushNorthboundMsgLiveViewWorldRegion liveview_world_region;

    AIGC_JSON_HELPER(liveview_world_region)
};

struct DrcDrcCameraOsdInfoPushNorthboundMsg {
    std::string payload_index;
    double gimbal_pitch = 0.0;
    double gimbal_roll = 0.0;
    double gimbal_yaw = 0.0;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgWideLense wide_lense;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgZoomLense zoom_lense;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgIrLense ir_lense;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgIrThermometry ir_thermometry;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgMeasureTarget measure_target;
    DrcUpDrcCameraOsdInfoPushNorthboundMsgLiveView liveview;

    AIGC_JSON_HELPER(payload_index, gimbal_pitch, gimbal_roll, gimbal_yaw, wide_lense, zoom_lense, ir_lense, ir_thermometry, measure_target, liveview)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraOsdInfoPushNorthbound final : public MessageV2 {
public:
    DrcDrcCameraOsdInfoPushNorthbound();
    explicit DrcDrcCameraOsdInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraOsdInfoPushNorthboundMsg msg;
};

}