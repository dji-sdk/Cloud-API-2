// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "camera_abs.h"
#include "base/common/logger.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/request_message.h"
#include "http_interface/http_interface.h"
#include "api/flight_hub_sdk.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include <common/executors/executors.h>
#include "tool/flight_hub_utils.h"

namespace flight_hub_sdk {

CameraAbs::CameraAbs(const std::string& payload_index, const std::string& drone_sn, const std::string& gateway_sn, const std::shared_ptr<message_engine::IMessageEngine>& message_engine, int drone_instance_id)
    : payload_index_(payload_index), drone_sn_(drone_sn), gateway_sn_(gateway_sn), message_engine_(message_engine) {
    instance_id_ = drone_instance_id;
    log_tag_ = drone_sn_ + "#" + std::to_string(instance_id_);
    LOG_INFO("[CameraAbs][%s][%s] constructor", log_tag_.c_str(), payload_index_.c_str());
    initializeDeviceStatusListening();
    initPayloadAuthorityListening();
}

CameraAbs::~CameraAbs() {
    LOG_INFO("[CameraAbs][%s][%s] destructor", log_tag_.c_str(), payload_index_.c_str());
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_payloads_list_listen_id_);
    if (payload_control_authority_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(payload_control_authority_listen_id_);
        payload_control_authority_listen_id_ = -1;
    }
    message_engine_->getCloudMessageManager()->CancelObserveMessage(drc_camera_osd_listen_id_);
    message_engine_->getCloudMessageManager()->CancelObserveMessage(drc_camera_state_listen_id_);
    message_engine_->getCloudMessageManager()->CancelObserveMessage(live_video_info_push_listen_id_);
    message_engine_->getCloudMessageManager()->CancelObserveMessage(drc_drone_state_push_listen_id_);
    message_engine_->getCloudMessageManager()->CancelObserveMessage(camera_photo_info_listen_id_);
}

