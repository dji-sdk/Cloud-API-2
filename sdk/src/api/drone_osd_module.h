// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include "message.h"

namespace flight_hub_sdk {

/// 飞行器 OSD 数据订阅模块（WebSocket NotificationCenter + HSI）
class IDroneOsdModule {
public:
    virtual ~IDroneOsdModule() = default;

    // ──────────────────────────────────────────────
    //  OSD 数据监听
    // ──────────────────────────────────────────────

    /// @brief 订阅飞机电池信息
    /// @param callback 电池信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_battery_info
    /// @endinternal
    virtual int listenAircraftBatteryInfo(const std::function<void(const AircraftBatteryInfo &battery_info)>& callback) = 0;

    /// @brief 取消订阅飞机电池信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftBatteryInfo(int listen_id) = 0;

    /// @brief 订阅飞机姿态
    /// @param callback 飞机姿态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_attitude
    /// @endinternal
    virtual int listenAircraftAttitude(const std::function<void(const AircraftAttitude &attitude)>& callback) = 0;

    /// @brief 取消订阅飞机姿态
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftAttitude(int listen_id) = 0;

    /// @brief 订阅飞机位置
    /// @param callback 飞机位置回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_location
    /// @endinternal
    virtual int listenAircraftLocation(const std::function<void(const AircraftLocation &location)>& callback) = 0;

    /// @brief 取消订阅飞机位置
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftLocation(int listen_id) = 0;

    /// @brief 订阅飞机模式码
    /// @param callback 飞机模式码回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_mode_code
    /// @endinternal
    virtual int listenAircraftModeCode(const std::function<void(const AircraftModeCode &mode_code)>& callback) = 0;

    /// @brief 取消订阅飞机模式码
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftModeCode(int listen_id) = 0;

    /// @brief 订阅飞机风速
    /// @param callback 飞机风速回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_wind_speed
    /// @endinternal
    virtual int listenAircraftWindSpeed(const std::function<void(const AircraftWindSpeed &msg)>& callback) = 0;

    /// @brief 取消订阅飞机风速
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftWindSpeed(int listen_id) = 0;

    /// @brief 订阅飞行任务异常
    /// @param callback 飞行任务异常回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_task_flight_exception
    /// @endinternal
    virtual int listenFlightTasksException(const std::function<void(const FlightTaskException &msg)>& callback) = 0;

    /// @brief 取消订阅飞行任务异常
    /// @param listen_id 监听 ID
    virtual void cancelListenFlightTasksException(int listen_id) = 0;

    /// @brief 订阅飞机控制码
    /// @param callback 飞机控制码回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_control_code
    /// @endinternal
    virtual int listenAircraftControlCode(const std::function<void(const AircraftControlCode &msg)>& callback) = 0;

    /// @brief 取消订阅飞机控制码
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftControlCode(int listen_id) = 0;

    /// @brief 订阅飞机速度
    /// @param callback 飞机速度回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_speed
    /// @endinternal
    virtual int listenAircraftSpeed(const std::function<void(const AircraftSpeed &msg)>& callback) = 0;

    /// @brief 取消订阅飞机速度
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftSpeed(int listen_id) = 0;

    /// @brief 订阅载荷相机实时视图世界区域
    /// @param callback 载荷相机实时视图世界区域回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_payloads_liveview_world_region
    /// @endinternal
    virtual int listenAircraftPayloadsCameraLiveviewWorldRegion(const std::function<void(const AircraftPayloadsCameraLiveviewWorldRegion &msg)>& callback) = 0;

    /// @brief 取消订阅载荷相机实时视图世界区域
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftPayloadsCameraLiveviewWorldRegion(int listen_id) = 0;

    /// @brief 订阅载荷云台姿态
    /// @param callback 载荷云台姿态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_payloads_gimbal_attitude
    /// @endinternal
    virtual int listenAircraftPayloadsGimbalAttitude(const std::function<void(const AircraftPayloadsGimbalAttitude &msg)>& callback) = 0;

    /// @brief 取消订阅载荷云台姿态
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftPayloadsGimbalAttitude(int listen_id) = 0;

