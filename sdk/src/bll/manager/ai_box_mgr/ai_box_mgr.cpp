// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "ai_box_mgr.h"
#include "base/common/logger.h"
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "cloud_storage_uploader/cloud_storage_factory.h"

#include <filesystem>


namespace flight_hub_sdk {

AIBoxManager::AIBoxManager() {
    LOG_INFO("[AIBoxManager] constructor");
}

AIBoxManager::~AIBoxManager() {
    LOG_INFO("[AIBoxManager] destructor invoked.");
}


/// AI Box: 获取 AI Box 信息;
void AIBoxManager::fetchAIBoxInfo(const std::string & device_sn, const std::function<void(FlightHubError err, const AIBoxInfoListResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("fetchAIBoxInfo(): callback is NULL !");
        return;
    }
    if (device_sn.empty()) {
        LOG_ERROR("fetchAIBoxInfo(): device_sn is empty !");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 获取 AI Box 信息(入参 device_sn 为空, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FETCH_AI_BOX_INFO, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);
    fetch_ai_box_info(device_sn, [weak_ptr, callback](const AIBoxInfoListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 获取 AI Box 信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FETCH_AI_BOX_INFO, err, "[AIBoxManager]");

            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("fetchAIBoxInfo(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }

            AIBoxInfoListResults result;
            result.ai_boxes.reserve(response.ai_boxes.size());
            for (const auto& item : response.ai_boxes) {
                AIBoxInfoResults box;
                box.index = item.index;
                box.type = item.type;
                box.sn = item.sn;
                box.firmware_version = item.firmware_version;
                box.storage_total = item.storage_total;
                box.storage_used = item.storage_used;
                result.ai_boxes.emplace_back(std::move(box));
            }
            callback(err, result);
        });
    });
}


/// AI Box: 获取文件下载 URL;
void AIBoxManager::FetchFileDownloadUrl(const FetchFileDownloadUrlParams &params, const std::function<void(FlightHubError err, const FileDownloadUrlListResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("FetchFileDownloadUrl(): callback is NULL !");
        return;
    }
    if (params.device_sn.empty() || params.box_sn.empty()) {
        LOG_ERROR("FetchFileDownloadUrl(): device_sn or box_sn is empty !");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 获取 AI Box 文件下载 URL(入参 device_sn 或 box_sn 为空, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_FETCH_FILE_DOWNLOAD_URL, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    FetchFileDownloadUrlRequest request;
    request.device_sn = params.device_sn;
    request.box_sn = params.box_sn;
    request.object_keys = params.object_keys;

    fetch_file_download_url(request, [weak_ptr, callback](const FileDownloadUrlListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 获取 AI Box 文件下载 URL; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_FETCH_FILE_DOWNLOAD_URL, err, "[AIBoxManager]");

            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("FetchFileDownloadUrl(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }

            FileDownloadUrlListResults result;
            result.list = response.list;

            callback(err, result);
        });
    });
}


/// AI Box: 获取应用列表;
void AIBoxManager::fetchAppList(const FetchAppListParam &param, const std::function<void(FlightHubError err, const FetchAppListResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("fetchAppList(): callback is NULL !");
        return;
    }
    if (param.device_sn.empty() || param.box_sn.empty()) {
        LOG_ERROR("fetchAppList(): device_sn or box_sn is empty !");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 获取 AI Box 应用列表(入参 device_sn 或 box_sn 为空, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_FETCH_APP_LIST, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    FetchAppListRequest request;
    request.device_sn = param.device_sn;
    request.box_sn = param.box_sn;

    fetch_app_list(request, [weak_ptr, callback](const FetchAppListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 获取 AI Box 应用列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_FETCH_APP_LIST, err, "[AIBoxManager]");

            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("fetchAppList(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }

            FetchAppListResults result;
            result.bid = response.bid;
            result.status = response.status;

            callback(err, result);
        });
    });
}



