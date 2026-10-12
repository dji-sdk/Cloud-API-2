// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "log_management_module_impl.h"

#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include <base/common/logger.h>
#include <chrono>
#include "cloud_storage_uploader/cloud_storage_factory.h"
#include <filesystem>
#include <memory>
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

namespace flight_hub_sdk {
LogManagementModuleImpl::LogManagementModuleImpl()
    {
    LOG_INFO("[LogManagementModuleImpl] constructor");
}

LogManagementModuleImpl::~LogManagementModuleImpl() {}

void LogManagementModuleImpl::QueryDeviceLogList(const std::string& device_sn, const std::vector<int> module_list, const std::function<void(FlightHubError err, DeviceLogListResult result)>& callback) {
    WeakDummy(weak_ptr);
    get_device_log_list(device_sn, module_list, [weak_ptr, callback](const DeviceLogListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("QueryDeviceLogList: err_code: %d", (int)err.error_code);
                }
                DeviceLogListResult result;
                for (const auto& file : response.files) {
                    DeviceLogListResultFileItem fileItem;
                    fileItem.device_sn = file.device_sn;
                    fileItem.module = file.module;
                    fileItem.result = file.result;
                    fileItem.ObjectKey = file.ObjectKey;
                    fileItem.log_type = file.log_type;
                    fileItem.responsibility_log_type = file.responsibility_log_type;

                    for (const auto& log : file.list) {
                        DeviceLogListResultLogItem logItem;
                        logItem.boot_index = log.boot_index;
                        logItem.start_time = log.start_time;
                        logItem.end_time = log.end_time;
                        logItem.size = log.size;
                        logItem.md5 = log.md5;
                        fileItem.list.push_back(logItem);
                    }
                    result.files.push_back(fileItem);
                }

                // 埋点: 查询设备日志列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_GET_DEVICE_LOG_LIST, err, "[LogManagementModuleImpl]");

                if (callback) {
                    callback(err, result);
                }
            });
    });
}

void LogManagementModuleImpl::CreateDeviceLogReport(const CreateDeviceLogReportRequestParam& param, const std::function<void(FlightHubError err, CreateDeviceLogReportResult result)>& callback) {
    WeakDummy(weak_ptr);
    CreateDeviceLogReportRequest request;
    request.report_information = param.report_information;
    request.error_start_time = param.error_start_time;

    // device_topo
    request.device_topo.parents.clear();
    for (const auto& parent : param.device_topo.parents) {
        DeviceLogReportDeviceTopoItem item;
        item.device_model_key = parent.device_model_key;
        item.sn = parent.sn;
        request.device_topo.parents.push_back(item);
    }
    request.device_topo.hosts.clear();
    for (const auto& host : param.device_topo.hosts) {
        DeviceLogReportDeviceTopoItem item;
        item.device_model_key = host.device_model_key;
        item.sn = host.sn;
        request.device_topo.hosts.push_back(item);
    }

    // report_contact_information
    request.report_contact_information.email = param.report_contact_information.email;
    request.report_contact_information.phone = param.report_contact_information.phone;

    // appendix_files
    request.appendix_files = param.appendix_files;

    // log_list
    request.log_list.clear();
    for (const auto& log_list_item : param.log_list) {
        DeviceLogReportLogListItem logItem;
        logItem.device_sn = log_list_item.device_sn;
        logItem.module = log_list_item.module;
        for (const auto& log : log_list_item.list) {
            DeviceLogReportLogItem logDetail;
            logDetail.boot_index = log.boot_index;
            logDetail.start_time = log.start_time;
            logDetail.end_time = log.end_time;
            logDetail.size = log.size;
            logDetail.md5 = log.md5;
            logItem.list.push_back(logDetail);
        }
        request.log_list.push_back(logItem);
    }

    // domains
    request.domains.clear();
    for (const auto& domain : param.domains) {
        DeviceLogReportDomainItem domainItem;
        domainItem.key = domain.key;
        domainItem.value = domain.value;
        request.domains.push_back(domainItem);
    }

    request.is_crashed = param.is_crashed;

    post_create_device_log_report(request, [weak_ptr, callback](const CreateDeviceLogReportResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("CreateDeviceLogReport: err_code: %d", (int)err.error_code);
                }
                CreateDeviceLogReportResult result;
                result.track_number = response.track_number;
                result.report_uuid = response.report_uuid;
                result.error_log_list = response.error_log_list;

                // creat_device_job_data
                result.creat_device_job_data.bid = response.creat_device_job_data.bid;
                result.creat_device_job_data.sn = response.creat_device_job_data.sn;
                result.creat_device_job_data.biz_code = response.creat_device_job_data.biz_code;
                result.creat_device_job_data.status = response.creat_device_job_data.status;
                result.creat_device_job_data.result = response.creat_device_job_data.result;
                result.creat_device_job_data.service_identifier = response.creat_device_job_data.service_identifier;

                // progress
                result.creat_device_job_data.progress.percent = response.creat_device_job_data.progress.percent;
                result.creat_device_job_data.progress.current_step = response.creat_device_job_data.progress.current_step;
                result.creat_device_job_data.progress.ext = response.creat_device_job_data.progress.ext;

                // 埋点: 创建设备日志上报单; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_CREATE_DEVICE_LOG_REPORT, err, "[LogManagementModuleImpl]");

                if (callback) {
                    callback(err, result);
                }
            });
    });
}

