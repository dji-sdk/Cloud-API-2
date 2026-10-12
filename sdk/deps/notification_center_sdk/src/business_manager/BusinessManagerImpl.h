// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <functional>
#include <memory>

#include "notification_center_sdk_api/BusinessManager.h"

#include "topic_engine/TopicManager.h"
#include "topic_message_all_define.h"
#include "common/util/async_capture_protect.h"

namespace notification_center {
    // 业务逻辑：消息订阅/取消订阅，处理业务逻辑，提供接口
    class BusinessManagerImpl : public BusinessManager, public drc_sdk::common::AsyncCaptureProtect{
    public:
        explicit BusinessManagerImpl(std::weak_ptr<TopicManager> topic_mgr);

        void CancelObserve(int64_t listen_id) override;

    private:
        // 通用模板方法，用于处理所有监听接口
        template<typename MsgCallbackType, typename TopicType>
        int64_t ListenTopic(
            const MsgCallbackType &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            const std::string &topic_name,
            NotificationFrequency freq) {
            auto strong_topic_mgr = topic_mgr_.lock();
            if (strong_topic_mgr == nullptr) {
                LOG_ERROR("[BusinessManager] ListenTopic failed, no topic_mgr_, device_sn: %s, topic_name: %s, freq: %d", device_sn.c_str(), topic_name.c_str(), freq);
                return NotificationCenterErrorCode_NotConnected;
            }

            WeakDummy(weak_ptr);
            auto listen_id = strong_topic_mgr->Observe(device_sn, topic_name, freq,
                [this, weak_ptr, on_msg_cb = on_messages_callback, device_sn, topic_name](const std::shared_ptr<PublishTopicWrapper> &message)-> void {
                    WeakDummyReturn(weak_ptr);
                    if (on_msg_cb && message) {
                        const auto msg = std::make_shared<TopicType>(message);
                        if (!msg->isValid()) {
                            LOG_DEBUG("[BusinessManager] ListenTopic [%s - %s] invalid! observe: %s",device_sn.c_str(), topic_name.c_str(),msg->ToJsonString().c_str());
                            return;
                        }
                        LOG_DEBUG("[BusinessManager] ListenTopic [%s - %s] observe: %s",device_sn.c_str(), topic_name.c_str(),msg->ToJsonString().c_str());
                        on_msg_cb(msg->getPodClass());
                    }
            }, [this, weak_ptr, on_result_cb = on_result_callback](int err)-> void {
                WeakDummyReturn(weak_ptr);
                if (on_result_cb) {
                    on_result_cb(static_cast<NotificationCenterErrorCode>(err));
                }
            });

            return listen_id;
        }

        // 辅助宏，用于简化监听接口的定义
#define DEFINE_LISTEN_METHOD(MethodName, MsgCallbackType, TopicType, TopicName) \
int64_t MethodName( \
    const MsgCallbackType& on_messages_callback, \
    const std::function<void(const NotificationCenterErrorCode &error_code)>& on_result_callback, \
    const std::string& device_sn, \
    NotificationFrequency freq) override { \
        return ListenTopic<MsgCallbackType, TopicType>(on_messages_callback, on_result_callback, device_sn, TopicName, freq); \
    }

        std::weak_ptr<TopicManager> topic_mgr_;