/// AI Box: 查询应用列表进度;
void AIBoxManager::fetchAppListProgress(const FetchAppListProgressParam &param, const std::function<void(FlightHubError err, const FetchAppListProgressResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("fetchAppListProgress(): callback is NULL !");
        return;
    }
    if (param.device_sn.empty() || param.box_sn.empty() || param.bid.empty()) {
        LOG_ERROR("fetchAppListProgress(): device_sn, box_sn or bid is empty !");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 查询 AI Box 应用列表进度(入参 device_sn、box_sn 或 bid 为空, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_FETCH_APP_LIST_PROGRESS, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    FetchAppListProgressRequest request;
    request.device_sn = param.device_sn;
    request.box_sn = param.box_sn;
    request.bid = param.bid;

    fetch_app_list_progress(request, [weak_ptr, callback](const FetchAppListProgressResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 查询 AI Box 应用列表进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_FETCH_APP_LIST_PROGRESS, err, "[AIBoxManager]");

            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("fetchAppListProgress(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }

            FetchAppListProgressResults result;
            result.bid = response.bid;
            result.status = response.status;
            result.percent = response.percent;
            result.create_time = response.create_time;
            result.update_time = response.update_time;
            result.device_code = response.device_code;
            result.ext.sn = response.ext.sn;
            result.ext.error_reason = response.ext.error_reason;
            result.ext.ai_box_app_info.reserve(response.ext.ai_box_app_info.size());
            for (const auto& item : response.ext.ai_box_app_info) {
                AIBoxAppInfoResults app;
                app.id = item.id;
                app.name = item.name;
                app.version = item.version;
                app.description = item.description;
                app.publisher = item.publisher;
                app.app_size = item.app_size;
                app.data_size = item.data_size;
                app.type = item.type;
                result.ext.ai_box_app_info.emplace_back(std::move(app));
            }
            callback(err, result);
        });
    });
}



/// AI Box: 导出文件;
void AIBoxManager::exportFile(const ExportFileParam &param, const std::function<void(FlightHubError err, const ExportFileResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("exportFile(): callback is NULL !");
        return;
    }
    if (param.device_sn.empty() || param.box_sn.empty() || param.app_id.empty()) {
        LOG_ERROR("exportFile(): device_sn, box_sn or app_id is empty !");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 导出 AI Box 文件(入参 device_sn、box_sn 或 app_id 为空, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_EXPORT_FILE, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    ExportFileRequest request;
    request.device_sn = param.device_sn;
    request.box_sn = param.box_sn;
    request.app_id = param.app_id;
    request.export_type = param.export_type;

    export_file(request, [weak_ptr, callback](const ExportFileResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 导出 AI Box 文件; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_EXPORT_FILE, err, "[AIBoxManager]");

            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("exportFile(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }

            ExportFileResults result;
            result.bid = response.bid;
            result.status = response.status;

            callback(err, result);
        });
    });
}



/// AI Box: 查询文件导出进度;
void AIBoxManager::exportFileProgress(const ExportFileProgressParam &param, const std::function<void(FlightHubError err, const ExportFileProgressResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("exportFileProgress(): callback is NULL !");
        return;
    }
    if (param.device_sn.empty() || param.box_sn.empty() || param.bid.empty()) {
        LOG_ERROR("exportFileProgress(): device_sn, box_sn or bid is empty !");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 查询 AI Box 文件导出进度(入参 device_sn、box_sn 或 bid 为空, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_EXPORT_FILE_PROGRESS, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    ExportFileProgressRequest request;
    request.device_sn = param.device_sn;
    request.box_sn = param.box_sn;
    request.bid = param.bid;

    export_file_progress(request, [weak_ptr, callback](const ExportFileProgressResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 查询 AI Box 文件导出进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_EXPORT_FILE_PROGRESS, err, "[AIBoxManager]");

            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("exportFileProgress(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }

            ExportFileProgressResults result;
            result.bid = response.bid;
            result.status = response.status;
            result.percent = response.percent;
            result.create_time = response.create_time;
            result.update_time = response.update_time;
            result.device_code = response.device_code;
            result.ext.sn = response.ext.sn;
            result.ext.export_type = response.ext.export_type;
            result.ext.app_id = response.ext.app_id;
            result.ext.object_key = response.ext.object_key;

            callback(err, result);
        });
    });
}