void LogManagementModuleImpl::LogReportList(const std::string& device_sn, int page, int page_size, const std::function<void(FlightHubError err, LogReportListResult result)>& callback) {
    WeakDummy(weak_ptr);
    get_log_report_list(device_sn, page, page_size, [weak_ptr, callback](const LogReportListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("LogReportList: err_code: %d", (int)err.error_code);
                }
                LogReportListResult result;
                result.pagination.page = response.pagination.page;
                result.pagination.page_size = response.pagination.page_size;
                result.pagination.total = response.pagination.total;

                result.list.clear();
                for (const auto& item : response.list) {
                    LogReportListResultItem resultItem;
                    resultItem.report_uuid = item.report_uuid;
                    resultItem.report_time = item.report_time;
                    resultItem.report_user_id = item.report_user_id;
                    resultItem.report_user_organization_callsign = item.report_user_organization_callsign;

                    // device_topo
                    resultItem.device_topo.hosts.clear();
                    for (const auto& host : item.device_topo.hosts) {
                        LogReportListResultDeviceTopoItem topoHost;
                        topoHost.sn = host.sn;
                        topoHost.device_model_key = host.device_model_key;
                        topoHost.device_organization_callsign = host.device_organization_callsign;
                        resultItem.device_topo.hosts.push_back(topoHost);
                    }
                    resultItem.device_topo.parents.clear();
                    for (const auto& parent : item.device_topo.parents) {
                        LogReportListResultDeviceTopoItem topoParent;
                        topoParent.sn = parent.sn;
                        topoParent.device_model_key = parent.device_model_key;
                        topoParent.device_organization_callsign = parent.device_organization_callsign;
                        resultItem.device_topo.parents.push_back(topoParent);
                    }

                    resultItem.report_information = item.report_information;
                    resultItem.track_number = item.track_number;
                    resultItem.error_start_time = item.error_start_time;

                    // appendix_files
                    resultItem.appendix_files.files.clear();
                    for (const auto& file : item.appendix_files.files) {
                        LogReportListResultAppendixFileItem fileItem;
                        fileItem.file_name = file.file_name;
                        fileItem.file_object_key = file.file_object_key;
                        fileItem.file_size = file.file_size;
                        resultItem.appendix_files.files.push_back(fileItem);
                    }

                    // device_logs
                    resultItem.device_logs.files.clear();
                    for (const auto& log : item.device_logs.files) {
                        LogReportListResultDeviceLog logItem;
                        logItem.device_sn = log.device_sn;
                        logItem.module = log.module;
                        logItem.result = log.result;
                        logItem.object_key = log.object_key;
                        for (const auto& listItem : log.list) {
                            LogReportListResultDeviceLogItemList logListItem;
                            logListItem.boot_index = listItem.boot_index;
                            logListItem.start_time = listItem.start_time;
                            logListItem.end_time = listItem.end_time;
                            logListItem.size = listItem.size;
                            logItem.list.push_back(logListItem);
                        }
                        resultItem.device_logs.files.push_back(logItem);
                    }

                    // device_upload_files
                    resultItem.device_upload_files.files.clear();
                    for (const auto& uploadFile : item.device_upload_files.files) {
                        LogReportListResultDeviceUploadFilesItem uploadItem;
                        uploadItem.module = uploadFile.module;
                        uploadItem.size = uploadFile.size;
                        uploadItem.device_sn = uploadFile.device_sn;
                        uploadItem.key = uploadFile.key;
                        uploadItem.fingerprint = uploadFile.fingerprint;
                        uploadItem.progress.current_step = uploadFile.progress.current_step;
                        uploadItem.progress.total_step = uploadFile.progress.total_step;
                        uploadItem.progress.progress = uploadFile.progress.progress;
                        uploadItem.progress.finish_time = uploadFile.progress.finish_time;
                        uploadItem.progress.upload_rate = uploadFile.progress.upload_rate;
                        uploadItem.progress.result = uploadFile.progress.result;
                        uploadItem.progress.status = uploadFile.progress.status;
                        uploadItem.download_url = uploadFile.download_url;
                        resultItem.device_upload_files.files.push_back(uploadItem);
                    }

                    resultItem.status = item.status;
                    resultItem.report_contact_information.email = item.report_contact_information.email;
                    resultItem.report_contact_information.phone = item.report_contact_information.phone;
                    resultItem.bid = item.bid;
                    resultItem.device_report_feedback = item.device_report_feedback;
                    resultItem.read_status = item.read_status;
                    resultItem.url = item.url;

                    result.list.push_back(resultItem);
                }

                // 埋点: 查询日志上报单列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_GET_LOG_REPORT_LIST, err, "[LogManagementModuleImpl]");

                if (callback) {
                    callback(err, result);
                }
            });
    });
}