    /// @brief 订阅载荷列表
    /// @param callback 载荷列表回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_payloads_list
    /// @endinternal
    virtual int listenAircraftPayloadsList(const std::function<void(const AircraftPayloadsList &msg)>& callback) = 0;

    /// @brief 取消订阅载荷列表
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftPayloadsList(int listen_id) = 0;

    /// @brief 订阅控制权限云控制状态
    /// @param callback 控制权限云控制状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_cloud_control_status
    /// @endinternal
    virtual int listenControlRightsCloudControlStatus(const std::function<void(const ControlRightsCloudControlStatus &msg)>& callback) = 0;

    /// @brief 取消订阅控制权限云控制状态
    /// @param listen_id 监听 ID
    virtual void cancelListenControlRightsCloudControlStatus(int listen_id) = 0;

    /// @brief 订阅控制权限设备控制状态
    /// @param callback 控制权限设备控制状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_cloud_control_status
    /// @endinternal
    virtual int listenControlRightsDeviceControlStatus(const std::function<void(const ControlRightsDeviceControlStatus &msg)>& callback) = 0;

    /// @brief 取消订阅控制权限设备控制状态
    /// @param listen_id 监听 ID
    virtual void cancelListenControlRightsDeviceControlStatus(int listen_id) = 0;

    /// @brief 订阅飞行任务媒体文件显示可用
    /// @param callback 飞行任务媒体文件显示可用回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_task_media_file_display_available
    /// @endinternal
    virtual int listenFlightTasksMediaFileDisplayAvailable(const std::function<void(const FlightTasksMediaFileDisplayAvailable &msg)>& callback) = 0;

    /// @brief 取消订阅飞行任务媒体文件显示可用
    /// @param listen_id 监听 ID
    virtual void cancelListenFlightTasksMediaFileDisplayAvailable(int listen_id) = 0;


    /// @brief 订阅飞机负载 OSD 信息列表
    /// @param callback 飞机负载 OSD 信息列表回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_payloads_osd_info
    /// @endinternal
    /// @note 当前版本后端暂不支持 topic_drone_payloads_osd_info 推送，调用后无法收到回调。
    ///       替代：云台姿态 listenAircraftPayloadsGimbalAttitude、激光测距 CameraControlModule::listenCameraMeasureTarget、
    ///       变焦倍率 listenAircraftPayloadsCamerasList；其余字段暂无替代接口。
    virtual int listenAircraftPayloadsOsdInfo(const std::function<void(const AircraftPayloadsOsdInfoList &msg)>& callback) = 0;

    /// @brief 取消订阅飞机负载 OSD 信息列表
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftPayloadsOsdInfo(int listen_id) = 0;

    /// @brief 订阅飞行器进入当前状态原因
    /// @param callback 状态原因回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_mode_code_reason
    /// @endinternal
    virtual int listenDroneModeCodeReason(const std::function<void(const DroneModeCodeReason &msg)>& callback) = 0;
    /// @brief 取消订阅飞行器进入当前状态原因
    virtual void cancelListenDroneModeCodeReason(int listen_id) = 0;

    /// @brief 订阅飞机4G Dongle信息
    /// @param callback Dongle信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_dongle_infos
    /// @endinternal
    virtual int listenDroneDongleInfos(const std::function<void(const DroneDongleInfos &msg)>& callback) = 0;
    /// @brief 取消订阅飞机4G Dongle信息
    virtual void cancelListenDroneDongleInfos(int listen_id) = 0;

    /// @brief 订阅限飞数据库版本
    /// @param callback 限飞数据库版本回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_flysafe_database_version
    /// @endinternal
    virtual int listenDroneFlysafeDatabaseVersion(const std::function<void(const DroneFlysafeDatabaseVersion &msg)>& callback) = 0;
    /// @brief 取消订阅限飞数据库版本
    virtual void cancelListenDroneFlysafeDatabaseVersion(int listen_id) = 0;

