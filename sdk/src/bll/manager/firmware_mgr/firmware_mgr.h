// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "http_interface/response_message.h"
#include "cloud_storage_uploader/cloud_storage_interface.h"
#include "message.h"
#include <functional>
#include <memory>


namespace flight_hub_sdk {

class FirmwareManager: public drc_sdk::common::AsyncCaptureProtect {
public:
    FirmwareManager();
    virtual ~FirmwareManager();
    void getLatestFirmwareReleaseNote(const std::vector<std::string>& device_model_keys, const std::string& language, std::function<void(FlightHubError err, const FirmwareReleaseNoteListResult& release_note_list)> callback);
    void invokeFirmwareUpgrade(const FirmwareUpgradeParam& params, std::function<void(FlightHubError err, const FirmwareUpgradeResult& result)> callback);
    void queryFirmwareUpgradeProgress(const std::string& device_sn, std::function<void(FlightHubError err, const FirmwareUpgradeProgressResult& progress_list)> callback);
    void uploadFirmwareFile(const std::string& file_path, std::function<void(FlightHubError err, UploadFileResults results)> callback);
    void releaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback);
    void unReleaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback);
    void deleteFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback);
    void queryFirmwareList(int page, int page_size, std::function<void(FlightHubError err, const FirmwareListResult& firmware_list)> callback);
    void queryFirmwareCompatibilityConfig(std::function<void(FlightHubError err, const FirmwareCompatibilityConfigResult& config)> callback);
    void checkFirmwareStatus(const std::vector<std::string>& device_sns, const std::vector<std::string>& status, std::function<void(FlightHubError err, const FirmwareStatusListResult& status_list)> callback);
    void submitFirmwareInfo(const FirmwareInfoParam& firmware_info, std::function<void(FlightHubError err)> callback);
private:
    void GetFirmwareStsToken(const std::function<void(FlightHubError err, const FirmwareUpgradeStsResponse& sts_response)>& callback);
    std::shared_ptr<ICloudStorageUploader> getCloudStorageUploader(const std::string& provider);

};

}  // namespace flight_hub_sdk