void CameraAbs::initializeDeviceStatusListening() {
    WeakDummy(p);
    drc_camera_osd_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcCameraOsdInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcCameraOsdInfoPushNorthbound>& msg) {
            WeakDummyReturn(p);
            if (!msg || msg->msg.payload_index != payload_index_) {
                return;
            }
            onReceiveCameraDrcOsdInfo(*msg);
        }
    );
    if (drc_camera_osd_listen_id_ < 0) {
        LOG_ERROR("[CameraAbs][%s][%s] ObserveDrcDrcCameraOsdInfoPushNorthbound failed, drc_camera_osd_listen_id_ %d", log_tag_.c_str(), payload_index_.c_str(), drc_camera_osd_listen_id_);
    }


    // TODO: Add NC(notification_center) listening;
    // TODO: 添加 激光打点信息 监听逻辑(以及回调前的聚合逻辑);
    // TODO: 激光打点信息;
    // TODO: MQTT 链路 NC 监听后, NC 定频接受推送;
    // TODO: DRC 链路监听后, DrcDrcCameraOsdInfoPushNorthbound 中含有 drc_camera_osd_info_push 信息；
    // TODO: 因为两条链路消息频率不同，消息数据没有时间戳，需要想办法聚合消息(特别要考虑时序问题)；


    drc_camera_state_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcCameraStatePushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcCameraStatePushNorthbound>& msg) {
            WeakDummyReturn(p);
            if (!msg || msg->msg.payload_index != payload_index_) {
                return;
            }
            onReceiveCameraState(*msg);
        }
    );
    if (drc_camera_state_listen_id_ < 0) {
        LOG_ERROR("[CameraAbs][%s][%s] ObserveDrcDrcCameraStatePushNorthbound failed, drc_camera_state_listen_id_ %d", log_tag_.c_str(), payload_index_.c_str(), drc_camera_state_listen_id_);
    }

    // drc_live_video_info_push
    live_video_info_push_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcLiveVideoInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcLiveVideoInfoPushNorthbound>& msg) {
            WeakDummyReturn(p);
            if (!msg || msg->msg.payload_index != payload_index_) {
                return;
            }
            onReceiveLiveVideoInfoPush(*msg);
        }
    );
    if (live_video_info_push_listen_id_ < 0) {
        LOG_ERROR("[CameraAbs][%s][%s] ObserveLiveVideoInfoPushNorthbound failed, live_video_info_push_listen_id_ %d", log_tag_.c_str(), payload_index_.c_str(), live_video_info_push_listen_id_);
    }

    drc_drone_state_push_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcDroneStatePushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcDroneStatePushNorthbound>& msg) {
            WeakDummyReturn(p);
            if (!msg) {
                return;
            }
            onReceiveDrcDroneStatePush(*msg);
        }
    );
    if (drc_drone_state_push_listen_id_ < 0) {
        LOG_ERROR("[CameraAbs][%s][%s] ObserveDrcDrcDroneStatePushNorthbound failed, drc_drone_state_push_listen_id_ %d", log_tag_.c_str(), payload_index_.c_str(), drc_drone_state_push_listen_id_);
    }

    // photo_info / panorama / interval 共用一条 Observe（ME 同 Topic；避免双注册 + 析构漏 Cancel）
    camera_photo_info_listen_id_ = message_engine_->getCloudMessageManager()->ObserveDrcDrcCameraPhotoInfoPushNorthbound(
        [this, p](const std::shared_ptr<message_engine::DrcDrcCameraPhotoInfoPushNorthbound>& msg) {
            WeakDummyReturn(p);
            onReceiveCameraPhotoInfoPush(msg);
        });
    if (camera_photo_info_listen_id_ < 0) {
        LOG_ERROR("[CameraAbs][%s][%s] ObserveDrcDrcCameraPhotoInfoPushNorthbound failed, id %d",
                  log_tag_.c_str(), payload_index_.c_str(), camera_photo_info_listen_id_);
    }

    aircraft_payloads_list_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftPayloadsList(
        [this, p](const notification_center::AircraftPayloadsList& msg)-> void {
            WeakDummyReturn(p);
            onReceiveAircraftPayloadsList(msg);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            LOG_INFO("[CameraAbs][%s][%s] ListenAircraftPayloadsList, listen result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code);
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);
    if (aircraft_payloads_list_listen_id_ < 0) {
        LOG_ERROR("[CameraAbs][%s][%s] ListenAircraftPayloadsList failed, aircraft_payloads_list_listen_id_ %d", log_tag_.c_str(), payload_index_.c_str(), aircraft_payloads_list_listen_id_);
    }

    // HardCode
    // 用 std::vector<int> 替换 std::vector<CameraMode>, 避免跨平台编译报错(比如 go 编译);
    std::vector<int> camera_mode_list;
    camera_mode_list.push_back(static_cast<int>(CameraMode::CameraMode_PHOTO));
    camera_mode_list.push_back(static_cast<int>(CameraMode::CameraMode_VIDEO));
    camera_mode_list.push_back(static_cast<int>(CameraMode::CameraMode_SMART_PHOTO));
    camera_mode_list.push_back(static_cast<int>(CameraMode::CameraMode_PANO_PHOTO));
    camera_mode_list.push_back(static_cast<int>(CameraMode::CameraMode_INTERVAL_PHOTO));
    camera_mode_available_range_callback_container_.notifyCallback(camera_mode_list);
}

void CameraAbs::initPayloadAuthorityListening() {
    WeakDummy(weak_ptr);
    payload_control_authority_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenControlRightsCloudControlStatus(
        [this, weak_ptr](const notification_center::ControlRightsCloudControlStatus& msg) -> void {
            WeakDummyReturn(weak_ptr);
            onReceivePayloadControlAuthority(msg);
        },
        [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            LOG_INFO("[CameraAbs][%s][%s] ListenControlRightsCloudControlStatus, listen result: %d",
                log_tag_.c_str(), payload_index_.c_str(), error_code);
        },
        drone_sn_, notification_center::NotificationFrequency_OnChanged);
    if (payload_control_authority_listen_id_ < 0) {
        LOG_ERROR("[CameraAbs][%s][%s] ListenControlRightsCloudControlStatus failed, payload_control_authority_listen_id_ %d",
            log_tag_.c_str(), payload_index_.c_str(), payload_control_authority_listen_id_);
    }
}

void CameraAbs::onReceivePayloadControlAuthority(const notification_center::ControlRightsCloudControlStatus& msg) {
    payload_control_authority_msg_ = msg;
    // TODO: 当前后端方案使得 SDK 没有身份标识，先默认有负载控制权
    is_payload_control_authority_ = true;
    payload_control_authority_callback_container_.notifyCallback(is_payload_control_authority_);
}

void CameraAbs::onReceiveAircraftPayloadsList(const notification_center::AircraftPayloadsList& msg) {
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::Main()->Post([this, msg, weak_ptr]() {
        WeakDummyReturn(weak_ptr);
        aircraft_payloads_list_msg_ = msg;
        // LOG_INFO("PayloadCtrlMgr: onReceiveAircraftPayloadsList, payload_index: %d", msg.payloads_list.size());
    });
}

void CameraAbs::onReceiveCameraDrcOsdInfo(const message_engine::DrcDrcCameraOsdInfoPushNorthbound& msg) {
    LOG_DEBUG("[CameraAbs][%s][%s] onReceiveCameraDrcOsdInfo, payload_index: %s", log_tag_.c_str(), payload_index_.c_str(), msg.msg.payload_index.c_str());

    // cache msg
    drc_camera_osd_msg_ = msg;

    // liveview world region callback
    LiveviewWorldRegion liveview_world_region;
    liveview_world_region.payload_index = msg.msg.payload_index;
    liveview_world_region.bottom = msg.msg.liveview.liveview_world_region.bottom;
    liveview_world_region.left = msg.msg.liveview.liveview_world_region.left;
    liveview_world_region.right = msg.msg.liveview.liveview_world_region.right;
    liveview_world_region.top = msg.msg.liveview.liveview_world_region.top;
    liveview_world_region_callback_container_.notifyCallbackIfChanged(liveview_world_region);

    // camera wide lens state callback
    CameraWideLensState camera_wide_lens_state;
    camera_wide_lens_state.payload_index = msg.msg.payload_index;
    camera_wide_lens_state.wide_calibrate_farthest_focus_value = msg.msg.wide_lense.wide_calibrate_farthest_focus_value;
    camera_wide_lens_state.wide_calibrate_nearest_focus_value = msg.msg.wide_lense.wide_calibrate_nearest_focus_value;
    camera_wide_lens_state.wide_exposure_mode = msg.msg.wide_lense.wide_exposure_mode;
    camera_wide_lens_state.wide_exposure_value = msg.msg.wide_lense.wide_exposure_value;
    camera_wide_lens_state.wide_focus_mode = msg.msg.wide_lense.wide_focus_mode;
    camera_wide_lens_state.wide_focus_state = msg.msg.wide_lense.wide_focus_state;
    camera_wide_lens_state.wide_focus_value = msg.msg.wide_lense.wide_focus_value;
    camera_wide_lens_state.wide_iso = msg.msg.wide_lense.wide_iso;
    camera_wide_lens_state.wide_max_focus_value = msg.msg.wide_lense.wide_max_focus_value;
    camera_wide_lens_state.wide_min_focus_value = msg.msg.wide_lense.wide_min_focus_value;
    camera_wide_lens_state.wide_shutter_speed = msg.msg.wide_lense.wide_shutter_speed;
    camera_wide_lens_state.wide_dewarping_state = msg.msg.wide_lense.wide_dewarping_state;
    camera_wide_lens_state.wide_mechanical_shutter_state = msg.msg.wide_lense.wide_mechanical_shutter_state;
    camera_wide_lens_state.wide_aperture_value = msg.msg.wide_lense.wide_aperture_value;
    camera_wide_lens_state.wide_fov_info.payload_index = msg.msg.payload_index;
    camera_wide_lens_state.wide_fov_info.width = msg.msg.wide_lense.wide_fov_info.width;
    camera_wide_lens_state.wide_fov_info.height = msg.msg.wide_lense.wide_fov_info.height;
    camera_wide_lens_state.wide_fov_info.image_ratio = msg.msg.wide_lense.wide_fov_info.image_ratio;
    camera_wide_lens_state.wide_fov_info.focal_length = msg.msg.wide_lense.wide_fov_info.focal_length;
    camera_wide_lens_state_callback_container_.notifyCallbackIfChanged(camera_wide_lens_state);

    // camera zoom lens state callback
    CameraZoomLensState camera_zoom_lens_state;
    camera_zoom_lens_state.payload_index = msg.msg.payload_index;
    camera_zoom_lens_state.zoom_calibrate_farthest_focus_value = msg.msg.zoom_lense.zoom_calibrate_farthest_focus_value;
    camera_zoom_lens_state.zoom_calibrate_nearest_focus_value = msg.msg.zoom_lense.zoom_calibrate_nearest_focus_value;
    camera_zoom_lens_state.zoom_exposure_mode = msg.msg.zoom_lense.zoom_exposure_mode;
    camera_zoom_lens_state.zoom_exposure_value = msg.msg.zoom_lense.zoom_exposure_value;
    camera_zoom_lens_state.zoom_factor = msg.msg.zoom_lense.zoom_factor;
    camera_zoom_lens_state.zoom_focus_mode = msg.msg.zoom_lense.zoom_focus_mode;
    camera_zoom_lens_state.zoom_focus_state = msg.msg.zoom_lense.zoom_focus_state;
    camera_zoom_lens_state.zoom_focus_value = msg.msg.zoom_lense.zoom_focus_value;
    camera_zoom_lens_state.zoom_iso = msg.msg.zoom_lense.zoom_iso;
    camera_zoom_lens_state.zoom_max_focus_value = msg.msg.zoom_lense.zoom_max_focus_value;
    camera_zoom_lens_state.zoom_min_focus_value = msg.msg.zoom_lense.zoom_min_focus_value;
    camera_zoom_lens_state.zoom_shutter_speed = msg.msg.zoom_lense.zoom_shutter_speed;
    camera_zoom_lens_state.zoom_aperture_value = msg.msg.zoom_lense.zoom_aperture_value;
    camera_zoom_lens_state.zoom_fov_info.payload_index = msg.msg.payload_index;
    camera_zoom_lens_state.zoom_fov_info.width = msg.msg.zoom_lense.zoom_fov_info.width;
    camera_zoom_lens_state.zoom_fov_info.height = msg.msg.zoom_lense.zoom_fov_info.height;
    camera_zoom_lens_state.zoom_fov_info.image_ratio = msg.msg.zoom_lense.zoom_fov_info.image_ratio;
    camera_zoom_lens_state.zoom_fov_info.focal_length = msg.msg.zoom_lense.zoom_fov_info.focal_length;
    camera_zoom_lens_state_callback_container_.notifyCallbackIfChanged(camera_zoom_lens_state);

    // camera measure target callback
    CameraMeasureTarget camera_measure_target;
    camera_measure_target.payload_index = msg.msg.payload_index;
    camera_measure_target.distance = msg.msg.measure_target.measure_target_distance;
    camera_measure_target.latitude = msg.msg.measure_target.measure_target_latitude;
    camera_measure_target.longitude = msg.msg.measure_target.measure_target_longitude;
    camera_measure_target.altitude = msg.msg.measure_target.measure_target_altitude;
    camera_measure_target.laser_measure_state = msg.msg.measure_target.measure_target_error_state;
    camera_measure_target_callback_container_.notifyCallbackIfChanged(camera_measure_target);

    // gimbal state callback
    GimbalState gimbal_state;
    gimbal_state.payload_index = msg.msg.payload_index;
    gimbal_state.yaw = msg.msg.gimbal_yaw;
    gimbal_state.pitch = msg.msg.gimbal_pitch;
    gimbal_state.roll = msg.msg.gimbal_roll;
    gimbal_state_callback_container_.notifyCallbackIfChanged(gimbal_state);

    // thermal gain mode callback
    ThermalGainModeInfo thermal_gain_mode_info;
    thermal_gain_mode_info.payload_index = msg.msg.payload_index;
    thermal_gain_mode_info.thermal_gain_mode = msg.msg.ir_lense.thermal_gain_mode;
    thermal_gain_mode_callback_container_.notifyCallbackIfChanged(thermal_gain_mode_info);

    // ir lens full state callback (含增益/调色板/等温线/倍率/分屏/FOV)
    CameraIrLensState camera_ir_lens_state;
    camera_ir_lens_state.payload_index = msg.msg.payload_index;
    camera_ir_lens_state.screen_split_enable = msg.msg.ir_lense.screen_split_enable;
    camera_ir_lens_state.ir_zoom_factor = msg.msg.ir_lense.ir_zoom_factor;
    camera_ir_lens_state.thermal_current_palette_style = msg.msg.ir_lense.thermal_current_palette_style;
    camera_ir_lens_state.thermal_gain_mode = msg.msg.ir_lense.thermal_gain_mode;
    camera_ir_lens_state.thermal_global_temperature_max = msg.msg.ir_lense.thermal_global_temperature_max;
    camera_ir_lens_state.thermal_global_temperature_min = msg.msg.ir_lense.thermal_global_temperature_min;
    camera_ir_lens_state.thermal_isotherm_lower_limit = msg.msg.ir_lense.thermal_isotherm_lower_limit;
    camera_ir_lens_state.thermal_isotherm_state = msg.msg.ir_lense.thermal_isotherm_state;
    camera_ir_lens_state.thermal_isotherm_upper_limit = msg.msg.ir_lense.thermal_isotherm_upper_limit;
    camera_ir_lens_state.ir_fov_info.payload_index = msg.msg.payload_index;
    camera_ir_lens_state.ir_fov_info.width = msg.msg.ir_lense.ir_fov_info.width;
    camera_ir_lens_state.ir_fov_info.height = msg.msg.ir_lense.ir_fov_info.height;
    camera_ir_lens_state.ir_fov_info.image_ratio = msg.msg.ir_lense.ir_fov_info.image_ratio;
    camera_ir_lens_state.ir_fov_info.focal_length = msg.msg.ir_lense.ir_fov_info.focal_length;
    camera_ir_lens_state_callback_container_.notifyCallbackIfChanged(camera_ir_lens_state);

    // ir thermometry callback (点/区域测温 + 极值点)
    CameraIrThermometryInfo camera_ir_thermometry_info;
    camera_ir_thermometry_info.payload_index = msg.msg.payload_index;
    camera_ir_thermometry_info.ir_metering_mode = msg.msg.ir_thermometry.ir_metering_mode;
    camera_ir_thermometry_info.ir_metering_point.temperature = msg.msg.ir_thermometry.ir_metering_point.temperature;
    camera_ir_thermometry_info.ir_metering_point.x = msg.msg.ir_thermometry.ir_metering_point.x;
    camera_ir_thermometry_info.ir_metering_point.y = msg.msg.ir_thermometry.ir_metering_point.y;
    camera_ir_thermometry_info.ir_metering_area.aver_temperature = msg.msg.ir_thermometry.ir_metering_area.aver_temperature;
    camera_ir_thermometry_info.ir_metering_area.height = msg.msg.ir_thermometry.ir_metering_area.height;
    camera_ir_thermometry_info.ir_metering_area.width = msg.msg.ir_thermometry.ir_metering_area.width;
    camera_ir_thermometry_info.ir_metering_area.x = msg.msg.ir_thermometry.ir_metering_area.x;
    camera_ir_thermometry_info.ir_metering_area.y = msg.msg.ir_thermometry.ir_metering_area.y;
    camera_ir_thermometry_info.ir_metering_area.max_temperature_point.temperature = msg.msg.ir_thermometry.ir_metering_area.max_temperature_point.temperature;
    camera_ir_thermometry_info.ir_metering_area.max_temperature_point.x = msg.msg.ir_thermometry.ir_metering_area.max_temperature_point.x;
    camera_ir_thermometry_info.ir_metering_area.max_temperature_point.y = msg.msg.ir_thermometry.ir_metering_area.max_temperature_point.y;
    camera_ir_thermometry_info.ir_metering_area.min_temperature_point.temperature = msg.msg.ir_thermometry.ir_metering_area.min_temperature_point.temperature;
    camera_ir_thermometry_info.ir_metering_area.min_temperature_point.x = msg.msg.ir_thermometry.ir_metering_area.min_temperature_point.x;
    camera_ir_thermometry_info.ir_metering_area.min_temperature_point.y = msg.msg.ir_thermometry.ir_metering_area.min_temperature_point.y;
    camera_ir_thermometry_callback_container_.notifyCallbackIfChanged(camera_ir_thermometry_info);
}

void CameraAbs::onReceiveCameraState(const message_engine::DrcDrcCameraStatePushNorthbound& msg) {
    LOG_DEBUG("[CameraAbs][%s][%s] onReceiveCameraState, payload_index: %s", log_tag_.c_str(), payload_index_.c_str(), msg.msg.payload_index.c_str());

    // cache msg
    drc_camera_state_msg_ = msg;

    const auto& payload_index = msg.msg.payload_index;
    const auto& cs = msg.msg.camera_state;

    // camera state callback（B4 扩展到 camera_state 全字段）
    CameraState camera_state;
    camera_state.payload_index = payload_index;
    camera_state.camera_mode = cs.camera_mode;
    camera_state.record_time = cs.record_time;
    camera_state.recording_state = cs.recording_state;
    camera_state.remain_record_duration = cs.remain_record_duration;
    camera_state.photo_state = cs.photo_state;
    camera_state.remain_photo_num = cs.remain_photo_num;
    camera_state.interval_photo_interval = cs.interval_photo_interval;
    camera_state.video_resolution = cs.video_resolution;
    camera_state.linkage_zoom_state = cs.linkage_zoom_state;
    camera_state.photo_size = cs.photo_size;
    camera_state.video_resolution_wide = cs.video_resolution_wide;
    camera_state.video_resolution_zoom = cs.video_resolution_zoom;
    camera_state.photo_size_wide = cs.photo_size_wide;
    camera_state.photo_size_zoom = cs.photo_size_zoom;
    camera_state.photo_format = cs.photo_format;
    camera_state.thermal_temp_exceed_range_valid = cs.thermal_temp_exceed_range_valid;
    camera_state.thermal_super_resolution_enable = cs.thermal_super_resolution_enable;
    camera_state.isotherm_enable = cs.isotherm_enable;
    camera_state.isotherm_roi_based_rendering_enable = cs.isotherm_roi_based_rendering_enable;
    camera_state.isotherm_color_rendering_type = cs.isotherm_color_rendering_type;
    camera_state.isotherm_lower_threshold = cs.isotherm_lower_threshold;
    camera_state.isotherm_upper_threshold = cs.isotherm_upper_threshold;
    camera_state_callback_container_.notifyCallbackIfChanged(camera_state);

    NightModeSettings night_mode_settings;
    night_mode_settings.payload_index = payload_index;
    night_mode_settings.night_mode = cs.night_mode_settings.night_mode;
    night_mode_settings.is_working = cs.night_mode_settings.is_working;
    night_mode_settings.denoise_level = cs.night_mode_settings.denoise_level;
    night_mode_settings.night_vision_enable = cs.night_mode_settings.night_vision_enable;
    night_mode_settings.infrared_fill_light_enable = cs.night_mode_settings.infrared_fill_light_enable;
    night_mode_settings.night_scene_mode_suggestion = cs.night_mode_settings.night_scene_mode_suggestion;
    night_mode_settings_callback_container_.notifyCallbackIfChanged(night_mode_settings);

    // Watermark
    CameraWatermarkSettings wm;
    wm.payload_index = payload_index;
    wm.global_enable = cs.camera_watermark_settings.global_enable;
    wm.drone_type_enable = cs.camera_watermark_settings.drone_type_enable;
    wm.manufacturer_enable = cs.camera_watermark_settings.manufacturer_enable;
    wm.drone_sn_enable = cs.camera_watermark_settings.drone_sn_enable;
    wm.datetime_enable = cs.camera_watermark_settings.datetime_enable;
    wm.gps_enable = cs.camera_watermark_settings.gps_enable;
    wm.user_custom_string_enable = cs.camera_watermark_settings.user_custom_string_enable;
    wm.user_custom_string = cs.camera_watermark_settings.user_custom_string;
    wm.layout = cs.camera_watermark_settings.layout;
    camera_watermark_settings_callback_container_.notifyCallbackIfChanged(wm);

    // Storage
    CameraStorageInfo storage;
    storage.payload_index = payload_index;
    storage.total = cs.storage.total;
    storage.used = cs.storage.used;
    camera_storage_info_callback_container_.notifyCallbackIfChanged(storage);

    // IrPipState
    CameraIrPipState ir_pip;
    ir_pip.payload_index = payload_index;
    ir_pip.display_mode = cs.ir_pip_state.display_mode;
    ir_pip.enhance_mode = cs.ir_pip_state.enhance_mode;
    ir_pip.ir_mix_rate = cs.ir_pip_state.ir_mix_rate;
    ir_pip.ir_meter_region.top_left_x = cs.ir_pip_state.ir_meter_region.top_left_x;
    ir_pip.ir_meter_region.top_left_y = cs.ir_pip_state.ir_meter_region.top_left_y;
    ir_pip.ir_meter_region.bottom_right_x = cs.ir_pip_state.ir_meter_region.bottom_right_x;
    ir_pip.ir_meter_region.bottom_right_y = cs.ir_pip_state.ir_meter_region.bottom_right_y;
    camera_ir_pip_state_callback_container_.notifyCallbackIfChanged(ir_pip);

    // Isotherm polygon
    CameraIsothermPolygon poly;
    poly.payload_index = payload_index;
    poly.points.reserve(cs.isotherm_rendering_polygon.size());
    for (const auto& p : cs.isotherm_rendering_polygon) {
        CameraIsothermPolygonPoint pt;
        pt.x = p.x;
        pt.y = p.y;
        poly.points.push_back(pt);
    }
    camera_isotherm_polygon_callback_container_.notifyCallbackIfChanged(poly);

    // MediaStorage
    CameraMediaStorage media;
    media.payload_index = payload_index;
    media.photo_storage_settings = msg.msg.media_storage.photo_storage_settings;
    media.video_storage_settings = msg.msg.media_storage.video_storage_settings;
    camera_media_storage_callback_container_.notifyCallbackIfChanged(media);

    // AlgorithmDehazeInfo
    CameraAlgorithmDehazeInfo dehaze;
    dehaze.payload_index = payload_index;
    dehaze.mode = msg.msg.algorithm_dehaze_info.mode;
    dehaze.level = msg.msg.algorithm_dehaze_info.level;
    dehaze.is_working = msg.msg.algorithm_dehaze_info.is_working;
    camera_algorithm_dehaze_info_callback_container_.notifyCallbackIfChanged(dehaze);

    // PreRecordedInfo
    CameraPreRecordedInfo pre_rec;
    pre_rec.payload_index = payload_index;
    pre_rec.status = msg.msg.pre_recorded_info.status;
    pre_rec.pre_recorded_time = msg.msg.pre_recorded_info.pre_recorded_time;
    camera_pre_recorded_info_callback_container_.notifyCallbackIfChanged(pre_rec);

    // Capability
    CameraCapability capability;
    capability.payload_index = payload_index;
    capability.zoom_factor_range = msg.msg.camera_capability.zoom_factor_range;
    capability.ir_factor_range = msg.msg.camera_capability.ir_factor_range;
    capability.base_photo_storage_format.photo_storage_format_num =
        msg.msg.camera_capability.base_photo_storage_format.photo_storage_format_num;
    capability.base_photo_storage_format.photo_storage_formats =
        msg.msg.camera_capability.base_photo_storage_format.photo_storage_formats;
    camera_capability_callback_container_.notifyCallbackIfChanged(capability);

    // SettableCapability
    CameraSettableCapability settable_cap;
    settable_cap.payload_index = payload_index;
    settable_cap.events.reserve(msg.msg.camera_settable_capability.events.size());
    for (const auto& e : msg.msg.camera_settable_capability.events) {
        CameraSettableEvent evt;
        evt.event_id = e.event_id;
        evt.settable = e.settable;
        settable_cap.events.push_back(evt);
    }
    camera_settable_capability_callback_container_.notifyCallbackIfChanged(settable_cap);

    // VisibleCapability
    CameraVisibleCapability visible_cap;
    visible_cap.payload_index = payload_index;
    visible_cap.camera_mode = msg.msg.camera_visible_capability.camera_mode;
    visible_cap.events.reserve(msg.msg.camera_visible_capability.events.size());
    for (const auto& e : msg.msg.camera_visible_capability.events) {
        CameraVisibleCapabilityItem item;
        item.event_id = e.event_id;
        item.visible = e.visible;
        visible_cap.events.push_back(item);
    }
    camera_visible_capability_callback_container_.notifyCallbackIfChanged(visible_cap);
}

void CameraAbs::onReceiveLiveVideoInfoPush(const message_engine::DrcDrcLiveVideoInfoPushNorthbound& msg) {
    LOG_DEBUG("[CameraAbs][%s][%s] onReceiveLiveVideoInfoPush, payload_index: %s", log_tag_.c_str(), payload_index_.c_str(), msg.msg.payload_index.c_str());

    // cache msg
    live_video_info_push_msg_ = msg;

    // 转换字符串为枚举整型（与 `value.h` 中 `CameraLensType` 一致）
    std::vector<int> switchable_video_types;
    for (const auto& type_str : live_video_info_push_msg_.msg.switchable_video_types) {
        switchable_video_types.push_back(static_cast<int>(cameraLensStringToEnum(type_str)));
    }

    LiveVideoInfo info{
        .payload_index = live_video_info_push_msg_.msg.payload_index,
        .video_index = live_video_info_push_msg_.msg.video_index,
        .video_type = static_cast<int>(cameraLensStringToEnum(live_video_info_push_msg_.msg.video_type)),
        .switchable_video_types = switchable_video_types,
    };
    live_video_info_callback_container_.notifyCallbackIfChanged(info);
}

void CameraAbs::onReceiveDrcDroneStatePush(const message_engine::DrcDrcDroneStatePushNorthbound& msg) {
    LOG_DEBUG("[CameraAbs][%s][%s] onReceiveDrcDroneStatePush, mode_code: %d", log_tag_.c_str(), payload_index_.c_str(), msg.msg.mode_code);

    // cache msg
    drc_drone_state_push_msg_ = msg;
}

void CameraAbs::onReceiveCameraPhotoInfoPush(const std::shared_ptr<message_engine::DrcDrcCameraPhotoInfoPushNorthbound>& msg) {
    if (!msg) {
        return;
    }

    const int push_mode = msg->msg.ext.camera_mode;
    LOG_INFO("[CameraAbs][%s][%s] onReceiveCameraPhotoInfoPush, camera_mode: %d, current_step: %d, percent: %d, status: %s, result: %d",
             log_tag_.c_str(), payload_index_.c_str(),
             push_mode, msg->msg.progress.current_step,
             msg->msg.progress.percent, msg->msg.status.c_str(), msg->msg.result);

    // ME 无 payload_index。有本载荷 state 时用 camera_mode 对齐再派发，避免多载荷误贴 index；
    // 尚无 state 时透传但 payload_index 留空（不捏造）。
    const bool has_state = !drc_camera_state_msg_.msg.payload_index.empty();
    const int local_mode = drc_camera_state_msg_.msg.camera_state.camera_mode;
    if (!has_state || local_mode == push_mode) {
        CameraPhotoInfo photo_info;
        photo_info.payload_index = has_state ? payload_index_ : "";
        photo_info.camera_mode = push_mode;
        photo_info.current_step = msg->msg.progress.current_step;
        photo_info.percent = msg->msg.progress.percent;
        photo_info.countdown_time = msg->msg.countdown_time;
        photo_info.result = msg->msg.result;
        photo_info.status = msg->msg.status;
        camera_photo_info_callback_container_.notifyCallbackIfChanged(photo_info);
    }

    if (push_mode == static_cast<int>(CameraMode::CameraMode_PANO_PHOTO)) {
        PanoramaStatus panorama_status;
        panorama_status.current_step = msg->msg.progress.current_step;
        panorama_status.percent = msg->msg.progress.percent;
        panorama_status.status = static_cast<int>(cameraPhotoStateStringToEnum(msg->msg.status));
        panorama_status_callback_container_.notifyCallback(panorama_status);
    }

    if (push_mode == static_cast<int>(CameraMode::CameraMode_INTERVAL_PHOTO)) {
        IntervalTakePhotoStatus interval_take_photo_status;
        interval_take_photo_status.countdown_time = msg->msg.countdown_time;
        LOG_INFO("[CameraAbs][%s][%s] IntervalTakePhotoStatus, countdown_time %d",
                 log_tag_.c_str(), payload_index_.c_str(), interval_take_photo_status.countdown_time);
        interval_take_photo_status_callback_container_.notifyCallback(interval_take_photo_status);
    }
}


void CameraAbs::acquirePayloadControlAuthority(const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] acquirePayloadControlAuthority", log_tag_.c_str(), payload_index_.c_str());
    AcquireCtrlAuthorityRequest request;
    request.drone_sn = drone_sn_;
    request.control_keys = {"payload_" + payload_index_};
    request.gateway_sn = gateway_sn_;
    WeakDummy(p);
    acquire_ctrl_authority(request, [callback, p, this](const AcquireCtrlAuthorityResponse& response, FlightHubError err) {
        WeakDummyReturn(p);
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            LOG_ERROR("[CameraAbs][%s][%s] acquirePayloadControlAuthority, err_code: %d", log_tag_.c_str(), payload_index_.c_str(), static_cast<int>(err.error_code));
        }
        if (callback) {
            callback(err);
        }
    });
}

