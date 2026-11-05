// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>
#include <memory>

namespace flight_hub_sdk {

/// PaaS ai-alert 告警配置与查询模块（Workspace 级，/drc/paas/v2/ai-alert）
class IAIAlertModule {
public:
    virtual ~IAIAlertModule() = default;

    /// @brief 写设备级告警配置
    /// @note POST /drc/paas/v2/ai-alert/device-setting
    virtual void postDeviceSetting(
        const PostAiAlertDeviceSettingParam& param,
        std::function<void(FlightHubError err, PostAiAlertDeviceSettingResult result)> callback) = 0;

    /// @brief 读设备级告警配置
    /// @note GET /drc/paas/v2/ai-alert/device-setting
    virtual void getDeviceSetting(
        const GetAiAlertDeviceSettingParam& param,
        std::function<void(FlightHubError err, GetAiAlertDeviceSettingResult result)> callback) = 0;

    /// @brief 写算法级告警配置（唯一键 drone_sn + algorithm_type，Upsert）
    /// @note POST /drc/paas/v2/ai-alert/algorithm-setting
    virtual void postAlgorithmSetting(
        const PostAiAlertAlgorithmSettingParam& param,
        std::function<void(FlightHubError err, PostAiAlertAlgorithmSettingResult result)> callback) = 0;

    /// @brief 读算法级告警配置
    /// @note GET /drc/paas/v2/ai-alert/algorithm-setting
    virtual void getAlgorithmSetting(
        const GetAiAlertAlgorithmSettingParam& param,
        std::function<void(FlightHubError err, GetAiAlertAlgorithmSettingResult result)> callback) = 0;

    /// @brief 查询架次告警统计（先拿 flight_id，再调 getAiAlertRecords）
    /// @note GET /drc/paas/v2/ai-alert/flights
    virtual void getAiAlertFlights(
        const GetAiAlertFlightsParam& param,
        std::function<void(FlightHubError err, GetAiAlertFlightsResult result)> callback) = 0;

    /// @brief 查询告警明细列表（按 flight_id 分组，含 thumbnail_url）
    /// @note GET /drc/paas/v2/ai-alert/records
    virtual void getAiAlertRecords(
        const GetAiAlertRecordsParam& param,
        std::function<void(FlightHubError err, GetAiAlertRecordsResult result)> callback) = 0;

    /// @brief 监听告警记录推送（增量更新 + 未读通知）
    /// @param device_sn 设备序列号（网关 SN）
    /// @param callback  收到推送时的回调，在主线程触发
    /// @return listen_id，用于 cancelListenAiAlertRecords 取消监听；< 0 表示订阅失败
    /// @internal 链路: WebSocket | topic: alert_records.alert_records_notify
    virtual int listenAiAlertRecords(
        const std::string& device_sn,
        std::function<void(AiAlertRecordsNotifyResult result)> callback) = 0;

    /// @brief 取消监听告警记录推送
    /// @param listen_id 由 listenAiAlertRecords 返回的订阅 ID
    virtual void cancelListenAiAlertRecords(int listen_id) = 0;

    /// @brief 删除告警记录：alert_uuid 非空时精确删除单条（优先）；为空时按 flight_id 批量删除该架次全部记录
    /// @note DELETE /drc/api/v2/ai-alert-record（proto 生成路由，与本模块其余接口所在的 /drc/paas/v2/ai-alert/* 手写路由不同源）
    virtual void deleteAiAlertRecord(
        const DeleteAiAlertRecordParam& param,
        std::function<void(FlightHubError err)> callback) = 0;
};

}  // namespace flight_hub_sdk
