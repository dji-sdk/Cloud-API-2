// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/firmware_manage_module.h"
#include "bll/manager/firmware_mgr/firmware_mgr.h"
#include "common/util/async_capture_protect.h"
#include <unordered_map>

namespace flight_hub_sdk {

class FirmwareManageModuleImpl : public IFirmwareManageModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit FirmwareManageModuleImpl();

    void getLatestFirmwareReleaseNote(const std::vector<std::string>& device_model_keys, const std::string& language, std::function<void(FlightHubError err, FirmwareReleaseNoteListResult release_note_list)> callback) override;
    void invokeFirmwareUpgrade(const FirmwareUpgradeParam& params, std::function<void(FlightHubError err, FirmwareUpgradeResult result)> callback) override;
    void queryFirmwareUpgradeProgress(const std::string& device_sn, std::function<void(FlightHubError err, FirmwareUpgradeProgressResult progress_list)> callback) override;
    void uploadFirmwareFile(const std::string& file_path, std::function<void(FlightHubError err, UploadFileResults results)> callback) override;
    void releaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) override;
    void unReleaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) override;
    void deleteFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) override;
    void queryFirmwareList(int page, int page_size, std::function<void(FlightHubError err, FirmwareListResult firmware_list)> callback) override;
    void queryFirmwareCompatibilityConfig(std::function<void(FlightHubError err, FirmwareCompatibilityConfigResult config)> callback) override;
    void checkFirmwareStatus(const std::vector<std::string>& device_sns, const std::vector<std::string>& status, std::function<void(FlightHubError err, FirmwareStatusListResult status_list)> callback) override;
    void submitFirmwareInfo(const FirmwareInfoParam& firmware_info, std::function<void(FlightHubError err)> callback) override;

    int listenDeviceOtaProgress(const std::string& device_sn, const std::function<void(const DeviceOtaProgress& msg)>& callback) override;
    void cancelListenDeviceOtaProgress(int listen_id) override;

private:
    FirmwareManager firmware_mgr_;

    // NC listen IDs for OTA progress topic
    std::unordered_map<int, int64_t> ota_progress_nc_listen_ids_;
    int ota_progress_next_id_ = 0;
};

}  // namespace flight_hub_sdk