    public:
        // 具体的监听接口，使用宏定义
        DEFINE_LISTEN_METHOD(ListenAircraftAttitude, std::function<void(const AircraftAttitude &msg)>, PublishAircraftAttitudeTopic, "topic_drone_attitude")
        DEFINE_LISTEN_METHOD(ListenAircraftBatteryInfo, std::function<void(const AircraftBatteryInfo &msg)>, PublishAircraftBatteryInfoTopic, "topic_drone_battery_info")
        DEFINE_LISTEN_METHOD(ListenAircraftControlCode, std::function<void(const AircraftControlCode &msg)>, PublishAircraftControlCodeTopic, "topic_drone_control_code")
        DEFINE_LISTEN_METHOD(ListenAircraftLocation, std::function<void(const AircraftLocation &msg)>, PublishAircraftLocationTopic, "topic_drone_location")
        DEFINE_LISTEN_METHOD(ListenAircraftModeCode, std::function<void(const AircraftModeCode &msg)>, PublishAircraftModeCodeTopic, "topic_drone_mode_code")
        DEFINE_LISTEN_METHOD(ListenAircraftPayloadsCameraLiveviewWorldRegion, std::function<void(const AircraftPayloadsCameraLiveviewWorldRegion &msg)>, PublishAircraftPayloadsCameraLiveviewWorldRegionTopic, "topic_drone_payloads_liveview_world_region")
        DEFINE_LISTEN_METHOD(ListenAircraftPayloadsCameraMeasureTargets, std::function<void(const AircraftPayloadsCameraMeasureTargets &msg)>, PublishAircraftPayloadsCameraMeasureTargetsTopic, "topic_drone_payloads_camera_measure_targets")
        DEFINE_LISTEN_METHOD(ListenAircraftPayloadsGimbalAttitude, std::function<void(const AircraftPayloadsGimbalAttitude &msg)>, PublishAircraftPayloadsGimbalAttitudeTopic, "topic_drone_payloads_gimbal_attitude")
        DEFINE_LISTEN_METHOD(ListenAircraftPayloadsList, std::function<void(const AircraftPayloadsList &msg)>, PublishAircraftPayloadsListTopic, "topic_drone_payloads_list")
        DEFINE_LISTEN_METHOD(ListenAircraftSpeed, std::function<void(const AircraftSpeed &msg)>, PublishAircraftSpeedTopic, "topic_drone_speed")
        DEFINE_LISTEN_METHOD(ListenAircraftTakeoffProgress, std::function<void(const AircraftTakeoffProgress &msg)>, PublishAircraftTakeoffProgressTopic, "topic_drone_takeoff_point_progress")
        DEFINE_LISTEN_METHOD(ListenAircraftWindSpeed, std::function<void(const AircraftWindSpeed &msg)>, PublishAircraftWindSpeedTopic, "topic_drone_wind_speed")
        DEFINE_LISTEN_METHOD(ListenAircraftHMS, std::function<void(const HmsData &msg)>, PublishAircraftHmsTopic, "topic_drone_hms")
        DEFINE_LISTEN_METHOD(ListenAircraftPayloadsOsdInfo, std::function<void(const AircraftPayloadsOsdInfoList &msg)>, PublishAircraftPayloadsOsdInfoTopic, "topic_drone_payloads_osd_info")
        DEFINE_LISTEN_METHOD(ListenAircraftGear, std::function<void(const AircraftGear &msg)>, PublishAircraftGearTopic, "topic_drone_gear")
        DEFINE_LISTEN_METHOD(ListenAircraftHomeDistance, std::function<void(const AircraftHomeDistance &msg)>, PublishAircraftHomeDistanceTopic, "topic_drone_home_distance")
        DEFINE_LISTEN_METHOD(ListenAircraftPositionState, std::function<void(const AircraftPositionState &msg)>, PublishAircraftPositionStateTopic, "topic_drone_position_state")
        DEFINE_LISTEN_METHOD(ListenAircraftControlSource, std::function<void(const AircraftControlSource &msg)>, PublishAircraftControlSourceTopic, "topic_drone_control_source")
        DEFINE_LISTEN_METHOD(ListenAircraftLimitsInfo, std::function<void(const AircraftLimitsInfo &msg)>, PublishAircraftLimitsInfoTopic, "topic_drone_limits_info")
        DEFINE_LISTEN_METHOD(ListenAircraftBatteryWarningThresholds, std::function<void(const AircraftBatteryWarningThresholds &msg)>, PublishAircraftBatteryWarningThresholdsTopic, "topic_drone_battery_warning_thresholds")
        DEFINE_LISTEN_METHOD(ListenAircraftFirmwareStatus, std::function<void(const AircraftFirmwareStatus &msg)>, PublishAircraftFirmwareStatusTopic, "topic_drone_firmware_status")
        DEFINE_LISTEN_METHOD(ListenAircraftTotalFlightInfo, std::function<void(const AircraftTotalFlightInfo &msg)>, PublishAircraftTotalFlightInfoTopic, "topic_drone_total_flight_info")
        DEFINE_LISTEN_METHOD(ListenAircraftActivationTime, std::function<void(const AircraftActivationTime &msg)>, PublishAircraftActivationTimeTopic, "topic_drone_activation_time")
        DEFINE_LISTEN_METHOD(ListenAircraftMaintainStatusInfo, std::function<void(const AircraftMaintainStatusInfo &msg)>, PublishAircraftMaintainStatusInfoTopic, "topic_drone_maintain_status_info")
        DEFINE_LISTEN_METHOD(ListenAircraftNightLightsState, std::function<void(const AircraftNightLightsState &msg)>, PublishAircraftNightLightsStateTopic, "topic_drone_night_lights_state")
        DEFINE_LISTEN_METHOD(ListenAircraftObstacleAvoidanceState, std::function<void(const AircraftObstacleAvoidanceState &msg)>, PublishAircraftObstacleAvoidanceStateTopic, "topic_drone_obstacle_avoidance_state")
        DEFINE_LISTEN_METHOD(ListenAircraftStorageInfo, std::function<void(const AircraftStorageInfo &msg)>, PublishAircraftStorageInfoTopic, "topic_drone_storage_info")
        DEFINE_LISTEN_METHOD(ListenAircraftTrackId, std::function<void(const AircraftTrackId &msg)>, PublishAircraftTrackIdTopic, "topic_drone_track_id")
        DEFINE_LISTEN_METHOD(ListenAircraftComplianceInfo, std::function<void(const AircraftComplianceInfo &msg)>, PublishAircraftComplianceInfoTopic, "topic_drone_compliance_info")
        DEFINE_LISTEN_METHOD(ListenAircraftPayloadsCamerasList, std::function<void(const AircraftPayloadsCamerasList &msg)>, PublishAircraftPayloadsCamerasListTopic, "topic_drone_payloads_cameras_list")
        DEFINE_LISTEN_METHOD(ListenAircraftRcLostAction, std::function<void(const AircraftRcLostAction &msg)>, PublishAircraftRcLostActionTopic, "topic_drone_rc_lost_action")
        DEFINE_LISTEN_METHOD(ListenAircraftRthInfo, std::function<void(const AircraftRthInfo &msg)>, PublishAircraftRthInfoTopic, "topic_drone_rth_info")
        DEFINE_LISTEN_METHOD(ListenAircraftHomeGeographicalInfo, std::function<void(const AircraftHomeGeographicalInfo &msg)>, PublishAircraftHomeGeographicalInfoTopic, "topic_drone_home_geographical_info")
        DEFINE_LISTEN_METHOD(ListenAircraftCurrentRthMode, std::function<void(const AircraftCurrentRthMode &msg)>, PublishAircraftCurrentRthModeTopic, "topic_drone_current_rth_mode")

