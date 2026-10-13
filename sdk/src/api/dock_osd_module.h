// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include "message.h"

namespace flight_hub_sdk {

/// 机场 OSD 数据订阅模块
class IDockOsdModule {
public:
    virtual ~IDockOsdModule() = default;

    /// @brief 订阅机场位置信息
    virtual int listenDockLocation(const std::function<void(const DockLocation &msg)>& callback) = 0;
    /// @brief 取消订阅机场位置信息
    virtual void cancelListenDockLocation(int listen_id) = 0;

    /// @brief 订阅机场中的无人机状态
    /// @param callback 无人机在机场状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_has_drone
    /// @endinternal
    virtual int listenDroneInDock(const std::function<void(const DroneInDock &msg)>& callback) = 0;
    /// @brief 取消订阅机场中的无人机状态
    virtual void cancelListenDroneInDock(int listen_id) = 0;

    /// @brief 订阅机场舱盖状态
    /// @param callback 舱盖状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_cover_state
    /// @endinternal
    virtual int listenDockCoverState(const std::function<void(const DockCoverState &msg)> & callback) = 0;
    /// @brief 取消订阅机场舱盖状态
    virtual void cancelListenDockCoverState(int listen_id) = 0;

    /// @brief 订阅机场飞机充电状态
    /// @param callback 充电状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_drone_charge_state
    /// @endinternal
    virtual int listenDockDroneChargeState(const std::function<void(const DockDroneChargeState &msg)> & callback) = 0;
    /// @brief 取消订阅机场飞机充电状态
    virtual void cancelListenDockDroneChargeState(int listen_id) = 0;

    /// @brief 订阅机场气象信息
    /// @param callback 气象信息回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_meteorological_info
    /// @endinternal
    virtual int listenDockMeteorologicalInfo(const std::function<void(const DockMeteorologicalInfo &msg)> & callback) = 0;
    /// @brief 取消订阅机场气象信息
    virtual void cancelListenDockMeteorologicalInfo(int listen_id) = 0;

    /// @brief 订阅机场备用电池信息
    /// @param callback 备用电池信息回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_backup_battery_info
    /// @endinternal
    virtual int listenDockBackupBatteryInfo(const std::function<void(const DockBackupBatteryInfo &msg)> & callback) = 0;
    /// @brief 取消订阅机场备用电池信息
    virtual void cancelListenDockBackupBatteryInfo(int listen_id) = 0;

    /// @brief 订阅机场保养状态信息
    /// @param callback 保养状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_maintain_status_info
    /// @endinternal
    virtual int listenDockMaintainStatusInfo(const std::function<void(const DockMaintainStatusInfo &msg)> & callback) = 0;
    /// @brief 取消订阅机场保养状态信息
    virtual void cancelListenDockMaintainStatusInfo(int listen_id) = 0;

    /// @brief 订阅机场 ModeCode
    /// @param callback ModeCode 回调（`DockModeCode::mode_code` 取值见 `message.h` 类注释）
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_mode_code
    /// @endinternal
    virtual int listenDockModeCode(const std::function<void(const DockModeCode &msg)>& callback) = 0;
    /// @brief 取消订阅机场 ModeCode
    virtual void cancelListenDockModeCode(int listen_id) = 0;

    /// @brief 订阅飞行任务步骤码
    /// @param callback 步骤码回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_task_flight_task_step_code
    /// @endinternal
    virtual int listenFlightTaskStepCode(const std::function<void(const FlightTaskStepCode &msg)>& callback) = 0;
    /// @brief 取消订阅飞行任务步骤码
    virtual void cancelListenFlightTaskStepCode(int listen_id) = 0;

    /// @brief 订阅机场子设备信息列表
    /// @param callback 子设备列表回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_sub_device
    /// @endinternal
    virtual int listenDockSubDevice(const std::function<void(const DockSubDeviceInfoList &msg)>& callback) = 0;
    /// @brief 取消订阅机场子设备信息列表
    virtual void cancelListenDockSubDevice(int listen_id) = 0;

    /// @brief 订阅机场网络状态
    /// @param callback 网络状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_network_state
    /// @endinternal
    virtual int listenDockNetworkState(const std::function<void(const DockNetworkState &msg)>& callback) = 0;
    /// @brief 取消订阅机场网络状态
    virtual void cancelListenDockNetworkState(int listen_id) = 0;

