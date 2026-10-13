// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "fly_safe_mgr.h"
#include "base/common/logger.h"
#include "common/util/async_capture_protect.h"
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "cloud_storage_uploader/cloud_storage_factory.h"
#include <filesystem>

namespace flight_hub_sdk {

FlySafeManager::FlySafeManager() {
    LOG_INFO("[FlySafeManager] constructor");
}

FlySafeManager::~FlySafeManager() {
    LOG_INFO("[FlySafeManager] FlySafeManager: destructor invoked.");
}

// 解锁证书管理

void FlySafeManager::getUnlockLicenseList(const QueryDeviceUnlockLicenseListParam& param, std::function<void(FlightHubError err, const QueryDeviceUnlockLicenseListResult& result)> callback) {
    WeakDummy(weak_ptr);
    QueryDeviceUnlockLicenseListRequest request;
    request.device_sn = param.device_sn;
    request.device_model_domain = param.device_model_domain;
    get_unlock_license_list(request, [this, weak_ptr, callback](const QueryDeviceUnlockLicenseListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 获取解锁证书列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_SAFETY_GET_UNLOCK_LICENSE_LIST, err, "[FlySafeManager]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("[FlySafeManager] getUnlockLicenseList: err_code: %d", static_cast<int>(err.error_code));
            }
            QueryDeviceUnlockLicenseListResult result;
            result.result = response.result;
            result.device_model_domain = response.device_model_domain;
            result.consistence = response.consistence;
            for (const auto& item : response.licenses) {
                DeviceUnlockLicenseItem license_item;
                license_item.common_fields.id = item.common_fields.id;
                license_item.common_fields.name = item.common_fields.name;
                license_item.common_fields.type = item.common_fields.type;
                license_item.common_fields.group_id = item.common_fields.group_id;
                license_item.common_fields.user_id = item.common_fields.user_id;
                license_item.common_fields.device_sn = item.common_fields.device_sn;
                license_item.common_fields.begin_time = item.common_fields.begin_time;
                license_item.common_fields.end_time = item.common_fields.end_time;
                license_item.common_fields.user_only = item.common_fields.user_only;
                license_item.common_fields.enabled = item.common_fields.enabled;
                license_item.area_unlock.area_ids = item.area_unlock.area_ids;
                license_item.circle_unlock.radius = item.circle_unlock.radius;
                license_item.circle_unlock.latitude = item.circle_unlock.latitude;
                license_item.circle_unlock.longitude = item.circle_unlock.longitude;
                license_item.circle_unlock.height = item.circle_unlock.height;
                license_item.country_unlock.country_number = item.country_unlock.country_number;
                license_item.country_unlock.height = item.country_unlock.height;
                license_item.height_unlock.height = item.height_unlock.height;
                for (const auto& point : item.polygon_unlock.points) {
                    DeviceUnlockPolygonPoint p;
                    p.latitude = point.latitude;
                    p.longitude = point.longitude;
                    license_item.polygon_unlock.points.push_back(p);
                }
                license_item.power_unlock.level = item.power_unlock.level;
                license_item.rid_unlock.level = item.rid_unlock.level;
                result.licenses.push_back(license_item);
            }
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void FlySafeManager::switchUnlockLicense(const SwitchUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    SwitchDeviceUnlockLicenseRequest request;
    request.device_sn = param.device_sn;
    request.device_cmd_data.device_sn = param.device_sn;
    request.device_cmd_data.license_id = param.license_id;
    request.device_cmd_data.enable = param.enable;
    post_switch_unlock_license(request, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 切换解锁证书状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_SAFETY_SWITCH_UNLOCK_LICENSE, err, "[FlySafeManager]");
            LOG_INFO("[FlySafeManager] switchUnlockLicense: err_code: %d", static_cast<int>(err.error_code));
            if (callback) {
                callback(err);
            }
        });
    });
}

