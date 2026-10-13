// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "psdk_resource_module_impl.h"
#include <base/common/logger.h>
#include "http_interface/http_interface.h"
#include "cloud_storage_uploader/cloud_storage_factory.h"
#include <filesystem>

namespace flight_hub_sdk {

PsdkResourceModuleImpl::PsdkResourceModuleImpl()
{
    LOG_INFO("[PsdkResourceModuleImpl] constructor");
}

void PsdkResourceModuleImpl::getPsdkSpeakerResourceList(std::function<void(FlightHubError err, const PsdkSpeakerResourceList& psdk_speaker_resource_list)> callback) {
    LOG_INFO("[PsdkResourceModuleImpl] getPsdkSpeakerResourceList");
    WeakDummy(weak_ptr);
    get_psdk_speaker_resource_list([this, weak_ptr, callback](const PsdkSpeakerResourceListResponse& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 获取 PSDK 喊话器资源列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_GET_PSDK_SPEAKER_RESOURCE_LIST, err, "[PsdkResourceModuleImpl]");
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, {});
            return;
        }
        LOG_INFO("[PsdkResourceModuleImpl] getPsdkSpeakerResourceList size: %d", response.resources.size());
        PsdkSpeakerResourceList psdk_speaker_resource_list;
        for (const auto& item : response.resources) {
            PsdkSpeakerResource resource;
            resource.id = item.id;
            resource.name = item.name;
            resource.file_type = item.file_type;
            resource.object_key = item.object_key;
            resource.object_url = item.object_url;
            resource.format = item.format;
            resource.size = item.size;
            resource.duration = item.duration;
            resource.state = item.state;
            resource.state_message = item.state_message;
            resource.pcm_object_key = item.pcm_object_key;
            resource.pcm_object_url = item.pcm_object_url;
            resource.pcm_size = item.pcm_size;
            resource.pcm_md5_sum = item.pcm_md5_sum;
            resource.content = item.content;
            resource.md5_sum = item.md5_sum;
            resource.created_at = item.created_at;

            psdk_speaker_resource_list.resources.push_back(resource);
            LOG_INFO("[PsdkResourceModuleImpl] getPsdkSpeakerResourceList id: %d, name: %s: file_type: %s, object_url: %s", resource.id, resource.name.c_str(), resource.file_type.c_str(), resource.object_url.c_str());
        }
        callback(FlightHubError::success(), psdk_speaker_resource_list);
    });
}

void PsdkResourceModuleImpl::createPsdkSpeakerResource(const CreatePsdkSpeakerResourceParams& params, std::function<void(FlightHubError err, const PsdkSpeakerResource& psdk_speaker_resource)> callback) {
    LOG_INFO("[PsdkResourceModuleImpl] createPsdkSpeakerResource: name %s, file_type %s", params.name.c_str(), params.file_type.c_str());
    WeakDummy(weak_ptr);

    CreateSpeakerResourceRequest request;
    request.name = params.name;
    request.file_type = params.file_type;
    request.object_key = params.object_key;
    request.duration = params.duration;
    request.content = params.content;
    request.create_scene = params.create_scene;
    request.need_opus = params.need_opus;
    request.size = params.size;
    request.visible = params.visible;

    create_psdk_speaker_resource(request, [this, weak_ptr, callback](const CreateSpeakerResourceResponse& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 创建 PSDK 喊话器资源; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_CREATE_PSDK_SPEAKER_RESOURCE, err, "[PsdkResourceModuleImpl]");
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, {});
            return;
        }
        LOG_INFO("[PsdkResourceModuleImpl] createPsdkSpeakerResource success: id %d, name %s, file_type %s", response.id, response.name.c_str(), response.file_type.c_str());

        PsdkSpeakerResource resource;
        resource.id = response.id;
        resource.name = response.name;
        resource.file_type = response.file_type;
        resource.object_key = response.object_key;
        resource.object_url = response.object_url;
        resource.format = response.format;
        resource.size = response.size;
        resource.duration = response.duration;
        resource.state = response.state;
        resource.state_message = response.state_message;
        resource.pcm_object_key = response.pcm_object_key;
        resource.pcm_object_url = response.pcm_object_url;
        resource.pcm_size = response.pcm_size;
        resource.pcm_md5_sum = response.pcm_md5_sum;
        resource.content = response.content;
        resource.md5_sum = response.md5_sum;
        resource.created_at = response.created_at;

        callback(FlightHubError::success(), resource);
    });
}

