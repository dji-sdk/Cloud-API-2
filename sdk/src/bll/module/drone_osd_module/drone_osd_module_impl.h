// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/drone_osd_module.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class FlightCtrlMgr;
class PayloadCtrlMgr;

class DroneOsdModuleImpl : public IDroneOsdModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    DroneOsdModuleImpl(
                       const std::string& drone_sn,
                       const std::string& gateway_sn,
                       const std::string& log_tag,
                       std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr,
                       std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr);
    ~DroneOsdModuleImpl() override;

    int listenAircraftBatteryInfo(const std::function<void(const AircraftBatteryInfo& battery_info)>& callback) override;
    void cancelListenAircraftBatteryInfo(int listen_id) override;
    int listenAircraftAttitude(const std::function<void(const AircraftAttitude& attitude)>& callback) override;
    void cancelListenAircraftAttitude(int listen_id) override;
    int listenAircraftLocation(const std::function<void(const AircraftLocation& location)>& callback) override;
    void cancelListenAircraftLocation(int listen_id) override;
    int listenAircraftModeCode(const std::function<void(const AircraftModeCode& mode_code)>& callback) override;
    void cancelListenAircraftModeCode(int listen_id) override;
    int listenAircraftWindSpeed(const std::function<void(const AircraftWindSpeed& wind_speed)>& callback) override;
    void cancelListenAircraftWindSpeed(int listen_id) override;
    int listenFlightTasksException(const std::function<void(const FlightTaskException& msg)>& callback) override;
    void cancelListenFlightTasksException(int listen_id) override;
    int listenAircraftControlCode(const std::function<void(const AircraftControlCode& msg)>& callback) override;
    void cancelListenAircraftControlCode(int listen_id) override;
    int listenAircraftSpeed(const std::function<void(const AircraftSpeed& msg)>& callback) override;
    void cancelListenAircraftSpeed(int listen_id) override;
    int listenAircraftPayloadsCameraLiveviewWorldRegion(const std::function<void(const AircraftPayloadsCameraLiveviewWorldRegion& msg)>& callback) override;
    void cancelListenAircraftPayloadsCameraLiveviewWorldRegion(int listen_id) override;
    int listenAircraftPayloadsGimbalAttitude(const std::function<void(const AircraftPayloadsGimbalAttitude& msg)>& callback) override;
    void cancelListenAircraftPayloadsGimbalAttitude(int listen_id) override;
    int listenAircraftPayloadsList(const std::function<void(const AircraftPayloadsList& msg)>& callback) override;
    void cancelListenAircraftPayloadsList(int listen_id) override;
    int listenControlRightsCloudControlStatus(const std::function<void(const ControlRightsCloudControlStatus& msg)>& callback) override;
    void cancelListenControlRightsCloudControlStatus(int listen_id) override;
    int listenControlRightsDeviceControlStatus(const std::function<void(const ControlRightsDeviceControlStatus& msg)>& callback) override;
    void cancelListenControlRightsDeviceControlStatus(int listen_id) override;
    int listenFlightTasksMediaFileDisplayAvailable(const std::function<void(const FlightTasksMediaFileDisplayAvailable& msg)>& callback) override;
    void cancelListenFlightTasksMediaFileDisplayAvailable(int listen_id) override;
    int listenAircraftPayloadsOsdInfo(const std::function<void(const AircraftPayloadsOsdInfoList& msg)>& callback) override;
    void cancelListenAircraftPayloadsOsdInfo(int listen_id) override;
    int listenDroneModeCodeReason(const std::function<void(const DroneModeCodeReason& msg)>& callback) override;
    void cancelListenDroneModeCodeReason(int listen_id) override;
    int listenDroneDongleInfos(const std::function<void(const DroneDongleInfos& msg)>& callback) override;
    void cancelListenDroneDongleInfos(int listen_id) override;
    int listenDroneFlysafeDatabaseVersion(const std::function<void(const DroneFlysafeDatabaseVersion& msg)>& callback) override;
    void cancelListenDroneFlysafeDatabaseVersion(int listen_id) override;
    int listenDroneWpmzVersion(const std::function<void(const DroneWpmzVersion& msg)>& callback) override;
    void cancelListenDroneWpmzVersion(int listen_id) override;
    int listenAircraftGear(const std::function<void(const AircraftGear& msg)>& callback) override;
    void cancelListenAircraftGear(int listen_id) override;
    int listenAircraftHomeDistance(const std::function<void(const AircraftHomeDistance& msg)>& callback) override;
    void cancelListenAircraftHomeDistance(int listen_id) override;
    int listenAircraftPositionState(const std::function<void(const AircraftPositionState& msg)>& callback) override;
    void cancelListenAircraftPositionState(int listen_id) override;
    int listenAircraftControlSource(const std::function<void(const AircraftControlSource& msg)>& callback) override;
    void cancelListenAircraftControlSource(int listen_id) override;
    int listenAircraftLimitsInfo(const std::function<void(const AircraftLimitsInfo& msg)>& callback) override;
    void cancelListenAircraftLimitsInfo(int listen_id) override;
    int listenAircraftBatteryWarningThresholds(const std::function<void(const AircraftBatteryWarningThresholds& msg)>& callback) override;
    void cancelListenAircraftBatteryWarningThresholds(int listen_id) override;
    int listenAircraftFirmwareStatus(const std::function<void(const AircraftFirmwareStatus& msg)>& callback) override;
    void cancelListenAircraftFirmwareStatus(int listen_id) override;
    int listenAircraftTotalFlightInfo(const std::function<void(const AircraftTotalFlightInfo& msg)>& callback) override;
    void cancelListenAircraftTotalFlightInfo(int listen_id) override;
    int listenAircraftActivationTime(const std::function<void(const AircraftActivationTime& msg)>& callback) override;
    void cancelListenAircraftActivationTime(int listen_id) override;
    int listenAircraftMaintainStatusInfo(const std::function<void(const AircraftMaintainStatusInfo& msg)>& callback) override;
    void cancelListenAircraftMaintainStatusInfo(int listen_id) override;
    int listenAircraftNightLightsState(const std::function<void(const AircraftNightLightsState& msg)>& callback) override;
    void cancelListenAircraftNightLightsState(int listen_id) override;
    int listenAircraftObstacleAvoidanceState(const std::function<void(const AircraftObstacleAvoidanceState& msg)>& callback) override;
    void cancelListenAircraftObstacleAvoidanceState(int listen_id) override;
    int listenAircraftStorageInfo(const std::function<void(const AircraftStorageInfo& msg)>& callback) override;
    void cancelListenAircraftStorageInfo(int listen_id) override;
    int listenAircraftTrackId(const std::function<void(const AircraftTrackId& msg)>& callback) override;
    void cancelListenAircraftTrackId(int listen_id) override;
    int listenAircraftComplianceInfo(const std::function<void(const AircraftComplianceInfo& msg)>& callback) override;
    void cancelListenAircraftComplianceInfo(int listen_id) override;
    int listenAircraftPayloadsCamerasList(const std::function<void(const AircraftPayloadsCamerasList& msg)>& callback) override;
    void cancelListenAircraftPayloadsCamerasList(int listen_id) override;
    int listenAircraftRcLostAction(const std::function<void(const AircraftRcLostAction& msg)>& callback) override;
    void cancelListenAircraftRcLostAction(int listen_id) override;
    int listenAircraftRthInfo(const std::function<void(const AircraftRthInfo& msg)>& callback) override;
    void cancelListenAircraftRthInfo(int listen_id) override;
    int listenAircraftHomeGeographicalInfo(const std::function<void(const AircraftHomeGeographicalInfo& msg)>& callback) override;
    void cancelListenAircraftHomeGeographicalInfo(int listen_id) override;
    int listenAircraftCurrentRthMode(const std::function<void(const AircraftCurrentRthMode& msg)>& callback) override;
    void cancelListenAircraftCurrentRthMode(int listen_id) override;
    int listenHsiInfo(const std::function<void(const HsiInfo&)>& callback) override;
    void cancelListenHsiInfo(int listen_id) override;