    /// @brief 订阅机场媒体文件详情
    /// @param callback 媒体文件详情回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_media_file_detail
    /// @endinternal
    virtual int listenDockMediaFileDetail(const std::function<void(const DockMediaFileDetail &msg)>& callback) = 0;
    /// @brief 取消订阅机场媒体文件详情
    virtual void cancelListenDockMediaFileDetail(int listen_id) = 0;

    /// @brief 订阅机场图传链路质量
    /// @param callback 图传质量回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_wireless_link
    /// @endinternal
    virtual int listenDockWirelessLink(const std::function<void(const DockWirelessLink &msg)>& callback) = 0;
    /// @brief 取消订阅机场图传链路质量
    virtual void cancelListenDockWirelessLink(int listen_id) = 0;

    /// @brief 订阅机场作业次数
    /// @param callback 作业次数回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_job_number
    /// @endinternal
    virtual int listenDockJobNumber(const std::function<void(const DockJobNumber &msg)>& callback) = 0;
    /// @brief 取消订阅机场作业次数
    virtual void cancelListenDockJobNumber(int listen_id) = 0;

    /// @brief 订阅机场电源状态
    /// @param callback 电源状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_power_state
    /// @endinternal
    virtual int listenDockPowerState(const std::function<void(const DockPowerState &msg)>& callback) = 0;
    /// @brief 取消订阅机场电源状态
    virtual void cancelListenDockPowerState(int listen_id) = 0;

    /// @brief 订阅机场存储容量状态
    /// @param callback 存储状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_storage_state
    /// @endinternal
    virtual int listenDockStorageState(const std::function<void(const DockStorageState &msg)>& callback) = 0;
    /// @brief 取消订阅机场存储容量状态
    virtual void cancelListenDockStorageState(int listen_id) = 0;

    /// @brief 订阅机场首次上电时间
    /// @param callback 首次上电时间回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_first_power_on_time
    /// @endinternal
    virtual int listenDockFirstPowerOnTime(const std::function<void(const DockFirstPowerOnTime &msg)>& callback) = 0;
    /// @brief 取消订阅机场首次上电时间
    virtual void cancelListenDockFirstPowerOnTime(int listen_id) = 0;

    /// @brief 订阅机场累计运行时长
    /// @param callback 累计运行时长回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_accumulated_running_time
    /// @endinternal
    virtual int listenDockAccumulatedRunningTime(const std::function<void(const DockAccTime &msg)>& callback) = 0;
    /// @brief 取消订阅机场累计运行时长
    virtual void cancelListenDockAccumulatedRunningTime(int listen_id) = 0;

    /// @brief 订阅机场备降点
    /// @param callback 备降点回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_alternate_land_point
    /// @endinternal
    virtual int listenDockAlternateLandPoint(const std::function<void(const DockAlternateLandPoint &msg)>& callback) = 0;
    /// @brief 取消订阅机场备降点
    virtual void cancelListenDockAlternateLandPoint(int listen_id) = 0;

    /// @brief 订阅机场搜星状态
    /// @param callback 搜星状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_position_state
    /// @endinternal
    virtual int listenDockPositionState(const std::function<void(const DockPositionState &msg)>& callback) = 0;
    /// @brief 取消订阅机场搜星状态
    virtual void cancelListenDockPositionState(int listen_id) = 0;

    /// @brief 订阅机场激活时间
    /// @param callback 激活时间回调（Unix 时间戳，秒）
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_activation_time
    /// @endinternal
    virtual int listenDockActivationTime(const std::function<void(const DockActivationTime &msg)>& callback) = 0;
    /// @brief 取消订阅机场激活时间
    virtual void cancelListenDockActivationTime(int listen_id) = 0;

    /// @brief 订阅机场电池存储模式
    /// @param callback 电池存储模式回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_battery_store_mode
    /// @endinternal
    virtual int listenDockBatteryStoreMode(const std::function<void(const DockBatteryStoreMode &msg)>& callback) = 0;
    /// @brief 取消订阅机场电池存储模式
    virtual void cancelListenDockBatteryStoreMode(int listen_id) = 0;

    /// @brief 订阅机场声光报警状态
    /// @param callback 报警状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_alarm_state
    /// @endinternal
    virtual int listenDockAlarmState(const std::function<void(const DockAlarmState &msg)>& callback) = 0;
    /// @brief 取消订阅机场声光报警状态
    virtual void cancelListenDockAlarmState(int listen_id) = 0;