    /// @brief 订阅航线解析库版本
    /// @param callback 航线解析库版本回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_wpmz_version
    /// @endinternal
    virtual int listenDroneWpmzVersion(const std::function<void(const DroneWpmzVersion &msg)>& callback) = 0;
    /// @brief 取消订阅航线解析库版本
    virtual void cancelListenDroneWpmzVersion(int listen_id) = 0;

    /// @brief 订阅飞机档位信息
    /// @param callback 飞机档位信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_gear
    /// @endinternal
    virtual int listenAircraftGear(const std::function<void(const AircraftGear &msg)>& callback) = 0;

    /// @brief 取消订阅飞机档位信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftGear(int listen_id) = 0;

    /// @brief 订阅飞机 Home 点距离
    /// @param callback Home 点距离回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_home_distance
    /// @endinternal
    virtual int listenAircraftHomeDistance(const std::function<void(const AircraftHomeDistance &msg)>& callback) = 0;

    /// @brief 取消订阅飞机 Home 点距离
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftHomeDistance(int listen_id) = 0;

    /// @brief 订阅飞机搜星状态
    /// @param callback 搜星状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_position_state
    /// @endinternal
    virtual int listenAircraftPositionState(const std::function<void(const AircraftPositionState &msg)>& callback) = 0;

    /// @brief 取消订阅飞机搜星状态
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftPositionState(int listen_id) = 0;

    /// @brief 订阅飞机当前控制源
    /// @param callback 当前控制源回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_control_source
    /// @endinternal
    virtual int listenAircraftControlSource(const std::function<void(const AircraftControlSource &msg)>& callback) = 0;

    /// @brief 取消订阅飞机当前控制源
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftControlSource(int listen_id) = 0;

    /// @brief 订阅飞机限制类信息状态
    /// @param callback 限制类信息状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_limits_info
    /// @endinternal
    virtual int listenAircraftLimitsInfo(const std::function<void(const AircraftLimitsInfo &msg)>& callback) = 0;

    /// @brief 取消订阅飞机限制类信息状态
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftLimitsInfo(int listen_id) = 0;

    /// @brief 订阅电量告警信息（用户设置的阈值）
    /// @param callback 电量告警信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_battery_warning_thresholds
    /// @endinternal
    virtual int listenAircraftBatteryWarningThresholds(const std::function<void(const AircraftBatteryWarningThresholds &msg)>& callback) = 0;

    /// @brief 取消订阅电量告警信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftBatteryWarningThresholds(int listen_id) = 0;

    /// @brief 订阅固件状态信息
    /// @param callback 固件状态信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_firmware_status
    /// @endinternal
    virtual int listenAircraftFirmwareStatus(const std::function<void(const AircraftFirmwareStatus &msg)>& callback) = 0;

    /// @brief 取消订阅固件状态信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftFirmwareStatus(int listen_id) = 0;

    /// @brief 订阅累计飞行信息
    /// @param callback 累计飞行信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_total_flight_info
    /// @endinternal
    virtual int listenAircraftTotalFlightInfo(const std::function<void(const AircraftTotalFlightInfo &msg)>& callback) = 0;

    /// @brief 取消订阅累计飞行信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftTotalFlightInfo(int listen_id) = 0;

    /// @brief 订阅飞行器激活时间
    /// @param callback 飞行器激活时间回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_activation_time
    /// @endinternal
    virtual int listenAircraftActivationTime(const std::function<void(const AircraftActivationTime &msg)>& callback) = 0;

    /// @brief 取消订阅飞行器激活时间
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftActivationTime(int listen_id) = 0;

    /// @brief 订阅飞机保养信息
    /// @param callback 保养信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_maintain_status_info
    /// @endinternal
    virtual int listenAircraftMaintainStatusInfo(const std::function<void(const AircraftMaintainStatusInfo &msg)>& callback) = 0;

    /// @brief 取消订阅飞机保养信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftMaintainStatusInfo(int listen_id) = 0;

    /// @brief 订阅飞行器夜航灯状态
    /// @param callback 夜航灯状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_night_lights_state
    /// @endinternal
    virtual int listenAircraftNightLightsState(const std::function<void(const AircraftNightLightsState &msg)>& callback) = 0;

