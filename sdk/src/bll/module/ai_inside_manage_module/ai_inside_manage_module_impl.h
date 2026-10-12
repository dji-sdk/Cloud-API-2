// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/ai_inside_manage_module.h"
#include "cloud_storage_uploader/cloud_storage_interface.h"
#include "common/util/async_capture_protect.h"
#include <memory>

namespace flight_hub_sdk {

class AIInsideManageModuleImpl : public IAIInsideManageModule,
                                  public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit AIInsideManageModuleImpl();
    ~AIInsideManageModuleImpl() override = default;

    void getAIInsideModels(
        std::function<void(FlightHubError err, GetAIInsideModelsResult result)> callback) override;

    void getAIInsideModelDevices(
        const GetAIInsideModelDevicesParam& param,
        std::function<void(FlightHubError err, GetAIInsideModelDevicesResult result)> callback) override;

    void getAIInsideModelDevicesSummary(
        const GetAIInsideModelDevicesSummaryParam& param,
        std::function<void(FlightHubError err, GetAIInsideModelDevicesSummaryResult result)> callback) override;

    void operateAIInsideModel(
        const OperateAIInsideModelParam& param,
        std::function<void(FlightHubError err, OperateAIInsideModelResult result)> callback) override;

    void createAIInsideAlgorithm(
        const CreateAIInsideAlgorithmParam& param,
        std::function<void(FlightHubError err, CreateAIInsideAlgorithmResult result)> callback) override;

    void fileUploadAIInsideAlgorithm(
        const FileUploadAIInsideAlgorithmParam& param,
        std::function<void(FlightHubError err, FileUploadAIInsideAlgorithmResult result)> callback) override;

    void deleteAIInsideAlgorithm(
        const DeleteAIInsideAlgorithmParam& param,
        std::function<void(FlightHubError err)> callback) override;

    void uploadAlgorithmFile(
        const UploadAIInsideAlgorithmFileParam& param,
        std::function<void(FlightHubError err, UploadAIInsideAlgorithmFileResult result)> callback) override;

private:

    /// 根据 provider 创建对应的云存储上传器（返回 shared_ptr 以便捕获到异步 lambda 中延长生命周期）
    std::shared_ptr<ICloudStorageUploader> getCloudStorageUploader(const std::string& provider);
};

}  // namespace flight_hub_sdk
