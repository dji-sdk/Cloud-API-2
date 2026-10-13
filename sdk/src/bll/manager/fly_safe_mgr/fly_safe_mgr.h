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

class FlySafeManager: public drc_sdk::common::AsyncCaptureProtect {
public:
    FlySafeManager();
    virtual ~FlySafeManager();

    // 解锁证书管理
    void getUnlockLicenseList(const QueryDeviceUnlockLicenseListParam& param, std::function<void(FlightHubError err, const QueryDeviceUnlockLicenseListResult& result)> callback);
    void switchUnlockLicense(const SwitchUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback);
    void checkUnlockLicense(const CheckUnlockLicenseParam& param, std::function<void(FlightHubError err, const CheckUnlockLicenseResult& result)> callback);
    void submitUnlockLicense(const ImportUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback);
    void clearDeviceUnlockLicense(const std::string& device_sn, std::function<void(FlightHubError err)> callback);
    void uploadUnlockLicenseFile(const std::string& file_path, std::function<void(FlightHubError err, const UploadUnlockLicenseFileResult& result)> callback);
private:
    void GetImportUnlockLicenseSts(const std::function<void(FlightHubError err, const GetImportUnlockLicenseStsResponse& sts_response)>& callback);
    std::shared_ptr<ICloudStorageUploader> getCloudStorageUploader(const std::string& provider);
};

}  // namespace flight_hub_sdk