    /// @brief 订阅机场飞行器电池保养信息
    /// @param callback 电池保养信息回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_drone_battery_maintenance_info
    /// @endinternal
    virtual int listenDockDroneBatteryMaintenanceInfo(const std::function<void(const DockDroneBatteryMaintenanceInfo &msg)>& callback) = 0;
    /// @brief 取消订阅机场飞行器电池保养信息
    virtual void cancelListenDockDroneBatteryMaintenanceInfo(int listen_id) = 0;

    /// @brief 订阅机场紧急停止按钮状态
    /// @param callback 紧急停止状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_emergency_stop_state
    /// @endinternal
    virtual int listenDockEmergencyStopState(const std::function<void(const DockEmergencyStopState &msg)>& callback) = 0;
    /// @brief 取消订阅机场紧急停止按钮状态
    virtual void cancelListenDockEmergencyStopState(int listen_id) = 0;

    /// @brief 订阅机场静音模式
    /// @param callback 静音模式回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_silent_mode
    /// @endinternal
    virtual int listenDockSilentMode(const std::function<void(const DockSilentMode &msg)>& callback) = 0;
    /// @brief 取消订阅机场静音模式
    virtual void cancelListenDockSilentMode(int listen_id) = 0;

    /// @brief 订阅机场用户体验改善计划状态
    /// @param callback 用户体验改善计划回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_user_experience_improvement
    /// @endinternal
    virtual int listenDockUserExperienceImprovement(const std::function<void(const DockUserExperienceImprovement &msg)>& callback) = 0;
    /// @brief 取消订阅机场用户体验改善计划状态
    virtual void cancelListenDockUserExperienceImprovement(int listen_id) = 0;

    /// @brief 订阅机场 DRC 链路状态
    /// @param callback DRC 链路状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_drc_state
    /// @endinternal
    virtual int listenDockDrcState(const std::function<void(const DockDrcState &msg)>& callback) = 0;
    /// @brief 取消订阅机场 DRC 链路状态
    virtual void cancelListenDockDrcState(int listen_id) = 0;

    /// @brief 订阅机场推杆状态
    /// @param callback 推杆状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_putter_state
    /// @endinternal
    virtual int listenDockPutterState(const std::function<void(const DockPutterState &msg)>& callback) = 0;
    /// @brief 取消订阅机场推杆状态
    virtual void cancelListenDockPutterState(int listen_id) = 0;

    /// @brief 订阅机场相对备降点
    /// @param callback 相对备降点回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_relative_alternate_land_point
    /// @endinternal
    virtual int listenDockRelativeAlternateLandPoint(const std::function<void(const DockRelativeAlternateLandPoint &msg)>& callback) = 0;
    /// @brief 取消订阅机场相对备降点
    virtual void cancelListenDockRelativeAlternateLandPoint(int listen_id) = 0;

    /// @brief 订阅机场自收敛坐标
    /// @param callback 自收敛坐标回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_self_converge_coordinate
    /// @endinternal
    virtual int listenDockSelfConvergeCoordinate(const std::function<void(const DockSelfConvergeCoordinate &msg)>& callback) = 0;
    /// @brief 取消订阅机场自收敛坐标
    virtual void cancelListenDockSelfConvergeCoordinate(int listen_id) = 0;

    /// @brief 订阅机场固件信息
    /// @param callback 固件信息回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_firmware_info
    /// @endinternal
    virtual int listenDockFirmwareInfo(const std::function<void(const DockFirmwareInfo &msg)>& callback) = 0;
    /// @brief 取消订阅机场固件信息
    virtual void cancelListenDockFirmwareInfo(int listen_id) = 0;

    /// @brief 订阅机场4G Dongle信息
    /// @param callback Dongle信息回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_dongle_infos
    /// @endinternal
    virtual int listenDockDongleInfos(const std::function<void(const DockDongleInfos &msg)>& callback) = 0;
    /// @brief 取消订阅机场4G Dongle信息
    virtual void cancelListenDockDongleInfos(int listen_id) = 0;

    /// @brief 订阅机场空调工作模式
    /// @param callback 空调工作模式回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_air_conditioner_mode
    /// @endinternal
    virtual int listenDockAirConditionerMode(const std::function<void(const DockAirConditionerMode &msg)>& callback) = 0;
    /// @brief 取消订阅机场空调工作模式
    virtual void cancelListenDockAirConditionerMode(int listen_id) = 0;


    /// @brief 获取部署/踏勘模块

};

}  // namespace flight_hub_sdk
