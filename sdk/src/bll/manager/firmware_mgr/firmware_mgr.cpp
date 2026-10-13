// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "firmware_mgr.h"
#include "base/common/logger.h"
#include "common/util/async_capture_protect.h"
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "cloud_storage_uploader/cloud_storage_factory.h"
#include <filesystem>

namespace flight_hub_sdk {

FirmwareManager::FirmwareManager() {
    LOG_INFO("[FirmwareManager] constructor");
}

FirmwareManager::~FirmwareManager() {
    LOG_INFO("[FirmwareManager] FirmwareManager: destructor invoked.");
}

void FirmwareManager::getLatestFirmwareReleaseNote(const std::vector<std::string>& device_model_keys, const std::string& language, std::function<void(FlightHubError err, const FirmwareReleaseNoteListResult& release_note_list)> callback) {
    WeakDummy(weak_ptr);
    get_latest_firmware_release_note(device_model_keys, language, [this, weak_ptr, callback](const FirmwareReleaseNoteListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 获取最新固件发布说明; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_GET_LATEST_RELEASE_NOTE, err, "[FirmwareManager]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("getLatestFirmwareReleaseNote: err_code: %d", static_cast<int>(err.error_code));
            }
            FirmwareReleaseNoteListResult result;
            for (const auto& item : response.list) {
                FirmwareReleaseNoteResult note;
                note.device_model_key = item.device_model_key;
                note.lang = item.lang;
                note.product_version = item.product_version;
                note.release_note = item.release_note;
                note.released_time = item.released_time;
                result.list.push_back(note);
            }
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void FirmwareManager::invokeFirmwareUpgrade(const FirmwareUpgradeParam& params, std::function<void(FlightHubError err, const FirmwareUpgradeResult& result)> callback) {
    WeakDummy(weak_ptr);
    FirmwareUpgradeRequest request;
    for (const auto& device : params.devices) {
        FirmwareUpgradeDeviceItemRequest item;
        item.sn = device.sn;
        item.device_model_key = device.device_model_key;
        item.firmware_upgrade_type = device.firmware_upgrade_type;
        item.product_version = device.product_version;
        request.devices.push_back(item);
    }
    post_invoke_firmware_upgrade(request, [this, weak_ptr, callback](const FirmwareUpgradeResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 触发固件升级; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_INVOKE_UPGRADE, err, "[FirmwareManager]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("invokeFirmwareUpgrade: err_code: %d", static_cast<int>(err.error_code));
            }
            FirmwareUpgradeResult result;
            result.device_code = response.device_code;
            result.status = response.status;
            for (auto& item : response.devices) {
                FirmwareUpgradeResponseDeviceItemResult device_item;
                device_item.sn = item.sn;
                device_item.firmware_upgrade_type = item.firmware_upgrade_type;
                device_item.product_version = item.product_version;
                result.devices.push_back(device_item);
            }
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void FirmwareManager::queryFirmwareUpgradeProgress(const std::string& device_sn, std::function<void(FlightHubError err, const FirmwareUpgradeProgressResult& progress_list)> callback) {
    WeakDummy(weak_ptr);
    get_query_firmware_upgrade_progress(device_sn, [this, weak_ptr, callback](const FirmwareUpgradeProgressResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 查询固件升级进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_QUERY_UPGRADE_PROGRESS, err, "[FirmwareManager]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("queryFirmwareUpgradeProgress: err_code: %d", static_cast<int>(err.error_code));
            }
            FirmwareUpgradeProgressResult result;
            for (const auto& item : response.list) {
                FirmwareUpgradeProgressItem progress_item;
                progress_item.sn = item.sn;
                progress_item.firmware_status = item.firmware_status;
                progress_item.working_status = item.working_status;
                progress_item.device_code = item.device_code;
                progress_item.ota.status = item.ota.status;
                progress_item.ota.progress.current_step = item.ota.progress.current_step;
                progress_item.ota.progress.percent = item.ota.progress.percent;
                progress_item.associate_drone.sn = item.associate_drone.sn;
                progress_item.associate_drone.firmware_status = item.associate_drone.firmware_status;
                progress_item.associate_drone.working_status = item.associate_drone.working_status;
                progress_item.associate_relay.sn = item.associate_relay.sn;
                progress_item.associate_relay.firmware_status = item.associate_relay.firmware_status;
                result.list.push_back(progress_item);
            }
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void FirmwareManager::uploadFirmwareFile(const std::string& file_path, std::function<void(FlightHubError err, UploadFileResults results)> callback) {
    LOG_INFO("[FirmwareManager] uploadFirmwareFile: file_path: %s", file_path.c_str());

    WeakDummy(weak_ptr);

    // 检查文件是否存在
    if (!std::filesystem::exists(file_path)) {
        LOG_ERROR("[FirmwareManager] uploadFirmwareFile: File does not exist: %s", file_path.c_str());

        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_NOT_FOUND);

        // 埋点: 上传固件文件(本地文件不存在, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_UPLOAD_FILE, not_found_err, "[FirmwareManager]");

        if (callback) {
            callback(not_found_err, UploadFileResults{});
        }
        return;
    }

    // 检查文件大小（限制为 20GB）
    auto file_size = std::filesystem::file_size(file_path);
    const size_t MAX_FILE_SIZE = 20ULL * 1024 * 1024 * 1024; // 20GB
    if (file_size > MAX_FILE_SIZE) {
        LOG_ERROR("[FirmwareManager] uploadFirmwareFile: File too large: %zu bytes (max: %zu)", file_size, MAX_FILE_SIZE);

        auto too_large_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_TOO_LARGE);

        // 埋点: 上传固件文件(文件超过 20GB 上限, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_UPLOAD_FILE, too_large_err, "[FirmwareManager]");

        if (callback) {
            callback(too_large_err, UploadFileResults{});
        }
        return;
    }

    // 先获取 STS token
    GetFirmwareStsToken([this, weak_ptr, file_path, callback](FlightHubError err, const FirmwareUpgradeStsResponse& sts_response) {
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            LOG_ERROR("[FirmwareManager] uploadFirmwareFile: GetFirmwareStsToken failed, err_code: %d", static_cast<int>(err.error_code));

            // 埋点: 上传固件文件(获取上传 STS 临时凭证失败, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_UPLOAD_FILE, err, "[FirmwareManager]");

            if (callback) {
                callback(err, UploadFileResults{});
            }
            return;
        }

        LOG_INFO("[FirmwareManager] uploadFirmwareFile: got STS token, provider: %s, bucket: %s, endpoint: %s, region: %s, file_store_dir: %s",
            sts_response.provider.c_str(),
            sts_response.bucket.c_str(),
            sts_response.endpoint.c_str(),
            sts_response.region.c_str(),
            sts_response.file_store_dir.c_str()
        );

        // 获取云存储上传器（shared_ptr：异步上传期间必须延长生命周期，
        // 避免外层 lambda 结束后 unique_ptr 析构 → DefaultExecutor::join 堵死 Main）
        auto uploader = getCloudStorageUploader(sts_response.provider);
        if (!uploader) {
            LOG_ERROR("[FirmwareManager] uploadFirmwareFile: Unsupported provider: %s", sts_response.provider.c_str());

            auto provider_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UNSUPPORTED_PROVIDER);

            // 埋点: 上传固件文件(云存储 provider 不支持, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_UPLOAD_FILE, provider_err, "[FirmwareManager]");

            if (callback) {
                callback(provider_err, UploadFileResults{});
            }
            return;
        }

        // 将 FirmwareUpgradeStsResponse 转换为 STSInfo
        STSInfo sts_info;
        sts_info.provider = sts_response.provider;
        sts_info.bucket = sts_response.bucket;
        sts_info.endpoint = sts_response.endpoint;
        sts_info.region = sts_response.region;
        sts_info.credentials.access_key_id = sts_response.credentials.access_key_id;
        sts_info.credentials.access_key_secret = sts_response.credentials.access_key_secret;
        sts_info.credentials.security_token = sts_response.credentials.security_token;
        sts_info.credentials.expire = static_cast<int>(sts_response.credentials.expire);

        // 生成对象键：file_store_dir + 文件名
        std::string file_name = std::filesystem::path(file_path).filename().string();
        std::string object_key = sts_response.file_store_dir.empty()
                                     ? file_name
                                     : sts_response.file_store_dir + "/" + file_name;

        // 设置进度回调
        auto bytes_uploaded_total = std::make_shared<size_t>(0);
        auto last_log_time = std::make_shared<std::chrono::steady_clock::time_point>(std::chrono::steady_clock::now());
        auto progress_callback = [file_path, bytes_uploaded_total, last_log_time](size_t bytes_uploaded, size_t total_bytes) {
            *bytes_uploaded_total += bytes_uploaded;
            auto now = std::chrono::steady_clock::now();
            if (std::chrono::duration_cast<std::chrono::seconds>(now - *last_log_time).count() >= 1) {
                *last_log_time = now;
                int progress = total_bytes > 0 ? (int)((double)*bytes_uploaded_total / total_bytes * 100.0) : 0;
                if (progress >= 100) progress = 99;
                LOG_INFO("[FirmwareManager] uploadFirmwareFile progress: %d%% (%zu/%zu bytes), file: %s",
                          progress, *bytes_uploaded_total, total_bytes, file_path.c_str());
            }
        };

        // 异步上传固件文件；捕获 uploader，保证后台线程跑完前 AwsS3Uploader/DefaultExecutor 不被析构
        uploader->uploadFileAsync(file_path, object_key, sts_info,
            [weak_ptr, callback, file_path, object_key, uploader](const CloudStorageUploadResult& result) {
                WeakDummyReturn(weak_ptr);
                if (result.success) {
                    LOG_INFO("[FirmwareManager] uploadFirmwareFile success: %s -> %s, ETag: %s",
                             file_path.c_str(), result.object_url.c_str(), result.etag.c_str());

                    auto success_err = FlightHubError::success();

                    // 埋点: 上传固件文件(上传成功); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_UPLOAD_FILE, success_err, "[FirmwareManager]");

                    if (callback) {
                        UploadFileResults res{};
                        res.object_url = result.object_url;
                        res.etag = result.etag;
                        res.object_key = object_key;

                        callback(success_err, res);
                    }
                } else {
                    LOG_ERROR("[FirmwareManager] uploadFirmwareFile failed: %s", result.error_message.c_str());

                    auto upload_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UPLOAD_FAILED);

                    // 埋点: 上传固件文件(云存储上传失败); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_UPLOAD_FILE, upload_err, "[FirmwareManager]");

                    if (callback) {
                        callback(upload_err, UploadFileResults{});
                    }
                }
            },
            progress_callback);
    });
}

void FirmwareManager::GetFirmwareStsToken(const std::function<void(FlightHubError err, const FirmwareUpgradeStsResponse& sts_response)>& callback) {
    WeakDummy(weak_ptr);
    post_firmware_upgrade_sts([weak_ptr, callback](const FirmwareUpgradeStsResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("[FirmwareManager] GetFirmwareStsToken: err_code: %d", static_cast<int>(err.error_code));
            }
            if (callback) {
                callback(err, response);
            }
        });
    });
}