        DEFINE_LISTEN_METHOD(ListenControlRightsCloudControlStatus, std::function<void(const ControlRightsCloudControlStatus &msg)>, PublishControlRightsCloudControlStatusTopic, "topic_drone_cloud_control_status")
        DEFINE_LISTEN_METHOD(ListenControlRightsDeviceControlStatus, std::function<void(const ControlRightsDeviceControlStatus &msg)>, PublishControlRightsDeviceControlStatusTopic, "topic_drone_cloud_control_status")
        DEFINE_LISTEN_METHOD(ListenDeviceOnlineStatus, std::function<void(const DeviceOnlineStatus &msg)>, PublishDeviceOnlineStatusTopic, "topic_device_online_status")
        DEFINE_LISTEN_METHOD(ListenDeviceTopoChanged, std::function<void(const DeviceTopoChange &msg)>, PublishDeviceTopoChangeTopic, "topic_device_topo_change")
        DEFINE_LISTEN_METHOD(ListenDeviceReboot, std::function<void(const DeviceReboot &msg)>, PublishDeviceRebootTopic, "topic_device_reboot")
        DEFINE_LISTEN_METHOD(ListenDeviceFormat, std::function<void(const DeviceFormat &msg)>, PublishDeviceFormatTopic, "topic_device_format")
        DEFINE_LISTEN_METHOD(ListenDeviceRtkCalibration, std::function<void(const DeviceRtkCalibration &msg)>, PublishDeviceRtkCalibrationTopic, "topic_device_rtk_calibration")
        DEFINE_LISTEN_METHOD(ListenDeviceSimSlotSwitch, std::function<void(const DeviceSimSlotSwitch &msg)>, PublishDeviceSimSlotSwitchTopic, "topic_device_sim_slot_switch")
        DEFINE_LISTEN_METHOD(ListenDeviceEsimOperatorSwitch, std::function<void(const DeviceEsimOperatorSwitch &msg)>, PublishDeviceEsimOperatorSwitchTopic, "topic_device_esim_operator_switch")
        DEFINE_LISTEN_METHOD(ListenDeviceCloudEnvironmentSwitch, std::function<void(const DeviceCloudEnvironmentSwitch &msg)>, PublishDeviceCloudEnvironmentSwitchTopic, "topic_cloud_environment_switch")
        DEFINE_LISTEN_METHOD(ListenDeviceNetworkSpeedTestProgress, std::function<void(const DeviceNetworkSpeedTestProgress &msg)>, PublishDeviceNetworkSpeedTestProgressTopic, "topic_device_network_speed_test_progress")