void FlySafeManager::checkUnlockLicense(const CheckUnlockLicenseParam& param, std::function<void(FlightHubError err, const CheckUnlockLicenseResult& result)> callback) {
    WeakDummy(weak_ptr);
    CheckUnlockLicenseRequest request;
    request.file_key = param.file_key;
    request.device_sn = param.device_sn;
    post_check_unlock_license(request, [this, weak_ptr, callback](const CheckUnlockLicenseResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 检查解锁证书; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_SAFETY_CHECK_UNLOCK_LICENSE, err, "[FlySafeManager]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("[FlySafeManager] checkUnlockLicense: err_code: %d", static_cast<int>(err.error_code));
            }
            CheckUnlockLicenseResult result;
            result.err_file_name = response.err_file_name;
            result.err_cert_mismatch = response.err_cert_mismatch;
            for (const auto& item : response.list) {
                CheckUnlockLicenseResultItem result_item;
                result_item.file_name = item.file_name;
                result_item.name = item.name;
                result_item.type = item.type;
                result_item.begin_time = item.begin_time;
                result_item.end_time = item.end_time;
                result_item.url = item.url;
                result.list.push_back(result_item);
            }
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void FlySafeManager::submitUnlockLicense(const ImportUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    ImportUnlockLicenseRequest request;
    request.device_sn = param.device_sn;
    request.cert_url = param.cert_url;
    post_import_unlock_license(request, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 提交解锁证书; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_SAFETY_SUBMIT_UNLOCK_LICENSE, err, "[FlySafeManager]");
            LOG_INFO("[FlySafeManager] importUnlockLicense: err_code: %d", static_cast<int>(err.error_code));
            if (callback) {
                callback(err);
            }
        });
    });
}

void FlySafeManager::clearDeviceUnlockLicense(const std::string& device_sn, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    post_clear_device_unlock_license(device_sn, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 清除设备解锁证书; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_SAFETY_CLEAR_DEVICE_UNLOCK_LICENSE, err, "[FlySafeManager]");
            LOG_INFO("[FlySafeManager] clearDeviceUnlockLicense: err_code: %d", static_cast<int>(err.error_code));
            if (callback) {
                callback(err);
            }
        });
    });
}

