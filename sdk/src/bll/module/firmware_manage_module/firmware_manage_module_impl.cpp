// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "firmware_manage_module_impl.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include <base/common/logger.h>

namespace flight_hub_sdk {

FirmwareManageModuleImpl::FirmwareManageModuleImpl()
    : firmware_mgr_() {
    LOG_INFO("[FirmwareManageModuleImpl] constructor");
}

void FirmwareManageModuleImpl::getLatestFirmwareReleaseNote(const std::vector<std::string>& device_model_keys, const std::string& language, std::function<void(FlightHubError err, FirmwareReleaseNoteListResult release_note_list)> callback) {
    firmware_mgr_.getLatestFirmwareReleaseNote(device_model_keys, language, std::move(callback));
}

void FirmwareManageModuleImpl::invokeFirmwareUpgrade(const FirmwareUpgradeParam& params, std::function<void(FlightHubError err, FirmwareUpgradeResult result)> callback) {
    firmware_mgr_.invokeFirmwareUpgrade(params, std::move(callback));
}

void FirmwareManageModuleImpl::queryFirmwareUpgradeProgress(const std::string& device_sn, std::function<void(FlightHubError err, FirmwareUpgradeProgressResult progress_list)> callback) {
    firmware_mgr_.queryFirmwareUpgradeProgress(device_sn, std::move(callback));
}

void FirmwareManageModuleImpl::uploadFirmwareFile(const std::string& file_path, std::function<void(FlightHubError err, UploadFileResults results)> callback) {
    firmware_mgr_.uploadFirmwareFile(file_path, std::move(callback));
}

void FirmwareManageModuleImpl::releaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) {
    firmware_mgr_.releaseFirmware(file_id, std::move(callback));
}

void FirmwareManageModuleImpl::unReleaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) {
    firmware_mgr_.unReleaseFirmware(file_id, std::move(callback));
}

void FirmwareManageModuleImpl::deleteFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) {
    firmware_mgr_.deleteFirmware(file_id, std::move(callback));
}

void FirmwareManageModuleImpl::queryFirmwareList(int page, int page_size, std::function<void(FlightHubError err, FirmwareListResult firmware_list)> callback) {
    firmware_mgr_.queryFirmwareList(page, page_size, std::move(callback));
}

void FirmwareManageModuleImpl::queryFirmwareCompatibilityConfig(std::function<void(FlightHubError err, FirmwareCompatibilityConfigResult config)> callback) {
    firmware_mgr_.queryFirmwareCompatibilityConfig(std::move(callback));
}

void FirmwareManageModuleImpl::checkFirmwareStatus(const std::vector<std::string>& device_sns, const std::vector<std::string>& status, std::function<void(FlightHubError err, FirmwareStatusListResult status_list)> callback) {
    firmware_mgr_.checkFirmwareStatus(device_sns, status, std::move(callback));
}

void FirmwareManageModuleImpl::submitFirmwareInfo(const FirmwareInfoParam& firmware_info, std::function<void(FlightHubError err)> callback) {
    firmware_mgr_.submitFirmwareInfo(firmware_info, std::move(callback));
}

int FirmwareManageModuleImpl::listenDeviceOtaProgress(const std::string& device_sn, const std::function<void(const DeviceOtaProgress& msg)>& callback) {
    WeakDummy(weak_ptr);
    int user_listen_id = ota_progress_next_id_++;

    auto nc_listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceOtaProgress(
        [this, weak_ptr, callback](const notification_center::DeviceOtaProgress& msg) -> void {
            WeakDummyReturn(weak_ptr);
            DeviceOtaProgress res{};
            res.bid = msg.bid;
            res.timestamp = msg.timestamp;
            res.status = msg.status;
            res.result = msg.result;
            res.progress.percent = msg.progress.percent;
            res.progress.current_step = msg.progress.current_step;
            res.progress.ext = msg.progress.ext;

            LOG_INFO("[FirmwareManageModuleImpl] ListenDeviceOtaProgress: bid: %s, status: %s, percent: %d",
                msg.bid.c_str(), msg.status.c_str(), msg.progress.percent);

            if (callback) {
                callback(res);
            }
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[FirmwareManageModuleImpl] ListenDeviceOtaProgress error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_Any);

    ota_progress_nc_listen_ids_[user_listen_id] = nc_listen_id;
    LOG_INFO("[FirmwareManageModuleImpl] listenDeviceOtaProgress listen_id: %d, nc_listen_id: %lld", user_listen_id, (long long)nc_listen_id);
    return user_listen_id;
}

void FirmwareManageModuleImpl::cancelListenDeviceOtaProgress(int listen_id) {
    LOG_INFO("[FirmwareManageModuleImpl] cancelListenDeviceOtaProgress listen_id: %d", listen_id);
    auto it = ota_progress_nc_listen_ids_.find(listen_id);
    if (it != ota_progress_nc_listen_ids_.end()) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(it->second);
        ota_progress_nc_listen_ids_.erase(it);
    }
}

}  // namespace flight_hub_sdk
