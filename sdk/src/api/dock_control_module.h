// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>
#include <string>

namespace flight_hub_sdk {

/// 机场控制模块接口，提供舱内/舱外摄像头切换、空调/补光灯/充电/舱盖控制及状态订阅。
class IDockControlModule {
public:
    virtual ~IDockControlModule() = default;

    /// @brief 切换机场舱内/舱外摄像头
    /// @param live_info 直播信息
    /// @param camera_position 摄像头位置（枚举 DockCameraPosition，见 value.h）
    /// @param gateway_sn 网关设备序列号
    /// @param callback 结果回调
    virtual void changeDockCameraPosition(
        const LiveInfo& live_info,
        int camera_position,
        const std::string& gateway_sn,
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 订阅机场空调工作模式
    /// @param callback 空调状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_air_conditioner_status_info
    /// @endinternal
    virtual int listenDockAirConditionerStatusInfo(const std::function<void(const DockAirConditionerStatusInfo &msg)>& callback) = 0;
    /// @brief 取消订阅机场空调工作模式
    virtual void cancelListenDockAirConditionerStatusInfo(int listen_id) = 0;

    /// @brief 订阅机场补光灯状态
    /// @param callback 补光灯状态回调
    /// @return 监听 ID
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_supplement_light_state
    /// @endinternal
    virtual int listenDockSupplementLightState(const std::function<void(const DockSupplementLightState &msg)>& callback) = 0;
    /// @brief 取消订阅机场补光灯状态
    virtual void cancelListenDockSupplementLightState(int listen_id) = 0;

    /// @brief 设置机场补光灯开关
    /// @param state true=开启 false=关闭
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/supplement-light-{open|close}
    /// @endinternal
    virtual void setDockSupplementLightState(bool state, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置机场充电开关
    /// @param state true=开启充电 false=关闭充电
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/charge-{open|close}
    /// @endinternal
    virtual void setDockChargeState(bool state, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置机场舱盖开关
    /// @param mode 舱盖操作模式
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/cover-{open|close}
    /// @endinternal
    virtual void setDockCoverState(int mode, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置机场空调模式
    /// @param mode 空调模式。枚举 DockAirConditionerState（value.h）：0=空闲, 1=制冷, 2=制热, 3=除湿
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/air-conditioner
    /// @endinternal
    virtual void setDockAirConditionerMode(int mode, const std::function<void(FlightHubError err)>& callback) = 0;
};

} // namespace flight_hub_sdk
