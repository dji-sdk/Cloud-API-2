// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/ai_box_module.h"
#include "bll/manager/ai_box_mgr/ai_box_mgr.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class AIBoxModuleImpl : public IAIBoxModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit AIBoxModuleImpl();
    ~AIBoxModuleImpl() override;

    void fetchAIBoxInfo(const std::string& device_sn, std::function<void(FlightHubError err, const AIBoxInfoListResults& results)> callback) override;
    void fetchFileDownloadUrl(const FetchFileDownloadUrlParams& params, std::function<void(FlightHubError err, const FileDownloadUrlListResults& results)> callback) override;
    void fetchAppList(const FetchAppListParam& param, std::function<void(FlightHubError err, const FetchAppListResults& results)> callback) override;
    void fetchAppListProgress(const FetchAppListProgressParam& param, std::function<void(FlightHubError err, const FetchAppListProgressResults& results)> callback) override;
    void exportFile(const ExportFileParam& param, std::function<void(FlightHubError err, const ExportFileResults& results)> callback) override;
    void exportFileProgress(const ExportFileProgressParam& param, std::function<void(FlightHubError err, const ExportFileProgressResults& results)> callback) override;
    void uploadAIBoxFile(const UploadFileParam& param, std::function<void(FlightHubError err, const UploadFileResults& results)> callback) override;
    void installApp(const InstallAppParam& param, std::function<void(FlightHubError err, const InstallAppResults& results)> callback) override;
    void installAppProgress(const InstallAppProgressParam& param, std::function<void(FlightHubError err, const InstallAppProgressResults& results)> callback) override;
    void operateApp(const OperateAppParam& param, std::function<void(FlightHubError err, const OperateAppResults& results)> callback) override;

private:
    AIBoxManager ai_box_mgr_;
};

}  // namespace flight_hub_sdk
