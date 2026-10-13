// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <string>
#include <memory>
#include "live_stream.h"
#include "message.h"

namespace flight_hub_sdk {

class IRtkCalibrationModule;
class INetworkTestModule;
class ILiveDeviceModule;

/// @brief 基站设备接口（EA610 / O4 Ground Station）
class IBaseStation {
public:
    virtual ~IBaseStation() = default;

    /// @brief 获取设备序列号
    virtual const std::string& getSn() const = 0;

    /// @brief 按 LiveInfo 获取直播模块（多摄像头用不同 payload_index）
    /// @param params device_sn=基站 SN，payload_index=camera_index，video_id=video_index
    virtual std::shared_ptr<ILiveModule> getLiveStream(const LiveInfo& params) = 0;

    /// @brief 订阅基站 live_capacity 解析出的可用源列表
    virtual std::shared_ptr<ILiveDeviceModule> getLiveDeviceModule() = 0;

    /// @brief 获取 RTK 标定模块
    virtual std::shared_ptr<IRtkCalibrationModule> getRtkCalibrationModule() = 0;
    /// @brief 获取网络测试模块
    virtual std::shared_ptr<INetworkTestModule> getNetworkTestModule() = 0;
};

} // namespace flight_hub_sdk