        DEFINE_LISTEN_METHOD(ListenDockAirConditionerStatusInfo, std::function<void(const DockAirConditionerStatusInfo &msg)>, PublishDockAirConditionerStatusInfoTopic, "topic_dock_air_conditioner_status_info")
        DEFINE_LISTEN_METHOD(ListenDockLocation, std::function<void(const DockLocation &msg)>, PublishDockLocationTopic, "topic_dock_location")
        DEFINE_LISTEN_METHOD(ListenDockDeparturePoint, std::function<void(const DockDeparturePoint &msg)>, PublishDockDeparturePointTopic, "topic_dock_departure_point")
        DEFINE_LISTEN_METHOD(ListenDockSupplementLightState, std::function<void(const DockSupplementLightState &msg)>, PublishDockSupplementLightStateTopic, "topic_dock_supplement_light_state")
        DEFINE_LISTEN_METHOD(ListenDroneInDock, std::function<void(const DroneInDock &msg)>, PublishDroneInDockTopic, "topic_dock_has_drone")
        DEFINE_LISTEN_METHOD(ListenDroneOpen, std::function<void(const DroneOpen &msg)>, PublishDroneOpenTopic, "topic_drone_open")
        DEFINE_LISTEN_METHOD(ListenDroneClose, std::function<void(const DroneClose &msg)>, PublishDroneCloseTopic, "topic_drone_close")
        DEFINE_LISTEN_METHOD(ListenDroneChargeOpen, std::function<void(const DroneChargeOpen &msg)>, PublishDroneChargeOpenTopic, "topic_drone_charge_open")
        DEFINE_LISTEN_METHOD(ListenDroneChargeClose, std::function<void(const DroneChargeClose &msg)>, PublishDroneChargeCloseTopic, "topic_drone_charge_close")
        DEFINE_LISTEN_METHOD(ListenDroneFormat, std::function<void(const DroneFormat &msg)>, PublishDroneFormatTopic, "topic_drone_format")
        DEFINE_LISTEN_METHOD(ListenDockCoverState, std::function<void(const DockCoverState &msg)>, PublishDockCoverStateTopic, "topic_dock_cover_state")
        DEFINE_LISTEN_METHOD(ListenDockCoverOpen, std::function<void(const DockCoverOpen &msg)>, PublishDockCoverOpenTopic, "topic_dock_cover_open")
        DEFINE_LISTEN_METHOD(ListenDockCoverClose, std::function<void(const DockCoverClose &msg)>, PublishDockCoverCloseTopic, "topic_dock_cover_close")
        DEFINE_LISTEN_METHOD(ListenDockDroneChargeState, std::function<void(const DockDroneChargeState &msg)>, PublishDockDroneChargeStateTopic, "topic_dock_drone_charge_state")
        DEFINE_LISTEN_METHOD(ListenDockMeteorologicalInfo, std::function<void(const DockMeteorologicalInfo &msg)>, PublishDockMeteorologicalInfoTopic, "topic_dock_meteorological_info")
        DEFINE_LISTEN_METHOD(ListenDockBackupBatteryInfo, std::function<void(const DockBackupBatteryInfo &msg)>, PublishDockBackupBatteryInfoTopic, "topic_dock_backup_battery_info")
        DEFINE_LISTEN_METHOD(ListenDockMaintainStatusInfo, std::function<void(const DockMaintainStatusInfo &msg)>, PublishDockMaintainStatusInfoTopic, "topic_dock_maintain_status_info")
        DEFINE_LISTEN_METHOD(ListenDockHMS, std::function<void(const HmsData &msg)>, PublishDockHmsTopic, "topic_dock_hms")
        DEFINE_LISTEN_METHOD(ListenDockModeCode, std::function<void(const DockModeCode &msg)>, PublishDockModeCodeTopic, "topic_dock_mode_code")
        DEFINE_LISTEN_METHOD(ListenFlightTaskStepCode, std::function<void(const FlightTaskStepCode &msg)>, PublishFlightTaskStepCodeTopic, "topic_task_flight_task_step_code")
        DEFINE_LISTEN_METHOD(ListenDockSubDevice, std::function<void(const DockSubDeviceInfoList &msg)>, PublishDockSubDeviceTopic, "topic_dock_sub_device")
        DEFINE_LISTEN_METHOD(ListenDockNetworkState, std::function<void(const DockNetworkState &msg)>, PublishDockNetworkStateTopic, "topic_dock_network_state")
        DEFINE_LISTEN_METHOD(ListenDockMediaFileDetail, std::function<void(const DockMediaFileDetail &msg)>, PublishDockMediaFileDetailTopic, "topic_dock_media_file_detail")
        DEFINE_LISTEN_METHOD(ListenDockWirelessLink, std::function<void(const DockWirelessLink &msg)>, PublishDockWirelessLinkTopic, "topic_dock_wireless_link")
        DEFINE_LISTEN_METHOD(ListenDockJobNumber, std::function<void(const DockJobNumber &msg)>, PublishDockJobNumberTopic, "topic_dock_job_number")
        DEFINE_LISTEN_METHOD(ListenDockPowerState, std::function<void(const DockPowerState &msg)>, PublishDockPowerStateTopic, "topic_dock_power_state")
        DEFINE_LISTEN_METHOD(ListenDockStorageState, std::function<void(const DockStorageState &msg)>, PublishDockStorageStateTopic, "topic_dock_storage_state")
        DEFINE_LISTEN_METHOD(ListenDockFirstPowerOnTime, std::function<void(const DockFirstPowerOnTime &msg)>, PublishDockFirstPowerOnTimeTopic, "topic_dock_first_power_on_time")
        DEFINE_LISTEN_METHOD(ListenDockAccumulatedRunningTime, std::function<void(const DockAccTime &msg)>, PublishDockAccTimeTopic, "topic_dock_accumulated_running_time")

