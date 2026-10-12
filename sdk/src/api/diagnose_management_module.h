// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// 设备诊断（定损）管理模块接口，提供定损指令下发、操作、报告查询等功能。
/// [注意] 本模块暂未开放使用;
class IDiagnosisManagementModule {
public:
    virtual ~IDiagnosisManagementModule() = default;

    /// [注意] 本接口暂未开放使用;
    /// @brief 下发设备自检定损指令
    /// @param params 定损参数（设备拓扑等）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/self-diagnostic
    /// @endinternal
    virtual void createSelfDiagnosis(
        const CreateSelfDiagnosisParams &params,
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// [注意] 本接口暂未开放使用;
    /// @brief 定损操作（取消/删除）
    /// @param params 操作参数（报告 UUID、操作类型）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/self-diagnostic/operation
    /// @endinternal
    virtual void operateSelfDiagnosis(
        const OperateSelfDiagnosisParams &params,
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// [注意] 本接口暂未开放使用;
    /// @brief 查询单条定损报告详情
    /// @param report_uuid 报告 UUID
    /// @param callback 结果回调，成功时返回报告详情
    ///
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/self-diagnostic/{report_uuid}
    /// @endinternal
    virtual void getSelfDiagnosisReport(
        const std::string &report_uuid,
        const std::function<void(const GetSelfDiagnosisReportResults& results, FlightHubError err)>& callback) = 0;

    /// [注意] 本接口暂未开放使用;
    /// @brief 查询定损报告列表
    /// @param params 查询参数（分页、设备 SN 等）
    /// @param callback 结果回调，成功时返回报告列表
    ///
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/self-diagnostic
    /// @endinternal
    virtual void getSelfDiagnosisReportList(
        const GetSelfDiagnosisReportListParams &params,
        const std::function<void(const GetSelfDiagnosisReportListResults& results, FlightHubError err)>& callback) = 0;

};


}  // namespace flight_hub_sdk
