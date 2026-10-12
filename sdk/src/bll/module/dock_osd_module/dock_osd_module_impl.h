// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/dock_osd_module.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class DockOsdModuleImpl : public IDockOsdModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    DockOsdModuleImpl(const std::string& dock_sn);
    ~DockOsdModuleImpl() override;

    int listenDockLocation(const std::function<void(const DockLocation& msg)>& callback) override;
    void cancelListenDockLocation(int listen_id) override;
    int listenDroneInDock(const std::function<void(const DroneInDock& msg)>& callback) override;
    void cancelListenDroneInDock(int listen_id) override;
    int listenDockCoverState(const std::function<void(const DockCoverState& msg)>& callback) override;
    void cancelListenDockCoverState(int listen_id) override;
    int listenDockDroneChargeState(const std::function<void(const DockDroneChargeState& msg)>& callback) override;
    void cancelListenDockDroneChargeState(int listen_id) override;
    int listenDockMeteorologicalInfo(const std::function<void(const DockMeteorologicalInfo& msg)>& callback) override;
    void cancelListenDockMeteorologicalInfo(int listen_id) override;
    int listenDockBackupBatteryInfo(const std::function<void(const DockBackupBatteryInfo& msg)>& callback) override;
    void cancelListenDockBackupBatteryInfo(int listen_id) override;
    int listenDockMaintainStatusInfo(const std::function<void(const DockMaintainStatusInfo& msg)>& callback) override;
    void cancelListenDockMaintainStatusInfo(int listen_id) override;
    int listenDockModeCode(const std::function<void(const DockModeCode& msg)>& callback) override;
    void cancelListenDockModeCode(int listen_id) override;
    int listenFlightTaskStepCode(const std::function<void(const FlightTaskStepCode& msg)>& callback) override;
    void cancelListenFlightTaskStepCode(int listen_id) override;
    int listenDockSubDevice(const std::function<void(const DockSubDeviceInfoList& msg)>& callback) override;
    void cancelListenDockSubDevice(int listen_id) override;
    int listenDockNetworkState(const std::function<void(const DockNetworkState& msg)>& callback) override;
    void cancelListenDockNetworkState(int listen_id) override;
    int listenDockMediaFileDetail(const std::function<void(const DockMediaFileDetail& msg)>& callback) override;
    void cancelListenDockMediaFileDetail(int listen_id) override;
    int listenDockWirelessLink(const std::function<void(const DockWirelessLink& msg)>& callback) override;
    void cancelListenDockWirelessLink(int listen_id) override;
    int listenDockJobNumber(const std::function<void(const DockJobNumber& msg)>& callback) override;
    void cancelListenDockJobNumber(int listen_id) override;
    int listenDockPowerState(const std::function<void(const DockPowerState& msg)>& callback) override;
    void cancelListenDockPowerState(int listen_id) override;
    int listenDockStorageState(const std::function<void(const DockStorageState& msg)>& callback) override;
    void cancelListenDockStorageState(int listen_id) override;
    int listenDockFirstPowerOnTime(const std::function<void(const DockFirstPowerOnTime& msg)>& callback) override;
    void cancelListenDockFirstPowerOnTime(int listen_id) override;
    int listenDockAccumulatedRunningTime(const std::function<void(const DockAccTime& msg)>& callback) override;
    void cancelListenDockAccumulatedRunningTime(int listen_id) override;
    int listenDockAlternateLandPoint(const std::function<void(const DockAlternateLandPoint& msg)>& callback) override;
    void cancelListenDockAlternateLandPoint(int listen_id) override;
    int listenDockPositionState(const std::function<void(const DockPositionState& msg)>& callback) override;
    void cancelListenDockPositionState(int listen_id) override;
    int listenDockActivationTime(const std::function<void(const DockActivationTime& msg)>& callback) override;
    void cancelListenDockActivationTime(int listen_id) override;
    int listenDockBatteryStoreMode(const std::function<void(const DockBatteryStoreMode& msg)>& callback) override;
    void cancelListenDockBatteryStoreMode(int listen_id) override;
    int listenDockAlarmState(const std::function<void(const DockAlarmState& msg)>& callback) override;
    void cancelListenDockAlarmState(int listen_id) override;
    int listenDockDroneBatteryMaintenanceInfo(const std::function<void(const DockDroneBatteryMaintenanceInfo& msg)>& callback) override;
    void cancelListenDockDroneBatteryMaintenanceInfo(int listen_id) override;
    int listenDockEmergencyStopState(const std::function<void(const DockEmergencyStopState& msg)>& callback) override;
    void cancelListenDockEmergencyStopState(int listen_id) override;
    int listenDockSilentMode(const std::function<void(const DockSilentMode& msg)>& callback) override;
    void cancelListenDockSilentMode(int listen_id) override;
    int listenDockUserExperienceImprovement(const std::function<void(const DockUserExperienceImprovement& msg)>& callback) override;
    void cancelListenDockUserExperienceImprovement(int listen_id) override;
    int listenDockDrcState(const std::function<void(const DockDrcState& msg)>& callback) override;
    void cancelListenDockDrcState(int listen_id) override;
    int listenDockPutterState(const std::function<void(const DockPutterState& msg)>& callback) override;
    void cancelListenDockPutterState(int listen_id) override;
    int listenDockRelativeAlternateLandPoint(const std::function<void(const DockRelativeAlternateLandPoint& msg)>& callback) override;
    void cancelListenDockRelativeAlternateLandPoint(int listen_id) override;
    int listenDockSelfConvergeCoordinate(const std::function<void(const DockSelfConvergeCoordinate& msg)>& callback) override;
    void cancelListenDockSelfConvergeCoordinate(int listen_id) override;
    int listenDockFirmwareInfo(const std::function<void(const DockFirmwareInfo& msg)>& callback) override;
    void cancelListenDockFirmwareInfo(int listen_id) override;
    int listenDockDongleInfos(const std::function<void(const DockDongleInfos& msg)>& callback) override;
    void cancelListenDockDongleInfos(int listen_id) override;
    int listenDockAirConditionerMode(const std::function<void(const DockAirConditionerMode& msg)>& callback) override;
    void cancelListenDockAirConditionerMode(int listen_id) override;