private:
    void registerNotificationCenterListeners();
    void bindPayloadsListCallback();

    std::string drone_sn_;
    std::string gateway_sn_;
    std::string log_tag_;
    std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr_;
    std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr_;

    CallbackContainer<AircraftBatteryInfo> aircraft_battery_info_callbacks_;
    CallbackContainer<AircraftAttitude> aircraft_attitude_callbacks_;
    CallbackContainer<AircraftLocation> aircraft_location_callbacks_;
    CallbackContainer<AircraftModeCode> aircraft_mode_code_callbacks_;
    CallbackContainer<AircraftWindSpeed> aircraft_wind_speed_callbacks_;
    CallbackContainer<FlightTaskException> flight_task_exception_callbacks_;
    CallbackContainer<AircraftControlCode> aircraft_control_code_callbacks_;
    CallbackContainer<AircraftSpeed> aircraft_speed_callbacks_;
    CallbackContainer<AircraftPayloadsCameraLiveviewWorldRegion> aircraft_payloads_camera_liveview_world_region_callbacks_;
    CallbackContainer<AircraftPayloadsGimbalAttitude> aircraft_payloads_gimbal_attitude_callbacks_;
    CallbackContainer<AircraftPayloadsList> aircraft_payloads_list_callbacks_;
    CallbackContainer<ControlRightsCloudControlStatus> control_rights_cloud_control_status_callbacks_;
    CallbackContainer<ControlRightsDeviceControlStatus> control_rights_device_control_status_callbacks_;
    CallbackContainer<FlightTasksMediaFileDisplayAvailable> flight_tasks_media_file_display_available_callbacks_;
    CallbackContainer<AircraftPayloadsOsdInfoList> aircraft_payloads_osd_info_list_callbacks_;
    CallbackContainer<AircraftGear> aircraft_gear_callbacks_;
    CallbackContainer<AircraftHomeDistance> aircraft_home_distance_callbacks_;
    CallbackContainer<AircraftPositionState> aircraft_position_state_callbacks_;
    CallbackContainer<AircraftControlSource> aircraft_control_source_callbacks_;
    CallbackContainer<AircraftLimitsInfo> aircraft_limits_info_callbacks_;
    CallbackContainer<AircraftBatteryWarningThresholds> aircraft_battery_warning_thresholds_callbacks_;
    CallbackContainer<AircraftFirmwareStatus> aircraft_firmware_status_callbacks_;
    CallbackContainer<AircraftTotalFlightInfo> aircraft_total_flight_info_callbacks_;
    CallbackContainer<AircraftActivationTime> aircraft_activation_time_callbacks_;
    CallbackContainer<AircraftMaintainStatusInfo> aircraft_maintain_status_info_callbacks_;
    CallbackContainer<AircraftNightLightsState> aircraft_night_lights_state_callbacks_;
    CallbackContainer<AircraftObstacleAvoidanceState> aircraft_obstacle_avoidance_state_callbacks_;
    CallbackContainer<AircraftStorageInfo> aircraft_storage_info_callbacks_;
    CallbackContainer<AircraftTrackId> aircraft_track_id_callbacks_;
    CallbackContainer<AircraftComplianceInfo> aircraft_compliance_info_callbacks_;
    CallbackContainer<AircraftPayloadsCamerasList> aircraft_payloads_cameras_list_callbacks_;
    CallbackContainer<AircraftRcLostAction> aircraft_rc_lost_action_callbacks_;
    CallbackContainer<AircraftRthInfo> aircraft_rth_info_callbacks_;
    CallbackContainer<AircraftHomeGeographicalInfo> aircraft_home_geographical_info_callbacks_;
    CallbackContainer<AircraftCurrentRthMode> aircraft_current_rth_mode_callbacks_;
    CallbackContainer<DroneModeCodeReason> drone_mode_code_reason_callbacks_;
    CallbackContainer<DroneDongleInfos> drone_dongle_infos_callbacks_;
    CallbackContainer<DroneFlysafeDatabaseVersion> drone_flysafe_database_version_callbacks_;
    CallbackContainer<DroneWpmzVersion> drone_wpmz_version_callbacks_;

    int aircraft_battery_info_listen_id_ = -1;
    int aircraft_attitude_listen_id_ = -1;
    int aircraft_location_listen_id_ = -1;
    int aircraft_mode_code_listen_id_ = -1;
    int aircraft_wind_speed_listen_id_ = -1;
    int flight_task_exception_listen_id_ = -1;
    int aircraft_control_code_listen_id_ = -1;
    int aircraft_speed_listen_id_ = -1;
    int aircraft_payloads_camera_liveview_world_region_listen_id_ = -1;
    int aircraft_payloads_gimbal_attitude_listen_id_ = -1;
    int control_rights_cloud_control_status_listen_id_ = -1;
    int control_rights_device_control_status_listen_id_ = -1;
    int flight_tasks_media_file_display_available_listen_id_ = -1;
    int aircraft_payloads_osd_info_listen_id_ = -1;
    int aircraft_gear_listen_id_ = -1;
    int aircraft_home_distance_listen_id_ = -1;
    int aircraft_position_state_listen_id_ = -1;
    int aircraft_control_source_listen_id_ = -1;
    int aircraft_limits_info_listen_id_ = -1;
    int aircraft_battery_warning_thresholds_listen_id_ = -1;
    int aircraft_firmware_status_listen_id_ = -1;
    int aircraft_total_flight_info_listen_id_ = -1;
    int aircraft_activation_time_listen_id_ = -1;
    int aircraft_maintain_status_info_listen_id_ = -1;
    int aircraft_night_lights_state_listen_id_ = -1;
    int aircraft_obstacle_avoidance_state_listen_id_ = -1;
    int aircraft_storage_info_listen_id_ = -1;
    int aircraft_track_id_listen_id_ = -1;
    int aircraft_compliance_info_listen_id_ = -1;
    int aircraft_payloads_cameras_list_listen_id_ = -1;
    int aircraft_rc_lost_action_listen_id_ = -1;
    int aircraft_rth_info_listen_id_ = -1;
    int aircraft_home_geographical_info_listen_id_ = -1;
    int aircraft_current_rth_mode_listen_id_ = -1;
    int drone_mode_code_reason_listen_id_ = -1;
    int drone_dongle_infos_listen_id_ = -1;
    int drone_flysafe_database_version_listen_id_ = -1;
    int drone_wpmz_version_listen_id_ = -1;
};

}  // namespace flight_hub_sdk
