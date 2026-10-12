// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "ai_inside_manage_module_impl.h"
#include <base/common/logger.h>
#include "http_interface/http_interface.h"
#include "common/executors/executors.h"
#include "cloud_storage_uploader/cloud_storage_factory.h"
#include <filesystem>

using namespace flight_hub_sdk;

namespace flight_hub_sdk {

AIInsideManageModuleImpl::AIInsideManageModuleImpl()
    {
    LOG_INFO("AIInsideManageModuleImpl: ");
}

void AIInsideManageModuleImpl::getAIInsideModels(
    std::function<void(FlightHubError err, GetAIInsideModelsResult result)> callback) {
    WeakDummy(weak_ptr);
    get_ai_inside_models([this, weak_ptr, callback](const GetAIInsideModelsResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 获取组织下机载算法列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_GET_MODELS, err, "[AIInsideManageModuleImpl]");
                GetAIInsideModelsResult result;
                for (const auto& item : rsp.models) {
                    AIInsideModelItem out;
                    out.signed_id      = item.signed_id;
                    out.signed_name    = item.signed_name;
                    out.model_name     = item.model_name;
                    out.signed_version = item.signed_version;
                    out.signed_utc     = item.signed_utc;
                    out.expired_utc    = item.expired_utc;
                    out.sn_list        = item.sn_list;
                    out.image_source   = item.image_source;
                    out.digital_effect = item.digital_effect;
                    out.vl_resolution  = item.vl_resolution;
                    out.model_param.conf_threshold = item.model_param.conf_threshold;
                    out.model_param.nms_pre        = item.model_param.nms_pre;
                    out.model_param.iou_threshold  = item.model_param.iou_threshold;
                    for (const auto& l : item.labels) {
                        AIInsideLabel label;
                        label.type = l.type;
                        label.name = l.name;
                        out.labels.push_back(label);
                    }
                    result.models.push_back(out);
                }
                if (callback) { callback(err, result); }
            });
        });
}

void AIInsideManageModuleImpl::getAIInsideModelDevices(
    const GetAIInsideModelDevicesParam& param,
    std::function<void(FlightHubError err, GetAIInsideModelDevicesResult result)> callback) {
    WeakDummy(weak_ptr);
    get_ai_inside_model_devices(param,
        [this, weak_ptr, callback](const GetAIInsideModelDevicesResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 获取指定算法绑定的设备详情（分页）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_GET_MODEL_DEVICES, err, "[AIInsideManageModuleImpl]");
                GetAIInsideModelDevicesResult result;
                result.total     = rsp.pagination.total;
                result.page      = rsp.pagination.page;
                result.page_size = rsp.pagination.page_size;
                for (const auto& item : rsp.list) {
                    AIInsideDeviceItem dev;
                    dev.device_sn             = item.device_sn;
                    dev.complete_machine_sn   = item.complete_machine_sn;
                    dev.device_online_status  = item.device_online_status;
                    dev.device_working_status = item.device_working_status;
                    dev.device_model.device_model_key = item.device_model.device_model_key;
                    dev.device_model.domain           = item.device_model.domain;
                    dev.device_model.name             = item.device_model.name;
                    dev.operate_ai_model.device_cmd_method = item.operate_ai_model.device_cmd_method;
                    dev.operate_ai_model.status            = item.operate_ai_model.status;
                    dev.operate_ai_model.progress.percent      = item.operate_ai_model.progress.percent;
                    dev.operate_ai_model.progress.current_step = item.operate_ai_model.progress.current_step;
                    dev.operate_ai_model.device_code  = item.operate_ai_model.device_code;
                    dev.operate_ai_model.create_time  = item.operate_ai_model.create_time;
                    const auto& d = item.associate_drone_device_info;
                    dev.associate_drone_device_info.device_sn             = d.device_sn;
                    dev.associate_drone_device_info.device_online_status  = d.device_online_status;
                    dev.associate_drone_device_info.device_working_status = d.device_working_status;
                    dev.associate_drone_device_info.device_model.device_model_key = d.device_model.device_model_key;
                    dev.associate_drone_device_info.device_model.domain           = d.device_model.domain;
                    dev.associate_drone_device_info.device_model.name             = d.device_model.name;
                    dev.associate_drone_device_info.ai_model_info.signed_id   = d.ai_model_info.signed_id;
                    dev.associate_drone_device_info.ai_model_info.model_name  = d.ai_model_info.model_name;
                    dev.associate_drone_device_info.ai_model_info.signed_utc  = d.ai_model_info.signed_utc;
                    dev.associate_drone_device_info.ai_model_info.expired_utc = d.ai_model_info.expired_utc;
                    dev.associate_drone_device_info.ai_model_info.status      = d.ai_model_info.status;
                    result.list.push_back(dev);
                }
                if (callback) { callback(err, result); }
            });
        });
}

