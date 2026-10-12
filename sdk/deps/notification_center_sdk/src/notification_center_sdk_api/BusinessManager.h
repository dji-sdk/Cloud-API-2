// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <string>
#include "Common.h"
#include "Message.h"
#include "APIExport.h"

namespace notification_center {
    // 业务管理器抽象接口
    class NOTIFICATION_CENTER_API BusinessManager {
    public:
        virtual ~BusinessManager() = default;

        // 取消订阅
        virtual void CancelObserve(int64_t listen_id) = 0;

        // 监听飞机姿态
        virtual int64_t ListenAircraftAttitude(
            const std::function<void(const AircraftAttitude &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机电池信息
        virtual int64_t ListenAircraftBatteryInfo(
            const std::function<void(const AircraftBatteryInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机控制码
        virtual int64_t ListenAircraftControlCode(
            const std::function<void(const AircraftControlCode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机位置
        virtual int64_t ListenAircraftLocation(
            const std::function<void(const AircraftLocation &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机模式码
        virtual int64_t ListenAircraftModeCode(
            const std::function<void(const AircraftModeCode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机载荷相机实时视图世界区域
        virtual int64_t ListenAircraftPayloadsCameraLiveviewWorldRegion(
            const std::function<void(const AircraftPayloadsCameraLiveviewWorldRegion &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机载荷相机激光打点信息;
        virtual int64_t ListenAircraftPayloadsCameraMeasureTargets(
            const std::function<void(const AircraftPayloadsCameraMeasureTargets &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机载荷云台姿态
        virtual int64_t ListenAircraftPayloadsGimbalAttitude(
            const std::function<void(const AircraftPayloadsGimbalAttitude &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机载荷列表
        virtual int64_t ListenAircraftPayloadsList(
            const std::function<void(const AircraftPayloadsList &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机速度
        virtual int64_t ListenAircraftSpeed(
            const std::function<void(const AircraftSpeed &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机起飞进度事件
        virtual int64_t ListenAircraftTakeoffProgress(
            const std::function<void(const AircraftTakeoffProgress &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机风速
        virtual int64_t ListenAircraftWindSpeed(
            const std::function<void(const AircraftWindSpeed &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞机 HMS 告警类信息;
        virtual int64_t ListenAircraftHMS(
            const std::function<void(const HmsData &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听云端控制权限状态
        virtual int64_t ListenControlRightsCloudControlStatus(
            const std::function<void(const ControlRightsCloudControlStatus &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备控制权限状态
        virtual int64_t ListenControlRightsDeviceControlStatus(
            const std::function<void(const ControlRightsDeviceControlStatus &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备在线状态
        virtual int64_t ListenDeviceOnlineStatus(
            const std::function<void(const DeviceOnlineStatus &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备拓扑变更
        virtual int64_t ListenDeviceTopoChanged(
            const std::function<void(const DeviceTopoChange &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备重启 (bid, timestamp, result, status)
        virtual int64_t ListenDeviceReboot(
            const std::function<void(const DeviceReboot &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场位置
        virtual int64_t ListenDockLocation(
            const std::function<void(const DockLocation &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场离场点
        virtual int64_t ListenDockDeparturePoint(
            const std::function<void(const DockDeparturePoint &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场中的无人机
        virtual int64_t ListenDroneInDock(
            const std::function<void(const DroneInDock &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场舱盖状态;
        virtual int64_t ListenDockCoverState(
            const std::function<void(const DockCoverState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场舱盖开启结果 (bid, timestamp, result, status)
        virtual int64_t ListenDockCoverOpen(
            const std::function<void(const DockCoverOpen &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场舱盖关闭结果 (bid, timestamp, result, status)
        virtual int64_t ListenDockCoverClose(
            const std::function<void(const DockCoverClose &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备格式化结果 (bid, timestamp, result, status)
        virtual int64_t ListenDeviceFormat(
            const std::function<void(const DeviceFormat &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备 RTK 校准结果 (bid, timestamp, result, status)
        virtual int64_t ListenDeviceRtkCalibration(
            const std::function<void(const DeviceRtkCalibration &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备 SIM 卡槽切换结果 (bid, timestamp, result, status)
        virtual int64_t ListenDeviceSimSlotSwitch(
            const std::function<void(const DeviceSimSlotSwitch &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备 eSIM 运营商切换结果 (bid, timestamp, result, status)
        virtual int64_t ListenDeviceEsimOperatorSwitch(
            const std::function<void(const DeviceEsimOperatorSwitch &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备云环境切换结果 (bid, timestamp, result, status, progress)
        virtual int64_t ListenDeviceCloudEnvironmentSwitch(
            const std::function<void(const DeviceCloudEnvironmentSwitch &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听设备网络测速进度 (bid, timestamp, result, status, speed_result)
        virtual int64_t ListenDeviceNetworkSpeedTestProgress(
            const std::function<void(const DeviceNetworkSpeedTestProgress &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听无人机开机结果 (bid, timestamp, result, status)
        virtual int64_t ListenDroneOpen(
            const std::function<void(const DroneOpen &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听无人机关机结果 (bid, timestamp, result, status)
        virtual int64_t ListenDroneClose(
            const std::function<void(const DroneClose &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听无人机充电开启结果 (bid, timestamp, result, status)
        virtual int64_t ListenDroneChargeOpen(
            const std::function<void(const DroneChargeOpen &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听无人机充电关闭结果 (bid, timestamp, result, status)
        virtual int64_t ListenDroneChargeClose(
            const std::function<void(const DroneChargeClose &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听无人机格式化结果 (bid, timestamp, result, status)
        virtual int64_t ListenDroneFormat(
            const std::function<void(const DroneFormat &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场飞机充电状态;
        virtual int64_t ListenDockDroneChargeState(
            const std::function<void(const DockDroneChargeState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场气象信息;
        virtual int64_t ListenDockMeteorologicalInfo(
            const std::function<void(const DockMeteorologicalInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场备用电池信息;
        virtual int64_t ListenDockBackupBatteryInfo(
            const std::function<void(const DockBackupBatteryInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场保养信息;
        virtual int64_t ListenDockMaintainStatusInfo(
            const std::function<void(const DockMaintainStatusInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场 HMS 告警类信息;
        virtual int64_t ListenDockHMS(
            const std::function<void(const HmsData &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场 mode code;
        virtual int64_t ListenDockModeCode(
            const std::function<void(const DockModeCode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞行任务 step code;
        virtual int64_t ListenFlightTaskStepCode(
            const std::function<void(const FlightTaskStepCode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场: 子设备信息列表(sub_device);
        virtual int64_t ListenDockSubDevice(
            const std::function<void(const DockSubDeviceInfoList &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞行任务异常
        virtual int64_t ListenFlightTasksException(
            const std::function<void(const FlightTaskException &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 航线任务上传的媒体在媒体库可加载通知
        virtual int64_t ListenFlightTasksMediaFileDisplayAvailable(
            const std::function<void(const FlightTasksMediaFileDisplayAvailable &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听无人机自主规划路径
        virtual int64_t ListenFlyToDroneSelfPlanPath(
            const std::function<void(const FlyToDroneSelfPlanPath &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听飞向点进度
        virtual int64_t ListenFlyToPointProgress(
            const std::function<void(const FlyToPointProgress &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听工作空间设备变更
        virtual int64_t ListenWorkspaceDeviceChanged(
            const std::function<void(const WorkspaceDeviceChange &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场空调工作模式
        virtual int64_t ListenDockAirConditionerStatusInfo(
            const std::function<void(const DockAirConditionerStatusInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场补光灯状态
        virtual int64_t ListenDockSupplementLightState(
            const std::function<void(const DockSupplementLightState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听直播录制状态推送（topic_live_record_info）
        virtual int64_t ListenLiveRecordInfo(
            const std::function<void(const LiveRecordInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 监听机场网路状态(network_state);
        virtual int64_t ListenDockNetworkState(
            const std::function<void(const DockNetworkState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 媒体文件数量(media_file_detail);
        virtual int64_t ListenDockMediaFileDetail(
            const std::function<void(const DockMediaFileDetail &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场图传质量(wireless_link);
        virtual int64_t ListenDockWirelessLink(
            const std::function<void(const DockWirelessLink &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场作业次数(job_number);
        virtual int64_t ListenDockJobNumber(
            const std::function<void(const DockJobNumber &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场 DockPowerState;
        virtual int64_t ListenDockPowerState(
            const std::function<void(const DockPowerState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场存储容量(storage);
        virtual int64_t ListenDockStorageState(
            const std::function<void(const DockStorageState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场首次上电时间(first_power_on);
        virtual int64_t ListenDockFirstPowerOnTime(
            const std::function<void(const DockFirstPowerOnTime &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场累计运行时长(acc_time);
        virtual int64_t ListenDockAccumulatedRunningTime(
            const std::function<void(const DockAccTime &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 备降点(alternate_land_point);
        virtual int64_t ListenDockAlternateLandPoint(
            const std::function<void(const DockAlternateLandPoint &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 搜星状态(position_state);
        virtual int64_t ListenDockPositionState(
            const std::function<void(const DockPositionState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场激活时间(activation_time, unix时间戳 s);
        virtual int64_t ListenDockActivationTime(
            const std::function<void(const DockActivationTime &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 电池(存储)模式(battery_store_mode);
        virtual int64_t ListenDockBatteryStoreMode(
            const std::function<void(const DockBatteryStoreMode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场声光报警状态(alarm_state);
        virtual int64_t ListenDockAlarmState(
            const std::function<void(const DockAlarmState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场飞行器电池保养信息(drone_battery_maintenance_info);
        // DockDroneBatteryMaintenanceInfo;
        virtual int64_t ListenDockDroneBatteryMaintenanceInfo(
            const std::function<void(const DockDroneBatteryMaintenanceInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 紧急停止按钮状态(emergency_stop_state);
        // DockEmergencyStopState;
        virtual int64_t ListenDockEmergencyStopState(
            const std::function<void(const DockEmergencyStopState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场静音模式(silent_mode);
        // DockSilentMode;
        virtual int64_t ListenDockSilentMode(
            const std::function<void(const DockSilentMode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 用户体验改善计划(user_experience_improvement);
        // DockUserExperienceImprovement;
        virtual int64_t ListenDockUserExperienceImprovement(
            const std::function<void(const DockUserExperienceImprovement &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // DRC 链路的状态(drc_state);
        // DockDrcState;
        virtual int64_t ListenDockDrcState(
            const std::function<void(const DockDrcState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 推杆状态(putter_state);
        // DockPutterState;
        virtual int64_t ListenDockPutterState(
            const std::function<void(const DockPutterState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 相对备降点(relative_alternate_land_point);
        // DockRelativeAlternateLandPoint;
        virtual int64_t ListenDockRelativeAlternateLandPoint(
            const std::function<void(const DockRelativeAlternateLandPoint &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 自收敛坐标(self_converge_coordinate);
        // DockSelfConvergeCoordinate;
        virtual int64_t ListenDockSelfConvergeCoordinate(
            const std::function<void(const DockSelfConvergeCoordinate &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机负载 OSD 信息列表({type-subtype-gimbalindex} + payloads);
        // 负载状态osd + 负载状态;
        // 飞机负载信息列表;
        // AircraftPayloadsOsdInfoList
        virtual int64_t ListenAircraftPayloadsOsdInfo(
            const std::function<void(const AircraftPayloadsOsdInfoList &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机: 档位信息(gear);
        // AircraftGear;
        virtual int64_t ListenAircraftGear(
            const std::function<void(const AircraftGear &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机: Home点距离(home_distance);
        // AircraftHomeDistance;
        virtual int64_t ListenAircraftHomeDistance(
            const std::function<void(const AircraftHomeDistance &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机: 搜星状态(position_state);
        // AircraftPositionState;
        virtual int64_t ListenAircraftPositionState(
            const std::function<void(const AircraftPositionState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机: 当前控制源(control_source);
        // AircraftControlSource;
        virtual int64_t ListenAircraftControlSource(
            const std::function<void(const AircraftControlSource &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机: 限制类信息状态;
        // AircraftLimitsInfo;
        virtual int64_t ListenAircraftLimitsInfo(
            const std::function<void(const AircraftLimitsInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 电量告警信息(用户设置的阈值);
        // AircraftBatteryWarningThresholds;
        virtual int64_t ListenAircraftBatteryWarningThresholds(
            const std::function<void(const AircraftBatteryWarningThresholds &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 固件状态信息;
        // AircraftFirmwareStatus;
        virtual int64_t ListenAircraftFirmwareStatus(
            const std::function<void(const AircraftFirmwareStatus &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 累计飞行信息(total_flight_info);
        // AircraftTotalFlightInfo;
        virtual int64_t ListenAircraftTotalFlightInfo(
            const std::function<void(const AircraftTotalFlightInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞行器激活时间(unix时间戳)(activation_time);
        // AircraftActivationTime;
        virtual int64_t ListenAircraftActivationTime(
            const std::function<void(const AircraftActivationTime &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机：保养信息(maintain_status);
        // AircraftMaintainStatusInfo;
        virtual int64_t ListenAircraftMaintainStatusInfo(
            const std::function<void(const AircraftMaintainStatusInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞行器夜航灯状态(night_lights_state);
        // AircraftNightLightsState;
        virtual int64_t ListenAircraftNightLightsState(
            const std::function<void(const AircraftNightLightsState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞行器避障状态(obstacle_avoidance);
        // AircraftObstacleAvoidanceState;
        virtual int64_t ListenAircraftObstacleAvoidanceState(
            const std::function<void(const AircraftObstacleAvoidanceState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 存储容量(storage);
        // AircraftStorageInfo;
        virtual int64_t ListenAircraftStorageInfo(
            const std::function<void(const AircraftStorageInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 航迹id(track_id);
        // AircraftTrackId;
        virtual int64_t ListenAircraftTrackId(
            const std::function<void(const AircraftTrackId &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 合规相关信息(compliance information);
        // AircraftComplianceInfo;
        virtual int64_t ListenAircraftComplianceInfo(
            const std::function<void(const AircraftComplianceInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞行器相机信息(cameras);
        // AircraftPayloadsCamerasList；
        virtual int64_t ListenAircraftPayloadsCamerasList(
            const std::function<void(const AircraftPayloadsCamerasList &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 遥控器失控动作(rc_lost_action);
        // AircraftRcLostAction;
        virtual int64_t ListenAircraftRcLostAction(
            const std::function<void(const AircraftRcLostAction &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 返航信息(返航模式, 返航高度);
        // AircraftRthInfo;
        virtual int64_t ListenAircraftRthInfo(
            const std::function<void(const AircraftRthInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机: Home点地理坐标信息(Home点经度, Home点纬度);
        // AircraftHomeGeographicalInfo;
        virtual int64_t ListenAircraftHomeGeographicalInfo(
            const std::function<void(const AircraftHomeGeographicalInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场固件信息;
        // DockFirmwareInfo
        virtual int64_t ListenDockFirmwareInfo(
            const std::function<void(const DockFirmwareInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 返航高度模式当前值(current_rth_mode);
        // AircraftCurrentRthMode;
        virtual int64_t ListenAircraftCurrentRthMode(
            const std::function<void(const AircraftCurrentRthMode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 进离场任务状态;
        // AircraftVisualScoutStatus;
        virtual int64_t ListenAircraftVisualScoutStatus(
            const std::function<void(const AircraftVisualScoutStatus &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 进离场任务异常消息;
        // AircraftVisualScoutAbnormalInfo;
        virtual int64_t ListenAircraftVisualScoutAbnormalInfo(
            const std::function<void(const AircraftVisualScoutAbnormalInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 障碍物过近;
        // AircraftVisualScoutObstacleInfo;
        virtual int64_t ListenAircraftVisualScoutObstacleInfo(
            const std::function<void(const AircraftVisualScoutObstacleInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 星历下载状态;
        // EphemerisDownloadState;
        virtual int64_t ListenAircraftEphemerisDownloadState(
            const std::function<void(const EphemerisDownloadState &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 星历信息;
        // EphemerisInfo;
        virtual int64_t ListenAircraftEphemerisInfo(
            const std::function<void(const EphemerisInfo &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 媒体服务: 设备媒体后处理完成通知("topic_media_file_post_process");
        // MediaFilePostProcess;
        virtual int64_t ListenMediaFilePostProcessMessage(
            const std::function<void(const MediaFilePostProcess &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 媒体服务: 设备媒体回传通知("topic_media_file_device_upload");
        // MediaFileDeviceUpload;
        virtual int64_t ListenMediaFileDeviceUploadMessage(
            const std::function<void(const MediaFileDeviceUpload &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // ImportWaylineEvent;
        virtual int64_t ListenImportWaylineEvent(
            const std::function<void(const ImportWaylineEvent &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // topic_reconstruction_status_changed_event — 重建任务状态变更推送
        virtual int64_t ListenReconstructionStatusChangedMessage(
            const std::function<void(const ReconstructionStatusChanged &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // topic_reconstruction_unzip_progress — 模型解压进度推送
        virtual int64_t ListenTerraUnzipProgress(
            const std::function<void(const TerraUnzipProgress &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // topic_model_status_changed — 模型状态变更推送
        virtual int64_t ListenTerraModelStatusChanged(
            const std::function<void(const TerraModelStatusChanged &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // topic_model_file_show_on_map — 模型文件展示地图状态推送
        virtual int64_t ListenTerraFileShowOnMap(
            const std::function<void(const TerraFileShowOnMap &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // topic_media_file_hide_on_map — 模型隐藏地图推送（来自 file-storage）
        virtual int64_t ListenTerraMediaFileHideOnMap(
            const std::function<void(const TerraMediaFileHideOnMap &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // topic_media_file_show_on_map — 模型显示地图推送（来自 file-storage）
        virtual int64_t ListenTerraMediaFileShowOnMap(
            const std::function<void(const TerraMediaFileShowOnMap &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 指令任务事件 ("topic_atc_command_task_event")
        virtual int64_t ListenCommandTaskEvent(
            const std::function<void(const CommandTaskEvent &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 指点飞行任务事件 ("topic_atc_flyto_task_event")
        virtual int64_t ListenFlyToTaskEvent(
            const std::function<void(const FlyToTaskEvent &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // POI 任务事件 ("topic_atc_poi_task_event")
        virtual int64_t ListenPoiTaskEvent(
            const std::function<void(const PoiTaskEvent &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 航线任务事件 ("topic_atc_wayline_task_event")
        virtual int64_t ListenWaylineTaskEvent(
            const std::function<void(const WaylineTaskEvent &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 空中航线任务进度推送 ("in_flight_wayline_task_notify")
        virtual int64_t ListenInFlightWaylineTaskNotify(
            const std::function<void(const InFlightWaylineTaskNotify &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 固件升级进度 ("topic_device_ota_progress")
        virtual int64_t ListenDeviceOtaProgress(
            const std::function<void(const DeviceOtaProgress &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 设备异常报告上传进度 ("topic_device_fileupload_progress")
        virtual int64_t ListenDeviceFileUploadProgress(
            const std::function<void(const DeviceFileUploadProgress &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // PSDK喊话器音频播放进度 ("topic_psdk_speaker_audio_play_start_progress")
        virtual int64_t ListenPsdkSpeakerAudioPlayProgress(
            const std::function<void(const PsdkSpeakerPlayProgress &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // PSDK喊话器TTS播放进度 ("topic_psdk_speaker_tts_play_start_progress")
        virtual int64_t ListenPsdkSpeakerTtsPlayProgress(
            const std::function<void(const PsdkSpeakerPlayProgress &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // POI飞行进度通知 ("topic_task_flight_poi_progress")
        virtual int64_t ListenFlightPoiProgress(
            const std::function<void(const FlightPoiProgress &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // ── Element WS topics ────────────────────────────────────────────────
        virtual int64_t ListenElementCreate(
            const std::function<void(const ElementCreate &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn, NotificationFrequency freq) = 0;

        virtual int64_t ListenElementUpdate(
            const std::function<void(const ElementUpdate &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn, NotificationFrequency freq) = 0;

        virtual int64_t ListenElementDelete(
            const std::function<void(const ElementDelete &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn, NotificationFrequency freq) = 0;
        // 漫游任务 topo 变更 ("topic_task_topo_changed")
        virtual int64_t ListenTaskTopoChanged(
            const std::function<void(const TaskTopoChanged &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // ── EA340 消息推送补全 · Phase A (koc3_EA340支持/00_消息推送补全) ──────────

        // 机场直播状态推送 ("topic_dock_live_status")
        virtual int64_t ListenDockLiveStatus(
            const std::function<void(const DockLiveStatus &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场直播能力推送 ("topic_dock_live_capacity")
        virtual int64_t ListenDockLiveCapacity(
            const std::function<void(const DockLiveCapacity &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场4G Dongle信息推送 ("topic_dock_dongle_infos")
        virtual int64_t ListenDockDongleInfos(
            const std::function<void(const DockDongleInfos &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 机场空调工作模式推送 ("topic_dock_air_conditioner_mode")
        virtual int64_t ListenDockAirConditionerMode(
            const std::function<void(const DockAirConditionerMode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 限飞数据库版本推送 ("topic_drone_flysafe_database_version")
        virtual int64_t ListenDroneFlysafeDatabaseVersion(
            const std::function<void(const DroneFlysafeDatabaseVersion &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // PSDK UI资源推送 ("topic_drone_psdk_ui_resource")
        virtual int64_t ListenDronePsdkUiResource(
            const std::function<void(const DronePsdkUiResource &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞行器进入当前状态原因推送 ("topic_drone_mode_code_reason")
        virtual int64_t ListenDroneModeCodeReason(
            const std::function<void(const DroneModeCodeReason &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 指点飞行模式设置值推送 ("topic_drone_commander_flight_mode")
        virtual int64_t ListenDroneCommanderFlightMode(
            const std::function<void(const DroneCommanderFlightMode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 指点飞行模式当前值推送 ("topic_drone_current_commander_flight_mode")
        virtual int64_t ListenDroneCurrentCommanderFlightMode(
            const std::function<void(const DroneCurrentCommanderFlightMode &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 指点飞行失控动作推送 ("topic_drone_commander_mode_lost_action")
        virtual int64_t ListenDroneCommanderModeLostAction(
            const std::function<void(const DroneCommanderModeLostAction &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 航线解析库版本推送 ("topic_drone_wpmz_version")
        virtual int64_t ListenDroneWpmzVersion(
            const std::function<void(const DroneWpmzVersion &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 离线地图开关推送 ("topic_drone_offline_map_enable")
        virtual int64_t ListenDroneOfflineMapEnable(
            const std::function<void(const DroneOfflineMapEnable &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机4G Dongle信息推送 ("topic_drone_dongle_infos")
        virtual int64_t ListenDroneDongleInfos(
            const std::function<void(const DroneDongleInfos &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

        // 飞机相机水印设置推送 ("topic_drone_camera_watermark_settings")
        virtual int64_t ListenDroneCameraWatermarkSettings(
            const std::function<void(const DroneCameraWatermarkSettingsPush &msg)> &on_messages_callback,
            const std::function<void(const NotificationCenterErrorCode &error_code)> &on_result_callback,
            const std::string &device_sn,
            NotificationFrequency freq) = 0;

    };
}