    /// @brief 取消订阅飞行器夜航灯状态
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftNightLightsState(int listen_id) = 0;

    /// @brief 订阅飞行器避障状态
    /// @param callback 避障状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_obstacle_avoidance_state
    /// @endinternal
    virtual int listenAircraftObstacleAvoidanceState(const std::function<void(const AircraftObstacleAvoidanceState &msg)>& callback) = 0;

    /// @brief 取消订阅飞行器避障状态
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftObstacleAvoidanceState(int listen_id) = 0;

    /// @brief 订阅存储容量信息
    /// @param callback 存储容量信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_storage_info
    /// @endinternal
    virtual int listenAircraftStorageInfo(const std::function<void(const AircraftStorageInfo &msg)>& callback) = 0;

    /// @brief 取消订阅存储容量信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftStorageInfo(int listen_id) = 0;

    /// @brief 订阅航迹 ID
    /// @param callback 航迹 ID 回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_track_id
    /// @endinternal
    virtual int listenAircraftTrackId(const std::function<void(const AircraftTrackId &msg)>& callback) = 0;

    /// @brief 取消订阅航迹 ID
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftTrackId(int listen_id) = 0;

    /// @brief 订阅合规相关信息
    /// @param callback 合规相关信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_compliance_info
    /// @endinternal
    virtual int listenAircraftComplianceInfo(const std::function<void(const AircraftComplianceInfo &msg)>& callback) = 0;

    /// @brief 取消订阅合规相关信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftComplianceInfo(int listen_id) = 0;

    /// @brief 订阅飞行器相机信息
    /// @param callback 飞行器相机信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_payloads_cameras_list
    /// @endinternal
    virtual int listenAircraftPayloadsCamerasList(const std::function<void(const AircraftPayloadsCamerasList &msg)>& callback) = 0;

    /// @brief 取消订阅飞行器相机信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftPayloadsCamerasList(int listen_id) = 0;

    /// @brief 订阅遥控器失控动作
    /// @param callback 遥控器失控动作回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_rc_lost_action
    /// @endinternal
    virtual int listenAircraftRcLostAction(const std::function<void(const AircraftRcLostAction &msg)>& callback) = 0;

    /// @brief 取消订阅遥控器失控动作
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftRcLostAction(int listen_id) = 0;

    /// @brief 订阅返航信息（返航模式、返航高度）
    /// @param callback 返航信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_rth_info
    /// @endinternal
    virtual int listenAircraftRthInfo(const std::function<void(const AircraftRthInfo &msg)>& callback) = 0;

    /// @brief 取消订阅返航信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftRthInfo(int listen_id) = 0;

    /// @brief 订阅飞机 Home 点地理坐标信息
    /// @param callback Home 点地理坐标信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_home_geographical_info
    /// @endinternal
    virtual int listenAircraftHomeGeographicalInfo(const std::function<void(const AircraftHomeGeographicalInfo &msg)>& callback) = 0;

    /// @brief 取消订阅飞机 Home 点地理坐标信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftHomeGeographicalInfo(int listen_id) = 0;

    /// @brief 订阅返航高度模式当前值
    /// @param callback 返航高度模式当前值回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_current_rth_mode
    /// @endinternal
    virtual int listenAircraftCurrentRthMode(const std::function<void(const AircraftCurrentRthMode &msg)>& callback) = 0;

    /// @brief 取消订阅返航高度模式当前值
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftCurrentRthMode(int listen_id) = 0;

    /// @brief 订阅飞行器 HSI 信息（障碍物感知/避障传感器数据）
    /// @param callback HSI 信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: SDK 内部 | HSI 障碍物感知数据
    /// @endinternal
    virtual int listenHsiInfo(const std::function<void(const HsiInfo&)>& callback) = 0;

    /// @brief 取消订阅飞行器 HSI 信息
    /// @param listen_id 监听 ID
    virtual void cancelListenHsiInfo(int listen_id) = 0;


};

}  // namespace flight_hub_sdk