void AIInsideManageModuleImpl::getAIInsideModelDevicesSummary(
    const GetAIInsideModelDevicesSummaryParam& param,
    std::function<void(FlightHubError err, GetAIInsideModelDevicesSummaryResult result)> callback) {
    WeakDummy(weak_ptr);
    get_ai_inside_model_devices_summary(param,
        [this, weak_ptr, callback](const GetAIInsideModelDevicesSummaryResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 获取指定算法在指定操作类型下的可操作设备数量统计; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_GET_MODEL_DEVICES_SUMMARY, err, "[AIInsideManageModuleImpl]");
                GetAIInsideModelDevicesSummaryResult result;
                result.total_devices    = rsp.total_devices;
                result.operable_devices = rsp.operable_devices;
                if (callback) { callback(err, result); }
            });
        });
}

void AIInsideManageModuleImpl::operateAIInsideModel(
    const OperateAIInsideModelParam& param,
    std::function<void(FlightHubError err, OperateAIInsideModelResult result)> callback) {
    WeakDummy(weak_ptr);
    OperateAIInsideModelRequest request;
    request.operation_type = param.operation_type;
    request.drone_sn_list  = param.drone_sn_list;
    post_operate_ai_inside_model(param.signed_id, request,
        [this, weak_ptr, callback](const OperateAIInsideModelResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 批量下发 Load / Update / Unload 操作指令; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_OPERATE_MODEL, err, "[AIInsideManageModuleImpl]");
                OperateAIInsideModelResult result;
                for (const auto& item : rsp.results) {
                    AIInsideOperateDeviceResult r;
                    r.drone_sn = item.drone_sn;
                    r.code     = item.code;
                    result.results.push_back(r);
                }
                if (callback) { callback(err, result); }
            });
        });
}

void AIInsideManageModuleImpl::createAIInsideAlgorithm(
    const CreateAIInsideAlgorithmParam& param,
    std::function<void(FlightHubError err, CreateAIInsideAlgorithmResult result)> callback) {
    WeakDummy(weak_ptr);
    CreateAIInsideAlgorithmRequest request;
    request.name = param.name;
    post_create_ai_inside_algorithm(request,
        [this, weak_ptr, callback](const CreateAIInsideAlgorithmResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 创建算法包上传凭证（私有化）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_CREATE_ALGORITHM, err, "[AIInsideManageModuleImpl]");
                CreateAIInsideAlgorithmResult result;
                result.credentials.access_key_id     = rsp.credentials.access_key_id;
                result.credentials.access_key_secret = rsp.credentials.access_key_secret;
                result.credentials.expire            = rsp.credentials.expire;
                result.credentials.security_token    = rsp.credentials.security_token;
                result.provider       = rsp.provider;
                result.bucket         = rsp.bucket;
                result.region         = rsp.region;
                result.endpoint       = rsp.endpoint;
                result.file_store_dir = rsp.file_store_dir;
                if (callback) { callback(err, result); }
            });
        });
}

