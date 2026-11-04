// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include <string>

#include "message.h"

namespace flight_hub_sdk {

/// 自动录制配置模块（项目级配置，PAAS 路径无 project_id，服务端硬编码默认值）。
/// 对齐 drc.proto PaasAutoRecordService：无设备上下文，无需先发现直播源，与 IFlashLedModule 同构。
class IAutoRecordModule {
public:
    virtual ~IAutoRecordModule() = default;

    /// @brief 获取自动录制配置
    /// @param callback 回调；配置不存在时服务端返回全零值 AutoRecordConfig，非错误
    /// @internal
    /// 链路: HTTP | GET /gateway/drc/api/v2/auto_record/configs
    /// @endinternal
    virtual void getAutoRecordConfig(
        std::function<void(FlightHubError err, const AutoRecordConfig& config)> callback) = 0;

    /// @brief 保存自动录制配置
    /// @param params 自动清理时间 + 各录制开关 + recording_items(允许空数组)
    /// @param callback 回调
    /// @internal
    /// 链路: HTTP | POST /gateway/drc/api/v2/auto_record/configs
    /// @endinternal
    virtual void saveAutoRecordConfig(
        const SaveAutoRecordConfigParams& params,
        std::function<void(FlightHubError err)> callback) = 0;
};

}  // namespace flight_hub_sdk
