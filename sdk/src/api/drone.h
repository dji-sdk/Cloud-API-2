// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <functional>
#include <memory>
#include "live_stream.h"
#include "live_record_module.h"
#include "value.h"
#include "message.h"
#include "fly_to_point.h"
#include "flight_task_control.h"
#include "poi_module.h"
#include "in_flight_wayline_module.h"
#include "takeoff_module.h"
#include "flight_control_module.h"
#include "control_auth_module.h"
#include "drc_module.h"
#include "flight_settings_module.h"
#include "test_flight_module.h"
#include "onboard_ai_module.h"
#include "drone_osd_module.h"
#include "psdk_control_module.h"
#include "camera_control_module.h"
#include "ai_box_module.h"

namespace flight_hub_sdk {

class IDeviceStateModule;
class IDeviceHMSModule;
class IDeviceDebugModule;
class ILiveDeviceModule;

/// @brief 飞行器模块接口，提供直播、负载控制、飞行控制、OSD 数据监听等功能。
class IDrone {
public:
    virtual ~IDrone() = default;

    // ──────────────────────────────────────────────
    //  直播
    // ──────────────────────────────────────────────

    /// @brief 获取直播流模块
    /// @param params 直播信息参数
    /// @return 直播流模块实例
    virtual std::shared_ptr<ILiveModule> getLiveStream(const LiveInfo& params) = 0;

    /// @brief 获取直播录制模块
    /// @param params 直播信息参数
    /// @return 直播录制模块实例
    virtual std::shared_ptr<ILiveRecordModule> getLiveRecord(const LiveInfo& params) = 0;

    /// @brief 获取设备级直播发现模块（可用直播源订阅）
    virtual std::shared_ptr<ILiveDeviceModule> getLiveDeviceModule() = 0;

    /// @brief 获取一键起飞模块
    virtual std::shared_ptr<ITakeoffModule> getTakeoffModule() = 0;

    /// @brief 获取飞控指令模块（紧急停/降落、返航、摇杆）
    virtual std::shared_ptr<IFlightControlModule> getFlightControlModule() = 0;

    /// @brief 获取飞行/负载控制权模块
    virtual std::shared_ptr<IControlAuthModule> getControlAuthModule() = 0;

    /// @brief 获取 DRC 链路模块
    virtual std::shared_ptr<IDrcModule> getDrcModule() = 0;

    /// @brief 获取飞行设置模块
    virtual std::shared_ptr<IFlightSettingsModule> getFlightSettingsModule() = 0;

    /// @brief 获取试飞任务模块
    virtual std::shared_ptr<ITestFlightModule> getTestFlightModule() = 0;

    /// @brief 获取机载 AI 模块
    virtual std::shared_ptr<IOnboardAiModule> getOnboardAiModule() = 0;

    /// @brief 获取 PSDK 控制模块（探照灯/喊话器/Widget）
    virtual std::shared_ptr<IPsdkControlModule> getPsdkControlModule() = 0;

    /// @brief 获取相机/云台控制模块
    virtual std::shared_ptr<ICameraControlModule> getCameraControlModule() = 0;

    // ──────────────────────────────────────────────
    //  指点飞行
    // ──────────────────────────────────────────────

    /// @brief 获取指点飞行模块
    /// @return 指点飞行模块实例
    virtual std::shared_ptr<IFlyToPointModule> getFlyToPointModule() = 0;

    // ──────────────────────────────────────────────
    //  飞行任务控制
    // ──────────────────────────────────────────────

    /// @brief 获取飞行任务控制模块
    /// @return 飞行任务控制模块实例
    virtual std::shared_ptr<IFlightTaskControlModule> getFlightTaskControlModule() = 0;

    // ──────────────────────────────────────────────
    //  环绕飞行 (POI)
    // ──────────────────────────────────────────────

    /// @brief 获取环绕飞行 (POI) 模块
    /// @return POI 模块实例
    virtual std::shared_ptr<IPoiModule> getPoiModule() = 0;

    /// @brief 获取空中下发航线模块
    virtual std::shared_ptr<IInFlightWaylineModule> getInFlightWaylineModule() = 0;

    /// @brief 获取飞行器 OSD 数据订阅模块
    virtual std::shared_ptr<IDroneOsdModule> getDroneOsdModule() = 0;

    /// @brief 获取设备状态模块（拓扑变更、在线状态）
    virtual std::shared_ptr<IDeviceStateModule> getDeviceStateModule() = 0;

    /// @brief 获取设备 HMS 实时推送模块
    virtual std::shared_ptr<IDeviceHMSModule> getDeviceHMSModule() = 0;

    /// @brief 获取设备调试模块
    virtual std::shared_ptr<IDeviceDebugModule> getDeviceDebugModule() = 0;

    /// @brief 获取 AI Box 模块
    virtual std::shared_ptr<IAIBoxModule> getAIBoxModule() = 0;
};

}  // namespace flight_hub_sdk