void LogManagementModuleImpl::LogReportOperation(const std::string& operation, const std::string& report_id, const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    post_log_report_operation(operation, report_id, [weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("LogReportOperation: err_code: %d", (int)err.error_code);
                }

                // 埋点: 日志上报单操作(cancel/delete/read); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_POST_LOG_REPORT_OPERATION, err, "[LogManagementModuleImpl]");

                if (callback) {
                    callback(err);
                }
            });
    });
}

void LogManagementModuleImpl::UploadLogAppendFile(const std::string& file_path, std::function<void(FlightHubError err)> callback) {
    LOG_INFO("[LogManagementModuleImpl] UploadLogAppendFile: file_path: %s", file_path.c_str());

    WeakDummy(weak_ptr);

    // 检查文件是否存在
    if (!std::filesystem::exists(file_path)) {
        LOG_ERROR("[LogManagementModuleImpl] UploadLogAppendFile: File does not exist: %s", file_path.c_str());

        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_NOT_FOUND);

        // 埋点: 上传日志附件(文件不存在); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_UPLOAD_LOG_APPEND_FILE, not_found_err, "[LogManagementModuleImpl]");

        if (callback) {
            callback(not_found_err);
        }
        return;
    }

    // 检查文件大小（限制为 10GB）
    auto file_size = std::filesystem::file_size(file_path);
    const size_t MAX_FILE_SIZE = 10ULL * 1024 * 1024 * 1024; // 10GB
    if (file_size > MAX_FILE_SIZE) {
        LOG_ERROR("[LogManagementModuleImpl] UploadLogAppendFile: File too large: %zu bytes (max: %zu)", file_size, MAX_FILE_SIZE);

        auto too_large_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_TOO_LARGE);

        // 埋点: 上传日志附件(文件超过 10GB 上限); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_UPLOAD_LOG_APPEND_FILE, too_large_err, "[LogManagementModuleImpl]");

        if (callback) {
            callback(too_large_err);
        }
        return;
    }

    // 先获取 STS token
    GetStsToken([this, weak_ptr, file_path, callback](FlightHubError err, const LogReportAppendStsResponse& sts_response) {
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            LOG_ERROR("[LogManagementModuleImpl] UploadLogAppendFile: GetStsToken failed, err_code: %d", (int)err.error_code);

            // 埋点: 上传日志附件(获取 STS token 失败); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_UPLOAD_LOG_APPEND_FILE, err, "[LogManagementModuleImpl]");

            if (callback) {
                callback(err);
            }
            return;
        }

        LOG_INFO("[LogManagementModuleImpl] UploadLogAppendFile: got STS token, provider: %s, bucket: %s, endpoint: %s, file_store_dir: %s",
                 sts_response.provider.c_str(), sts_response.bucket.c_str(),
                 sts_response.endpoint.c_str(), sts_response.file_store_dir.c_str());

        // shared_ptr：异步上传期间延长生命周期，避免 DefaultExecutor 析构 join 堵死调用线程
        auto uploader = std::shared_ptr<ICloudStorageUploader>(
            CloudStorageFactory::createUploader(sts_response.provider));
        if (!uploader) {
            LOG_ERROR("[LogManagementModuleImpl] UploadLogAppendFile: Unsupported provider: %s", sts_response.provider.c_str());

            auto provider_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UNSUPPORTED_PROVIDER);

            // 埋点: 上传日志附件(云存储 provider 不支持); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_UPLOAD_LOG_APPEND_FILE, provider_err, "[LogManagementModuleImpl]");

            if (callback) {
                callback(provider_err);
            }
            return;
        }

        // 将 LogReportAppendStsResponse 转换为 STSInfo
        STSInfo sts_info;
        sts_info.provider = sts_response.provider;
        sts_info.bucket = sts_response.bucket;
        sts_info.endpoint = sts_response.endpoint;
        sts_info.region = sts_response.region;
        sts_info.object_key_prefix = sts_response.file_store_dir;
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
                LOG_INFO("[LogManagementModuleImpl] UploadLogAppendFile progress: %d%% (%zu/%zu bytes), file: %s",
                          progress, *bytes_uploaded_total, total_bytes, file_path.c_str());
            }
        };

        // 捕获 uploader，保证后台线程跑完前不被析构
        uploader->uploadFileAsync(file_path, object_key, sts_info,
            [weak_ptr, callback, file_path, object_key, uploader](const CloudStorageUploadResult& result) {
                WeakDummyReturn(weak_ptr);
                if (result.success) {
                    LOG_INFO("[LogManagementModuleImpl] UploadLogAppendFile success: %s -> %s, ETag: %s",
                             file_path.c_str(), result.object_url.c_str(), result.etag.c_str());

                    auto upload_err = FlightHubError::success();

                    // 埋点: 上传日志附件(上传成功); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_UPLOAD_LOG_APPEND_FILE, upload_err, "[LogManagementModuleImpl]");

                    if (callback) {
                        callback(upload_err);
                    }
                } else {
                    LOG_ERROR("[LogManagementModuleImpl] UploadLogAppendFile failed: %s", result.error_message.c_str());

                    auto upload_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UPLOAD_FAILED);

                    // 埋点: 上传日志附件(云存储上传失败); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_UPLOAD_LOG_APPEND_FILE, upload_err, "[LogManagementModuleImpl]");

                    if (callback) {
                        callback(upload_err);
                    }
                }
            },
            progress_callback);
    });
}