        DEFINE_LISTEN_METHOD(ListenDockAlternateLandPoint, std::function<void(const DockAlternateLandPoint &msg)>, PublishDockAlternateLandPointTopic, "topic_dock_alternate_land_point")
        DEFINE_LISTEN_METHOD(ListenDockPositionState, std::function<void(const DockPositionState &msg)>, PublishDockPositionStateTopic, "topic_dock_position_state")
        DEFINE_LISTEN_METHOD(ListenDockActivationTime, std::function<void(const DockActivationTime &msg)>, PublishDockActivationTimeTopic, "topic_dock_activation_time")
        DEFINE_LISTEN_METHOD(ListenDockBatteryStoreMode, std::function<void(const DockBatteryStoreMode &msg)>, PublishDockBatteryStoreModeTopic, "topic_dock_battery_store_mode")
        DEFINE_LISTEN_METHOD(ListenDockAlarmState, std::function<void(const DockAlarmState &msg)>, PublishDockAlarmStateTopic, "topic_dock_alarm_state")
        DEFINE_LISTEN_METHOD(ListenDockDroneBatteryMaintenanceInfo, std::function<void(const DockDroneBatteryMaintenanceInfo &msg)>, PublishDockDroneBatteryMaintenanceInfoTopic, "topic_dock_drone_battery_maintenance_info")
        DEFINE_LISTEN_METHOD(ListenDockEmergencyStopState, std::function<void(const DockEmergencyStopState &msg)>, PublishDockEmergencyStopStateTopic, "topic_dock_emergency_stop_state")
        DEFINE_LISTEN_METHOD(ListenDockSilentMode, std::function<void(const DockSilentMode &msg)>, PublishDockSilentModeTopic, "topic_dock_silent_mode")
        DEFINE_LISTEN_METHOD(ListenDockUserExperienceImprovement, std::function<void(const DockUserExperienceImprovement &msg)>, PublishDockUserExperienceImprovementTopic, "topic_dock_user_experience_improvement")
        DEFINE_LISTEN_METHOD(ListenDockDrcState, std::function<void(const DockDrcState &msg)>, PublishDockDrcStateTopic, "topic_dock_drc_state")
        DEFINE_LISTEN_METHOD(ListenDockPutterState, std::function<void(const DockPutterState &msg)>, PublishDockPutterStateTopic, "topic_dock_putter_state")
        DEFINE_LISTEN_METHOD(ListenDockRelativeAlternateLandPoint, std::function<void(const DockRelativeAlternateLandPoint &msg)>, PublishDockRelativeAlternateLandPointTopic, "topic_dock_relative_alternate_land_point")
        DEFINE_LISTEN_METHOD(ListenDockSelfConvergeCoordinate, std::function<void(const DockSelfConvergeCoordinate &msg)>, PublishDockSelfConvergeCoordinateTopic, "topic_dock_self_converge_coordinate")
        DEFINE_LISTEN_METHOD(ListenDockFirmwareInfo, std::function<void(const DockFirmwareInfo &msg)>, PublishDockFirmwareInfoTopic, "topic_dock_firmware_info")


