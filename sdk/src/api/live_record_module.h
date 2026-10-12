// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include <vector>
#include "message.h"

namespace flight_hub_sdk {

/// 直播录制模块接口，提供直播流的录制启停、录制记录查询、录制状态订阅等功能。
class ILiveRecordModule {
public:
    virtual ~ILiveRecordModule() = default;

    /** @deprecated: 注意，本接口暂未开放使用 */
    /// @brief 开始录制当前直播流
    /// @param time_zone 时区（可选），合法取值如：America/Sao_Paulo, Africa/Johannesburg, Asia/Shanghai
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-live/api/v1/record/start
    /// @endinternal
    virtual void startLiveRecord(const std::string& time_zone, const std::function<void(FlightHubError err)>& callback) = 0;

    /** @deprecated: 注意，本接口暂未开放使用 */
    /// @brief 停止录制当前直播流
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /pf-live/api/v1/record/stop
    /// @endinternal
    virtual void stopLiveRecord(const std::function<void(FlightHubError err)>& callback) = 0;

    /** @deprecated: 注意，本接口暂未开放使用 */
    /// @brief 按设备 SN 查询录制记录列表
    /// @param device_sn 设备序列号
    /// @param callback 结果回调，成功时返回录制记录列表（LiveRecordItem 对齐 RecordTask：bid/sn/state/camera/created_by/duration）
    ///
    /// @internal
    /// 链路: HTTP | GET /pf-live/api/v1/record/device/{sn}
    /// @endinternal
    virtual void getDeviceLiveRecords(const std::string& device_sn, const std::function<void(FlightHubError err, const std::vector<LiveRecordItem>& records)>& callback) = 0;

    /** @deprecated: 注意，本接口暂未开放使用 */
    /// @brief 订阅录制状态推送
    /// @param callback 录制状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_live_record_info
    /// @endinternal
    virtual int listenRecordState(const std::function<void(const LiveRecordStateInfo& info)>& callback) = 0;

    /** @deprecated: 注意，本接口暂未开放使用 */
    /// @brief 取消订阅录制状态
    /// @param listen_id 订阅时返回的监听 ID
    virtual void cancelListenRecordState(int listen_id) = 0;

    /** @deprecated: 注意，本接口暂未开放使用 */
    /// @brief 注入录制状态推送数据（内部方法，供 DockImpl/DroneImpl 调用）
    /// @param info 录制状态信息
    ///
    /// @internal
    /// 链路: SDK 内部 | 将 NCSDK 推送数据注入并通知已注册的监听回调
    /// @endinternal
    virtual void notifyRecordState(const LiveRecordStateInfo& info) = 0;
};

}  // namespace flight_hub_sdk