private:
    void registerNotificationCenterListeners();

    std::string dock_sn_;
    CallbackContainer<DockLocation> dock_location_callbacks_;
    CallbackContainer<DroneInDock> drone_in_dock_callbacks_;
    CallbackContainer<DockCoverState> dock_cover_state_callbacks_;
    CallbackContainer<DockDroneChargeState> dock_drone_charge_state_callbacks_;
    CallbackContainer<DockMeteorologicalInfo> dock_meteorological_info_callbacks_;
    CallbackContainer<DockBackupBatteryInfo> dock_backup_battery_info_callbacks_;
    CallbackContainer<DockMaintainStatusInfo> dock_maintain_status_info_callbacks_;
    CallbackContainer<DockModeCode> dock_mode_code_callbacks_;
    CallbackContainer<FlightTaskStepCode> flight_task_step_code_callbacks_;
    CallbackContainer<DockSubDeviceInfoList> dock_sub_device_info_list_callbacks_;
    CallbackContainer<DockNetworkState> dock_network_state_callbacks_;
    CallbackContainer<DockMediaFileDetail> dock_media_file_detail_callbacks_;
    CallbackContainer<DockWirelessLink> dock_wireless_link_callbacks_;
    CallbackContainer<DockJobNumber> dock_job_number_callbacks_;
    CallbackContainer<DockPowerState> dock_power_state_callbacks_;
    CallbackContainer<DockStorageState> dock_storage_state_callbacks_;
    CallbackContainer<DockFirstPowerOnTime> dock_first_power_on_time_callbacks_;
    CallbackContainer<DockAccTime> dock_acc_time_callbacks_;
    CallbackContainer<DockAlternateLandPoint> dock_alternate_land_point_callbacks_;
    CallbackContainer<DockPositionState> dock_position_state_callbacks_;
    CallbackContainer<DockActivationTime> dock_activation_time_callbacks_;
    CallbackContainer<DockBatteryStoreMode> dock_battery_store_mode_callbacks_;
    CallbackContainer<DockAlarmState> dock_alarm_state_callbacks_;
    CallbackContainer<DockDroneBatteryMaintenanceInfo> dock_drone_battery_maintenance_info_callbacks_;
    CallbackContainer<DockEmergencyStopState> dock_emergency_stop_state_callbacks_;
    CallbackContainer<DockSilentMode> dock_silent_mode_callbacks_;
    CallbackContainer<DockUserExperienceImprovement> dock_user_experience_improvement_callbacks_;
    CallbackContainer<DockDrcState> dock_drc_state_callbacks_;
    CallbackContainer<DockPutterState> dock_putter_state_callbacks_;
    CallbackContainer<DockRelativeAlternateLandPoint> dock_relative_alternate_land_point_callbacks_;
    CallbackContainer<DockSelfConvergeCoordinate> dock_self_converge_coordinate_callbacks_;
    CallbackContainer<DockFirmwareInfo> dock_firmware_info_callbacks_;
    CallbackContainer<DockDongleInfos> dock_dongle_infos_callbacks_;
    CallbackContainer<DockAirConditionerMode> dock_air_conditioner_mode_callbacks_;
    int dock_location_listen_id_ = -1;
    int drone_in_dock_listen_id_ = -1;
    int dock_cover_state_listen_id_ = -1;
    int dock_drone_charge_state_listen_id_ = -1;
    int dock_meteorological_info_listen_id_ = -1;
    int dock_backup_battery_info_listen_id_ = -1;
    int dock_maintain_status_info_listen_id_ = -1;
    int dock_mode_code_listen_id_ = -1;
    int flight_task_step_code_listen_id_ = -1;
    int dock_sub_device_info_list_listen_id_ = -1;
    int dock_network_state_listen_id_ = -1;
    int dock_media_file_detail_listen_id_ = -1;
    int dock_wireless_link_listen_id_ = -1;
    int dock_job_number_listen_id_ = -1;
    int dock_power_state_listen_id_ = -1;
    int dock_storage_state_listen_id_ = -1;
    int dock_first_power_on_time_listen_id_ = -1;
    int dock_acc_time_listen_id_ = -1;
    int dock_alternate_land_point_listen_id_ = -1;
    int dock_position_state_listen_id_ = -1;
    int dock_activation_time_listen_id_ = -1;
    int dock_battery_store_mode_listen_id_ = -1;
    int dock_alarm_state_listen_id_ = -1;
    int dock_drone_battery_maintenance_info_listen_id_ = -1;
    int dock_emergency_stop_state_listen_id_ = -1;
    int dock_silent_mode_listen_id_ = -1;
    int dock_user_experience_improvement_listen_id_ = -1;
    int dock_drc_state_listen_id_ = -1;
    int dock_putter_state_listen_id_ = -1;
    int dock_relative_alternate_land_point_listen_id_ = -1;
    int dock_self_converge_coordinate_listen_id_ = -1;
    int dock_firmware_info_listen_id_ = -1;
    int dock_dongle_infos_listen_id_ = -1;
    int dock_air_conditioner_mode_listen_id_ = -1;
};

}  // namespace flight_hub_sdk