void PsdkResourceModuleImpl::editPsdkSpeakerResource(const EditPsdkSpeakerResourceParams& params, std::function<void(FlightHubError err, const PsdkSpeakerResource& psdk_speaker_resource)> callback) {
    LOG_INFO("[PsdkResourceModuleImpl] editPsdkSpeakerResource: id %s, name %s, file_type %s", params.id.c_str(), params.name.c_str(), params.file_type.c_str());
    WeakDummy(weak_ptr);

    EditSpeakerResourceRequest request;
    request.id = params.id;
    request.name = params.name;
    request.file_type = params.file_type;
    request.visible = params.visible;
    request.content = params.content;

    edit_psdk_speaker_resource(params.id, request, [this, weak_ptr, callback](const EditSpeakerResourceResponse& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 编辑 PSDK 喊话器资源; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_EDIT_PSDK_SPEAKER_RESOURCE, err, "[PsdkResourceModuleImpl]");
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, {});
            return;
        }
        LOG_INFO("[PsdkResourceModuleImpl] editPsdkSpeakerResource success: id %d, name %s, file_type %s", response.id, response.name.c_str(), response.file_type.c_str());

        PsdkSpeakerResource resource;
        resource.id = response.id;
        resource.name = response.name;
        resource.file_type = response.file_type;
        resource.object_key = response.object_key;
        resource.object_url = response.object_url;
        resource.format = response.format;
        resource.size = response.size;
        resource.duration = response.duration;
        resource.state = response.state;
        resource.state_message = response.state_message;
        resource.pcm_object_key = response.pcm_object_key;
        resource.pcm_object_url = response.pcm_object_url;
        resource.pcm_size = response.pcm_size;
        resource.pcm_md5_sum = response.pcm_md5_sum;
        resource.content = response.content;
        resource.md5_sum = response.md5_sum;
        resource.created_at = response.created_at;

        callback(FlightHubError::success(), resource);
    });
}

void PsdkResourceModuleImpl::deletePsdkSpeakerResource(int resource_id, std::function<void(FlightHubError err)> callback) {
    LOG_INFO("[PsdkResourceModuleImpl] deletePsdkSpeakerResource: resource_id %d", resource_id);
    WeakDummy(weak_ptr);

    delete_psdk_speaker_resource(resource_id, [this, weak_ptr, callback, resource_id](const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 删除 PSDK 喊话器资源; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_DELETE_PSDK_SPEAKER_RESOURCE, err, "[PsdkResourceModuleImpl]");
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err);
            return;
        }
        LOG_INFO("[PsdkResourceModuleImpl] deletePsdkSpeakerResource success: resource_id %d", resource_id);
        callback(FlightHubError::success());
    });
}

void PsdkResourceModuleImpl::getSTS(std::function<void(FlightHubError err, const STSInfo& sts_response)> callback) {
    LOG_INFO("[PsdkResourceModuleImpl] getSTS");
    WeakDummy(weak_ptr);

    get_sts([this, weak_ptr, callback](const STSInfo& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 获取 STS 临时凭证; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_GET_STS, err, "[PsdkResourceModuleImpl]");
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, {});
            return;
        }
        LOG_INFO("[PsdkResourceModuleImpl] getSTS success: bucket %s, endpoint %s, provider %s", response.bucket.c_str(), response.endpoint.c_str(), response.provider.c_str());
        callback(FlightHubError::success(), response);
    });
}

void PsdkResourceModuleImpl::getPsdkWidgetResources(const GetPsdkWidgetResourceParams& params, std::function<void(FlightHubError err, const PsdkWidgetResourceList& psdk_widget_resource_list)> callback) {
    LOG_INFO("[PsdkResourceModuleImpl] getPsdkWidgetResources: object_keys count %d", params.object_keys.size());
    WeakDummy(weak_ptr);

    PsdkWidgetResourceRequest request;
    request.object_keys = params.object_keys;

    get_psdk_widget_resources(request, [this, weak_ptr, callback](const PsdkWidgetResourceListResponse& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        // 埋点: 批量获取 PSDK 控件资源; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_GET_PSDK_WIDGET_RESOURCES, err, "[PsdkResourceModuleImpl]");
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            callback(err, {});
            return;
        }
        LOG_INFO("[PsdkResourceModuleImpl] getPsdkWidgetResources success: resources count %d", response.ui_resource_urls.size());

        PsdkWidgetResourceList psdk_widget_resource_list;
        for (const auto& resource : response.ui_resource_urls) {
            PsdkWidgetResource psdk_widget_resource;
            psdk_widget_resource.object_key = resource.object_key;
            psdk_widget_resource.url = resource.url;
            psdk_widget_resource_list.ui_resource_urls.push_back(psdk_widget_resource);
        }

        callback(FlightHubError::success(), psdk_widget_resource_list);
    });
}

