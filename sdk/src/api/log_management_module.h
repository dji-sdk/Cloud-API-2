// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// 设备日志管理模块接口，提供日志文件查询、日志报告创建与管理、追加文件上传等功能。
class ILogManagementModule {
public:
    virtual ~ILogManagementModule() = default;

    /// @brief 查询设备日志文件列表
    /// @param device_sn 设备序列号
    /// @param module_list 模块过滤列表
    /// @param callback 结果回调，成功时返回日志文件列表
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/reports/devices/logs
    /// @endinternal
    virtual void QueryDeviceLogList(const std::string& device_sn, const std::vector<int> module_list, const std::function<void(FlightHubError err, DeviceLogListResult result)>& callback) = 0;

    /// @brief 创建设备日志报告
    /// @param param 创建参数（报告信息、错误时间、设备拓扑、联系方式、日志列表等）
    /// @param callback 结果回调，成功时返回报告 UUID 和工单号
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/reports
    /// @endinternal
    virtual void CreateDeviceLogReport(const CreateDeviceLogReportRequestParam& param, const std::function<void(FlightHubError err, CreateDeviceLogReportResult result)>& callback) = 0;

    /// @brief 查询日志报告列表（分页）
    /// @param device_sn 设备序列号
    /// @param page 页码
    /// @param page_size 每页数量
    /// @param callback 结果回调，成功时返回报告列表和分页信息
    ///
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/reports
    /// @endinternal
    virtual void LogReportList(const std::string& device_sn, int page, int page_size, const std::function<void(FlightHubError err, LogReportListResult result)>& callback) = 0;

    /// @brief 日志报告操作（取消、删除等）
    /// @param operation 操作类型
    /// @param report_id 报告 ID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/reports/operation
    /// @endinternal
    virtual void LogReportOperation(const std::string& operation, const std::string& report_id, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 上传追加文件到日志报告
    /// @param file_path 本地文件路径
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | 先获取 STS 临时凭证（/device/api/v1/manager/reports/sts），再上传至对象存储
    /// @endinternal
    virtual void UploadLogAppendFile(const std::string& file_path, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 订阅设备异常报告上传进度
    /// @param device_sn 设备序列号
    /// @param callback 上传进度回调
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_fileupload_progress
    /// @endinternal
    virtual int listenDeviceFileUploadProgress(
        const std::string& device_sn,
        const std::function<void(const DeviceFileUploadProgress& msg)>& callback) = 0;

    /// @brief 取消订阅设备异常报告上传进度
    /// @param listen_id 监听 ID
    virtual void cancelListenDeviceFileUploadProgress(int listen_id) = 0;
};

}