void AIInsideManageModuleImpl::fileUploadAIInsideAlgorithm(
    const FileUploadAIInsideAlgorithmParam& param,
    std::function<void(FlightHubError err, FileUploadAIInsideAlgorithmResult result)> callback) {
    WeakDummy(weak_ptr);
    FileUploadAIInsideAlgorithmRequest request;
    request.file_key = param.file_key;
    post_file_upload_ai_inside_algorithm(request,
        [this, weak_ptr, callback](const FileUploadAIInsideAlgorithmResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 处理已上传的算法文件; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_FILE_UPLOAD_ALGORITHM, err, "[AIInsideManageModuleImpl]");
                FileUploadAIInsideAlgorithmResult result;
                const auto& d = rsp.detail;
                result.detail.id             = d.id;
                result.detail.created_at     = d.created_at;
                result.detail.updated_at     = d.updated_at;
                result.detail.signed_id      = d.signed_id;
                result.detail.signed_name    = d.signed_name;
                result.detail.signed_version = d.signed_version;
                result.detail.signed_utc     = d.signed_utc;
                result.detail.model_uuid     = d.model_uuid;
                result.detail.model_name     = d.model_name;
                result.detail.model_type     = d.model_type;
                result.detail.file_key       = d.file_key;
                result.detail.applicable_product = d.applicable_product;
                result.detail.image_source   = d.image_source;
                result.detail.digital_effect = d.digital_effect;
                result.detail.vl_resolution  = d.vl_resolution;
                result.detail.model_param.conf_threshold = d.model_param.conf_threshold;
                result.detail.model_param.nms_pre        = d.model_param.nms_pre;
                result.detail.model_param.iou_threshold  = d.model_param.iou_threshold;
                result.detail.license.sn_list     = d.license.sn_list;
                result.detail.license.started_utc = d.license.started_utc;
                result.detail.license.expired_utc = d.license.expired_utc;
                for (const auto& l : d.labels) {
                    AIInsideLabel label;
                    label.type = l.type;
                    label.name = l.name;
                    result.detail.labels.push_back(label);
                }
                if (callback) { callback(err, result); }
            });
        });
}

void AIInsideManageModuleImpl::deleteAIInsideAlgorithm(
    const DeleteAIInsideAlgorithmParam& param,
    std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    DeleteAIInsideAlgorithmRequest request;
    request.signed_id = param.signed_id;
    delete_ai_inside_algorithm(request,
        [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 删除指定算法; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_DELETE_ALGORITHM, err, "[AIInsideManageModuleImpl]");
                if (callback) { callback(err); }
            });
        });
}

// =========================================================================
// uploadAlgorithmFile：一站式算法文件上传
// 串联三步：① 调 createAIInsideAlgorithm 获取 STS 凭证 → ② 上传 ZIP 到 MinIO → ③ 通知后端入库
// =========================================================================

std::shared_ptr<ICloudStorageUploader> AIInsideManageModuleImpl::getCloudStorageUploader(
    const std::string& provider) {
    // 工厂返回 unique_ptr；此处接管所有权转为 shared_ptr，以便捕获到异步 lambda 中延长对象生命周期。
    // uploadFileAsync 在后台线程异步执行，若使用 unique_ptr 则 uploader 在外层 lambda
    // 退出时即析构，后台线程随后访问悬空的 this 指针会触发 SIGSEGV。
    return std::shared_ptr<ICloudStorageUploader>(CloudStorageFactory::createUploader(provider));
}

