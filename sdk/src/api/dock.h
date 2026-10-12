// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <functional>
#include <memory>
#include "live_stream.h"
#include "live_record_module.h"
#include "message.h"

namespace flight_hub_sdk {

class IRtkCalibrationModule;
class INetworkTestModule;
class IDeviceDebugModule;
class IDeviceStateModule;
class IDeviceHMSModule;
class IDeployModule;
class IDockControlModule;
class IDockOsdModule;
class ILiveDeviceModule;

/// 机场模块接口，提供直播、录制、设备状态监听、设备控制、进离场航线等功能。
class IDock {
public:
    virtual ~IDock() = default;

    /// @brief 获取直播模块（按 LiveInfo 区分不同摄像头/视频流）
    /// @param params 直播信息（SN、摄像头、视频流标识）
    /// @return 直播模块实例
    virtual std::shared_ptr<ILiveModule> getLiveStream(const LiveInfo& params) = 0;

    /// @brief 获取录制模块（按 LiveInfo 区分不同摄像头/视频流）
    /// @param params 直播信息
    /// @return 录制模块实例
    virtual std::shared_ptr<ILiveRecordModule> getLiveRecord(const LiveInfo& params) = 0;

    /// @brief 获取设备级直播发现模块（可用直播源订阅）
    virtual std::shared_ptr<ILiveDeviceModule> getLiveDeviceModule() = 0;

    /// @brief 获取机场 OSD 数据订阅模块
    virtual std::shared_ptr<IDockOsdModule> getDockOsdModule() = 0;

    /// @brief 获取部署/踏勘模块
    virtual std::shared_ptr<IDeployModule> getDeployModule() = 0;

    /// @brief 获取机场控制模块（环控：空调/补光灯/充电/舱盖等）
    virtual std::shared_ptr<IDockControlModule> getDockControlModule() = 0;

    /// @brief 获取 RTK 标定模块
    virtual std::shared_ptr<IRtkCalibrationModule> getRtkCalibrationModule() = 0;

    /// @brief 获取网络测速模块
    virtual std::shared_ptr<INetworkTestModule> getNetworkTestModule() = 0;

    /// @brief 获取设备调试模块
    virtual std::shared_ptr<IDeviceDebugModule> getDeviceDebugModule() = 0;

    /// @brief 获取设备状态模块（拓扑变更、在线状态）
    virtual std::shared_ptr<IDeviceStateModule> getDeviceStateModule() = 0;

    /// @brief 获取设备 HMS 实时推送模块
    virtual std::shared_ptr<IDeviceHMSModule> getDeviceHMSModule() = 0;

};

}  // namespace flight_hub_sdk
