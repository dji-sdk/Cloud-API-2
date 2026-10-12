// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include "message.h"
#include <functional>
#include <memory>
#include <string>

namespace flight_hub_sdk {

/// 直播转发模块（旁路转码器 CRUD）。挂载于 IWorkspace。
/// 路径前缀：/gateway/manage/api/v1/paas/c2c/live-sync
class ILiveSyncModule {
public:
    virtual ~ILiveSyncModule() = default;

    /// @brief 创建转码器
    /// @note POST /gateway/manage/api/v1/paas/c2c/live-sync
    /// @note 超限时返回 HTTP 400，FlightHubError.server_code = ConverterMaxNum（C2CErrCode+317）；
    ///       成功时 LiveSyncCreateResult.hasLimit 始终 false
    virtual void createLiveSync(
        const LiveSyncCreateParams& params,
        std::function<void(FlightHubError err, LiveSyncCreateResult result)> callback) = 0;

    /// @brief 删除转码器
    /// @note DELETE /gateway/manage/api/v1/paas/c2c/live-sync/{id}
    virtual void deleteLiveSync(
        const std::string& id,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 更新转码器（仅 enable 或全量改配置）
    /// @note PUT /gateway/manage/api/v1/paas/c2c/live-sync/{id}
    virtual void updateLiveSync(
        const LiveSyncUpdateParams& params,
        std::function<void(FlightHubError err, LiveSyncItem result)> callback) = 0;

    /// @brief 分页查询转码器列表
    /// @note GET /gateway/manage/api/v1/paas/c2c/live-sync/list
    virtual void getLiveSyncList(
        const LiveSyncListParams& params,
        std::function<void(FlightHubError err, LiveSyncListResult result)> callback) = 0;

    /// @brief 获取可用设备树（V2，含基站/Dock）
    /// @note GET /gateway/manage/api/v1/paas/c2c/live-sync/devices-v2
    virtual void getLiveSyncDevicesV2(
        std::function<void(FlightHubError err, LiveSyncDevicesV2Result result)> callback) = 0;

    /// @brief 获取转码器总数与启用数汇总
    /// @note GET /gateway/manage/api/v1/paas/c2c/live-sync/summary
    virtual void getLiveSyncSummary(
        std::function<void(FlightHubError err, LiveSyncSummaryResult result)> callback) = 0;
};

}  // namespace flight_hub_sdk