/// AI Box: 安装应用;
void AIBoxManager::installApp(const InstallAppParam &param, const std::function<void(FlightHubError err, const InstallAppResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("installApp(): callback is NULL !");
        return;
    }

    // 参数检查;
    if (param.device_sn.empty() || param.box_sn.empty() || param.object_key.empty()) {
        LOG_ERROR("installApp(): device_sn, box_sn or object_key is empty !");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 安装 AI Box 应用(入参 device_sn、box_sn 或 object_key 为空, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_INSTALL_APP, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    InstallAppRequest request;
    request.device_sn = param.device_sn;
    request.box_sn = param.box_sn;
    request.object_key = param.object_key;

    install_app(request, [weak_ptr, callback](const InstallAppResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 安装 AI Box 应用; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_INSTALL_APP, err, "[AIBoxManager]");

            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("installApp(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }

            InstallAppResults result;
            result.bid = response.bid;
            result.status = response.status;

            callback(err, result);
        });
    });
}


/// AI Box: upload 应用;
void AIBoxManager::uploadFile(const UploadFileParam& param, const std::function<void(FlightHubError err, const UploadFileResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("uploadFile(): callback is NULL !");
        return;
    }

    // [必须参数] 参数检查;
    if (param.file_path.empty() || param.device_sn.empty() || param.box_sn.empty()) {
        LOG_ERROR("[AIBoxManager] uploadFile: param error, param: {device_sn: \"%s\", box_sn: \"%s\", file_path: \"%s\"}",
            param.device_sn.c_str(),
            param.box_sn.c_str(),
            param.file_path.c_str()
        );

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 上传 AI Box 应用文件(入参 file_path、device_sn 或 box_sn 为空, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_UPLOAD_FILE, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    LOG_INFO("[AIBoxManager] uploadFile:, param: {device_sn: \"%s\", box_sn: \"%s\", file_path: \"%s\"}",
        param.device_sn.c_str(),
        param.box_sn.c_str(),
        param.file_path.c_str()
    );

    WeakDummy(weak_ptr);

    std::error_code ec;
    if (!std::filesystem::is_regular_file(param.file_path, ec)) {
        LOG_ERROR("[AIBoxManager] uploadFile: File does not exist or is not a regular file: %s, error: %s",
            param.file_path.c_str(), ec ? ec.message().c_str() : "not a regular file"
        );

        auto file_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_NOT_FOUND);

        // 埋点: 上传 AI Box 应用文件(文件不存在或非常规文件, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_UPLOAD_FILE, file_err, "[AIBoxManager]");

        callback(file_err, {});
        return;
    }

    auto file_size = std::filesystem::file_size(param.file_path, ec);
    if (ec) {
        LOG_ERROR("[AIBoxManager] uploadFile: Failed to get file size: %s, error: %s",
            param.file_path.c_str(), ec.message().c_str()
        );

        auto size_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_NOT_FOUND);

        // 埋点: 上传 AI Box 应用文件(读取文件大小失败, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_UPLOAD_FILE, size_err, "[AIBoxManager]");

        callback(size_err, {});
        return;
    }

    const size_t MAX_FILE_SIZE = 20ULL * 1024 * 1024 * 1024; // 20GB
    if (file_size > MAX_FILE_SIZE) {
        LOG_ERROR("[AIBoxManager] uploadFile: File too large: %zu bytes (max: %zu)", file_size, MAX_FILE_SIZE);

        auto too_large_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_TOO_LARGE);

        // 埋点: 上传 AI Box 应用文件(文件超过 20GB 上限, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_UPLOAD_FILE, too_large_err, "[AIBoxManager]");

        callback(too_large_err, {});
        return;
    }

    // 参数;
    std::string the_file_path = param.file_path;  // 文件路径;
    std::string the_device_sn = param.device_sn;  // 设备序列号;
    std::string the_box_sn = param.box_sn;        // AI Box序列号;

    // 先获取 STS token
    getUploadStsToken(the_device_sn, the_box_sn, [this, weak_ptr, the_file_path, the_device_sn, the_box_sn, callback](FlightHubError err, const GetUploadStsResponse& response) {
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            LOG_ERROR("[AIBoxManager] uploadFile: getUploadStsToken failed, err: %s", err.to_string().c_str());

            // 埋点: 上传 AI Box 应用文件(获取上传 STS 临时凭证失败, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_UPLOAD_FILE, err, "[AIBoxManager]");

            callback(err, {});
            return;
        }

        // 打印临时凭证相关信息;
        LOG_INFO("[AIBoxManager] uploadFile: getUploadStsToken, basic info: { provider: \"%s\", bucket: \"%s\", region: \"%s\", endpoint: \"%s\", object_key_prefix: \"%s\"}",
            response.provider.c_str(),
            response.bucket.c_str(),
            response.region.c_str(),
            response.endpoint.c_str(),
            response.object_key_prefix.c_str()
        );

        // 获取云存储上传器
        auto uploader = std::shared_ptr<ICloudStorageUploader>(getCloudStorageUploader(response.provider).release());
        if (uploader == nullptr) {
            LOG_ERROR("[AIBoxManager] uploadFile: getCloudStorageUploader() failed with UNSUPPORTED_PROVIDER(provider: %s)", response.provider.c_str());

            auto provider_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UNSUPPORTED_PROVIDER);

            // 埋点: 上传 AI Box 应用文件(云存储 provider 不支持, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_UPLOAD_FILE, provider_err, "[AIBoxManager]");

            callback(provider_err, {});
            return;
        }

        // 将 GetUploadStsResponse 转换为 STSInfo
        STSInfo sts_info;
        sts_info.provider = response.provider;
        sts_info.bucket = response.bucket;
        sts_info.endpoint = response.endpoint;
        sts_info.region = response.region;

        sts_info.credentials.access_key_id = response.credentials.access_key_id;
        sts_info.credentials.access_key_secret = response.credentials.access_key_secret;
        sts_info.credentials.security_token = response.credentials.security_token;
        sts_info.credentials.expire = static_cast<long long>(response.credentials.expire);

        std::filesystem::path file_path(the_file_path);
        std::string file_name = file_path.filename().string();
        if (file_name.empty()) {
            LOG_ERROR("[AIBoxManager] uploadFile: Failed to extract filename from path: %s", the_file_path.c_str());

            auto name_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_NOT_FOUND);

            // 埋点: 上传 AI Box 应用文件(无法从文件路径中提取文件名, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_UPLOAD_FILE, name_err, "[AIBoxManager]");

            callback(name_err, {});
            return;
        }

        // object-key;
        std::string object_key;

        // object_key_prefix: "ai-box-app/{device_sn}/{box_sn};
        if (response.object_key_prefix.empty()) {
            object_key = std::string("ai-box-app") + "/" + the_device_sn + "/" + the_box_sn + "/" + file_name;
        } else {
            if (response.object_key_prefix.back() == '/') {
                object_key = response.object_key_prefix + file_name;
            } else {
                object_key = response.object_key_prefix + "/" + file_name;
            }
        }

        // 打印 object_key;
        LOG_INFO("[AIBoxManager] uploadFile: the synthetic object_key: %s", object_key.c_str());

        // 设置进度回调
        auto bytes_uploaded_total = std::make_shared<size_t>(0);
        auto last_log_time = std::make_shared<std::chrono::steady_clock::time_point>(std::chrono::steady_clock::now());
        auto progress_callback = [the_file_path, bytes_uploaded_total, last_log_time](size_t bytes_sent_delta, size_t total_bytes) {
            *bytes_uploaded_total += bytes_sent_delta;
            auto now = std::chrono::steady_clock::now();
            if (std::chrono::duration_cast<std::chrono::seconds>(now - *last_log_time).count() >= 1) {
                *last_log_time = now;
                int progress = total_bytes > 0 ? (int)((double)*bytes_uploaded_total / total_bytes * 100.0) : 0;
                if (progress > 100) progress = 100;
                LOG_INFO("[AIBoxManager] uploadFile progress: %d%% (%zu/%zu bytes), file: %s",
                    progress, *bytes_uploaded_total, total_bytes, the_file_path.c_str());
            }
        };

        // 异步上传文件;
        uploader->uploadFileAsync(the_file_path, object_key, sts_info,
            [weak_ptr, callback, the_file_path, object_key, uploader](const CloudStorageUploadResult& result) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([callback, result, the_file_path, object_key]() {
                    if (result.success) {
                        LOG_INFO("[AIBoxManager] uploadFile success(100%%): file_path(%s) -> result.object_url(%s), ETag: %s",
                            the_file_path.c_str(), result.object_url.c_str(), result.etag.c_str()
                        );

                        UploadFileResults res{};
                        res.object_url = result.object_url;
                        res.etag = result.etag;
                        res.object_key = object_key;

                        auto success_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::SUCCESS);

                        // 埋点: 上传 AI Box 应用文件(上传成功); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_UPLOAD_FILE, success_err, "[AIBoxManager]");

                        callback(success_err, res);
                    } else {
                        LOG_ERROR("[AIBoxManager] uploadFile failed: %s", result.error_message.c_str());

                        auto upload_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UPLOAD_FAILED);

                        // 埋点: 上传 AI Box 应用文件(云存储上传失败); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_UPLOAD_FILE, upload_err, "[AIBoxManager]");

                        callback(upload_err, {});
                    }
                });
            },
            progress_callback
        );
    });
}