        DEFINE_LISTEN_METHOD(ListenFlightTasksException, std::function<void(const FlightTaskException &msg)>, PublishFlightTaskExceptionTopic, "topic_task_flight_exception")
        DEFINE_LISTEN_METHOD(ListenFlightTasksMediaFileDisplayAvailable, std::function<void(const FlightTasksMediaFileDisplayAvailable &msg)>, PublishFlightTasksMediaFileDisplayAvailableTopic, "topic_task_media_file_display_available")

        DEFINE_LISTEN_METHOD(ListenFlyToDroneSelfPlanPath, std::function<void(const FlyToDroneSelfPlanPath &msg)>, PublishFlyToDroneSelfPlanPathTopic, "topic_drone_self_plan_path")
        DEFINE_LISTEN_METHOD(ListenFlyToPointProgress, std::function<void(const FlyToPointProgress &msg)>, PublishFlyToPointProgressTopic, "topic_drone_flyto_point_progress")
        
        DEFINE_LISTEN_METHOD(ListenLiveRecordInfo, std::function<void(const LiveRecordInfo &msg)>, PublishLiveRecordInfoTopic, "topic_live_record_info")
        
        DEFINE_LISTEN_METHOD(ListenWorkspaceDeviceChanged, std::function<void(const WorkspaceDeviceChange &msg)>, PublishWorkspaceDeviceChangeTopic, "topic_workspace_device_change")

        DEFINE_LISTEN_METHOD(ListenAircraftVisualScoutStatus, std::function<void(const AircraftVisualScoutStatus &msg)>, PublishAircraftVisualScoutStatusTopic, "topic_task_visual_scout_status")
        DEFINE_LISTEN_METHOD(ListenAircraftVisualScoutAbnormalInfo, std::function<void(const AircraftVisualScoutAbnormalInfo &msg)>, PublishAircraftVisualScoutAbnormalInfoTopic, "topic_task_visual_scout_abnormal_info")
        DEFINE_LISTEN_METHOD(ListenAircraftVisualScoutObstacleInfo, std::function<void(const AircraftVisualScoutObstacleInfo &msg)>, PublishAircraftVisualScoutObstacleInfoTopic, "topic_task_visual_scout_obstacle_info")

        DEFINE_LISTEN_METHOD(ListenAircraftEphemerisDownloadState, std::function<void(const EphemerisDownloadState &msg)>, PublishAircraftEphemerisDownloadStateTopic, "topic_task_ephemeris_download_state")
        DEFINE_LISTEN_METHOD(ListenAircraftEphemerisInfo, std::function<void(const EphemerisInfo &msg)>, PublishAircraftEphemerisInfoTopic, "topic_drone_ephemeris_info")

