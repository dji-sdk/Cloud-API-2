// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drone_osd_module_impl.h"
#include "manager/flight_ctrl_mgr/flight_ctrl_mgr.h"
#include "manager/payload_ctrl_mgr/payload_ctrl_mgr.h"
#include <base/common/logger.h>
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

using namespace flight_hub_sdk;
using namespace notification_center;

DroneOsdModuleImpl::DroneOsdModuleImpl(
    const std::string& drone_sn,
    const std::string& gateway_sn,
    const std::string& log_tag,
    std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr,
    std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr)
    :
      drone_sn_(drone_sn),
      gateway_sn_(gateway_sn),
      log_tag_(log_tag),
      flight_ctrl_mgr_(std::move(flight_ctrl_mgr)),
      payload_ctrl_mgr_(std::move(payload_ctrl_mgr)) {
    registerNotificationCenterListeners();
    bindPayloadsListCallback();
}

DroneOsdModuleImpl::~DroneOsdModuleImpl() {
    if (aircraft_battery_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_battery_info_listen_id_);
        aircraft_battery_info_listen_id_ = -1;
    }
    if (aircraft_attitude_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_attitude_listen_id_);
        aircraft_attitude_listen_id_ = -1;
    }
    if (aircraft_location_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_location_listen_id_);
        aircraft_location_listen_id_ = -1;
    }
    if (aircraft_mode_code_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_mode_code_listen_id_);
        aircraft_mode_code_listen_id_ = -1;
    }
    if (aircraft_wind_speed_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_wind_speed_listen_id_);
        aircraft_wind_speed_listen_id_ = -1;
    }
    if (flight_task_exception_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(flight_task_exception_listen_id_);
        flight_task_exception_listen_id_ = -1;
    }
    if (aircraft_control_code_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_control_code_listen_id_);
        aircraft_control_code_listen_id_ = -1;
    }
    if (aircraft_speed_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_speed_listen_id_);
        aircraft_speed_listen_id_ = -1;
    }
    if (aircraft_payloads_camera_liveview_world_region_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_payloads_camera_liveview_world_region_listen_id_);
        aircraft_payloads_camera_liveview_world_region_listen_id_ = -1;
    }
    if (aircraft_payloads_gimbal_attitude_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_payloads_gimbal_attitude_listen_id_);
        aircraft_payloads_gimbal_attitude_listen_id_ = -1;
    }
    if (control_rights_cloud_control_status_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(control_rights_cloud_control_status_listen_id_);
        control_rights_cloud_control_status_listen_id_ = -1;
    }
    if (control_rights_device_control_status_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(control_rights_device_control_status_listen_id_);
        control_rights_device_control_status_listen_id_ = -1;
    }
    if (flight_tasks_media_file_display_available_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(flight_tasks_media_file_display_available_listen_id_);
        flight_tasks_media_file_display_available_listen_id_ = -1;
    }
    if (aircraft_payloads_osd_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_payloads_osd_info_listen_id_);
        aircraft_payloads_osd_info_listen_id_ = -1;
    }
    if (aircraft_gear_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_gear_listen_id_);
        aircraft_gear_listen_id_ = -1;
    }
    if (aircraft_home_distance_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_home_distance_listen_id_);
        aircraft_home_distance_listen_id_ = -1;
    }
    if (aircraft_position_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_position_state_listen_id_);
        aircraft_position_state_listen_id_ = -1;
    }
    if (aircraft_control_source_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_control_source_listen_id_);
        aircraft_control_source_listen_id_ = -1;
    }
    if (aircraft_limits_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_limits_info_listen_id_);
        aircraft_limits_info_listen_id_ = -1;
    }
    if (aircraft_battery_warning_thresholds_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_battery_warning_thresholds_listen_id_);
        aircraft_battery_warning_thresholds_listen_id_ = -1;
    }
    if (aircraft_firmware_status_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_firmware_status_listen_id_);
        aircraft_firmware_status_listen_id_ = -1;
    }
    if (aircraft_total_flight_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_total_flight_info_listen_id_);
        aircraft_total_flight_info_listen_id_ = -1;
    }
    if (aircraft_activation_time_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_activation_time_listen_id_);
        aircraft_activation_time_listen_id_ = -1;
    }
    if (aircraft_maintain_status_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_maintain_status_info_listen_id_);
        aircraft_maintain_status_info_listen_id_ = -1;
    }
    if (aircraft_night_lights_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_night_lights_state_listen_id_);
        aircraft_night_lights_state_listen_id_ = -1;
    }
    if (aircraft_obstacle_avoidance_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_obstacle_avoidance_state_listen_id_);
        aircraft_obstacle_avoidance_state_listen_id_ = -1;
    }
    if (aircraft_storage_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_storage_info_listen_id_);
        aircraft_storage_info_listen_id_ = -1;
    }
    if (aircraft_track_id_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_track_id_listen_id_);
        aircraft_track_id_listen_id_ = -1;
    }
    if (aircraft_compliance_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_compliance_info_listen_id_);
        aircraft_compliance_info_listen_id_ = -1;
    }
    if (aircraft_payloads_cameras_list_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_payloads_cameras_list_listen_id_);
        aircraft_payloads_cameras_list_listen_id_ = -1;
    }
    if (aircraft_rc_lost_action_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_rc_lost_action_listen_id_);
        aircraft_rc_lost_action_listen_id_ = -1;
    }
    if (aircraft_rth_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_rth_info_listen_id_);
        aircraft_rth_info_listen_id_ = -1;
    }
    if (aircraft_home_geographical_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_home_geographical_info_listen_id_);
        aircraft_home_geographical_info_listen_id_ = -1;
    }
    if (aircraft_current_rth_mode_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_current_rth_mode_listen_id_);
        aircraft_current_rth_mode_listen_id_ = -1;
    }
    if (drone_mode_code_reason_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(drone_mode_code_reason_listen_id_);
        drone_mode_code_reason_listen_id_ = -1;
    }
    if (drone_dongle_infos_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(drone_dongle_infos_listen_id_);
        drone_dongle_infos_listen_id_ = -1;
    }
    if (drone_flysafe_database_version_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(drone_flysafe_database_version_listen_id_);
        drone_flysafe_database_version_listen_id_ = -1;
    }
    if (drone_wpmz_version_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(drone_wpmz_version_listen_id_);
        drone_wpmz_version_listen_id_ = -1;
    }
}

void DroneOsdModuleImpl::bindPayloadsListCallback() {
    if (!payload_ctrl_mgr_) {
        return;
    }
    WeakDummy(weak_ptr);
    payload_ctrl_mgr_->setAircraftPayloadsListCallback([this, weak_ptr](const AircraftPayloadsList& payloads_list) {
        WeakDummyReturn(weak_ptr);
        LOG_DEBUG("[DroneOsdModuleImpl][%s] listenAircraftPayloadsList, payloads count: %zu", log_tag_.c_str(), payloads_list.payloads_list.size());
        aircraft_payloads_list_callbacks_.notifyCallbackIfChanged(payloads_list);
    });
}

