// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>

namespace flight_hub_sdk {

/// 飞行控制模块接口，提供紧急停桨/降落、返航、摇杆控制及相关能力订阅。
class IFlightControlModule {
public:
    virtual ~IFlightControlModule() = default;

    /// @brief 紧急停桨
    ///
    /// @internal
    /// 链路: DRC | method: drone_emergency_stop
    /// @endinternal
    virtual void drcEmergencyStop(const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 紧急降落
    ///
    /// @internal
    /// 链路: DRC | method: drc_emergency_landing
    /// @endinternal
    virtual void drcEmergencyLanding(const std::function<void(FlightHubError err)>& callback) = 0;
    /// @brief 强制降落
    ///
    /// @internal
    /// 链路: DRC | method: drc_force_landing
    /// @endinternal
    virtual void drcForceLanding(const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 返航
    ///
    /// @internal
    /// 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd (method: return_home)
    /// @endinternal
    virtual void returnHome(const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 返航到指定机场（蛙跳/多机场任务，用于返航到非默认降落点的机场）
    /// @param target_dock_sn 目标机场 SN
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd (method: return_specific_home，携带 home_dock_sn)
    /// @endinternal
    virtual void returnHome(const std::string& target_dock_sn, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 取消返航
    ///
    /// @internal
    /// 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd (method: return_home_cancel)
    /// @endinternal
    virtual void returnHomeCancel(const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 发送摇杆控制量（DRC 虚拟摇杆）
    /// @param data 摇杆控制数据；各轴中性 1024，建议操控范围 364–1684
    /// 需先 connectDrcLink 并持有飞行控制权
    ///
    /// @internal
    /// 链路: DRC | method: stick_control (高频 Southbound；无 Reply)
    /// @endinternal
    virtual void drcSendStickControl(const StickControlData& data) = 0;

    // ══════════════════════════════════════════════
    // Flight Control Auxiliary (DRC)
    // ══════════════════════════════════════════════

    /// @brief 设置飞机悬停时间
    /// @note 仅支持大疆机场 4（DJI Dock 4）搭载机型，其他机型不支持
    /// @param duration 悬停时长（毫秒）。取值范围 [0, 1800000]；0 表示取消悬停保活定时器，超出范围将被截断到 [0, 1800000] 且不报错
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_drone_hover_set
    /// @endinternal
    virtual void drcSetDroneHover(int duration, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置指哪打哪参数
    /// @param speed 飞行速度
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_follow_mouse_parameter_set
    /// @endinternal
    virtual void drcSetFollowMouseParameter(int speed, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置飞行速度
    /// @param speed 飞行速度，单位 m/s。调用方常用档位：低速 5 / 默认 15 / 高速 21；
    ///              协议接受连续值，档位仅为参考，SDK 不做范围校验（超限由设备侧拒绝）。
    /// @param callback 结果回调
    ///
    /// 实时指令，不依附于任何飞行任务。注意与两个 HTTP 任务参数区分：
    /// `speed_mode`（是否开启高速模式）与 `max_speed`（本次任务的速度上限）
    /// 都只用于 HTTP 接口，与本接口无关。
    ///
    /// @internal
    /// 链路: DRC | method: drc_set_flying_max_speed
    /// @endinternal
    virtual void drcSetFlyingMaxSpeed(double speed, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置夜航灯状态
    /// @param night_lights_state 夜航灯状态（0=关 1=开）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_night_lights_state_set
    /// @endinternal
    virtual void drcSetNightLightsState(int night_lights_state, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置隐身模式
    /// @param stealth_state 隐身状态（0=关 1=开）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_stealth_state_set
    /// @endinternal
    virtual void drcSetStealthState(int stealth_state, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 开伞
    /// @param force 是否强制开伞（0=正常 1=强制）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_open_parachute
    /// @endinternal
    virtual void drcOpenParachute(int force, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 设置爆闪灯
    /// @param params 爆闪灯设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_flash_led_set
    /// @endinternal
    virtual void drcSetFlashLed(const FlashLedParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    /// @brief 订阅爆闪灯状态（来自 drc_drone_state_push.flash_led）
    /// @param callback 推送数据回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_drone_state_push (field: flash_led)
    /// @endinternal
    virtual int listenDrcFlashLed(const std::function<void(const FlashLedStateInfo&)>& callback) = 0;

    /// @brief 取消订阅爆闪灯状态
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcFlashLed(int listen_id) = 0;

    /// @brief 相机 LookAt（指定目标点让相机持续对准）
    /// @param params LookAt 参数（含负载索引、锁定、高度、经纬度）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drc_camera_look_at
    /// @endinternal
    virtual void drcCameraLookAt(const CameraLookAtParams& params, const std::function<void(FlightHubError)>& callback) = 0;

    // ══════════════════════════════════════════════
    // Northbound Push Listeners (DRC)
    // ══════════════════════════════════════════════

    /// @brief 订阅 DRC OSD 飞行遥测推送
    /// @param callback 推送数据回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: osd_info_push
    /// @endinternal
    virtual int listenDrcOsdInfo(const std::function<void(const DrcOsdInfo&)>& callback) = 0;

    /// @brief 取消订阅 DRC OSD 推送
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcOsdInfo(int listen_id) = 0;

    /// @brief 订阅 DRC 电池信息推送
    /// @param callback 推送数据回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_batteries_info_push
    /// @endinternal
    virtual int listenDrcBatteriesInfo(const std::function<void(const DrcBatteriesInfo&)>& callback) = 0;

    /// @brief 取消订阅 DRC 电池信息推送
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcBatteriesInfo(int listen_id) = 0;

    /// @brief 订阅 DRC 降落伞信息推送
    /// @param callback 推送数据回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: DRC Northbound | method: drc_parachute_info_push
    /// @endinternal
    virtual int listenDrcParachuteInfo(const std::function<void(const DrcParachuteInfo&)>& callback) = 0;

    /// @brief 取消订阅 DRC 降落伞信息推送
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcParachuteInfo(int listen_id) = 0;

    /// @brief 订阅 DRC 降落任务状态推送
    /// @param callback 推送数据回调
    /// @return 监听 ID，用于取消订阅
    /// @note DrcLandingMissionState::landing_state 取值：
    ///       0=初始空闲，1=等待参数同步，2=航向对齐，3=精准降落(有水平源)，4=无源降落(无水平源, 继续下降)，
    ///       5=悬停微调(有水平源)，6=纯悬停(无水平源)，7=备降(有源)，8=备降(无源)，9=备降(用户 RC 接管)。
    /// @note 该字段在协议侧拼作 cover_precent（协议设计如此，非笔误），对外 API 已更正为
    ///       DrcLandingMissionState::cover_percent，转换在 SDK 内部完成，业务侧按正确拼写使用即可。
    ///
    /// @internal
    /// 链路: DRC Northbound | method: landing_mission_state
    /// @endinternal
    virtual int listenDrcLandingMissionState(const std::function<void(const DrcLandingMissionState&)>& callback) = 0;

    /// @brief 取消订阅 DRC 降落任务状态推送
    /// @param listen_id 监听 ID
    virtual void cancelListenDrcLandingMissionState(int listen_id) = 0;
};

} // namespace flight_hub_sdk
