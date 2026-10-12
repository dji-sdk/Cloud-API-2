// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/log_management_module.h"
#include "tool/callback_container.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include <unordered_map>

namespace flight_hub_sdk {

class LogManagementModuleImpl : public ILogManagementModule, public drc_sdk::common::AsyncCaptureProtect{
public:
    explicit LogManagementModuleImpl();
    ~LogManagementModuleImpl();
    void QueryDeviceLogList(const std::string& device_sn, const std::vector<int> module_list, const std::function<void(FlightHubError err, DeviceLogListResult result)>& callback) override;
    void CreateDeviceLogReport(const CreateDeviceLogReportRequestParam& param, const std::function<void(FlightHubError err, CreateDeviceLogReportResult result)>& callback) override;
    void LogReportList(const std::string& device_sn, int page, int page_size, const std::function<void(FlightHubError err, LogReportListResult result)>& callback) override;
    void LogReportOperation(const std::string& operation, const std::string& report_id, const std::function<void(FlightHubError err)>& callback) override;
    void UploadLogAppendFile(const std::string& file_path, std::function<void(FlightHubError err)> callback) override;
    int listenDeviceFileUploadProgress(const std::string& device_sn, const std::function<void(const DeviceFileUploadProgress& msg)>& callback) override;
    void cancelListenDeviceFileUploadProgress(int listen_id) override;
private:
    void GetStsToken(const std::function<void(FlightHubError err, const LogReportAppendStsResponse& sts_response)>& callback);
    std::unordered_map<int, int64_t> file_upload_nc_listen_ids_;
    int file_upload_next_id_ = 0;
};

} // namespace flight_hub_sdk