        DEFINE_LISTEN_METHOD(ListenMediaFilePostProcessMessage, std::function<void(const MediaFilePostProcess& msg)>, PublishMediaFilePostProcessTopic, "topic_media_file_post_process")
        DEFINE_LISTEN_METHOD(ListenMediaFileDeviceUploadMessage, std::function<void(const MediaFileDeviceUpload& msg)>, PublishMediaFileDeviceUploadTopic, "topic_media_file_device_upload")
        DEFINE_LISTEN_METHOD(ListenReconstructionStatusChangedMessage, std::function<void(const ReconstructionStatusChanged& msg)>, PublishReconstructionStatusChangedTopic, "topic_reconstruction_status_changed_event")
        DEFINE_LISTEN_METHOD(ListenTerraUnzipProgress, std::function<void(const TerraUnzipProgress& msg)>, PublishTerraUnzipProgressTopic, "topic_reconstruction_unzip_progress")
        DEFINE_LISTEN_METHOD(ListenTerraModelStatusChanged, std::function<void(const TerraModelStatusChanged& msg)>, PublishTerraModelStatusChangedTopic, "topic_model_status_changed")
        DEFINE_LISTEN_METHOD(ListenTerraFileShowOnMap, std::function<void(const TerraFileShowOnMap& msg)>, PublishTerraFileShowOnMapTopic, "topic_model_file_show_on_map")
        DEFINE_LISTEN_METHOD(ListenTerraMediaFileHideOnMap, std::function<void(const TerraMediaFileHideOnMap& msg)>, PublishTerraMediaFileHideOnMapTopic, "topic_media_file_hide_on_map")
        DEFINE_LISTEN_METHOD(ListenTerraMediaFileShowOnMap, std::function<void(const TerraMediaFileShowOnMap& msg)>, PublishTerraMediaFileShowOnMapTopic, "topic_media_file_show_on_map")

        DEFINE_LISTEN_METHOD(ListenImportWaylineEvent, std::function<void(const ImportWaylineEvent &msg)>, PublishImportWaylineEventTopic, "topic_import_wayline_event")
        DEFINE_LISTEN_METHOD(ListenCommandTaskEvent, std::function<void(const CommandTaskEvent &msg)>, PublishCommandTaskEventTopic, "topic_atc_command_task_event")
        DEFINE_LISTEN_METHOD(ListenFlyToTaskEvent, std::function<void(const FlyToTaskEvent &msg)>, PublishFlyToTaskEventTopic, "topic_atc_flyto_task_event")
        DEFINE_LISTEN_METHOD(ListenPoiTaskEvent, std::function<void(const PoiTaskEvent &msg)>, PublishPoiTaskEventTopic, "topic_atc_poi_task_event")
        DEFINE_LISTEN_METHOD(ListenWaylineTaskEvent, std::function<void(const WaylineTaskEvent &msg)>, PublishWaylineTaskEventTopic, "topic_atc_wayline_task_event")
        DEFINE_LISTEN_METHOD(ListenInFlightWaylineTaskNotify, std::function<void(const InFlightWaylineTaskNotify &msg)>, PublishInFlightWaylineTaskNotifyTopic, "in_flight_wayline_task_notify")

        DEFINE_LISTEN_METHOD(ListenDeviceOtaProgress, std::function<void(const DeviceOtaProgress &msg)>, PublishDeviceOtaProgressTopic, "topic_device_ota_progress")
        DEFINE_LISTEN_METHOD(ListenDeviceFileUploadProgress, std::function<void(const DeviceFileUploadProgress &msg)>, PublishDeviceFileUploadProgressTopic, "topic_device_fileupload_progress")
        DEFINE_LISTEN_METHOD(ListenPsdkSpeakerAudioPlayProgress, std::function<void(const PsdkSpeakerPlayProgress &msg)>, PublishPsdkSpeakerAudioPlayProgressTopic, "topic_psdk_speaker_audio_play_start_progress")
        DEFINE_LISTEN_METHOD(ListenPsdkSpeakerTtsPlayProgress, std::function<void(const PsdkSpeakerPlayProgress &msg)>, PublishPsdkSpeakerTtsPlayProgressTopic, "topic_psdk_speaker_tts_play_start_progress")
        DEFINE_LISTEN_METHOD(ListenFlightPoiProgress, std::function<void(const FlightPoiProgress &msg)>, PublishFlightPoiProgressTopic, "topic_task_flight_poi_progress")
        // Element WS topics
        DEFINE_LISTEN_METHOD(ListenElementCreate, std::function<void(const ElementCreate &msg)>, PublishElementCreateTopic, "topic_element_create")
        DEFINE_LISTEN_METHOD(ListenElementUpdate, std::function<void(const ElementUpdate &msg)>, PublishElementUpdateTopic, "topic_element_update")
        DEFINE_LISTEN_METHOD(ListenElementDelete, std::function<void(const ElementDelete &msg)>, PublishElementDeleteTopic, "topic_element_delete")
        DEFINE_LISTEN_METHOD(ListenTaskTopoChanged, std::function<void(const TaskTopoChanged &msg)>, PublishTaskTopoChangedTopic, "topic_task_topo_changed")