void DroneOsdModuleImpl::registerNotificationCenterListeners() {
    WeakDummy(p);

    aircraft_battery_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftBatteryInfo(
        [this, p](const notification_center::AircraftBatteryInfo& msg)-> void {
            WeakDummyReturn(p);
            AircraftBatteryInfo battery_info;
            for (const auto& item : msg.batteries) {
                BatteryInfo bat_info;
                bat_info.capacity_percent = item.capacity_percent;
                bat_info.firmware_version = item.firmware_version;
                bat_info.high_voltage_storage_days = item.high_voltage_storage_days;
                bat_info.index = item.index;
                bat_info.loop_times = item.loop_times;
                bat_info.sn = item.sn;
                bat_info.sub_type = item.sub_type;
                bat_info.temperature = item.temperature;
                bat_info.type = item.type;
                bat_info.voltage = item.voltage;
                battery_info.batteries.emplace_back(bat_info);
            }
            battery_info.capacity_percent = msg.capacity_percent;
            battery_info.landing_power = msg.landing_power;
            battery_info.remain_flight_time = msg.remain_flight_time;
            battery_info.return_home_power = msg.return_home_power;
            LOG_DEBUG("[DroneOsdModuleImpl][%s] ListenAircraftBatteryInfo, capacity_percent: %d", log_tag_.c_str(), battery_info.capacity_percent);
            aircraft_battery_info_callbacks_.notifyCallbackIfChanged(battery_info);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneOsdModuleImpl][%s] ListenAircraftBatteryInfo, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    aircraft_attitude_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftAttitude(
        [this, p](const notification_center::AircraftAttitude& msg)-> void {
            WeakDummyReturn(p);
            AircraftAttitude attitude;
            attitude.attitude_head = msg.attitude_head;
            attitude.attitude_pitch = msg.attitude_pitch;
            attitude.attitude_roll = msg.attitude_roll;
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenAircraftAttitude, head: %.2f, pitch: %.2f, roll: %.2f", log_tag_.c_str(), attitude.attitude_head, attitude.attitude_pitch, attitude.attitude_roll);
            aircraft_attitude_callbacks_.notifyCallbackIfChanged(attitude);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_INFO("[DroneOsdModuleImpl][%s] listenAircraftAttitude, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);
    
    aircraft_location_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftLocation(
        [this, p](const notification_center::AircraftLocation& msg)-> void {
            WeakDummyReturn(p);
            AircraftLocation location;
            location.height = msg.height;
            location.elevation = msg.elevation;
            location.longitude = msg.longitude;
            location.latitude = msg.latitude;
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenAircraftLocation, lon: %.2f, lat: %.2f, h: %.2f, ele: %.2f", log_tag_.c_str(), location.longitude, location.latitude, location.height, location.elevation);
            aircraft_location_callbacks_.notifyCallbackIfChanged(location);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_INFO("[DroneOsdModuleImpl][%s] listenAircraftLocation, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    aircraft_mode_code_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftModeCode(
        [this, p](const notification_center::AircraftModeCode& msg)-> void {
            WeakDummyReturn(p);
            AircraftModeCode mode_code;
            mode_code.mode_code = msg.mode_code;
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenAircraftModeCode, mode_code: %d", log_tag_.c_str(), mode_code.mode_code);
            aircraft_mode_code_callbacks_.notifyCallbackIfChanged(mode_code);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_INFO("[DroneOsdModuleImpl][%s] listenAircraftModeCode, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    aircraft_wind_speed_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftWindSpeed(
        [this, p](const notification_center::AircraftWindSpeed& msg)-> void {
            WeakDummyReturn(p);
            AircraftWindSpeed wind_speed;
            wind_speed.wind_direction = msg.wind_direction;
            wind_speed.wind_speed = msg.wind_speed;
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenAircraftWindSpeed, dir: %d, spd: %.2f", log_tag_.c_str(), wind_speed.wind_direction, wind_speed.wind_speed);
            aircraft_wind_speed_callbacks_.notifyCallbackIfChanged(wind_speed);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_INFO("[DroneOsdModuleImpl][%s] listenAircraftWindSpeed, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    flight_task_exception_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenFlightTasksException(
        [this, p](const notification_center::FlightTaskException& msg)-> void {
            WeakDummyReturn(p);
            FlightTaskException flight_task_exception;
            flight_task_exception.uuid = msg.uuid;
            flight_task_exception.name = msg.name;
            flight_task_exception.sn = msg.sn;
            flight_task_exception.task_type = msg.task_type;
            flight_task_exception.status = msg.status;
            flight_task_exception.progress = msg.progress;
            flight_task_exception.timestamp = msg.timestamp;
            
            // 转换异常详情
            for (const auto& detail : msg.exceptions) {
                FlightTaskExceptionDetail exception_detail;
                exception_detail.code = detail.code;
                exception_detail.message = detail.message;
                exception_detail.task_id = detail.task_id;
                exception_detail.happen_at = detail.happen_at;
                exception_detail.sn = detail.sn;
                flight_task_exception.exceptions.push_back(exception_detail);
            }
            
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenFlightTasksException, uuid: %s, name: %s, sn: %s, task_type: %d, status: %d, progress: %d, exceptions_count: %zu, timestamp: %ld", log_tag_.c_str(), 
                flight_task_exception.uuid.c_str(), flight_task_exception.name.c_str(), flight_task_exception.sn.c_str(), 
                flight_task_exception.task_type, flight_task_exception.status, flight_task_exception.progress, 
                flight_task_exception.exceptions.size(), flight_task_exception.timestamp);
            flight_task_exception_callbacks_.notifyCallbackIfChanged(flight_task_exception);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneOsdModuleImpl][%s] listenFlightTasksException, error: %d", log_tag_.c_str(), error_code);
            }
        }, gateway_sn_, notification_center::NotificationFrequency_OnChanged);
    if (flight_task_exception_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DroneOsdModuleImpl][%s] ListenFlightTasksException, error: %d", log_tag_.c_str(), flight_task_exception_listen_id_);
    }

    // 飞机控制码监听
    aircraft_control_code_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftControlCode(
        [this, p](const notification_center::AircraftControlCode& msg)-> void {
            WeakDummyReturn(p);
            AircraftControlCode control_code;
            control_code.control_mode = msg.control_mode;
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenAircraftControlCode, control_mode: %d", log_tag_.c_str(), control_code.control_mode);
            aircraft_control_code_callbacks_.notifyCallbackIfChanged(control_code);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneOsdModuleImpl][%s] listenAircraftControlCode, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    // 飞机速度监听
    aircraft_speed_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftSpeed(
        [this, p](const notification_center::AircraftSpeed& msg)-> void {
            WeakDummyReturn(p);
            AircraftSpeed speed;
            speed.horizontal_speed = msg.horizontal_speed;
            speed.vertical_speed = msg.vertical_speed;
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenAircraftSpeed, horizontal: %.2f, vertical: %.2f", log_tag_.c_str(), speed.horizontal_speed, speed.vertical_speed);
            aircraft_speed_callbacks_.notifyCallbackIfChanged(speed);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneOsdModuleImpl][%s] listenAircraftSpeed, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    // 载荷相机实时视图世界区域监听
    aircraft_payloads_camera_liveview_world_region_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftPayloadsCameraLiveviewWorldRegion(
        [this, p](const notification_center::AircraftPayloadsCameraLiveviewWorldRegion& msg)-> void {
            WeakDummyReturn(p);
            AircraftPayloadsCameraLiveviewWorldRegion region;
            for (const auto& [payload_index, camera_region] : msg.payloads_list) {
                LiveviewWorldRegion liveview_region;
                liveview_region.payload_index = payload_index;
                liveview_region.bottom = camera_region.bottom;
                liveview_region.left = camera_region.left;
                liveview_region.right = camera_region.right;
                liveview_region.top = camera_region.top;
                region.payloads_list[payload_index] = liveview_region;
            }
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenAircraftPayloadsCameraLiveviewWorldRegion, payloads count: %zu", log_tag_.c_str(), region.payloads_list.size());
            aircraft_payloads_camera_liveview_world_region_callbacks_.notifyCallbackIfChanged(region);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneOsdModuleImpl][%s] listenAircraftPayloadsCameraLiveviewWorldRegion, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    // 载荷云台姿态监听;
    // 底层跨平台的 c++ 代码, 从跨平台/跨语言的架构上考虑, 封装时应该往 C 接口靠拢(C 拥有更好的跨平台特性支持!), 需要避免使用高级 C++ 特性;
    aircraft_payloads_gimbal_attitude_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftPayloadsGimbalAttitude(
        [this, p](const notification_center::AircraftPayloadsGimbalAttitude& msg)-> void {
            WeakDummyReturn(p);

            // ! 底层 NC-SDK 和后端设计协议时，也避免使用 map 直接映射(改用 vector支持对象数组);
            flight_hub_sdk::AircraftPayloadsGimbalAttitude attitudes;
            for (const auto& [payload_index, gimbal_attitude] : msg.payloads_gimbal_attitude) {
                flight_hub_sdk::AircraftPayloadGimbalAttitude payload_attitude;

                // 组装数据;
                payload_attitude.payload_index = payload_index;
                payload_attitude.gimbal_pitch = gimbal_attitude.gimbal_pitch;
                payload_attitude.gimbal_roll = gimbal_attitude.gimbal_roll;
                payload_attitude.gimbal_yaw = gimbal_attitude.gimbal_yaw;

                // 缓存数据;
                attitudes.payloads_gimbal_attitude.emplace_back(payload_attitude);
            }
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenAircraftPayloadsGimbalAttitude, payloads count: %zu", log_tag_.c_str(), attitudes.payloads_gimbal_attitude.size());
            aircraft_payloads_gimbal_attitude_callbacks_.notifyCallbackIfChanged(attitudes);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneOsdModuleImpl][%s] listenAircraftPayloadsGimbalAttitude, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    // 载荷列表监听已统一到 PayloadCtrlMgr 中处理
    // PayloadCtrlMgr 会先更新内部状态，再通过回调通知用户，确保状态一致性
    // 监听在 initPayloadCtrlMgr() 中通过注册回调的方式设置

    // 控制权限云控制状态监听
    control_rights_cloud_control_status_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenControlRightsCloudControlStatus(
        [this, p](const notification_center::ControlRightsCloudControlStatus& msg)-> void {
            WeakDummyReturn(p);
            ControlRightsCloudControlStatus status;
            status.sn = msg.sn;
            status.reason = msg.reason;
            for (const auto& control : msg.controls) {
                ControlRightsCloudControlStatusItem item;
                item.type = control.type;
                item.key = control.key;
                item.user_id = control.user_id;
                item.is_locked = control.is_locked;
                item.version = control.version;
                status.controls.push_back(item);
            }
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenControlRightsCloudControlStatus, sn: %s, controls count: %zu", log_tag_.c_str(), status.sn.c_str(), status.controls.size());
            control_rights_cloud_control_status_callbacks_.notifyCallbackIfChanged(status);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneOsdModuleImpl][%s] listenControlRightsCloudControlStatus, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    // 控制权限设备控制状态监听
    control_rights_device_control_status_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenControlRightsDeviceControlStatus(
        [this, p](const notification_center::ControlRightsDeviceControlStatus& msg)-> void {
            WeakDummyReturn(p);
            ControlRightsDeviceControlStatus status;
            status.sn = msg.sn;
            status.reason = msg.reason;
            for (const auto& control : msg.controls) {
                ControlRightsDeviceControlStatusItem item;
                item.type = control.type;
                item.key = control.key;
                item.owner = control.owner;
                item.version = control.version;
                status.controls.push_back(item);
            }
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenControlRightsDeviceControlStatus, sn: %s, controls count: %zu", log_tag_.c_str(), status.sn.c_str(), status.controls.size());
            control_rights_device_control_status_callbacks_.notifyCallbackIfChanged(status);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneOsdModuleImpl][%s] listenControlRightsDeviceControlStatus, error: %d", log_tag_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    // 飞机负载信息列表;
    // AircraftPayloadsOsdInfoList;
    aircraft_payloads_osd_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftPayloadsOsdInfo(
        [this, p](const notification_center::AircraftPayloadsOsdInfoList& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftPayloadsOsdInfoList res;
            res.payloads_info_list.reserve(msg.payloads_info_list.size());

            for (const auto& payload: msg.payloads_info_list) {
                flight_hub_sdk::AircraftPayloadsOsdInfo res_payload_info;
                res_payload_info.smart_track_points.reserve(payload.smart_track_points.size());
                res_payload_info.thermal_supported_palette_styles.reserve(payload.thermal_supported_palette_styles.size());

                res_payload_info.payload_index = payload.payload_index;

                res_payload_info.gimbal_pitch = payload.gimbal_pitch;
                res_payload_info.gimbal_roll = payload.gimbal_roll;
                res_payload_info.gimbal_yaw = payload.gimbal_yaw;
                
                res_payload_info.measure_target_altitude = payload.measure_target_altitude;
                res_payload_info.measure_target_longitude = payload.measure_target_longitude;
                res_payload_info.measure_target_latitude = payload.measure_target_latitude;
                res_payload_info.measure_target_distance = payload.measure_target_distance;
                res_payload_info.measure_target_error_state = payload.measure_target_error_state;


                 flight_hub_sdk::SmartTrackPointInfo res_point;
                 for (const auto& point: payload.smart_track_points) {
                     res_point.track_altitude    = point.track_altitude;
                     res_point.track_latitude    = point.track_latitude;
                     res_point.track_longitude   = point.track_longitude;
                     res_point.track_target_mode = point.track_target_mode;
                     
                     res_payload_info.smart_track_points.emplace_back(res_point);
                 }

                res_payload_info.zoom_factor = payload.zoom_factor;

                res_payload_info.thermal_current_palette_style = payload.thermal_current_palette_style;
                res_payload_info.thermal_gain_mode = payload.thermal_gain_mode;
                res_payload_info.thermal_global_temperature_min = payload.thermal_global_temperature_min;
                res_payload_info.thermal_global_temperature_max = payload.thermal_global_temperature_max;
                res_payload_info.thermal_isotherm_state = payload.thermal_isotherm_state;
                res_payload_info.thermal_isotherm_lower_limit = payload.thermal_isotherm_lower_limit;
                res_payload_info.thermal_isotherm_upper_limit = payload.thermal_isotherm_upper_limit;

                res_payload_info.thermal_supported_palette_styles = payload.thermal_supported_palette_styles;

                res_payload_info.control_source = payload.control_source;
                res_payload_info.firmware_version = payload.firmware_version;
                res_payload_info.sn = payload.sn;

                // save date to results;
                res.payloads_info_list.emplace_back(res_payload_info);
            }

            // debug log;
            LOG_DEBUG("[DroneImpl AircraftPayloadsOsdInfo][%s] ListenAircraftPayloadsOsdInfo(), msg payloads_info_list count: %zu ", log_tag_.c_str(),
                static_cast<int>(msg.payloads_info_list.size())
            );

            // Notify data;
            aircraft_payloads_osd_info_list_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftPayloadsOsdInfo][%s] ListenAircraftPayloadsOsdInfo(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞机: 档位信息(gear);
    // AircraftGear;
    aircraft_gear_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftGear(
        [this, p](const notification_center::AircraftGear& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftGear res{};
            res.gear = msg.gear;

            // debug log;
            LOG_DEBUG("[DroneImpl AircraftGear][%s] ListenAircraftGear(), msg gear: %d", log_tag_.c_str(), msg.gear);

            // Notify data;
            aircraft_gear_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftGear][%s] ListenAircraftGear(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞机: Home点距离(home_distance);
    // AircraftHomeDistance;
    aircraft_home_distance_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftHomeDistance(
        [this, p](const notification_center::AircraftHomeDistance& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftHomeDistance home_distance{};
            home_distance.home_distance = msg.home_distance;

            // log;
            LOG_DEBUG("[DroneImpl AircraftHomeDistance][%s] ListenAircraftHomeDistance(), msg home_distance: %f", log_tag_.c_str(), msg.home_distance);

            // Notify data;
            aircraft_home_distance_callbacks_.notifyCallbackIfChanged(home_distance);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftHomeDistance][%s] ListenAircraftHomeDistance(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞机: 搜星状态(position_state);
    // AircraftPositionState;
    aircraft_position_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftPositionState(
        [this, p](const notification_center::AircraftPositionState& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftPositionState res{};

            res.is_fixed = msg.is_fixed;
            res.quality = msg.quality;
            res.gps_number = msg.gps_number;
            res.rtk_number = msg.rtk_number;

            // log;
            LOG_DEBUG("[DroneImpl AircraftPositionState][%s] ListenAircraftPositionState(), msg {is_fixed: %d, quality: %d, gps_number: %d, rtk_number: %d}", log_tag_.c_str(),
                msg.is_fixed, msg.quality, msg.gps_number, msg.rtk_number
            );

            // Notify data;
            aircraft_position_state_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftPositionState][%s] ListenAircraftPositionState(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    // 飞机: 当前控制源(control_source);
    // AircraftControlSource;
    aircraft_control_source_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftControlSource(
        [this, p](const notification_center::AircraftControlSource& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftControlSource res{};
            res.control_source = msg.control_source;

            // log;
            LOG_DEBUG("[DroneImpl AircraftControlSource][%s] ListenAircraftControlSource(), msg control_source: %s", log_tag_.c_str(), msg.control_source.c_str());

            // Notify data;
            aircraft_control_source_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftControlSource][%s] ListenAircraftControlSource(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞机: 限制类信息状态;
    // AircraftLimitsInfo;
    aircraft_limits_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftLimitsInfo(
        [this, p](const notification_center::AircraftLimitsInfo& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftLimitsInfo res{};

            res.height_limit = msg.height_limit;
            res.is_near_height_limit = msg.is_near_height_limit;
            res.is_near_area_limit = msg.is_near_area_limit;
            res.distance_limit_state = msg.distance_limit_state;
            res.distance_limit = msg.distance_limit;
            res.is_near_distance_limit = msg.is_near_distance_limit;

            // log;
            LOG_DEBUG("[DroneImpl AircraftLimitsInfo][%s] ListenAircraftLimitsInfo(): \n"
                "  {\n"
                "    distance_limit: %d, \n"
                "    distance_limit_state: %d, \n"
                "    height_limit: %d, \n"
                "    is_near_area_limit: %d, \n"
                "    is_near_distance_limit: %d, \n"
                "    is_near_height_limit: %d\n"
                "  }", log_tag_.c_str(),
                msg.distance_limit,
                msg.distance_limit_state,
                msg.height_limit,
                msg.is_near_area_limit,
                msg.is_near_distance_limit,
                msg.is_near_height_limit
            );

            // Notify data;
            aircraft_limits_info_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftLimitsInfo][%s] ListenAircraftLimitsInfo(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 电量告警信息(用户设置的阈值);
    // AircraftBatteryWarningThresholds;
    aircraft_battery_warning_thresholds_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftBatteryWarningThresholds(
        [this, p](const notification_center::AircraftBatteryWarningThresholds& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftBatteryWarningThresholds res{};
            res.low_battery_warning_threshold = msg.low_battery_warning_threshold;
            res.serious_low_battery_warning_threshold = msg.serious_low_battery_warning_threshold;

            // log;
            LOG_DEBUG("[DroneImpl AircraftBatteryWarningThresholds][%s] ListenAircraftBatteryWarningThresholds(), msg {low_battery_warning_threshold: %d, serious_low_battery_warning_threshold: %d}", log_tag_.c_str(),
                msg.low_battery_warning_threshold, msg.serious_low_battery_warning_threshold
            );

            // Notify data;
            aircraft_battery_warning_thresholds_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftBatteryWarningThresholds][%s] ListenAircraftBatteryWarningThresholds(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 固件状态信息;
    // AircraftFirmwareStatus;
    aircraft_firmware_status_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftFirmwareStatus(
        [this, p](const notification_center::AircraftFirmwareStatus& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftFirmwareStatus res{};

            res.firmware_version = msg.firmware_version;                  // 固件版本, such as "00.01.0439";
            res.compatible_status = msg.compatible_status;                // 固件一致性, {"0: 一致, 1: 不一致"};
            res.firmware_upgrade_status = msg.firmware_upgrade_status;    // 固件升级状态, {"0: 未升级, 1: 升级中"};

            // log;
            LOG_DEBUG("[DroneImpl AircraftFirmwareStatus][%s] ListenAircraftFirmwareStatus(), "
                "msg { firmware_version: %s, compatible_status: %d, firmware_upgrade_status: %d }", log_tag_.c_str(),
                msg.firmware_version.c_str(),
                msg.compatible_status,
                msg.firmware_upgrade_status
            );

            // Notify data;
            aircraft_firmware_status_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftFirmwareStatus][%s] ListenAircraftFirmwareStatus(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 累计飞行信息(total_flight_info);
    // AircraftTotalFlightInfo;
    aircraft_total_flight_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftTotalFlightInfo(
        [this, p](const notification_center::AircraftTotalFlightInfo& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftTotalFlightInfo res{};
            res.total_flight_distance = msg.total_flight_distance;
            res.total_flight_sorties = msg.total_flight_sorties;
            res.total_flight_time = msg.total_flight_time;

            // debug logs;
            LOG_DEBUG("[DroneImpl AircraftTotalFlightInfo][%s] ListenAircraftTotalFlightInfo(), "
                "msg { total_flight_distance: %f, total_flight_sorties: %d, total_flight_time: %f }", log_tag_.c_str(),
                msg.total_flight_distance,
                msg.total_flight_sorties,
                msg.total_flight_time
            );

            // Notify data;
            aircraft_total_flight_info_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftTotalFlightInfo][%s] ListenAircraftTotalFlightInfo(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞行器激活时间(unix时间戳)(activation_time);
    // AircraftActivationTime;
    aircraft_activation_time_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftActivationTime(
        [this, p](const notification_center::AircraftActivationTime& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftActivationTime res{};
            res.activation_time = msg.activation_time;
            
            // debug logs;
            LOG_DEBUG("[DroneImpl AircraftActivationTime][%s] ListenAircraftActivationTime(), msg { activation_time: %ld }", log_tag_.c_str(),
                msg.activation_time
            );


            // Notify data;
            aircraft_activation_time_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftActivationTime][%s] ListenAircraftActivationTime(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞机：保养信息(maintain_status);
    // AircraftMaintainStatusInfo;
    aircraft_maintain_status_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftMaintainStatusInfo(
        [this, p](const notification_center::AircraftMaintainStatusInfo& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftMaintainStatusInfo res{};
            for (const auto& info : msg.maintain_status_info) {
                flight_hub_sdk::AircraftMaintainStatus temp_maintain_status{};

                temp_maintain_status.state = info.state;
                temp_maintain_status.last_maintain_type = info.last_maintain_type;
                temp_maintain_status.last_maintain_time = info.last_maintain_time;
                temp_maintain_status.last_maintain_flight_time = info.last_maintain_flight_time;
                temp_maintain_status.last_maintain_flight_sorties = info.last_maintain_flight_sorties;

                // save data;
                res.maintain_status_info.emplace_back(temp_maintain_status);
            }
            
            // debug logs;
            LOG_DEBUG("[DroneImpl AircraftMaintainStatusInfo][%s] ListenAircraftMaintainStatusInfo(), maintain_status_info size: %zu }", log_tag_.c_str(),
                res.maintain_status_info.size()
            );

            for (const auto &item : res.maintain_status_info) {
                LOG_DEBUG("[DroneImpl AircraftMaintainStatusInfo][%s] ListenAircraftMaintainStatusInfo(): \n"
                    "    {\n"
                    "        state: %d, \n"
                    "        last_maintain_type: %d, \n"
                    "        last_maintain_time: %ld, \n"
                    "        last_maintain_flight_time: %d, \n"
                    "        last_maintain_flight_sorties: %d \n"
                    "    }", log_tag_.c_str(),
                        item.state,
                        item.last_maintain_type,
                        item.last_maintain_time,
                        item.last_maintain_flight_time,
                        item.last_maintain_flight_sorties
                );
            }

            // Notify data;
            aircraft_maintain_status_info_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftMaintainStatusInfo][%s] ListenAircraftMaintainStatusInfo(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    // 飞行器夜航灯状态(night_lights_state);
    // AircraftNightLightsState;
    aircraft_night_lights_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftNightLightsState(
        [this, p](const notification_center::AircraftNightLightsState& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftNightLightsState res{};
            res.night_lights_state = msg.night_lights_state;

            // debug logs;
            LOG_DEBUG("[DroneImpl AircraftNightLightsState][%s] ListenAircraftNightLightsState(), msg { night_lights_state: %d }", log_tag_.c_str(),
                msg.night_lights_state
            );

            // Notify data;
            aircraft_night_lights_state_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftNightLightsState][%s] ListenAircraftNightLightsState(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞行器避障状态(obstacle_avoidance);
    // AircraftObstacleAvoidanceState;
    aircraft_obstacle_avoidance_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftObstacleAvoidanceState(
        [this, p](const notification_center::AircraftObstacleAvoidanceState& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftObstacleAvoidanceState res{};

            res.horizon = msg.horizon;
            res.upside = msg.upside;
            res.downside = msg.downside;
            
            LOG_DEBUG("[DroneImpl AircraftObstacleAvoidanceState][%s] ListenAircraftObstacleAvoidanceState(), "
               "msg { horizon: %d, upside: %d, downside: %d }", log_tag_.c_str(),
                msg.horizon,
                msg.upside,
                msg.downside
            );

            // Notify data;
            aircraft_obstacle_avoidance_state_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftObstacleAvoidanceState][%s] ListenAircraftObstacleAvoidanceState(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 存储容量(storage);
    // AircraftStorageInfo;
    aircraft_storage_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftStorageInfo(
        [this, p](const notification_center::AircraftStorageInfo& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftStorageInfo res{};
            res.total = msg.total;
            res.used = msg.used;
            
            // debug logs;
            LOG_DEBUG("[DroneImpl AircraftStorageInfo][%s] ListenAircraftStorageInfo(), msg { total: %d, used: %d }", log_tag_.c_str(),
                msg.total,
                msg.used
            );

            // Notify data;
            aircraft_storage_info_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftStorageInfo][%s] ListenAircraftStorageInfo(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 航迹id(track_id);
    // AircraftTrackId;
    aircraft_track_id_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftTrackId(
        [this, p](const notification_center::AircraftTrackId& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftTrackId res{};
            res.track_id = msg.track_id;
            
            // debug logs;
            LOG_DEBUG("[DroneImpl AircraftTrackId][%s] ListenAircraftTrackId(), msg { track_id: %s }", log_tag_.c_str(),
                msg.track_id.c_str()
            );

            // Notify data;
            aircraft_track_id_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftTrackId][%s] ListenAircraftTrackId(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 合规相关信息(compliance information);
    // AircraftComplianceInfo;
    aircraft_compliance_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftComplianceInfo(
        [this, p](const notification_center::AircraftComplianceInfo& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftComplianceInfo res{};
            res.country = msg.country;
            res.rid_state = msg.rid_state;

            // debug logs;
            LOG_DEBUG("[DroneImpl AircraftComplianceInfo][%s] ListenAircraftComplianceInfo(), msg { country: %s, rid_state: %d }", log_tag_.c_str(),
                msg.country.c_str(),
                msg.rid_state
            );

            // Notify data;
            aircraft_compliance_info_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftComplianceInfo][%s] ListenAircraftComplianceInfo(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞行器相机信息(cameras);
    // AircraftPayloadsCamerasList;
    aircraft_payloads_cameras_list_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftPayloadsCamerasList(
        [this, p](const notification_center::AircraftPayloadsCamerasList& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftPayloadsCamerasList res{};
            res.cameras_list.reserve(msg.cameras_list.size());

            int index = 0;
            for (const auto& camera : msg.cameras_list) {
                flight_hub_sdk::AircraftPayloadCameraInfo camera_info{};

                camera_info.payload_index = camera.payload_index;
                camera_info.camera_mode = camera.camera_mode;
                camera_info.ir_metering_mode = camera.ir_metering_mode;

                // For ir_metering_point;
                camera_info.ir_metering_point.temperature = camera.ir_metering_point.temperature;
                camera_info.ir_metering_point.x = camera.ir_metering_point.x;
                camera_info.ir_metering_point.y = camera.ir_metering_point.y;

                camera_info.ir_zoom_factor = camera.ir_zoom_factor;

                // For liveview_world_region;
                camera_info.liveview_world_region.bottom = camera.liveview_world_region.bottom;
                camera_info.liveview_world_region.left = camera.liveview_world_region.left;
                camera_info.liveview_world_region.right = camera.liveview_world_region.right;
                camera_info.liveview_world_region.top = camera.liveview_world_region.top;

                camera_info.photo_state = camera.photo_state;
                camera_info.photo_storage_settings = camera.photo_storage_settings;

                camera_info.record_time = camera.record_time;
                camera_info.recording_state = camera.recording_state;
                camera_info.remain_photo_num = camera.remain_photo_num;
                camera_info.remain_record_duration = camera.remain_record_duration;
                camera_info.screen_split_enable = camera.screen_split_enable;
                
                camera_info.wide_calibrate_farthest_focus_value = camera.wide_calibrate_farthest_focus_value;
                camera_info.wide_calibrate_nearest_focus_value = camera.wide_calibrate_nearest_focus_value;
                camera_info.wide_exposure_mode = camera.wide_exposure_mode;
                camera_info.wide_exposure_value = camera.wide_exposure_value;
                camera_info.wide_focus_mode = camera.wide_focus_mode;
                camera_info.wide_focus_state = camera.wide_focus_state;
                camera_info.wide_focus_value = camera.wide_focus_value;
                camera_info.wide_iso = camera.wide_iso;
                camera_info.wide_max_focus_value = camera.wide_max_focus_value;
                camera_info.wide_min_focus_value = camera.wide_min_focus_value;
                camera_info.wide_shutter_speed = camera.wide_shutter_speed;
                
                camera_info.zoom_calibrate_farthest_focus_value = camera.zoom_calibrate_farthest_focus_value;
                camera_info.zoom_calibrate_nearest_focus_value = camera.zoom_calibrate_nearest_focus_value;
                camera_info.zoom_exposure_mode = camera.zoom_exposure_mode;
                camera_info.zoom_exposure_value = camera.zoom_exposure_value;
                camera_info.zoom_factor = camera.zoom_factor;
                camera_info.zoom_focus_mode = camera.zoom_focus_mode;
                camera_info.zoom_focus_state = camera.zoom_focus_state;
                camera_info.zoom_focus_value = camera.zoom_focus_value;
                camera_info.zoom_iso = camera.zoom_iso;
                camera_info.zoom_max_focus_value = camera.zoom_max_focus_value;
                camera_info.zoom_min_focus_value = camera.zoom_min_focus_value;
                camera_info.zoom_shutter_speed = camera.zoom_shutter_speed;

                res.cameras_list.emplace_back(camera_info);


                // For debug logs;
                // 拼接字符串;  photo_storage_settings, std::vector<std::string>;
                std::string photo_storage_settings_list;
                for (const auto &s : camera.photo_storage_settings) {
                       photo_storage_settings_list += (s + ", ");
                }

                if (photo_storage_settings_list.size() >= 2) {
                    if (photo_storage_settings_list.back() == ' ') {
                        photo_storage_settings_list.pop_back();
                    }

                    if (photo_storage_settings_list.back() == ',') {
                         photo_storage_settings_list.pop_back();
                    }
                }


                LOG_DEBUG("[DroneImpl AircraftPayloadsCamerasList] [deviceSN: %s] ListenAircraftPayloadsCamerasList(): \n"
                    "  camera[%d]  {\n"
                    "      payload_index: %s, \n"
                    "      camera_mode: %d, \n"
                    "      ir_metering_mode: %d, \n"
                    "      ir_metering_point: { temperature: %f, x: %f, y: %f }, \n"
                    "      ir_zoom_factor: %f, \n"
                    "      liveview_world_region: { bottom: %f, left: %f, right: %f, top: %f }, \n"
                    "      photo_state: %d, \n"
                    "      photo_storage_settings: [%s], \n"
                    "      record_time: %d, \n"
                    "      recording_state: %d, \n"
                    "      remain_photo_num: %d, \n"
                    "      remain_record_duration: %d, \n"
                    "      screen_split_enable: %s, \n"
                    "      wide_calibrate_farthest_focus_value: %d, \n"
                    "      wide_calibrate_nearest_focus_value: %d, \n"
                    "      wide_exposure_mode: %d, \n"
                    "      wide_exposure_value: %d, \n"
                    "      wide_focus_mode: %d, \n"
                    "      wide_focus_state: %d, \n"
                    "      wide_focus_value: %d, \n"
                    "      wide_iso: %d, \n"
                    "      wide_max_focus_value: %d, \n"
                    "      wide_min_focus_value: %d, \n"
                    "      wide_shutter_speed: %d, \n"
                    "      zoom_calibrate_farthest_focus_value: %d, \n"
                    "      zoom_calibrate_nearest_focus_value: %d, \n"
                    "      zoom_exposure_mode: %d, \n"
                    "      zoom_exposure_value: %d, \n"
                    "      zoom_factor: %f,"
                    "      zoom_focus_mode: %d, \n"
                    "      zoom_focus_state: %d, \n"
                    "      zoom_focus_value: %d, \n"
                    "      zoom_iso: %d, \n"
                    "      zoom_max_focus_value: %d, \n"
                    "      zoom_min_focus_value: %d, \n"
                    "      zoom_shutter_speed: %d, \n"
                    "  }",
                    this->log_tag_.c_str(),
                    index,
                    camera_info.payload_index.c_str(),
                    camera_info.camera_mode,
                    camera_info.ir_metering_mode,
                    camera_info.ir_metering_point.temperature, camera_info.ir_metering_point.x, camera_info.ir_metering_point.y,
                    camera_info.ir_zoom_factor,
                    camera_info.liveview_world_region.bottom, camera_info.liveview_world_region.left, camera_info.liveview_world_region.right, camera_info.liveview_world_region.top,
                    camera_info.photo_state,
                    photo_storage_settings_list.c_str(),
                    camera_info.record_time,
                    camera_info.recording_state,
                    camera_info.remain_photo_num,
                    camera_info.remain_record_duration,
                    camera_info.screen_split_enable ? "true" : "false",
                    camera_info.wide_calibrate_farthest_focus_value,
                    camera_info.wide_calibrate_nearest_focus_value,
                    camera_info.wide_exposure_mode,
                    camera_info.wide_exposure_value,
                    camera_info.wide_focus_mode,
                    camera_info.wide_focus_state,
                    camera_info.wide_focus_value,
                    camera_info.wide_iso,
                    camera_info.wide_max_focus_value,
                    camera_info.wide_min_focus_value,
                    camera_info.wide_shutter_speed,
                    camera_info.zoom_calibrate_farthest_focus_value,
                    camera_info.zoom_calibrate_nearest_focus_value,
                    camera_info.zoom_exposure_mode,
                    camera_info.zoom_exposure_value,
                    camera_info.zoom_factor,
                    camera_info.zoom_focus_mode,
                    camera_info.zoom_focus_state,
                    camera_info.zoom_focus_value,
                    camera_info.zoom_iso,
                    camera_info.zoom_max_focus_value,
                    camera_info.zoom_min_focus_value,
                    camera_info.zoom_shutter_speed
                );

                ++index;
            }


            // Notify data;
            aircraft_payloads_cameras_list_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftPayloadsCamerasList] [deviceSN: %s] ListenAircraftPayloadsCamerasList(): error_code: %d",
                    this->log_tag_.c_str(),
                    error_code
                );
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    // 遥控器失控动作(rc_lost_action);
    // AircraftRcLostAction;
    aircraft_rc_lost_action_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftRcLostAction(
        [this, p](const notification_center::AircraftRcLostAction& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftRcLostAction res{};
            res.rc_lost_action = msg.rc_lost_action;

            // debug log;
            LOG_INFO("[DroneImpl AircraftRcLostAction] [deviceSN: %s] ListenAircraftRcLostAction(): msg { rc_lost_action: %d }",
                this->log_tag_.c_str(), msg.rc_lost_action
            );

            // Notify data;
            aircraft_rc_lost_action_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftRcLostAction] [deviceSN: %s] ListenAircraftRcLostAction(): error_code: %d",
                    this->log_tag_.c_str(),
                    error_code
                );
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    // 返航信息(返航模式, 返航高度);
    // AircraftRthInfo;
    aircraft_rth_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftRthInfo(
        [this, p](const notification_center::AircraftRthInfo& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftRthInfo res{};

            res.rth_altitude = msg.rth_altitude;
            res.rth_mode = msg.rth_mode;

            // debug log;
            LOG_INFO("[DroneImpl AircraftRthInfo] [deviceSN: %s] ListenAircraftRthInfo(): msg { rth_altitude: %d, rth_mode: %d }",
                this->log_tag_.c_str(), msg.rth_altitude, msg.rth_mode
            );

            // Notify data;
            aircraft_rth_info_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftRthInfo] [deviceSN: %s] ListenAircraftRthInfo(): error_code: %d",
                    this->log_tag_.c_str(),
                    error_code
                );
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞机: Home点地理坐标信息(Home点经度, Home点纬度);
    // AircraftHomeGeographicalInfo;
    aircraft_home_geographical_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftHomeGeographicalInfo(
        [this, p](const notification_center::AircraftHomeGeographicalInfo& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftHomeGeographicalInfo res{};

            res.home_latitude = msg.home_latitude;
            res.home_longitude = msg.home_longitude;

            // debug log;
            LOG_INFO("[DroneImpl AircraftHomeGeographicalInfo] [deviceSN: %s] ListenAircraftHomeGeographicalInfo(): "
                "msg { home_latitude: %f, home_longitude: %f }",
                this->log_tag_.c_str(),
                msg.home_latitude,
                msg.home_longitude
            );

            // Notify data;
            aircraft_home_geographical_info_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftHomeGeographicalInfo] [deviceSN: %s] ListenAircraftHomeGeographicalInfo(): error_code: %d",
                    this->log_tag_.c_str(),
                    error_code
                );
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 返航高度模式当前值(current_rth_mode);
    // AircraftCurrentRthMode;
    aircraft_current_rth_mode_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftCurrentRthMode(
        [this, p](const notification_center::AircraftCurrentRthMode& msg)-> void {
            WeakDummyReturn(p);

            // 数据转换;
            flight_hub_sdk::AircraftCurrentRthMode res{};
            res.current_rth_mode = msg.current_rth_mode;

            // debug log;
            LOG_INFO("[DroneImpl AircraftCurrentRthMode] [deviceSN: %s] ListenAircraftCurrentRthMode(): msg { current_rth_mode: %d }",
                this->log_tag_.c_str(), msg.current_rth_mode
            );
 
            // Notify data;
            aircraft_current_rth_mode_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl AircraftCurrentRthMode] [deviceSN: %s] ListenAircraftCurrentRthMode(): error_code: %d",
                    this->log_tag_.c_str(),
                    error_code
                );
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );


    // 飞行任务媒体文件显示可用监听
    flight_tasks_media_file_display_available_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenFlightTasksMediaFileDisplayAvailable(
        [this, p](const notification_center::FlightTasksMediaFileDisplayAvailable& msg)-> void {
            WeakDummyReturn(p);
            FlightTasksMediaFileDisplayAvailable available;
            available.uuid = msg.uuid;
            available.idx_parent_id = msg.idx_parent_id;
            available.file_type = msg.file_type;
            available.sub_file_type = msg.sub_file_type;
            available.media_file_id = msg.media_file_id;
            available.flight_id = msg.flight_id;
            available.sn = msg.sn;
            available.media_source = msg.media_source;
            available.url = msg.url;
            LOG_DEBUG("[DroneOsdModuleImpl][%s] listenFlightTasksMediaFileDisplayAvailable, uuid: %s, file_type: %d", log_tag_.c_str(), available.uuid.c_str(), available.file_type);
            flight_tasks_media_file_display_available_callbacks_.notifyCallbackIfChanged(available);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneOsdModuleImpl][%s] listenFlightTasksMediaFileDisplayAvailable, error: %d", log_tag_.c_str(), error_code);
            }
        }, gateway_sn_, notification_center::NotificationFrequency_OnChanged);

    // 飞行器进入当前状态原因(mode_code_reason);
    drone_mode_code_reason_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneModeCodeReason(
        [this, p](const notification_center::DroneModeCodeReason& msg)-> void {
            WeakDummyReturn(p);
            flight_hub_sdk::DroneModeCodeReason res{};
            res.mode_code_reason = msg.mode_code_reason;
            LOG_DEBUG("[DroneImpl DroneModeCodeReason][%s] ListenDroneModeCodeReason(), mode_code_reason: %lld", log_tag_.c_str(), (long long)msg.mode_code_reason);
            drone_mode_code_reason_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl DroneModeCodeReason][%s] ListenDroneModeCodeReason(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    // 飞机4G Dongle信息(dongle_infos);
    drone_dongle_infos_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneDongleInfos(
        [this, p](const notification_center::DroneDongleInfos& msg)-> void {
            WeakDummyReturn(p);
            flight_hub_sdk::DroneDongleInfos res{};
            for (const auto& it : msg.dongle_infos) {
                flight_hub_sdk::DongleInfoItem item{};
                item.dongle_type = it.dongle_type;
                item.eid = it.eid;
                item.esim_activate_state = it.esim_activate_state;
                for (const auto& esim : it.esim_infos) {
                    flight_hub_sdk::EsimInfoItem esim_item{};
                    esim_item.enabled = esim.enabled;
                    esim_item.iccid = esim.iccid;
                    esim_item.telecom_operator = esim.telecom_operator;
                    item.esim_infos.emplace_back(esim_item);
                }
                item.imei = it.imei;
                item.sim_card_state = it.sim_card_state;
                item.sim_slot = it.sim_slot;
                item.sim_info.iccid = it.sim_info.iccid;
                item.sim_info.telecom_operator = it.sim_info.telecom_operator;
                item.telecom_operator = it.telecom_operator;
                res.dongle_infos.emplace_back(item);
            }
            LOG_DEBUG("[DroneImpl DroneDongleInfos][%s] ListenDroneDongleInfos(), dongle_infos size: %d", log_tag_.c_str(), (int)res.dongle_infos.size());
            drone_dongle_infos_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl DroneDongleInfos][%s] ListenDroneDongleInfos(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    // 限飞数据库版本(flysafe_database_version);
    drone_flysafe_database_version_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneFlysafeDatabaseVersion(
        [this, p](const notification_center::DroneFlysafeDatabaseVersion& msg)-> void {
            WeakDummyReturn(p);
            flight_hub_sdk::DroneFlysafeDatabaseVersion res{};
            res.flysafe_database_version = msg.flysafe_database_version;
            LOG_DEBUG("[DroneImpl DroneFlysafeDatabaseVersion][%s] ListenDroneFlysafeDatabaseVersion(), version: %s", log_tag_.c_str(), msg.flysafe_database_version.c_str());
            drone_flysafe_database_version_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl DroneFlysafeDatabaseVersion][%s] ListenDroneFlysafeDatabaseVersion(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );

    // 航线解析库版本(wpmz_version);
    drone_wpmz_version_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneWpmzVersion(
        [this, p](const notification_center::DroneWpmzVersion& msg)-> void {
            WeakDummyReturn(p);
            flight_hub_sdk::DroneWpmzVersion res{};
            res.wpmz_version = msg.wpmz_version;
            LOG_DEBUG("[DroneImpl DroneWpmzVersion][%s] ListenDroneWpmzVersion(), version: %s", log_tag_.c_str(), msg.wpmz_version.c_str());
            drone_wpmz_version_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DroneImpl DroneWpmzVersion][%s] ListenDroneWpmzVersion(), error: %d", log_tag_.c_str(), error_code);
            }
        },
        drone_sn_,
        notification_center::NotificationFrequency_OnChanged
    );
}

int DroneOsdModuleImpl::listenAircraftBatteryInfo(const std::function<void(const AircraftBatteryInfo& battery_info)>& callback) {
    return aircraft_battery_info_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftBatteryInfo(int listen_id){
    aircraft_battery_info_callbacks_.removeCallback(listen_id);
}

int DroneOsdModuleImpl::listenAircraftAttitude(const std::function<void(const AircraftAttitude& attitude)>& callback) {
    return aircraft_attitude_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftAttitude(int listen_id) {
    aircraft_attitude_callbacks_.removeCallback(listen_id);
}

int DroneOsdModuleImpl::listenAircraftLocation(const std::function<void(const AircraftLocation& location)>& callback) {
    return aircraft_location_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftLocation(int listen_id) {
    aircraft_location_callbacks_.removeCallback(listen_id);
}

int DroneOsdModuleImpl::listenAircraftModeCode(const std::function<void(const AircraftModeCode& mode_code)>& callback) {
    return aircraft_mode_code_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftModeCode(int listen_id) {
    aircraft_mode_code_callbacks_.removeCallback(listen_id);
}

int DroneOsdModuleImpl::listenAircraftWindSpeed(const std::function<void(const AircraftWindSpeed& wind_speed)>& callback) {
    return aircraft_wind_speed_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftWindSpeed(int listen_id) {
    aircraft_wind_speed_callbacks_.removeCallback(listen_id);
}

int DroneOsdModuleImpl::listenFlightTasksException(const std::function<void(const FlightTaskException& msg)>& callback) {
    return flight_task_exception_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenFlightTasksException(int listen_id) {
    flight_task_exception_callbacks_.removeCallback(listen_id);
}

// 飞机控制码
int DroneOsdModuleImpl::listenAircraftControlCode(const std::function<void(const AircraftControlCode& msg)>& callback) {
    return aircraft_control_code_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftControlCode(int listen_id) {
    aircraft_control_code_callbacks_.removeCallback(listen_id);
}

// 飞机速度
int DroneOsdModuleImpl::listenAircraftSpeed(const std::function<void(const AircraftSpeed& msg)>& callback) {
    return aircraft_speed_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftSpeed(int listen_id) {
    aircraft_speed_callbacks_.removeCallback(listen_id);
}

// 载荷相机实时视图世界区域
int DroneOsdModuleImpl::listenAircraftPayloadsCameraLiveviewWorldRegion(const std::function<void(const AircraftPayloadsCameraLiveviewWorldRegion& msg)>& callback) {
    return aircraft_payloads_camera_liveview_world_region_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftPayloadsCameraLiveviewWorldRegion(int listen_id) {
    aircraft_payloads_camera_liveview_world_region_callbacks_.removeCallback(listen_id);
}

// 载荷云台姿态
int DroneOsdModuleImpl::listenAircraftPayloadsGimbalAttitude(const std::function<void(const AircraftPayloadsGimbalAttitude& msg)>& callback) {
    return aircraft_payloads_gimbal_attitude_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftPayloadsGimbalAttitude(int listen_id) {
    aircraft_payloads_gimbal_attitude_callbacks_.removeCallback(listen_id);
}

// 载荷列表
int DroneOsdModuleImpl::listenAircraftPayloadsList(const std::function<void(const AircraftPayloadsList& msg)>& callback) {
    return aircraft_payloads_list_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftPayloadsList(int listen_id) {
    aircraft_payloads_list_callbacks_.removeCallback(listen_id);
}

// 控制权限云控制状态
int DroneOsdModuleImpl::listenControlRightsCloudControlStatus(const std::function<void(const ControlRightsCloudControlStatus& msg)>& callback) {
    return control_rights_cloud_control_status_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenControlRightsCloudControlStatus(int listen_id) {
    control_rights_cloud_control_status_callbacks_.removeCallback(listen_id);
}

// 控制权限设备控制状态
int DroneOsdModuleImpl::listenControlRightsDeviceControlStatus(const std::function<void(const ControlRightsDeviceControlStatus& msg)>& callback) {
    return control_rights_device_control_status_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenControlRightsDeviceControlStatus(int listen_id) {
    control_rights_device_control_status_callbacks_.removeCallback(listen_id);
}

// 飞行任务媒体文件显示可用
int DroneOsdModuleImpl::listenFlightTasksMediaFileDisplayAvailable(const std::function<void(const FlightTasksMediaFileDisplayAvailable& msg)>& callback) {
    return flight_tasks_media_file_display_available_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenFlightTasksMediaFileDisplayAvailable(int listen_id) {
    flight_tasks_media_file_display_available_callbacks_.removeCallback(listen_id);
}

// 飞机负载信息列表;
// AircraftPayloadsOsdInfoList
int DroneOsdModuleImpl::listenAircraftPayloadsOsdInfo(const std::function<void(const AircraftPayloadsOsdInfoList &msg)>& callback) {
    return aircraft_payloads_osd_info_list_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftPayloadsOsdInfo(int listen_id) {
    aircraft_payloads_osd_info_list_callbacks_.removeCallback(listen_id);
}


// 飞机: 档位信息(gear);
// AircraftGear;
int DroneOsdModuleImpl::listenAircraftGear(const std::function<void(const AircraftGear &msg)>& callback) {
    return aircraft_gear_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftGear(int listen_id) {
    aircraft_gear_callbacks_.removeCallback(listen_id);
}

// 飞机: Home点距离(home_distance);
// AircraftHomeDistance;
int DroneOsdModuleImpl::listenAircraftHomeDistance(const std::function<void(const AircraftHomeDistance &msg)>& callback) {
    return aircraft_home_distance_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftHomeDistance(int listen_id) {
    aircraft_home_distance_callbacks_.removeCallback(listen_id);
}


// 飞机: 搜星状态(position_state);
// AircraftPositionState;
int DroneOsdModuleImpl::listenAircraftPositionState(const std::function<void(const AircraftPositionState &msg)>& callback) {
    return aircraft_position_state_callbacks_.addCallback(callback);
}


void DroneOsdModuleImpl::cancelListenAircraftPositionState(int listen_id) {
    aircraft_position_state_callbacks_.removeCallback(listen_id);
}


// 飞机: 当前控制源(control_source);
// AircraftControlSource;
int DroneOsdModuleImpl::listenAircraftControlSource(const std::function<void(const AircraftControlSource &msg)>& callback) {
    return aircraft_control_source_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftControlSource(int listen_id) {
    aircraft_control_source_callbacks_.removeCallback(listen_id);
}


// 飞机: 限制类信息状态;
// AircraftLimitsInfo;
int DroneOsdModuleImpl::listenAircraftLimitsInfo(const std::function<void(const AircraftLimitsInfo &msg)>& callback) {
    return aircraft_limits_info_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftLimitsInfo(int listen_id) {
    aircraft_limits_info_callbacks_.removeCallback(listen_id);
}


// 电量告警信息(用户设置的阈值);
// AircraftBatteryWarningThresholds;
int DroneOsdModuleImpl::listenAircraftBatteryWarningThresholds(const std::function<void(const AircraftBatteryWarningThresholds &msg)>& callback) {
    return aircraft_battery_warning_thresholds_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftBatteryWarningThresholds(int listen_id) {
    aircraft_battery_warning_thresholds_callbacks_.removeCallback(listen_id);
}


// 固件状态信息;
// AircraftFirmwareStatus;
int DroneOsdModuleImpl::listenAircraftFirmwareStatus(const std::function<void(const AircraftFirmwareStatus &msg)>& callback) {
    return aircraft_firmware_status_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftFirmwareStatus(int listen_id) {
    aircraft_firmware_status_callbacks_.removeCallback(listen_id);
}


// 累计飞行信息(total_flight_info);
// AircraftTotalFlightInfo;
int DroneOsdModuleImpl::listenAircraftTotalFlightInfo(const std::function<void(const AircraftTotalFlightInfo &msg)>& callback) {
    return aircraft_total_flight_info_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftTotalFlightInfo(int listen_id) {
    aircraft_total_flight_info_callbacks_.removeCallback(listen_id);
}


// 飞行器激活时间(unix时间戳)(activation_time);
// AircraftActivationTime;
int DroneOsdModuleImpl::listenAircraftActivationTime(const std::function<void(const AircraftActivationTime &msg)>& callback) {
    return aircraft_activation_time_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftActivationTime(int listen_id) {
    aircraft_activation_time_callbacks_.removeCallback(listen_id);
}


// 飞机：保养信息(maintain_status);
// AircraftMaintainStatusInfo;
int DroneOsdModuleImpl::listenAircraftMaintainStatusInfo(const std::function<void(const AircraftMaintainStatusInfo &msg)>& callback) {
    return aircraft_maintain_status_info_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftMaintainStatusInfo(int listen_id) {
    aircraft_maintain_status_info_callbacks_.removeCallback(listen_id);
}


// 飞行器夜航灯状态(night_lights_state);
// AircraftNightLightsState;
int DroneOsdModuleImpl::listenAircraftNightLightsState(const std::function<void(const AircraftNightLightsState &msg)>& callback) {
    return aircraft_night_lights_state_callbacks_.addCallback(callback);
}
void DroneOsdModuleImpl::cancelListenAircraftNightLightsState(int listen_id) {
    aircraft_night_lights_state_callbacks_.removeCallback(listen_id);
}


// 飞行器避障状态(obstacle_avoidance);
// AircraftObstacleAvoidanceState;
int DroneOsdModuleImpl::listenAircraftObstacleAvoidanceState(const std::function<void(const AircraftObstacleAvoidanceState &msg)>& callback) {
    return aircraft_obstacle_avoidance_state_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftObstacleAvoidanceState(int listen_id) {
    aircraft_obstacle_avoidance_state_callbacks_.removeCallback(listen_id);
}


// 存储容量(storage);
// AircraftStorageInfo;
int DroneOsdModuleImpl::listenAircraftStorageInfo(const std::function<void(const AircraftStorageInfo &msg)>& callback) {
    return aircraft_storage_info_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftStorageInfo(int listen_id) {
    aircraft_storage_info_callbacks_.removeCallback(listen_id);
}


// 航迹id(track_id);
// AircraftTrackId;
int DroneOsdModuleImpl::listenAircraftTrackId(const std::function<void(const AircraftTrackId &msg)>& callback) {
    return aircraft_track_id_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftTrackId(int listen_id) {
    aircraft_track_id_callbacks_.removeCallback(listen_id);
}


// 合规相关信息(compliance information);
// AircraftComplianceInfo;
int DroneOsdModuleImpl::listenAircraftComplianceInfo(const std::function<void(const AircraftComplianceInfo &msg)>& callback) {
    return aircraft_compliance_info_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftComplianceInfo(int listen_id) {
    aircraft_compliance_info_callbacks_.removeCallback(listen_id);
}


// 飞行器相机信息(cameras);
// AircraftPayloadsCamerasList;
int DroneOsdModuleImpl::listenAircraftPayloadsCamerasList(const std::function<void(const AircraftPayloadsCamerasList &msg)>& callback) {
    return aircraft_payloads_cameras_list_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftPayloadsCamerasList(int listen_id) {
    aircraft_payloads_cameras_list_callbacks_.removeCallback(listen_id);
}


// 遥控器失控动作(rc_lost_action);
// AircraftRcLostAction;
int DroneOsdModuleImpl::listenAircraftRcLostAction(const std::function<void(const AircraftRcLostAction &msg)>& callback) {
    return aircraft_rc_lost_action_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftRcLostAction(int listen_id) {
    aircraft_rc_lost_action_callbacks_.removeCallback(listen_id);
}


// 返航信息(返航模式, 返航高度);
// AircraftRthInfo;
int DroneOsdModuleImpl::listenAircraftRthInfo(const std::function<void(const AircraftRthInfo &msg)>& callback) {
    return aircraft_rth_info_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftRthInfo(int listen_id) {
    aircraft_rth_info_callbacks_.removeCallback(listen_id);
}


// 飞机: Home点地理坐标信息(Home点经度, Home点纬度);
// AircraftHomeGeographicalInfo;
int DroneOsdModuleImpl::listenAircraftHomeGeographicalInfo(const std::function<void(const AircraftHomeGeographicalInfo &msg)>& callback) {
    return aircraft_home_geographical_info_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenAircraftHomeGeographicalInfo(int listen_id) {
    aircraft_home_geographical_info_callbacks_.removeCallback(listen_id);
}


// 返航高度模式当前值(current_rth_mode);
// AircraftCurrentRthMode;
int DroneOsdModuleImpl::listenAircraftCurrentRthMode(const std::function<void(const AircraftCurrentRthMode &msg)>& callback) {
    return aircraft_current_rth_mode_callbacks_.addCallback(callback);
}


void DroneOsdModuleImpl::cancelListenAircraftCurrentRthMode(int listen_id) {
    aircraft_current_rth_mode_callbacks_.removeCallback(listen_id);
}

// HSI 信息
int DroneOsdModuleImpl::listenHsiInfo(const std::function<void(const HsiInfo&)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("DroneOsdModuleImpl: flight_ctrl_mgr_ is not initialized");
        return -1;
    }
    return flight_ctrl_mgr_->listenHsiInfo(callback);
}

void DroneOsdModuleImpl::cancelListenHsiInfo(int listen_id) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("DroneOsdModuleImpl: flight_ctrl_mgr_ is not initialized");
        return;
    }
    flight_ctrl_mgr_->cancelListenHsiInfo(listen_id);
}

// 飞行器进入当前状态原因(mode_code_reason);
int DroneOsdModuleImpl::listenDroneModeCodeReason(const std::function<void(const DroneModeCodeReason& msg)>& callback) {
    return drone_mode_code_reason_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenDroneModeCodeReason(int listen_id) {
    drone_mode_code_reason_callbacks_.removeCallback(listen_id);
}

// 飞机4G Dongle信息(dongle_infos);
int DroneOsdModuleImpl::listenDroneDongleInfos(const std::function<void(const DroneDongleInfos& msg)>& callback) {
    return drone_dongle_infos_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenDroneDongleInfos(int listen_id) {
    drone_dongle_infos_callbacks_.removeCallback(listen_id);
}

// 限飞数据库版本(flysafe_database_version);
int DroneOsdModuleImpl::listenDroneFlysafeDatabaseVersion(const std::function<void(const DroneFlysafeDatabaseVersion& msg)>& callback) {
    return drone_flysafe_database_version_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenDroneFlysafeDatabaseVersion(int listen_id) {
    drone_flysafe_database_version_callbacks_.removeCallback(listen_id);
}

// 航线解析库版本(wpmz_version);
int DroneOsdModuleImpl::listenDroneWpmzVersion(const std::function<void(const DroneWpmzVersion& msg)>& callback) {
    return drone_wpmz_version_callbacks_.addCallback(callback);
}

void DroneOsdModuleImpl::cancelListenDroneWpmzVersion(int listen_id) {
    drone_wpmz_version_callbacks_.removeCallback(listen_id);
}
