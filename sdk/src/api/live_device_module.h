// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>
#include <vector>

namespace flight_hub_sdk {

/// 设备级直播发现模块：订阅可用直播源列表（live_capacity）。
class ILiveDeviceModule {
public:
    virtual ~ILiveDeviceModule() = default;

    /// @brief 监听直播能力（live_capacity）变化，回调可用直播源列表
    /// @param callback 回调；解析后的 LiveInfo 列表相对上次变化时触发；注册时若有缓存则立即回调一次
    /// @return 监听 ID
    /// SDK 每 5 秒轮询 device topo/properties 读取 live_capacity（直播能力上报），解析为 LiveInfo 列表；
    /// 非 WebSocket 实时推送，不监听已开播流状态。结果用于 getLiveStream/getLiveRecord。
    /// AvailableRange 指由 live_capacity 解析的可用直播源（LiveInfo）列表，非地理范围。不依赖 DRC。
    ///
    /// @internal
    /// 链路: SDK 内部 | properties / live_capacity（经 get_device_topo）
    /// @endinternal
    virtual int listenLiveAvailableRange(
        const std::function<void(const std::vector<LiveInfo>&)>& callback) = 0;

    /// @brief 取消订阅可用直播流列表
    virtual void cancelListenLiveAvailableRange(int listen_id) = 0;

    /// @brief 查询本设备在线观众数量，group 可选（传 nullptr 表示不按分组筛选）
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-live/api/v1/stream/audience-count
    /// @endinternal
    virtual void getAudienceCount(
        const AudienceCountGroup* group,
        const std::function<void(FlightHubError err, const std::vector<DeviceAudienceCountItem>& result)>& callback) = 0;

    /// @brief 订阅机场直播状态
    /// @param callback 直播状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_live_status
    /// @endinternal
    virtual int listenDockLiveStatus(const std::function<void(const DockLiveStatus& msg)>& callback) = 0;

    /// @brief 取消订阅机场直播状态
    /// @param listen_id 监听 ID
    virtual void cancelListenDockLiveStatus(int listen_id) = 0;

    /// @brief 订阅机场直播能力
    /// @param callback 直播能力回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_live_capacity
    /// @endinternal
    virtual int listenDockLiveCapacity(const std::function<void(const DockLiveCapacity& msg)>& callback) = 0;

    /// @brief 取消订阅机场直播能力
    /// @param listen_id 监听 ID
    virtual void cancelListenDockLiveCapacity(int listen_id) = 0;
};

} // namespace flight_hub_sdk