void CameraAbs::releasePayloadControlAuthority(const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] releasePayloadControlAuthority", log_tag_.c_str(), payload_index_.c_str());
    ReleaseCtrlAuthorityRequest request;
    request.drone_sn = drone_sn_;
    request.control_keys = {"payload_" + payload_index_};
    request.gateway_sn = gateway_sn_;
    WeakDummy(p);
    release_ctrl_authority(request, [callback, p, this](const ReleaseCtrlAuthorityResponse& response, FlightHubError err) {
        WeakDummyReturn(p);
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            LOG_ERROR("[CameraAbs][%s][%s] releasePayloadControlAuthority, err_code: %d", log_tag_.c_str(), payload_index_.c_str(), static_cast<int>(err.error_code));
        }
        if (callback) {
            callback(err);
        }
    });
}

int CameraAbs::listenPayloadControlAuthority(const std::function<void(bool)>& callback) {
    auto listen_id = payload_control_authority_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenPayloadControlAuthority listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenPayloadControlAuthority(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenPayloadControlAuthority listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    payload_control_authority_callback_container_.removeCallback(listen_id);
}

// switch live lens
void CameraAbs::drcSwitchLiveLens(const SwitchLiveLensParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcSwitchLiveLens, lens_type: %s", log_tag_.c_str(), payload_index_.c_str(), cameraLensTypeToString(static_cast<CameraLensType>(params.lens_type)).c_str());
    auto msg = std::make_shared<message_engine::DrcDrcLiveLensChangeSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.video_type = cameraLensTypeToString(static_cast<CameraLensType>(params.lens_type));
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcLiveLensChangeSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcLiveLensChangeNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_DEBUG("[CameraAbs][%s][%s] drcSwitchLiveLens, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetPhotoStorageSettings(const SetPhotoStorageSettingsParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("CameraAbs %s: drcSetPhotoStorageSettings", payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcPhotoStorageSetSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.photo_storage_settings = params.photo_storage_settings;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcPhotoStorageSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcPhotoStorageSetNorthbound>& reply_msg) {
            WeakDummyReturn(p);
            LOG_INFO("CameraAbs %s: drcSetPhotoStorageSettings, error_code: %d, result: %d", payload_index_.c_str(), error_code, reply_msg ? reply_msg->msg.result : -1);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply_msg || reply_msg->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply_msg ? reply_msg->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetVideoStorageSettings(const SetVideoStorageSettingsParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("CameraAbs %s: drcSetVideoStorageSettings", payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcVideoStorageSetSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.video_storage_settings = params.video_storage_settings;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcVideoStorageSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcVideoStorageSetNorthbound>& reply_msg) {
            WeakDummyReturn(p);
            LOG_INFO("CameraAbs %s: drcSetVideoStorageSettings, error_code: %d, result: %d", payload_index_.c_str(), error_code, reply_msg ? reply_msg->msg.result : -1);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply_msg || reply_msg->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply_msg ? reply_msg->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetNightSceneMode(const SetNightSceneModeParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("CameraAbs %s: drcSetNightSceneMode, mode: %d", payload_index_.c_str(), params.mode);
    auto msg = std::make_shared<message_engine::DrcDrcCameraNightModeSetSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.mode = static_cast<int>(params.mode);
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraNightModeSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraNightModeSetNorthbound>& reply_msg) {
            WeakDummyReturn(p);
            LOG_INFO("CameraAbs %s: drcSetNightSceneMode, error_code: %d, result: %d", payload_index_.c_str(), error_code, reply_msg ? reply_msg->msg.result : -1);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply_msg || reply_msg->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply_msg ? reply_msg->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcTakePhoto(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcTakePhoto", log_tag_.c_str(), payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraPhotoTakeSouthbound>();
    msg->msg.payload_index = payload_index_;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraPhotoTakeSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraPhotoTakeNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_DEBUG("[CameraAbs][%s][%s] drcTakePhoto, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

// smart photo
void CameraAbs::drcSmartPhotoTakePhoto(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("CameraAbs %s: drcSmartPhotoTakePhoto", payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraPhotoTakeSouthbound>();
    msg->msg.payload_index = payload_index_;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraPhotoTakeSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraPhotoTakeNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_DEBUG("CameraAbs %s: drcSmartPhotoTakePhoto, error_code: %d, result: %d", payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

int CameraAbs::listenDrcCameraState(const std::function<void(const CameraState& state)> callback) {
    auto listen_id = camera_state_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcCameraState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcCameraState(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcCameraState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    camera_state_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcLiveviewWorldRegion(const std::function<void(const LiveviewWorldRegion& region)> callback) {
    auto listen_id = liveview_world_region_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcLiveviewWorldRegion listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcLiveviewWorldRegion(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcLiveviewWorldRegion listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    liveview_world_region_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraWideLensState(const std::function<void(const CameraWideLensState& state)> callback) {
    auto listen_id = camera_wide_lens_state_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcCameraWideLensState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcCameraWideLensState(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcCameraWideLensState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    camera_wide_lens_state_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraZoomLensState(const std::function<void(const CameraZoomLensState& state)> callback) {
    auto listen_id = camera_zoom_lens_state_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcCameraZoomLensState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcCameraZoomLensState(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcCameraZoomLensState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    camera_zoom_lens_state_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraMeasureTarget(const std::function<void(const CameraMeasureTarget& target)> callback) {
    auto listen_id = camera_measure_target_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcCameraMeasureTarget listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcCameraMeasureTarget(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcCameraMeasureTarget listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    camera_measure_target_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcNightModeSettings(const std::function<void(const NightModeSettings& settings)> callback) {
    auto listen_id = night_mode_settings_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcNightModeSettings listen_id: %d", payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcNightModeSettings(int listen_id) {
    LOG_INFO("CameraAbs %s: cancelListenDrcNightModeSettings listen_id: %d", payload_index_.c_str(), listen_id);
    night_mode_settings_callback_container_.removeCallback(listen_id);
}

// ── B4 drc_camera_state_push 拆分 listen ──

int CameraAbs::listenDrcCameraWatermarkSettings(const std::function<void(const CameraWatermarkSettings&)>& callback) {
    auto id = camera_watermark_settings_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraWatermarkSettings listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraWatermarkSettings(int listen_id) {
    camera_watermark_settings_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraStorageInfo(const std::function<void(const CameraStorageInfo&)>& callback) {
    auto id = camera_storage_info_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraStorageInfo listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraStorageInfo(int listen_id) {
    camera_storage_info_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraIrPipState(const std::function<void(const CameraIrPipState&)>& callback) {
    auto id = camera_ir_pip_state_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraIrPipState listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraIrPipState(int listen_id) {
    camera_ir_pip_state_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraIsothermPolygon(const std::function<void(const CameraIsothermPolygon&)>& callback) {
    auto id = camera_isotherm_polygon_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraIsothermPolygon listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraIsothermPolygon(int listen_id) {
    camera_isotherm_polygon_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraMediaStorage(const std::function<void(const CameraMediaStorage&)>& callback) {
    auto id = camera_media_storage_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraMediaStorage listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraMediaStorage(int listen_id) {
    camera_media_storage_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraAlgorithmDehazeInfo(const std::function<void(const CameraAlgorithmDehazeInfo&)>& callback) {
    auto id = camera_algorithm_dehaze_info_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraAlgorithmDehazeInfo listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraAlgorithmDehazeInfo(int listen_id) {
    camera_algorithm_dehaze_info_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraPreRecordedInfo(const std::function<void(const CameraPreRecordedInfo&)>& callback) {
    auto id = camera_pre_recorded_info_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraPreRecordedInfo listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraPreRecordedInfo(int listen_id) {
    camera_pre_recorded_info_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraCapability(const std::function<void(const CameraCapability&)>& callback) {
    auto id = camera_capability_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraCapability listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraCapability(int listen_id) {
    camera_capability_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraSettableCapability(const std::function<void(const CameraSettableCapability&)>& callback) {
    auto id = camera_settable_capability_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraSettableCapability listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraSettableCapability(int listen_id) {
    camera_settable_capability_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraVisibleCapability(const std::function<void(const CameraVisibleCapability&)>& callback) {
    auto id = camera_visible_capability_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraVisibleCapability listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraVisibleCapability(int listen_id) {
    camera_visible_capability_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcCameraPhotoInfo(const std::function<void(const CameraPhotoInfo&)>& callback) {
    auto id = camera_photo_info_callback_container_.addCallback(callback);
    LOG_INFO("CameraAbs %s: listenDrcCameraPhotoInfo listen_id: %d", payload_index_.c_str(), id);
    return id;
}
void CameraAbs::cancelListenDrcCameraPhotoInfo(int listen_id) {
    camera_photo_info_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcGimbalState(const std::function<void(const GimbalState& state)> callback) {
    auto listen_id = gimbal_state_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcGimbalState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcGimbalState(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcGimbalState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    gimbal_state_callback_container_.removeCallback(listen_id);
}

int CameraAbs::listenDrcLiveVideoInfo(const std::function<void(const LiveVideoInfo& info)>& callback) {
    auto listen_id = live_video_info_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcLiveVideoInfo listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcLiveVideoInfo(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcLiveVideoInfo listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    live_video_info_callback_container_.removeCallback(listen_id);
}

// 红外增益模式
int CameraAbs::listenDrcThermalGainMode(const std::function<void(const ThermalGainModeInfo& info)>& callback) {
    auto listen_id = thermal_gain_mode_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcThermalGainMode listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcThermalGainMode(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcThermalGainMode listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    thermal_gain_mode_callback_container_.removeCallback(listen_id);
}

// 红外镜头完整状态
int CameraAbs::listenDrcCameraIrLensState(const std::function<void(const CameraIrLensState& state)>& callback) {
    auto listen_id = camera_ir_lens_state_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcCameraIrLensState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcCameraIrLensState(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcCameraIrLensState listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    camera_ir_lens_state_callback_container_.removeCallback(listen_id);
}

// 红外测温上行推送
int CameraAbs::listenDrcCameraIrThermometry(const std::function<void(const CameraIrThermometryInfo& info)>& callback) {
    auto listen_id = camera_ir_thermometry_callback_container_.addCallback(callback);
    LOG_INFO("[CameraAbs][%s][%s] listenDrcCameraIrThermometry listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    return listen_id;
}

void CameraAbs::cancelListenDrcCameraIrThermometry(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcCameraIrThermometry listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    camera_ir_thermometry_callback_container_.removeCallback(listen_id);
}

void CameraAbs::drcSetThermalGainMode(const ThermalGainModeSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcSetThermalGainMode, thermal_gain_mode: %d", log_tag_.c_str(), payload_index_.c_str(), params.thermal_gain_mode);

    if (!message_engine_) {
        LOG_ERROR("[CameraAbs][%s][%s] drcSetThermalGainMode, message_engine_ is null", log_tag_.c_str(), payload_index_.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE));
        return;
    }

    // 检查当前是否在红外镜头模式下
    if (cameraLensStringToEnum(live_video_info_push_msg_.msg.video_type) != CameraLensType::CameraLensType_IR) {
        LOG_ERROR("[CameraAbs][%s][%s] drcSetThermalGainMode failed, current video_type is not IR", log_tag_.c_str(), payload_index_.c_str());
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE));
        return;
    }

    auto msg = std::make_shared<message_engine::DrcDrcThermalGainModeSetSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.thermal_gain_mode = params.thermal_gain_mode;

    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcThermalGainModeSetSouthbound(
        msg,
        [this, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcThermalGainModeSetNorthbound>& reply_msg) {
            if (error_code != 0) {
                LOG_ERROR("[CameraAbs][%s][%s] drcSetThermalGainMode failed, error_code: %d", log_tag_.c_str(), payload_index_.c_str(), error_code);
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply_msg || reply_msg->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply_msg ? reply_msg->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        }
    );
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK));
    }
}

// record
void CameraAbs::drcStartRecord(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcStartRecord", log_tag_.c_str(), payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraRecordingStartSouthbound>();
    msg->msg.payload_index = payload_index_;
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraRecordingStartSouthbound(msg,
        [this, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraRecordingStartNorthbound>& msg) {
        LOG_INFO("[CameraAbs][%s][%s] drcStartRecord, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcStopRecord(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcStopRecord", log_tag_.c_str(), payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraRecordingStopSouthbound>();
    msg->msg.payload_index = payload_index_;
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraRecordingStopSouthbound(msg,
        [this, callback](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraRecordingStopNorthbound>& msg) {
        LOG_INFO("[CameraAbs][%s][%s] drcStopRecord, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
}

// zoom at target
void CameraAbs::drcZoomAtTarget(const ZoomAtTargetParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcZoomAtTarget", log_tag_.c_str(), payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraFrameZoomSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.locked = true;
    msg->msg.x = params.x;
    msg->msg.y = params.y;
    msg->msg.width = params.width;
    msg->msg.height = params.height;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraFrameZoomSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraFrameZoomNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("[CameraAbs][%s][%s] drcZoomAtTarget, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}


// look at target
void CameraAbs::lookAtTarget(const LookAtTargetParams& params, const std::function<void(FlightHubError)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] lookAtTarget", log_tag_.c_str(), payload_index_.c_str());
    LookAtTargetRequest request;
    request.data.payload_index = payload_index_;
    request.data.locked = (payload_index_ == "80-0-0" || payload_index_ == "81-0-0"); // TODO: 根据机型是否锁定, 设计模块间通信方案
    request.data.longitude = params.longitude;
    request.data.latitude = params.latitude;
    request.data.height = params.height;
    request.sn = gateway_sn_;
    WeakDummy(p);
    look_at_target(request, [callback, this, p](const NonRealtimeCmdResponse& response, FlightHubError err) {
        WeakDummyReturn(p);
        LOG_INFO("[CameraAbs][%s][%s] lookAtTarget, err_code: %d, http_code: %d, server_code: %d",
                 log_tag_.c_str(), payload_index_.c_str(),
                 static_cast<int>(err.error_code), err.http_code, err.server_code);
        callback(err);
    });
}

// 云台拖拽: drc_camera_screen_drag
void CameraAbs::drcSendCameraScreenDrag(const CameraScreenDragParams& params, const std::function<void(FlightHubError err)>& callback) {
    const std::string& idx = params.payload_index.empty() ? payload_index_ : params.payload_index;
    LOG_INFO("[CameraAbs][%s][%s] drcSendCameraScreenDrag locked=%d pitch_speed=%.2f yaw_speed=%.2f",
             log_tag_.c_str(),
             idx.c_str(), params.locked ? 1 : 0, params.pitch_speed, params.yaw_speed);
    message_engine::DrcDrcCameraScreenDragSouthboundMsg msg_data;
    msg_data.locked = params.locked;
    msg_data.payload_index = idx;
    msg_data.pitch_speed = params.pitch_speed;
    msg_data.yaw_speed = params.yaw_speed;
    auto msg = std::make_shared<message_engine::DrcDrcCameraScreenDragSouthbound>(msg_data);
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraScreenDragSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraScreenDragNorthbound>& reply) {
            WeakDummyReturn(p);
            LOG_INFO("[CameraAbs][%s][%s] drcSendCameraScreenDrag, error_code: %d, result: %d",
                     log_tag_.c_str(),
                     payload_index_.c_str(), error_code, reply ? reply->msg.result : -1);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        LOG_ERROR("[CameraAbs][%s][%s] drcSendCameraScreenDrag Send failed, err: %d", log_tag_.c_str(), payload_index_.c_str(), static_cast<int>(err));
        callback(FlightHubError::from_drc(err));
    }
}

// set focal length;
void CameraAbs::drcSetFocalLength(const CameraFocalLengthParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcSetFocalLength", log_tag_.c_str(), payload_index_.c_str());

    auto msg = std::make_shared<message_engine::DrcDrcCameraFocalLengthSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_type));
    msg->msg.zoom_factor = params.zoom_factor;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraFocalLengthSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraFocalLengthSetNorthbound>& msg) {
            WeakDummyReturn(p);
            LOG_INFO("[CameraAbs][%s][%s] drcSetFocalLength, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!msg || msg->msg.result != 0) {
                callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}


// set linkage zoom;
void CameraAbs::drcSetLinkageZoom(const LinkageZoomParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("CameraAbs %s: drcSetLinkageZoom, state: %d", payload_index_.c_str(), params.state);

    auto msg = std::make_shared<message_engine::DrcDrcLinkageZoomSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.state = params.state;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcLinkageZoomSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcLinkageZoomSetNorthbound>& msg) {
            WeakDummyReturn(p);
            LOG_INFO("CameraAbs %s: drcSetLinkageZoom, error_code: %d, result: %d", payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!msg || msg->msg.result != 0) {
                callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}


// 负载控制: 重置云台(method: gimbal_reset);
void CameraAbs::drcResetGimbal(const GimbalResetParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcResetGimbal params: { payload_index: \"%s\", reset_mode: %d }",
        log_tag_.c_str(),
        payload_index_.c_str(), params.payload_index.c_str(), params.reset_mode
    );

    // todo: 目前 sdk 设计上没有一套好的机制(设备固件能力)，用来判别 设置操作是否 available;

    auto msg = std::make_shared<message_engine::DrcDrcGimbalResetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.reset_mode = params.reset_mode;

    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcGimbalResetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcGimbalResetNorthbound>& msg) {
            WeakDummyReturn(p);
            LOG_INFO("[CameraAbs][%s][%s] drcResetGimbal, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!msg || msg->msg.result != 0) {
                callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
    });

    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}


// switch camera mode
void CameraAbs::drcSwitchCameraMode(const SwitchCameraModeParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcSwitchCameraMode", log_tag_.c_str(), payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraModeSwitchSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.camera_mode = static_cast<int>(params.camera_mode);
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraModeSwitchSouthbound(msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraModeSwitchNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("[CameraAbs][%s][%s] drcSwitchCameraMode, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

int CameraAbs::listenDrcCameraModeAvailableRange(const std::function<void(const std::vector<int>&)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] listenDrcCameraModeAvailableRange", log_tag_.c_str(), payload_index_.c_str());
    return camera_mode_available_range_callback_container_.addCallback(callback);
}

void CameraAbs::cancelListenDrcCameraModeAvailableRange(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcCameraModeAvailableRange listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    camera_mode_available_range_callback_container_.removeCallback(listen_id);
}


// panorama
void CameraAbs::drcStartPanorama(const PanoramaParam& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcStartPanorama", log_tag_.c_str(), payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraPhotoTakeSouthbound>();
    msg->msg.payload_index = params.payload_index;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraPhotoTakeSouthbound(msg,
        [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraPhotoTakeNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("[CameraAbs][%s][%s] drcStartPanorama, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcStopPanorama(const std::function<void(FlightHubError err)>& callback) {
    // 全景停止与「停止拍照」一致：统一走 drcStopTakePhoto（drc_camera_photo_stop），避免重复实现（HYAPP-167698）
    LOG_INFO("[CameraAbs][%s][%s] drcStopPanorama -> drcStopTakePhoto", log_tag_.c_str(), payload_index_.c_str());
    drcStopTakePhoto(callback);
}

int CameraAbs::listenDrcPanoramaStatus(const std::function<void(const PanoramaStatus& status)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] listenDrcPanoramaStatus", log_tag_.c_str(), payload_index_.c_str());
    return panorama_status_callback_container_.addCallback(callback);
}

void CameraAbs::cancelListenDrcPanoramaStatus(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcPanoramaStatus listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    panorama_status_callback_container_.removeCallback(listen_id);
}

void CameraAbs::drcSetPhotoSize(const PhotoSizeParam& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("CameraAbs %s: drcSetPhotoSize", payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraPhotoSizeSetSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.photo_size = params.photo_size;
    msg->msg.camera_type = params.camera_type;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraPhotoSizeSetSouthbound(msg,
        [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraPhotoSizeSetNorthbound>& msg) {
            WeakDummyReturn(p);
            LOG_INFO("CameraAbs %s: drcSetPhotoSize, error_code: %d, result: %d", payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!msg || msg->msg.result != 0) {
                callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
}

// 定时拍
void CameraAbs::drcSetIntervalTakePhoto(const IntervalTakePhotoSetParam& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcSetIntervalTakePhoto, interval = %lf", log_tag_.c_str(), payload_index_.c_str(), params.interval);
    auto msg = std::make_shared<message_engine::DrcDrcIntervalPhotoSetSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.interval = params.interval;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcIntervalPhotoSetSouthbound(msg,
        [callback, this, p, params](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcIntervalPhotoSetNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("[CameraAbs][%s][%s] drcSetIntervalTakePhoto, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcIntervalTakePhoto(const IntervalTakePhotoParam& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcIntervalTakePhoto", log_tag_.c_str(), payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraPhotoTakeSouthbound>();
    msg->msg.payload_index = params.payload_index;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraPhotoTakeSouthbound(msg,
        [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraPhotoTakeNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("[CameraAbs][%s][%s] drcIntervalTakePhoto, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcStopTakePhoto(const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] drcStopTakePhoto", log_tag_.c_str(), payload_index_.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraPhotoStopSouthbound>();
    msg->msg.payload_index = payload_index_;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraPhotoStopSouthbound(msg,
        [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraPhotoStopNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("[CameraAbs][%s][%s] drcStopTakePhoto, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

int CameraAbs::listenDrcIntervalTakePhotoStatus(const std::function<void(const IntervalTakePhotoStatus& status)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] listenDrcIntervalTakePhotoStatus", log_tag_.c_str(), payload_index_.c_str());
    return interval_take_photo_status_callback_container_.addCallback(callback);
}

void CameraAbs::cancelListenDrcIntervalTakePhotoStatus(int listen_id) {
    LOG_INFO("[CameraAbs][%s][%s] cancelListenDrcIntervalTakePhotoStatus listen_id: %d", log_tag_.c_str(), payload_index_.c_str(), listen_id);
    interval_take_photo_status_callback_container_.removeCallback(listen_id);
}

// 双击AIM
void CameraAbs::drcCameraAimAtTarget(const CameraAimParam& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[CameraAbs][%s][%s] CameraAimAtTarget, camera_type = %s", log_tag_.c_str(), payload_index_.c_str(), params.camera_type.c_str());
    auto msg = std::make_shared<message_engine::DrcDrcCameraAimSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.camera_type = params.camera_type;
    msg->msg.locked = params.locked;
    msg->msg.x = params.x;
    msg->msg.y = params.y;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraAimSouthbound(msg,
        [callback, this, p, params](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraAimNorthbound>& msg) {
        WeakDummyReturn(p);
        LOG_INFO("[CameraAbs][%s][%s] drcCameraAimAtTarget, error_code: %d, result: %d", log_tag_.c_str(), payload_index_.c_str(), error_code, msg ? msg->msg.result : -1);
        if (error_code != 0) {
            callback(FlightHubError::from_drc(error_code));
        } else if (!msg || msg->msg.result != 0) {
            callback(FlightHubError::from_drc(0, msg ? msg->msg.result : -1));
        } else {
            callback(FlightHubError::success());
        }
    });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

// ── Camera Advanced Params ──

void CameraAbs::drcSetCameraApertureValue(const CameraApertureValueSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraApertureValueSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.aperture_value = params.aperture_value;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraApertureValueSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraApertureValueSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraExposureMode(const CameraExposureModeSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraExposureModeSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.exposure_mode = params.exposure_mode;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraExposureModeSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraExposureModeSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraExposureValue(const CameraExposureSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraExposureSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.exposure_value = params.exposure_value;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraExposureSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraExposureSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraFocusMode(const CameraFocusModeSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraFocusModeSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.focus_mode = params.focus_mode;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraFocusModeSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraFocusModeSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraFocusValue(const CameraFocusValueSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraFocusValueSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.focus_value = params.focus_value;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraFocusValueSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraFocusValueSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraIso(const CameraIsoSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraIsoSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.iso_value = params.iso_value;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraIsoSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraIsoSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraMechanicalShutter(const CameraMechanicalShutterSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraMechanicalShutterSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.mechanical_shutter_state = params.state;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraMechanicalShutterSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraMechanicalShutterSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcCameraPointFocusAction(const CameraPointFocusActionParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraPointFocusActionSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.x = params.x;
    msg->msg.y = params.y;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraPointFocusActionSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraPointFocusActionNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraShutter(const CameraShutterSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraShutterSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.shutter_value = params.shutter_value;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraShutterSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraShutterSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraWatermark(const CameraWatermarkParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraWatermarkSetSouthbound>();
    msg->msg.payload_index = params.payload_index;
    msg->msg.global_enable = params.global_enable;
    msg->msg.drone_type_enable = params.drone_type_enable;
    msg->msg.manufacturer_enable = params.manufacturer_enable;
    msg->msg.drone_sn_enable = params.drone_sn_enable;
    msg->msg.datetime_enable = params.datetime_enable;
    msg->msg.gps_enable = params.gps_enable;
    msg->msg.user_custom_string_enable = params.user_custom_string_enable;
    msg->msg.user_custom_string = params.user_custom_string;
    msg->msg.layout = params.layout;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraWatermarkSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraWatermarkSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraDewarping(const CameraDewarpingSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraDewarpingSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.dewarping_state = params.dewarping_state;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraDewarpingSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraDewarpingSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

// ── Screen/Gimbal Advanced ──

void CameraAbs::drcSetCameraScreenSplit(const CameraScreenSplitParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraScreenSplitSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.enable = params.enable;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraScreenSplitSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraScreenSplitNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcCleanFog(const CleanFogParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCleanFogSouthbound>();
    msg->msg.payload_index = payload_index_;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCleanFogSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCleanFogNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

// ── Thermal/IR ──

void CameraAbs::drcSetThermalCurrentPalette(const ThermalCurrentPaletteSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcThermalCurrentPaletteSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.palette_style = params.palette_style;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcThermalCurrentPaletteSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcThermalCurrentPaletteSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetThermalSuperResolutionEnable(const ThermalSuperResolutionEnableParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcThermalSuperResolutionEnableSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.status = params.enable;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcThermalSuperResolutionEnableSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcThermalSuperResolutionEnableNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIrMeteringMode(const IrMeteringModeSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcIrMeteringModeSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.mode = params.mode;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcIrMeteringModeSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcIrMeteringModeSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIrMeteringPoint(const IrMeteringPointSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcIrMeteringPointSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.x = params.x;
    msg->msg.y = params.y;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcIrMeteringPointSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcIrMeteringPointSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIrMeteringArea(const IrMeteringAreaSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcIrMeteringAreaSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.x = params.x;
    msg->msg.y = params.y;
    msg->msg.width = params.width;
    msg->msg.height = params.height;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcIrMeteringAreaSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcIrMeteringAreaSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIrDisplayMode(const SetIrDisplayModeParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcSetIrDisplayModeSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.display_mode = params.display_mode;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSetIrDisplayModeSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSetIrDisplayModeNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIrEnhanceMode(const SetIrEnhanceModeParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcSetIrEnhanceModeSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.enhance_mode = params.enhance_mode;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSetIrEnhanceModeSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSetIrEnhanceModeNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIrMixRate(const SetIrMixRateParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcSetIrMixRateSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.ir_mix_rate = params.ir_mix_rate;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSetIrMixRateSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSetIrMixRateNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIsothermEnable(const SetIsothermEnableParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcSetIsothermEnableSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.enable = params.enable;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSetIsothermEnableSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSetIsothermEnableNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIsothermColorRenderingType(const SetIsothermColorRenderingTypeParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcSetIsothermColorRenderingTypeSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.type = params.type;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSetIsothermColorRenderingTypeSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSetIsothermColorRenderingTypeNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIsothermLowerUpperRange(const SetIsothermLowerUpperRangeParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcSetIsothermLowerUpperRangeSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.lower = params.lower;
    msg->msg.upper = params.upper;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSetIsothermLowerUpperRangeSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSetIsothermLowerUpperRangeNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIsothermRenderingPolygon(const SetIsothermRenderingPolygonParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcSetIsothermRenderingPolygonSouthbound>();
    msg->msg.payload_index = payload_index_;
    std::vector<message_engine::DrcSetIsothermRenderingPolygonPoint> points;
    for (const auto& pt : params.polygon) {
        message_engine::DrcSetIsothermRenderingPolygonPoint point;
        point.x = pt.x;
        point.y = pt.y;
        points.push_back(point);
    }
    msg->msg.polygon = points;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSetIsothermRenderingPolygonSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSetIsothermRenderingPolygonNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetIsothermRoiBasedRenderingEnable(const SetIsothermRoiBasedRenderingEnableParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcSetIsothermRoiBasedRenderingEnableSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.enable = params.enable;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcSetIsothermRoiBasedRenderingEnableSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcSetIsothermRoiBasedRenderingEnableNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetInfraredFillLightEnable(const InfraredFillLightEnableParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcInfraredFillLightEnableSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.enable = params.enable;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcInfraredFillLightEnableSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcInfraredFillLightEnableNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraNightVisionEnable(const CameraNightVisionEnableParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraNightVisionEnableSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.enable = params.enable;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraNightVisionEnableSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraNightVisionEnableNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcResetCameraSettings(const CameraSettingsResetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraSettingsResetSouthbound>();
    msg->msg.payload_index = payload_index_;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraSettingsResetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraSettingsResetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

// ── Other Camera ──

void CameraAbs::drcSetCameraDenoiseLevelSet(const CameraDenoiseLevelSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraDenoiseLevelSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.level = params.level;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraDenoiseLevelSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraDenoiseLevelSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetVideoResolution(const VideoResolutionSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcVideoResolutionSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.camera_type = cameraLensTypeToString(static_cast<CameraLensType>(params.camera_lens_type));
    msg->msg.video_resolution = params.video_resolution;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcVideoResolutionSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcVideoResolutionSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCameraPhotoFormat(const CameraPhotoFormatSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCameraPhotoFormatSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.photo_format = params.photo_format;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCameraPhotoFormatSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCameraPhotoFormatSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetPreRecordedStatus(const PreRecordedStatusSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcPreRecordedStatusSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.status = params.status;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcPreRecordedStatusSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcPreRecordedStatusSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetPreRecordedTime(const PreRecordedTimeSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcPreRecordedTimeSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.pre_recorded_time = params.pre_recorded_time;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcPreRecordedTimeSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcPreRecordedTimeSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetCountDownAction(int count_down_type, int action_type, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcCountDownActionSetSouthbound>();
    msg->msg.count_down_type = count_down_type;
    msg->msg.action_type = action_type;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcCountDownActionSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcCountDownActionSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetAssistantVideoMode(int mode, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcAssistantVideoModeSetSouthbound>();
    msg->msg.mode = mode;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAssistantVideoModeSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAssistantVideoModeSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetAssistantVideoSwitch(int sw, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcAssistantVideoSwitchSetSouthbound>();
    msg->msg.sw = sw;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAssistantVideoSwitchSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAssistantVideoSwitchSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetAlgorithmDehazeMode(const AlgorithmDehazeModeSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcAlgorithmDehazeModeSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.mode = params.mode;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAlgorithmDehazeModeSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAlgorithmDehazeModeSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

void CameraAbs::drcSetAlgorithmDehazeLevel(const AlgorithmDehazeLevelSetParams& params, const std::function<void(FlightHubError err)>& callback) {
    auto msg = std::make_shared<message_engine::DrcDrcAlgorithmDehazeLevelSetSouthbound>();
    msg->msg.payload_index = payload_index_;
    msg->msg.level = params.level;
    WeakDummy(p);
    auto err = message_engine_->getCloudMessageManager()->SendDrcDrcAlgorithmDehazeLevelSetSouthbound(
        msg, [callback, this, p](int error_code, const std::shared_ptr<message_engine::DrcReplyDrcAlgorithmDehazeLevelSetNorthbound>& reply) {
            WeakDummyReturn(p);
            if (error_code != 0) {
                callback(FlightHubError::from_drc(error_code));
            } else if (!reply || reply->msg.result != 0) {
                callback(FlightHubError::from_drc(0, reply ? reply->msg.result : -1));
            } else {
                callback(FlightHubError::success());
            }
        });
    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        callback(FlightHubError::from_drc(err));
    }
}

}