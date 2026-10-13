// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "ai_box_module_impl.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

AIBoxModuleImpl::AIBoxModuleImpl()
    :
      ai_box_mgr_() {
    LOG_INFO("[AIBoxModuleImpl] constructor");
}

AIBoxModuleImpl::~AIBoxModuleImpl() = default;

void AIBoxModuleImpl::fetchAIBoxInfo(const std::string& device_sn, std::function<void(FlightHubError err, const AIBoxInfoListResults& results)> callback) {
    ai_box_mgr_.fetchAIBoxInfo(device_sn, std::move(callback));
}

void AIBoxModuleImpl::fetchFileDownloadUrl(const FetchFileDownloadUrlParams& params, std::function<void(FlightHubError err, const FileDownloadUrlListResults& results)> callback) {
    ai_box_mgr_.FetchFileDownloadUrl(params, std::move(callback));
}

void AIBoxModuleImpl::fetchAppList(const FetchAppListParam& param, std::function<void(FlightHubError err, const FetchAppListResults& results)> callback) {
    ai_box_mgr_.fetchAppList(param, std::move(callback));
}

void AIBoxModuleImpl::fetchAppListProgress(const FetchAppListProgressParam& param, std::function<void(FlightHubError err, const FetchAppListProgressResults& results)> callback) {
    ai_box_mgr_.fetchAppListProgress(param, std::move(callback));
}

void AIBoxModuleImpl::exportFile(const ExportFileParam& param, std::function<void(FlightHubError err, const ExportFileResults& results)> callback) {
    ai_box_mgr_.exportFile(param, std::move(callback));
}

void AIBoxModuleImpl::exportFileProgress(const ExportFileProgressParam& param, std::function<void(FlightHubError err, const ExportFileProgressResults& results)> callback) {
    ai_box_mgr_.exportFileProgress(param, std::move(callback));
}

void AIBoxModuleImpl::uploadAIBoxFile(const UploadFileParam& param, std::function<void(FlightHubError err, const UploadFileResults& results)> callback) {
    ai_box_mgr_.uploadFile(param, std::move(callback));
}

void AIBoxModuleImpl::installApp(const InstallAppParam& param, std::function<void(FlightHubError err, const InstallAppResults& results)> callback) {
    ai_box_mgr_.installApp(param, std::move(callback));
}

void AIBoxModuleImpl::installAppProgress(const InstallAppProgressParam& param, std::function<void(FlightHubError err, const InstallAppProgressResults& results)> callback) {
    ai_box_mgr_.installAppProgress(param, std::move(callback));
}

void AIBoxModuleImpl::operateApp(const OperateAppParam& param, std::function<void(FlightHubError err, const OperateAppResults& results)> callback) {
    ai_box_mgr_.operateApp(param, std::move(callback));
}

}  // namespace flight_hub_sdk
