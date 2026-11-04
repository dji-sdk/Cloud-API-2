// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>
#include <string>

namespace flight_hub_sdk {

/// 飞行任务媒体上传模块接口。
/// - 下行 upload_flighttask_media_prioritize（MQTT services）
/// - 上行 highest_priority_upload_flighttask_media（WebSocket events）
class IFlightTaskMediaUploadModule {
public:
    virtual ~IFlightTaskMediaUploadModule() = default;

    /// @brief 下发飞行任务媒体优先上传（下行 services，云端→dock）
    /// @param flight_id 飞行任务 ID
    /// @param callback 结果回调，err.success() 表示设备接受（result==0）
    ///
    /// @internal
    /// 链路: MQTT services | method: upload_flighttask_media_prioritize
    /// @endinternal
    virtual void uploadFlighttaskMediaPrioritize(
        const std::string& flight_id,
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 订阅飞行任务媒体最高优先级上传（上行 events，dock→云端）
    /// @param callback 事件回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_highest_priority_upload_flighttask_media
    /// @endinternal
    virtual int listenHighestPriorityUploadFlighttaskMedia(
        const std::function<void(const HighestPriorityUploadFlighttaskMedia& msg)>& callback) = 0;

    /// @brief 取消订阅飞行任务媒体最高优先级上传
    /// @param listen_id 监听 ID
    virtual void cancelListenHighestPriorityUploadFlighttaskMedia(int listen_id) = 0;
};

} // namespace flight_hub_sdk
