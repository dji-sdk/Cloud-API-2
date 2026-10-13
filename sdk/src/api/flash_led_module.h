// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include <string>

#include "message.h"

namespace flight_hub_sdk {

/// 爆闪灯模板库模块（HTTP flash-configs CRUD）。控灯走 IFlightControlModule::setFlashLed。
/// PAAS 路径无 project_id（服务端硬编码默认值），对齐 drc.proto DrcDeviceControlService。
class IFlashLedModule {
public:
    virtual ~IFlashLedModule() = default;

    /// @brief 获取爆闪灯模板列表
    /// @param callback 回调
    /// @internal
    /// 链路: HTTP | GET /gateway/drc/api/v2/flash-configs
    /// @endinternal
    virtual void getFlashLedConfigList(
        std::function<void(FlightHubError err, const FlashLedConfigList& list)> callback) = 0;

    /// @brief 创建爆闪灯模板
    /// @param params 名称 + flash_segments
    /// @param callback 回调（成功时 config.id 为服务端生成）
    /// @internal
    /// 链路: HTTP | POST /gateway/drc/api/v2/flash-configs
    /// @endinternal
    virtual void createFlashLedConfig(
        const FlashLedConfigParams& params,
        std::function<void(FlightHubError err, const FlashLedConfig& config)> callback) = 0;

    /// @brief 更新爆闪灯模板
    /// @param config_id 模板 UUID
    /// @param params 名称 + flash_segments
    /// @param callback 回调
    /// @internal
    /// 链路: HTTP | PUT /gateway/drc/api/v2/flash-configs/{id}
    /// @endinternal
    virtual void updateFlashLedConfig(
        const std::string& config_id,
        const FlashLedConfigParams& params,
        std::function<void(FlightHubError err, const FlashLedConfig& config)> callback) = 0;

    /// @brief 删除爆闪灯模板
    /// @param config_id 模板 UUID
    /// @param callback 回调
    /// @internal
    /// 链路: HTTP | DELETE /gateway/drc/api/v2/flash-configs/{id}
    /// @endinternal
    virtual void deleteFlashLedConfig(
        const std::string& config_id,
        std::function<void(FlightHubError err)> callback) = 0;
};

}  // namespace flight_hub_sdk