std::shared_ptr<ICloudStorageUploader> FirmwareManager::getCloudStorageUploader(const std::string& provider) {
    // 工厂返回 unique_ptr；转为 shared_ptr 以便捕获到异步回调中延长生命周期
    return std::shared_ptr<ICloudStorageUploader>(CloudStorageFactory::createUploader(provider));
}

void FirmwareManager::releaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    put_release_firmware(file_id, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 发布固件; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_RELEASE, err, "[FirmwareManager]");
            LOG_INFO("releaseFirmware: err_code: %d", static_cast<int>(err.error_code));
            if (callback) {
                callback(err);
            }
        });
    });
}

void FirmwareManager::unReleaseFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    put_unrelease_firmware(file_id, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 取消发布固件; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_UNRELEASE, err, "[FirmwareManager]");
            LOG_INFO("unReleaseFirmware: err_code: %d", static_cast<int>(err.error_code));
            if (callback) {
                callback(err);
            }
        });
    });
}

void FirmwareManager::deleteFirmware(const std::string& file_id, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    delete_delete_firmware(file_id, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 删除固件; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_DELETE, err, "[FirmwareManager]");
            LOG_INFO("deleteFirmware: err_code: %d", static_cast<int>(err.error_code));
            if (callback) {
                callback(err);
            }
        });
    });
}