void AIInsideManageModuleImpl::uploadAlgorithmFile(
    const UploadAIInsideAlgorithmFileParam& param,
    std::function<void(FlightHubError err, UploadAIInsideAlgorithmFileResult result)> callback) {
    LOG_INFO("[AIInsideManageModule] uploadAlgorithmFile: file_path: %s", param.file_path.c_str());

    WeakDummy(weak_ptr);

    // ① 校验文件存在
    if (!std::filesystem::exists(param.file_path)) {
        LOG_ERROR("[AIInsideManageModule] uploadAlgorithmFile: File does not exist: %s",
                  param.file_path.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_NOT_FOUND);
        // 埋点: 上传 AI Inside 算法文件(本地文件不存在, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_UPLOAD_ALGORITHM_FILE, not_found_err, "[AIInsideManageModuleImpl]");
        if (callback) {
            callback(not_found_err, UploadAIInsideAlgorithmFileResult{});
        }
        return;
    }

    // ① 校验文件大小（算法包限制 500MB）
    auto file_size = std::filesystem::file_size(param.file_path);
    const size_t MAX_FILE_SIZE = 500ULL * 1024 * 1024;
    if (file_size > MAX_FILE_SIZE) {
        LOG_ERROR("[AIInsideManageModule] uploadAlgorithmFile: File too large: %zu bytes (max: %zu)",
                  file_size, MAX_FILE_SIZE);
        auto too_large_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_TOO_LARGE);
        // 埋点: 上传 AI Inside 算法文件(文件超出 500MB 限制, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_UPLOAD_ALGORITHM_FILE, too_large_err, "[AIInsideManageModuleImpl]");
        if (callback) {
            callback(too_large_err, UploadAIInsideAlgorithmFileResult{});
        }
        return;
    }

    std::string file_path = param.file_path;
    std::string file_name = std::filesystem::path(file_path).filename().string();

    // ② 调 post_create_ai_inside_algorithm 获取 STS 上传凭证
    CreateAIInsideAlgorithmRequest sts_request;
    sts_request.name = file_name;
    post_create_ai_inside_algorithm(sts_request,
        [this, weak_ptr, file_path, file_name, callback](
                const CreateAIInsideAlgorithmResponse& sts_rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("[AIInsideManageModule] uploadAlgorithmFile: createAIInsideAlgorithm failed, "
                          "err_code: %d", static_cast<int>(err.error_code));
                // 埋点: 上传 AI Inside 算法文件(获取 STS 上传凭证失败); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_UPLOAD_ALGORITHM_FILE, err, "[AIInsideManageModuleImpl]");
                if (callback) {
                    callback(err, UploadAIInsideAlgorithmFileResult{});
                }
                return;
            }

            LOG_INFO("[AIInsideManageModule] uploadAlgorithmFile: got STS, provider: %s, bucket: %s, "
                     "endpoint: %s, file_store_dir: %s",
                     sts_rsp.provider.c_str(), sts_rsp.bucket.c_str(),
                     sts_rsp.endpoint.c_str(), sts_rsp.file_store_dir.c_str());

            // ③ 根据 provider 创建云存储上传器
            // 使用 shared_ptr 并捕获到 lambda，确保异步上传期间 uploader 不被析构（避免 use-after-free）
            auto uploader = getCloudStorageUploader(sts_rsp.provider);
            if (!uploader) {
                LOG_ERROR("[AIInsideManageModule] uploadAlgorithmFile: Unsupported provider: %s",
                          sts_rsp.provider.c_str());
                auto provider_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UNSUPPORTED_PROVIDER);
                // 埋点: 上传 AI Inside 算法文件(云存储 provider 不支持, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_UPLOAD_ALGORITHM_FILE, provider_err, "[AIInsideManageModuleImpl]");
                if (callback) {
                    callback(provider_err, UploadAIInsideAlgorithmFileResult{});
                }
                return;
            }

            // 构造 STSInfo
            STSInfo sts_info;
            sts_info.provider = sts_rsp.provider;
            sts_info.bucket   = sts_rsp.bucket;
            sts_info.endpoint = sts_rsp.endpoint;
            sts_info.region   = sts_rsp.region;
            sts_info.credentials.access_key_id     = sts_rsp.credentials.access_key_id;
            sts_info.credentials.access_key_secret = sts_rsp.credentials.access_key_secret;
            sts_info.credentials.security_token    = sts_rsp.credentials.security_token;
            sts_info.credentials.expire = static_cast<int>(sts_rsp.credentials.expire);

            // 对象键：file_store_dir + 文件名
            std::string object_key = sts_rsp.file_store_dir.empty()
                                         ? file_name
                                         : sts_rsp.file_store_dir + "/" + file_name;

            // 上传进度回调（每秒打一次日志）
            auto bytes_uploaded_total = std::make_shared<size_t>(0);
            auto last_log_time = std::make_shared<std::chrono::steady_clock::time_point>(
                std::chrono::steady_clock::now());
            auto progress_callback = [file_path, bytes_uploaded_total, last_log_time](
                    size_t bytes_uploaded, size_t total_bytes) {
                *bytes_uploaded_total += bytes_uploaded;
                auto now = std::chrono::steady_clock::now();
                if (std::chrono::duration_cast<std::chrono::seconds>(now - *last_log_time).count() >= 1) {
                    *last_log_time = now;
                    int progress = total_bytes > 0
                        ? static_cast<int>(static_cast<double>(*bytes_uploaded_total) / total_bytes * 100.0)
                        : 0;
                    if (progress >= 100) progress = 99;
                    LOG_INFO("[AIInsideManageModule] uploadAlgorithmFile progress: %d%% (%zu/%zu bytes), "
                              "file: %s", progress, *bytes_uploaded_total, total_bytes, file_path.c_str());
                }
            };

            // ④ 异步上传文件到 MinIO
            // 捕获 uploader shared_ptr，保证后台线程回调执行期间 AwsS3Uploader 不被析构
            uploader->uploadFileAsync(file_path, object_key, sts_info,
                [this, weak_ptr, callback, file_path, object_key, uploader](
                        const CloudStorageUploadResult& upload_result) {
                    WeakDummyReturn(weak_ptr);
                    if (!upload_result.success) {
                        LOG_ERROR("[AIInsideManageModule] uploadAlgorithmFile: upload failed: %s",
                                  upload_result.error_message.c_str());
                        auto upload_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UPLOAD_FAILED);
                        // 埋点: 上传 AI Inside 算法文件(云存储上传失败); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                        report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_UPLOAD_ALGORITHM_FILE, upload_err, "[AIInsideManageModuleImpl]");
                        if (callback) {
                            callback(upload_err, UploadAIInsideAlgorithmFileResult{});
                        }
                        return;
                    }

                    LOG_INFO("[AIInsideManageModule] uploadAlgorithmFile: upload success: %s -> %s, ETag: %s",
                             file_path.c_str(), upload_result.object_url.c_str(),
                             upload_result.etag.c_str());

                    // ⑤ 通知后端解压入库（fileUploadAIInsideAlgorithm）
                    FileUploadAIInsideAlgorithmRequest notify_request;
                    notify_request.file_key = object_key;

                    std::string captured_object_key = object_key;
                    std::string captured_object_url = upload_result.object_url;
                    std::string captured_etag       = upload_result.etag;

                    post_file_upload_ai_inside_algorithm(notify_request,
                        [this, weak_ptr, callback,
                         captured_object_key, captured_object_url, captured_etag](
                                const FileUploadAIInsideAlgorithmResponse& rsp, FlightHubError err) {
                            WeakDummyReturn(weak_ptr);
                            drc_sdk::common::Executors::Main()->Post(
                                [err, callback, rsp,
                                 captured_object_key, captured_object_url, captured_etag]() {
                                    // 埋点: 上传 AI Inside 算法文件(通知后端解压入库); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                                    report_beacon_event_helper(kBeaconReportEvent_AI_INSIDE_UPLOAD_ALGORITHM_FILE, err, "[AIInsideManageModuleImpl]");
                                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                                        LOG_ERROR("[AIInsideManageModule] uploadAlgorithmFile: "
                                                  "fileUpload notify failed, err_code: %d",
                                                  static_cast<int>(err.error_code));
                                        if (callback) {
                                            callback(err, UploadAIInsideAlgorithmFileResult{});
                                        }
                                        return;
                                    }

                                    // 组装最终结果
                                    UploadAIInsideAlgorithmFileResult final_result;
                                    final_result.object_key = captured_object_key;
                                    final_result.object_url = captured_object_url;
                                    final_result.etag       = captured_etag;

                                    const auto& d = rsp.detail;
                                    final_result.detail.id             = d.id;
                                    final_result.detail.created_at     = d.created_at;
                                    final_result.detail.updated_at     = d.updated_at;
                                    final_result.detail.signed_id      = d.signed_id;
                                    final_result.detail.signed_name    = d.signed_name;
                                    final_result.detail.signed_version = d.signed_version;
                                    final_result.detail.signed_utc     = d.signed_utc;
                                    final_result.detail.model_uuid     = d.model_uuid;
                                    final_result.detail.model_name     = d.model_name;
                                    final_result.detail.model_type     = d.model_type;
                                    final_result.detail.file_key       = d.file_key;
                                    final_result.detail.applicable_product = d.applicable_product;
                                    final_result.detail.image_source   = d.image_source;
                                    final_result.detail.digital_effect = d.digital_effect;
                                    final_result.detail.vl_resolution  = d.vl_resolution;
                                    final_result.detail.model_param.conf_threshold = d.model_param.conf_threshold;
                                    final_result.detail.model_param.nms_pre        = d.model_param.nms_pre;
                                    final_result.detail.model_param.iou_threshold  = d.model_param.iou_threshold;
                                    final_result.detail.license.sn_list     = d.license.sn_list;
                                    final_result.detail.license.started_utc = d.license.started_utc;
                                    final_result.detail.license.expired_utc = d.license.expired_utc;
                                    for (const auto& l : d.labels) {
                                        AIInsideLabel label;
                                        label.type = l.type;
                                        label.name = l.name;
                                        final_result.detail.labels.push_back(label);
                                    }

                                    LOG_INFO("[AIInsideManageModule] uploadAlgorithmFile done: "
                                             "signed_id=%s, version=%d",
                                             final_result.detail.signed_id.c_str(),
                                             final_result.detail.signed_version);

                                    if (callback) { callback(FlightHubError::success(), final_result); }
                                });
                        });
                },
                progress_callback);
        });
}

}  // namespace flight_hub_sdk