void PsdkResourceModuleImpl::uploadSpeakerAudioFile(const std::string& file_path, const STSInfo& sts_info, std::function<void(FlightHubError err)> callback) {
    LOG_INFO("[PsdkResourceModuleImpl] uploadSpeakerAudioFile: file_path %s, provider %s, bucket %s",
             file_path.c_str(), sts_info.provider.c_str(), sts_info.bucket.c_str());

    WeakDummy(weak_ptr);

    if (!std::filesystem::exists(file_path)) {
        LOG_ERROR("[PsdkResourceModuleImpl] uploadSpeakerAudioFile: File does not exist: %s", file_path.c_str());
        auto not_found_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_NOT_FOUND);
        // 埋点: 上传扬声器音频文件(本地文件不存在, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_UPLOAD_SPEAKER_AUDIO_FILE, not_found_err, "[PsdkResourceModuleImpl]");
        callback(not_found_err);
        return;
    }

    auto file_size = std::filesystem::file_size(file_path);
    const size_t MAX_FILE_SIZE = 10 * 1024 * 1024;
    if (file_size > MAX_FILE_SIZE) {
        LOG_ERROR("[PsdkResourceModuleImpl] uploadSpeakerAudioFile: File too large: %zu bytes (max: %zu)", file_size, MAX_FILE_SIZE);
        auto too_large_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::FILE_TOO_LARGE);
        // 埋点: 上传扬声器音频文件(文件超出 10MB 限制, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_UPLOAD_SPEAKER_AUDIO_FILE, too_large_err, "[PsdkResourceModuleImpl]");
        callback(too_large_err);
        return;
    }

    std::string object_key = std::filesystem::path(file_path).filename().string();

    auto uploader = getCloudStorageUploader(sts_info.provider);
    if (!uploader) {
        LOG_ERROR("[PsdkResourceModuleImpl] uploadSpeakerAudioFile: Unsupported provider: %s", sts_info.provider.c_str());
        auto provider_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UNSUPPORTED_PROVIDER);
        // 埋点: 上传扬声器音频文件(云存储 provider 不支持, 未发起上传); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_UPLOAD_SPEAKER_AUDIO_FILE, provider_err, "[PsdkResourceModuleImpl]");
        callback(provider_err);
        return;
    }

    auto progress_callback = [this, file_size](size_t bytes_uploaded, size_t total_bytes) {
        double progress = (double)bytes_uploaded / total_bytes * 100.0;
        LOG_DEBUG("[PsdkResourceModuleImpl] uploadSpeakerAudioFile progress: %.1f%% (%zu/%zu bytes)", progress, bytes_uploaded, total_bytes);
    };

    // 捕获 uploader，保证后台线程跑完前不被析构（避免 DefaultExecutor join 堵死调用线程）
    uploader->uploadFileAsync(file_path, object_key, sts_info,
        [this, weak_ptr, callback, file_path, object_key, uploader](const CloudStorageUploadResult& result) {
            WeakDummyReturn(weak_ptr);

            if (result.success) {
                LOG_DEBUG("[PsdkResourceModuleImpl] uploadSpeakerAudioFile success: %s -> %s", file_path.c_str(), result.object_url.c_str());
                LOG_DEBUG("[PsdkResourceModuleImpl] uploadSpeakerAudioFile ETag: %s", result.etag.c_str());
                auto success_err = FlightHubError::success();
                // 埋点: 上传扬声器音频文件(上传成功); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_UPLOAD_SPEAKER_AUDIO_FILE, success_err, "[PsdkResourceModuleImpl]");
                callback(success_err);
            } else {
                LOG_ERROR("[PsdkResourceModuleImpl] uploadSpeakerAudioFile failed: %s", result.error_message.c_str());
                auto upload_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::UPLOAD_FAILED);
                // 埋点: 上传扬声器音频文件(云存储上传失败); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_PSDK_RES_UPLOAD_SPEAKER_AUDIO_FILE, upload_err, "[PsdkResourceModuleImpl]");
                callback(upload_err);
            }
        },
        progress_callback);
}

std::shared_ptr<ICloudStorageUploader> PsdkResourceModuleImpl::getCloudStorageUploader(const std::string& provider) {
    return std::shared_ptr<ICloudStorageUploader>(CloudStorageFactory::createUploader(provider));
}

}  // namespace flight_hub_sdk