/// AI Box: 查询应用安装进度;
void AIBoxManager::installAppProgress(const InstallAppProgressParam &param, const std::function<void(FlightHubError err, const InstallAppProgressResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("installAppProgress(): callback is NULL !");
        return;
    }
    if (param.device_sn.empty() || param.box_sn.empty() || param.bid.empty()) {
        LOG_ERROR("installAppProgress(): device_sn, box_sn or bid is empty !");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 查询 AI Box 应用安装进度(入参 device_sn、box_sn 或 bid 为空, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_INSTALL_APP_PROGRESS, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    InstallAppProgressRequest request;
    request.device_sn = param.device_sn;
    request.box_sn = param.box_sn;
    request.bid = param.bid;

    install_app_progress(request, [weak_ptr, callback](const InstallAppProgressResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 查询 AI Box 应用安装进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_INSTALL_APP_PROGRESS, err, "[AIBoxManager]");

            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("installAppProgress(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }

            InstallAppProgressResults result;
            result.bid = response.bid;
            result.status = response.status;
            result.percent = response.percent;
            result.create_time = response.create_time;
            result.update_time = response.update_time;
            result.device_code = response.device_code;
            result.ext.sn = response.ext.sn;

            callback(err, result);
        });
    });
}



/// AI Box: 操作应用;
void AIBoxManager::operateApp(const OperateAppParam &param, const std::function<void(FlightHubError err, const OperateAppResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("operateApp(): callback is NULL !");
        return;
    }
    if (param.device_sn.empty() || param.box_sn.empty() || param.app_id.empty() || param.action.empty()) {
        LOG_ERROR("operateApp(): device_sn, box_sn, app_id or action is empty !");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 操作 AI Box 应用(入参 device_sn、box_sn、app_id 或 action 为空, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_AI_BOX_OPERATE_APP, param_err, "[AIBoxManager]");

        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    OperateAppRequest request;
    request.device_sn = param.device_sn;
    request.box_sn = param.box_sn;
    request.app_id = param.app_id;
    request.action = param.action;

    operate_app(request, [weak_ptr, callback](const OperateAppResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 操作 AI Box 应用; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_AI_BOX_OPERATE_APP, err, "[AIBoxManager]");

            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("operateApp(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }

            OperateAppResults result;
            result.bid = response.bid;
            result.status = response.status;

            callback(err, result);
        });
    });
}



/// AI Box: 获取上传 STS Token;
void AIBoxManager::getUploadStsToken(const std::string &device_sn, const std::string &box_sn, const std::function<void(FlightHubError err, const GetUploadStsResponse& response)>& callback) {
    WeakDummy(weak_ptr);
    get_upload_app_sts(device_sn, box_sn, [weak_ptr, callback](const GetUploadStsResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("getUploadStsToken(): err_code: %d", static_cast<int>(err.error_code));
                callback(err, {});
                return;
            }
            callback(err, response);
        });
    });
}


std::unique_ptr<ICloudStorageUploader> AIBoxManager::getCloudStorageUploader(const std::string& provider) {
    return CloudStorageFactory::createUploader(provider);
}



}  // namespace flight_hub_sdk