        // EA340 消息推送补全 · Phase A (koc3_EA340支持/00_消息推送补全)
        DEFINE_LISTEN_METHOD(ListenDockLiveStatus, std::function<void(const DockLiveStatus &msg)>, PublishDockLiveStatusTopic, "topic_dock_live_status")
        DEFINE_LISTEN_METHOD(ListenDockLiveCapacity, std::function<void(const DockLiveCapacity &msg)>, PublishDockLiveCapacityTopic, "topic_dock_live_capacity")
        DEFINE_LISTEN_METHOD(ListenDockDongleInfos, std::function<void(const DockDongleInfos &msg)>, PublishDockDongleInfosTopic, "topic_dock_dongle_infos")
        DEFINE_LISTEN_METHOD(ListenDockAirConditionerMode, std::function<void(const DockAirConditionerMode &msg)>, PublishDockAirConditionerModeTopic, "topic_dock_air_conditioner_mode")
        DEFINE_LISTEN_METHOD(ListenDroneFlysafeDatabaseVersion, std::function<void(const DroneFlysafeDatabaseVersion &msg)>, PublishDroneFlysafeDatabaseVersionTopic, "topic_drone_flysafe_database_version")
        DEFINE_LISTEN_METHOD(ListenDronePsdkUiResource, std::function<void(const DronePsdkUiResource &msg)>, PublishDronePsdkUiResourceTopic, "topic_drone_psdk_ui_resource")
        DEFINE_LISTEN_METHOD(ListenDroneModeCodeReason, std::function<void(const DroneModeCodeReason &msg)>, PublishDroneModeCodeReasonTopic, "topic_drone_mode_code_reason")
        DEFINE_LISTEN_METHOD(ListenDroneCommanderFlightMode, std::function<void(const DroneCommanderFlightMode &msg)>, PublishDroneCommanderFlightModeTopic, "topic_drone_commander_flight_mode")
        DEFINE_LISTEN_METHOD(ListenDroneCurrentCommanderFlightMode, std::function<void(const DroneCurrentCommanderFlightMode &msg)>, PublishDroneCurrentCommanderFlightModeTopic, "topic_drone_current_commander_flight_mode")
        DEFINE_LISTEN_METHOD(ListenDroneCommanderModeLostAction, std::function<void(const DroneCommanderModeLostAction &msg)>, PublishDroneCommanderModeLostActionTopic, "topic_drone_commander_mode_lost_action")
        DEFINE_LISTEN_METHOD(ListenDroneWpmzVersion, std::function<void(const DroneWpmzVersion &msg)>, PublishDroneWpmzVersionTopic, "topic_drone_wpmz_version")
        DEFINE_LISTEN_METHOD(ListenDroneOfflineMapEnable, std::function<void(const DroneOfflineMapEnable &msg)>, PublishDroneOfflineMapEnableTopic, "topic_drone_offline_map_enable")
        DEFINE_LISTEN_METHOD(ListenDroneDongleInfos, std::function<void(const DroneDongleInfos &msg)>, PublishDroneDongleInfosTopic, "topic_drone_dongle_infos")
        DEFINE_LISTEN_METHOD(ListenDroneCameraWatermarkSettings, std::function<void(const DroneCameraWatermarkSettingsPush &msg)>, PublishDroneCameraWatermarkSettingsTopic, "topic_drone_camera_watermark_settings")

#undef DEFINE_LISTEN_METHOD
    };
}
