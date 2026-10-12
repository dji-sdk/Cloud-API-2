// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0


#pragma once

#include "api/diagnose_management_module.h"
#include "tool/callback_container.h"
#include "common/util/async_capture_protect.h"
#include "BusinessManager.h"

#include <unordered_map>
#include <vector>
#include <string>
#include <functional>



namespace flight_hub_sdk {
/**
  * @brief [设备定损] 模块 API;
  * [注意] 本模块暂未开放使用;
  */
class DiagnosisManagementModuleImpl : public IDiagnosisManagementModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit DiagnosisManagementModuleImpl();

    // 析构函数;
    ~DiagnosisManagementModuleImpl();

    //  [注意] 本接口暂未开放使用;
    // (设备)诊断管理模块;
    // 设备定损: 下发定损指令;
    void createSelfDiagnosis(
        const CreateSelfDiagnosisParams &params,
        const std::function<void(FlightHubError err)>& callback) override;

    //  [注意] 本接口暂未开放使用;
    // (设备)诊断管理模块;
    // 定损操作(取消/删除);
    void operateSelfDiagnosis(
        const OperateSelfDiagnosisParams &params,
        const std::function<void(FlightHubError err)>& callback) override;


    //  [注意] 本接口暂未开放使用;
    // (设备)诊断管理模块;
    // 设备定损: 查询单条定损报告详情;
    void getSelfDiagnosisReport(
        const std::string &report_uuid,
        const std::function<void(const GetSelfDiagnosisReportResults& results, FlightHubError err)>& callback) override;


    //  [注意] 本接口暂未开放使用;
    // (设备)诊断管理模块;
    // 设备定损: 查询定损报告列表;
    void getSelfDiagnosisReportList(
        const GetSelfDiagnosisReportListParams &params,
        const std::function<void(const GetSelfDiagnosisReportListResults& results, FlightHubError err)>& callback) override;


private:
};

} // namespace flight_hub_sdk