void LogManagementModuleImpl::GetStsToken(const std::function<void(FlightHubError err, const LogReportAppendStsResponse& sts_response)>& callback) {
    WeakDummy(weak_ptr);
    post_log_report_append_sts([weak_ptr, callback](const LogReportAppendStsResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("GetStsToken: err_code: %d", (int)err.error_code);
                }

                if (callback) {
                    callback(err, response);
                }
            });
    });
}

int LogManagementModuleImpl::listenDeviceFileUploadProgress(const std::string& device_sn, const std::function<void(const DeviceFileUploadProgress& msg)>& callback) {
    WeakDummy(weak_ptr);
    int user_listen_id = file_upload_next_id_++;

    auto nc_listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceFileUploadProgress(
        [this, weak_ptr, callback](const notification_center::DeviceFileUploadProgress& msg) -> void {
            WeakDummyReturn(weak_ptr);
            DeviceFileUploadProgress res{};
            res.bid = msg.bid;
            res.timestamp = msg.timestamp;
            res.status = msg.status;
            res.result = msg.result;
            for (const auto& f : msg.files) {
                DeviceFileUploadFileInfo file_info{};
                file_info.device_sn = f.device_sn;
                file_info.key = f.key;
                file_info.module = f.module;
                file_info.progress.percent = f.progress.percent;
                file_info.progress.current_step = f.progress.current_step;
                file_info.progress.status = f.progress.status;
                file_info.progress.total_step = f.progress.total_step;
                file_info.progress.upload_rate = f.progress.upload_rate;
                file_info.size = f.size;
                res.files.push_back(file_info);
            }

            LOG_INFO("[LogManagementModuleImpl] ListenDeviceFileUploadProgress: bid: %s, status: %s",
                msg.bid.c_str(), msg.status.c_str());

            if (callback) {
                callback(res);
            }
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[LogManagementModuleImpl] ListenDeviceFileUploadProgress error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_Any);

    file_upload_nc_listen_ids_[user_listen_id] = nc_listen_id;
    LOG_INFO("[LogManagementModuleImpl] listenDeviceFileUploadProgress listen_id: %d, nc_listen_id: %lld", user_listen_id, (long long)nc_listen_id);
    return user_listen_id;
}

void LogManagementModuleImpl::cancelListenDeviceFileUploadProgress(int listen_id) {
    LOG_INFO("[LogManagementModuleImpl] cancelListenDeviceFileUploadProgress listen_id: %d", listen_id);
    auto it = file_upload_nc_listen_ids_.find(listen_id);
    if (it != file_upload_nc_listen_ids_.end()) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(it->second);
        file_upload_nc_listen_ids_.erase(it);
    }
}

} // namespace flight_hub_sdk