void FlySafeManager::uploadUnlockLicenseFile(const std::string& file_path, std::function<void(FlightHubError err, const UploadUnlockLicenseFileResult& result)> callback) {
    LOG_INFO("[FlySafeManager] uploadUnlockLicenseFile: file_path: %s", file_path.c_str());

    WeakDummy(weak_ptr);

    // 检查文件是否存在
    if (!std::filesystem::exists(file_path)) {
        LOG_ERROR("[FlySafeManager] uploadUnlockLicenseFile: File does not exist: %s", file_path.c_str());

        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_NOT_FOUND);

        // 埋点: 上传解禁证书文件(本地文件不存在, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_SAFETY_UPLOAD_UNLOCK_LICENSE_FILE, not_found_err, "[FlySafeManager]");

        if (callback) {
            callback(not_found_err, UploadUnlockLicenseFileResult{});
        }
        return;
    }

    // 检查文件大小（限制为 2GB）
    auto file_size = std::filesystem::file_size(file_path);
    const size_t MAX_FILE_SIZE = 2ULL * 1024 * 1024 * 1024; // 2GB
    if (file_size > MAX_FILE_SIZE) {
        LOG_ERROR("[FlySafeManager] uploadUnlockLicenseFile: File too large: %zu bytes (max: %zu)", file_size, MAX_FILE_SIZE);

        auto too_large_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_TOO_LARGE);

        // 埋点: 上传解禁证书文件(文件超过 2GB 上限, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_SAFETY_UPLOAD_UNLOCK_LICENSE_FILE, too_large_err, "[FlySafeManager]");

        if (callback) {
            callback(too_large_err, UploadUnlockLicenseFileResult{});
        }
        return;
    }

    // 先获取 STS token
    GetImportUnlockLicenseSts([this, weak_ptr, file_path, callback](FlightHubError err, const GetImportUnlockLicenseStsResponse& sts_response) {
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            LOG_ERROR("[FlySafeManager] uploadUnlockLicenseFile: GetImportUnlockLicenseSts failed, err_code: %d", static_cast<int>(err.error_code));

            // 埋点: 上传解禁证书文件(获取上传 STS 临时凭证失败, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_SAFETY_UPLOAD_UNLOCK_LICENSE_FILE, err, "[FlySafeManager]");

            if (callback) {
                callback(err, UploadUnlockLicenseFileResult{});
            }
            return;
        }

        LOG_INFO("[FlySafeManager] uploadUnlockLicenseFile: got STS token, provider: %s, bucket: %s, endpoint: %s, file_store_dir: %s",
                 sts_response.provider.c_str(), sts_response.bucket.c_str(),
                 sts_response.endpoint.c_str(), sts_response.file_store_dir.c_str());

        // shared_ptr：异步上传期间延长生命周期，避免 DefaultExecutor 析构 join 堵死 Main
        auto uploader = getCloudStorageUploader(sts_response.provider);
        if (!uploader) {
            LOG_ERROR("[FlySafeManager] uploadUnlockLicenseFile: Unsupported provider: %s", sts_response.provider.c_str());

            auto provider_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UNSUPPORTED_PROVIDER);

            // 埋点: 上传解禁证书文件(云存储 provider 不支持, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_SAFETY_UPLOAD_UNLOCK_LICENSE_FILE, provider_err, "[FlySafeManager]");

            if (callback) {
                callback(provider_err, UploadUnlockLicenseFileResult{});
            }
            return;
        }

        // 将 GetImportUnlockLicenseStsResponse 转换为 STSInfo
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
                LOG_INFO("[FlySafeManager] uploadUnlockLicenseFile progress: %d%% (%zu/%zu bytes), file: %s",
                          progress, *bytes_uploaded_total, total_bytes, file_path.c_str());
            }
        };

        std::string file_store_dir = sts_response.file_store_dir;
        // 捕获 uploader，保证后台线程跑完前不被析构
        uploader->uploadFileAsync(file_path, object_key, sts_info,
            [weak_ptr, callback, file_path, object_key, file_store_dir, uploader](const CloudStorageUploadResult& result) {
                WeakDummyReturn(weak_ptr);
                if (result.success) {
                    UploadUnlockLicenseFileResult upload_result;
                    upload_result.file_store_dir = file_store_dir;
                    LOG_INFO("[FlySafeManager] uploadUnlockLicenseFile success: %s, file_store_dir: %s",
                             file_path.c_str(), file_store_dir.c_str());

                    auto success_err = FlightHubError::success();

                    // 埋点: 上传解禁证书文件(上传成功); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_SAFETY_UPLOAD_UNLOCK_LICENSE_FILE, success_err, "[FlySafeManager]");

                    if (callback) {
                        callback(success_err, upload_result);
                    }
                } else {
                    LOG_ERROR("[FlySafeManager] uploadUnlockLicenseFile failed: %s", result.error_message.c_str());

                    auto upload_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UPLOAD_FAILED);

                    // 埋点: 上传解禁证书文件(云存储上传失败); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_SAFETY_UPLOAD_UNLOCK_LICENSE_FILE, upload_err, "[FlySafeManager]");

                    if (callback) {
                        callback(upload_err, UploadUnlockLicenseFileResult{});
                    }
                }
            },
            progress_callback);
    });
}

void FlySafeManager::GetImportUnlockLicenseSts(const std::function<void(FlightHubError err, const GetImportUnlockLicenseStsResponse& sts_response)>& callback) {
    WeakDummy(weak_ptr);
    post_import_unlock_license_sts([weak_ptr, callback](const GetImportUnlockLicenseStsResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("[FlySafeManager] GetImportUnlockLicenseSts: err_code: %d", static_cast<int>(err.error_code));
            }
            if (callback) {
                callback(err, response);
            }
        });
    });
}

std::shared_ptr<ICloudStorageUploader> FlySafeManager::getCloudStorageUploader(const std::string& provider) {
    return std::shared_ptr<ICloudStorageUploader>(CloudStorageFactory::createUploader(provider));
}

} // namespace flight_hub_sdk