void FirmwareManager::queryFirmwareList(int page, int page_size, std::function<void(FlightHubError err, const FirmwareListResult& firmware_list)> callback) {
    WeakDummy(weak_ptr);
    get_query_firmware_list(page, page_size, [this, weak_ptr, callback](const FirmwareListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 查询固件列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_QUERY_LIST, err, "[FirmwareManager]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("queryFirmwareList: err_code: %d", static_cast<int>(err.error_code));
            }
            FirmwareListResult result;
            for (const auto& item : response.list) {
                FirmwareItemResult firmware_item;
                firmware_item.id = item.id;
                firmware_item.created_at = item.created_at;
                firmware_item.device_model_key = item.device_model_key;
                firmware_item.status = item.status;
                firmware_item.version = item.version;
                firmware_item.desc = item.desc;
                result.list.push_back(firmware_item);
            }
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void FirmwareManager::queryFirmwareCompatibilityConfig(std::function<void(FlightHubError err, const FirmwareCompatibilityConfigResult& config)> callback) {
    WeakDummy(weak_ptr);
    post_query_firmware_compatibility_config([this, weak_ptr, callback](const FirmwareCompatibilityConfigResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 查询固件兼容性配置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_QUERY_COMPATIBILITY_CONFIG, err, "[FirmwareManager]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("queryFirmwareCompatibilityConfig: err_code: %d", static_cast<int>(err.error_code));
            }
            FirmwareCompatibilityConfigResult result;
            for (const auto& item : response.data) {
                FirmwareCompatibilityConfigItemResult config_item;
                config_item.device_model_key = item.device_model_key;
                config_item.compatible_versions = item.compatible_versions;
                config_item.rules.min_version = item.rules.min_version;
                config_item.rules.max_version = item.rules.max_version;
                result.data.push_back(config_item);
            }
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void FirmwareManager::checkFirmwareStatus(const std::vector<std::string>& device_sns, const std::vector<std::string>& status, std::function<void(FlightHubError err, const FirmwareStatusListResult& status_list)> callback) {
    WeakDummy(weak_ptr);
    get_check_firmware_status(device_sns, status, [this, weak_ptr, callback](const FirmwareStatusListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 检查固件状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_CHECK_STATUS, err, "[FirmwareManager]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("checkFirmwareStatus: err_code: %d", static_cast<int>(err.error_code));
            }
            FirmwareStatusListResult result;
            for (const auto& item : response.list) {
                FirmwareStatusItem status_item;
                status_item.sn = item.sn;
                status_item.firmware_version = item.firmware_version;
                status_item.firmware_status = item.firmware_status;
                status_item.device_model_key = item.device_model_key;
                status_item.services.bid = item.services.bid;
                status_item.services.status = item.services.status;
                status_item.services.create_time = item.services.create_time;
                status_item.services.update_time = item.services.update_time;
                status_item.services.device_code = item.services.device_code;
                status_item.services.progress.percent = item.services.progress.percent;
                status_item.services.progress.current_step = item.services.progress.current_step;
                for (const auto& device : item.services.data.devices) {
                    FirmwareServiceDevice service_device;
                    service_device.sn = device.sn;
                    service_device.firmware_upgrade_type = device.firmware_upgrade_type;
                    service_device.product_version = device.product_version;
                    status_item.services.data.devices.push_back(service_device);
                }
                result.list.push_back(status_item);
            }
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void FirmwareManager::submitFirmwareInfo(const FirmwareInfoParam& firmware_info, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    FirmwareInfoRequest request;
    request.device_model_key = firmware_info.device_model_key;
    request.version = firmware_info.version;
    request.desc = firmware_info.desc;
    request.object_key = firmware_info.object_key;
    request.md5 = firmware_info.md5;
    post_submit_firmware_info(request, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 提交固件信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FIRMWARE_SUBMIT_INFO, err, "[FirmwareManager]");
            LOG_INFO("submitFirmwareInfo: err_code: %d", static_cast<int>(err.error_code));
            if (callback) {
                callback(err);
            }
        });
    });
}

} // namespace flight_hub_sdk