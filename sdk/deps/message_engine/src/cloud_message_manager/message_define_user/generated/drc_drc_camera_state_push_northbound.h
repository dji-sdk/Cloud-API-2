// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct CameraStatePushCameraWatermarkMsg {
    int global_enable = 0;
    int drone_type_enable = 0;
    int manufacturer_enable = 0;
    int drone_sn_enable = 0;
    int datetime_enable = 0;
    int gps_enable = 0;
    int user_custom_string_enable = 0;
    std::string user_custom_string;
    int layout = 0;

    AIGC_JSON_HELPER(global_enable, drone_type_enable, manufacturer_enable, drone_sn_enable, datetime_enable, gps_enable, user_custom_string_enable, user_custom_string, layout)
};

struct DrcUpDrcCameraStorage {
    int total = 0;
    int used = 0;

    AIGC_JSON_HELPER(total, used)
};

struct DrcUpDrcCameraStateNightModeSettings {
    int night_mode = 0;
    int denoise_level = 0;
    bool night_vision_enable = false;
    bool infrared_fill_light_enable = false;
    int night_scene_mode_suggestion = 0;
    int is_working = 0;

    AIGC_JSON_HELPER(night_mode, denoise_level, night_vision_enable, infrared_fill_light_enable, night_scene_mode_suggestion, is_working)
};

struct DrcUpDrcCameraStateIrMeterRegion {
    int top_left_x = 0;
    int top_left_y = 0;
    int bottom_right_x = 0;
    int bottom_right_y = 0;

    AIGC_JSON_HELPER(top_left_x, top_left_y, bottom_right_x, bottom_right_y)
};

struct DrcUpDrcCameraStateIrPipState {
    int display_mode = 0;
    int enhance_mode = 0;
    int ir_mix_rate = 0;
    DrcUpDrcCameraStateIrMeterRegion ir_meter_region;

    AIGC_JSON_HELPER(display_mode, enhance_mode, ir_mix_rate, ir_meter_region)
};

struct DrcUpDrcCameraStateIsothermPolygonPoint {
    int x = 0;
    int y = 0;

    AIGC_JSON_HELPER(x, y)
};

struct DrcUpDrcCameraState {
    int camera_mode = 0;
    int video_resolution = 0;
    int record_time = 0;
    int recording_state = 0;
    int photo_state = 0;
    double interval_photo_interval = 0.0;
    int linkage_zoom_state = 0;
    int photo_size = 0;
    int remain_photo_num = 0;
    int remain_record_duration = 0;
    CameraStatePushCameraWatermarkMsg camera_watermark_settings;
    DrcUpDrcCameraStorage storage;
    bool thermal_temp_exceed_range_valid = false;
    bool thermal_super_resolution_enable = false;
    DrcUpDrcCameraStateNightModeSettings night_mode_settings;
    DrcUpDrcCameraStateIrPipState ir_pip_state;
    int video_resolution_wide = 0;
    int video_resolution_zoom = 0;
    int photo_size_wide = 0;
    int photo_size_zoom = 0;
    int photo_format = 0;
    int isotherm_enable = 0;
    int isotherm_roi_based_rendering_enable = 0;
    int isotherm_color_rendering_type = 0;
    int isotherm_lower_threshold = 0;
    int isotherm_upper_threshold = 0;
    std::vector<DrcUpDrcCameraStateIsothermPolygonPoint> isotherm_rendering_polygon;

    AIGC_JSON_HELPER(camera_mode, video_resolution, record_time, recording_state, photo_state, interval_photo_interval, linkage_zoom_state, photo_size, remain_photo_num, remain_record_duration, camera_watermark_settings, storage, thermal_temp_exceed_range_valid, thermal_super_resolution_enable, night_mode_settings, ir_pip_state, video_resolution_wide, video_resolution_zoom, photo_size_wide, photo_size_zoom, photo_format, isotherm_enable, isotherm_roi_based_rendering_enable, isotherm_color_rendering_type, isotherm_lower_threshold, isotherm_upper_threshold, isotherm_rendering_polygon)
};

struct DrcUpDrcCameraStateMediaStorage {
    std::vector<std::string> photo_storage_settings;
    std::vector<std::string> video_storage_settings;

    AIGC_JSON_HELPER(photo_storage_settings, video_storage_settings)
};

struct DrcUpDrcCameraStateAlgorithmDehazeInfo {
    int mode = 0;
    int level = 0;
    bool is_working = false;

    AIGC_JSON_HELPER(mode, level, is_working)
};

struct DrcUpDrcCameraStatePreRecordedInfo {
    int status = 0;
    int pre_recorded_time = 0;

    AIGC_JSON_HELPER(status, pre_recorded_time)
};

struct DrcUpDrcCameraBasePhotoStorageFormat {
    int photo_storage_format_num = 0;
    std::vector<std::string> photo_storage_formats;

    AIGC_JSON_HELPER(photo_storage_format_num, photo_storage_formats)
};

struct DrcUpDrcCameraCapabilities {
    std::vector<int> zoom_factor_range;
    std::vector<int> ir_factor_range;
    DrcUpDrcCameraBasePhotoStorageFormat base_photo_storage_format;

    AIGC_JSON_HELPER(zoom_factor_range, ir_factor_range, base_photo_storage_format)
};

struct DrcUpDrcCameraSettableEvent {
    int event_id = 0;
    int settable = 0;

    AIGC_JSON_HELPER(event_id, settable)
};

struct DrcUpDrcCameraSettableCapability {
    std::vector<DrcUpDrcCameraSettableEvent> events;

    AIGC_JSON_HELPER(events)
};

struct DrcUpDrcCameraVisibleCapabilityItem {
    int event_id = 0;
    int visible = 0;

    AIGC_JSON_HELPER(event_id, visible)
};

struct DrcUpDrcCameraVisibleCapability {
    int camera_mode = 0;
    std::vector<DrcUpDrcCameraVisibleCapabilityItem> events;

    AIGC_JSON_HELPER(camera_mode, events)
};

struct DrcDrcCameraStatePushNorthboundMsg {
    std::string payload_index;
    DrcUpDrcCameraState camera_state;
    DrcUpDrcCameraStateMediaStorage media_storage;
    DrcUpDrcCameraStateAlgorithmDehazeInfo algorithm_dehaze_info;
    DrcUpDrcCameraStatePreRecordedInfo pre_recorded_info;
    DrcUpDrcCameraCapabilities camera_capability;
    DrcUpDrcCameraSettableCapability camera_settable_capability;
    DrcUpDrcCameraVisibleCapability camera_visible_capability;

    AIGC_JSON_HELPER(payload_index, camera_state, media_storage, algorithm_dehaze_info, pre_recorded_info, camera_capability, camera_settable_capability, camera_visible_capability)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraStatePushNorthbound final : public MessageV2 {
public:
    DrcDrcCameraStatePushNorthbound();
    explicit DrcDrcCameraStatePushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraStatePushNorthboundMsg msg;
};

}