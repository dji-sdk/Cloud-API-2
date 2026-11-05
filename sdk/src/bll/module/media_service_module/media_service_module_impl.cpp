// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "media_service_module_impl.h"
#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include "base/common/logger.h"
#include "notification_center/notification_center.h"
#include "BusinessManager.h"

#include <cstdint>
#include "session/session_bm.h"


namespace flight_hub_sdk {

MediaServiceModuleImpl::MediaServiceModuleImpl(int session_id) : session_id_(session_id) {
    LOG_INFO("[MediaServiceModuleImpl] constructor");
}

MediaServiceModuleImpl::~MediaServiceModuleImpl() {
    // add impl if necessary;
}


// [媒体服务]
// 获取媒体文件列表;
void MediaServiceModuleImpl::getMediaFilesList(
    const GetMediaFilesListParams &request_params,
    const std::function<void(FlightHubError err, const GetMediaFilesListResults& results)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("getMediaFilesList(): callback is NULL !");
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    get_media_files_list(
        request_params,
        [weak_ptr, callback, this](const GetMediaFilesListResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 获取媒体文件列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_MEDIA_GET_FILES_LIST, err, session_id_, "[MediaServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("getMediaFilesList(): err_code: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                // 根据 resp 组装返回值;
                GetMediaFilesListResults results;

                results.list.reserve(response.list.size());

                // 填充页信息;
                results.page_size = response.page_size;
                results.next_index = response.next_index;
                results.total = response.total;

                // 填充数据列表;
                for (const auto &item : response.list) {
                    MediaFilesListItemInfoData item_data;

                    item_data.file_id = static_cast<long long>(item.file_id);
                    item_data.flight_id = item.flight_id;
                    item_data.wayline_uuid = item.wayline_uuid;
                    item_data.wayline_action_uuid = item.wayline_action_uuid;
                    item_data.size = static_cast<long long>(item.size);
                    item_data.object_key = item.object_key;
                    item_data.file_name = item.file_name;
                    item_data.suffix = item.suffix;
                    item_data.created_at = item.created_at;
                    item_data.file_path = item.file_path;
                    item_data.device_sn = item.device_sn;
                    item_data.payload_type = item.payload_type;
                    item_data.preview_key = item.preview_key;
                    item_data.pano_total_count     = item.pano_total_count;
                    item_data.pano_stitch_state    = item.pano_stitch_state;
                    item_data.pano_stitch_progress = item.pano_stitch_progress;
                    item_data.type = item.type;

                    // ext object;
                    item_data.ext.create_time = item.ext.create_time;
                    item_data.ext.flight_yaw = item.ext.flight_yaw;
                    item_data.ext.focal_length = item.ext.focal_length;
                    item_data.ext.gimbal_pitch = item.ext.gimbal_pitch;
                    item_data.ext.gimbal_roll = item.ext.gimbal_roll;
                    item_data.ext.gimbal_yaw = item.ext.gimbal_yaw;
                    item_data.ext.width = item.ext.width;
                    item_data.ext.height = item.ext.height;
                    item_data.ext.image_type = item.ext.image_type;
                    item_data.ext.zoom_factor = item.ext.zoom_factor;

                    // ext photo_position;
                    item_data.ext.photo_position.lat = item.ext.photo_position.lat;
                    item_data.ext.photo_position.lng = item.ext.photo_position.lng;
                    item_data.ext.photo_position.abs_alt = item.ext.photo_position.abs_alt;
                    item_data.ext.photo_position.rel_alt = item.ext.photo_position.rel_alt;
                    // ext shoot_position;
                    item_data.ext.shoot_position.lat = item.ext.shoot_position.lat;
                    item_data.ext.shoot_position.lng = item.ext.shoot_position.lng;
                    item_data.ext.shoot_position.abs_alt = item.ext.shoot_position.abs_alt;
                    item_data.ext.shoot_position.rel_alt = item.ext.shoot_position.rel_alt;

                    // Save data;
                    results.list.emplace_back(std::move(item_data));
                }

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    , session_id_);
}


// [媒体服务]
// 获取媒体文件下载地址;
void MediaServiceModuleImpl::getMediaFilesDownloadURL(
    long long file_id,  // 文件 id;
    const std::function<void(FlightHubError err, const GetMediaFilesDownloadUrlResults& result)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("getMediaFilesDownloadURL(): callback is NULL !");
        return;
    }

    // file_id 检查;
    if (file_id < 0) {
        LOG_ERROR("getMediaFilesDownloadURL(): file_id(%lld) is invalid !", file_id);
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 获取媒体文件下载地址（按 file_id）(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MEDIA_GET_FILES_DOWNLOAD_URL_BY_FILE_ID, param_err, session_id_, "[MediaServiceModuleImpl]");
        callback(param_err, {});
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    get_media_files_download_url(
        file_id,
        [weak_ptr, callback, this](const GetMediaFilesDownloadUrlResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 获取媒体文件下载地址（按 file_id）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_MEDIA_GET_FILES_DOWNLOAD_URL_BY_FILE_ID, err, session_id_, "[MediaServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("getMediaFilesDownloadURL(): err_code: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                // 根据 resp 组装返回值;
                GetMediaFilesDownloadUrlResults results;
                results.url = response.url;

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    , session_id_);
}


// [媒体服务]
// 获取媒体文件下载地址;
void  MediaServiceModuleImpl::getMediaFilesDownloadURL(
    const std::vector<std::string> & object_keys,
    const std::function<void(FlightHubError err, const GetMediaFilesDownloadUrlListResults& results)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("getMediaFilesDownloadURL(): callback is NULL !");
        return;
    }

    // object_keys 检查;
    if (object_keys.empty()) {
        LOG_ERROR("getMediaFilesDownloadURL(): object_keys(empty list) is invalid !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 获取媒体文件下载地址（按 object_keys）(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MEDIA_GET_FILES_DOWNLOAD_URL_BY_OBJECT_KEYS, param_err, session_id_, "[MediaServiceModuleImpl]");
        callback(param_err, {});
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    get_media_files_download_url(
        object_keys,
        [weak_ptr, callback, this](const GetMediaFilesDownloadUrlListResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 获取媒体文件下载地址（按 object_keys）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_MEDIA_GET_FILES_DOWNLOAD_URL_BY_OBJECT_KEYS, err, session_id_, "[MediaServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("getMediaFilesDownloadURL(): err_code: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                // 根据 resp 组装返回值;
                GetMediaFilesDownloadUrlListResults results;
                results.list.reserve(response.list.size());

                for (const auto &info : response.list) {
                    results.list.emplace_back(MediaFileDownloadInfo(info.object_key, info.url));
                }

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    , session_id_);
}



// [媒体服务]
// 批量删除媒体文件;
void MediaServiceModuleImpl::deleteMediaFiles(
    const std::vector<long long> &file_ids,    // 文件 id 列表;
    const std::function<void(FlightHubError err)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("deleteMediaFiles(): callback is NULL !");
        return;
    }

    // [必选参数] file_ids 检查;
    if (file_ids.empty()) {
        LOG_ERROR("deleteMediaFiles(): file_ids is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 批量删除媒体文件(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MEDIA_DELETE_FILES, param_err, session_id_, "[MediaServiceModuleImpl]");
        callback(param_err);
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // Send request;
    delete_media_files(
        file_ids,  // 文件 id 列表;
        [weak_ptr, callback, this](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 批量删除媒体文件; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_MEDIA_DELETE_FILES, err, session_id_, "[MediaServiceModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("deleteMediaFiles(): err_code: %d", (int)err.error_code);
                }

                // 统一返回 err;
                callback(err);
            });
        }
    , session_id_);
}


// 监听/取消监听消息;
// "topic_media_file_post_process";
// 设备媒体后处理完成通知;
int MediaServiceModuleImpl::listenMediaFilePostProcessMessage(const std::string &device_sn, const std::function<void(const MediaFilePostProcess& msg)>& callback) {
    auto bm = ResolveBusinessManager(session_id_);
    // callback 检查;
    if (!callback) {
        LOG_ERROR("listenMediaFilePostProcessMessage(): callback is NULL !");
        return -1;
    }

    // protection use dummy weak_ptr;
    WeakDummy(wk_ptr);

    // init listen id;
    auto listen_id = -1;
    if (bm) {
        listen_id = bm->ListenMediaFilePostProcessMessage(
        [wk_ptr, callback, session_id_ = session_id_](const notification_center::MediaFilePostProcess& msg)-> void {
            WeakDummyReturn(wk_ptr);

            // 将 NC SDK 消息转换成 FH SDK 消息; 
            flight_hub_sdk::MediaFilePostProcess res;

            // basics;
            res.file_id = static_cast<long long>(msg.file_id);          // 文件 id;
            res.flight_id = msg.flight_id;                              // 飞行任务 id;
            res.device_sn = msg.device_sn;                              // 设备序列号;
            res.object_key = msg.object_key;                            // 文件对象 key(媒体文件在对象存储中的唯一标识);
            res.object_size = static_cast<long long>(msg.object_size);  // 文件大小(字节);
            res.file_type = msg.file_type;                              // 文件类型，2：图片、3：视频;
            res.preview_object_key = msg.preview_object_key;            // 预览文件对象 key(媒体文件在对象存储中的唯一标识);
            
            // MediaFileImageMetaData object;
            res.image_metadata.photo_create_time = msg.image_metadata.photo_create_time;
            res.image_metadata.flight_yaw = msg.image_metadata.flight_yaw;
            res.image_metadata.focal_length = msg.image_metadata.focal_length;
            res.image_metadata.gimbal_pitch = msg.image_metadata.gimbal_pitch;
            res.image_metadata.gimbal_roll = msg.image_metadata.gimbal_roll;
            res.image_metadata.gimbal_yaw = msg.image_metadata.gimbal_yaw;
            res.image_metadata.height = msg.image_metadata.height;
            res.image_metadata.width = msg.image_metadata.width;
            res.image_metadata.zoom_factor = msg.image_metadata.zoom_factor;
            res.image_metadata.digital_zoom_ratio = msg.image_metadata.digital_zoom_ratio;
            res.image_metadata.length_in_35mm_film = msg.image_metadata.length_in_35mm_film;
            res.image_metadata.image_type = msg.image_metadata.image_type;
            res.image_metadata.payload_type = msg.image_metadata.payload_type;
            res.image_metadata.action_uuid = msg.image_metadata.action_uuid;
            
            // MediaFileImageMetaData object photo_position;
            res.image_metadata.photo_position.lat = msg.image_metadata.photo_position.lat;
            res.image_metadata.photo_position.lng = msg.image_metadata.photo_position.lng;
            res.image_metadata.photo_position.abs_alt = msg.image_metadata.photo_position.abs_alt;
            res.image_metadata.photo_position.rel_alt = msg.image_metadata.photo_position.rel_alt;
            
            // MediaFileImageMetaData object shoot_position;
            res.image_metadata.shoot_position.lat = msg.image_metadata.shoot_position.lat;
            res.image_metadata.shoot_position.lng = msg.image_metadata.shoot_position.lng;
            res.image_metadata.shoot_position.abs_alt = msg.image_metadata.shoot_position.abs_alt;
            res.image_metadata.shoot_position.rel_alt = msg.image_metadata.shoot_position.rel_alt;

            // MediaFileVideoMetaData video_metadata;
            res.video_metadata.duration = msg.video_metadata.duration;
            res.video_metadata.encoder = msg.video_metadata.encoder;
            res.video_metadata.format_long_name = msg.video_metadata.format_long_name;
            res.video_metadata.format_name = msg.video_metadata.format_name;
            res.video_metadata.height = msg.video_metadata.height;
            res.video_metadata.width = msg.video_metadata.width;

            // 打印日志;
            LOG_DEBUG("[MediaServiceModuleImpl] ListenMediaFilePostProcessMessage() flight_hub_sdk::MediaFilePostProcess res: {"
                "file_id=%lld, flight_id=%s, device_sn=%s, object_key=%s, object_size=%lld, file_type=%d, "
                "preview_object_key=%s | "
                "image: photo_create_time=%s, flight_yaw=%f, gimbal_pitch=%f, gimbal_roll=%f, gimbal_yaw=%f, focal_length=%f, zoom_factor=%f, digital_zoom_ratio=%f, "
                "length_in_35mm_film=%f, image_type=%s, payload_type=%s, action_uuid=%s, "
                "width=%d, height=%d, photo_position.lat=%f, photo_position.lng=%f, photo_position.abs_alt=%f, photo_position.rel_alt=%f, "
                "shoot_position.lat=%f, shoot_position.lng=%f, shoot_position.abs_alt=%f, shoot_position.rel_alt=%f | "
                "video: duration=%f, encoder=%s, format_name=%s, format_long_name=%s, width=%d, height=%d }\n",
                res.file_id,
                res.flight_id.c_str(),
                res.device_sn.c_str(),
                res.object_key.c_str(),
                res.object_size,
                res.file_type,
                res.preview_object_key.c_str(),
                res.image_metadata.photo_create_time.c_str(),
                res.image_metadata.flight_yaw,
                res.image_metadata.gimbal_pitch,
                res.image_metadata.gimbal_roll,
                res.image_metadata.gimbal_yaw,
                res.image_metadata.focal_length,
                res.image_metadata.zoom_factor,
                res.image_metadata.digital_zoom_ratio,
                res.image_metadata.length_in_35mm_film,
                res.image_metadata.image_type.c_str(),
                res.image_metadata.payload_type.c_str(),
                res.image_metadata.action_uuid.c_str(),
                res.image_metadata.width,
                res.image_metadata.height,
                res.image_metadata.photo_position.lat,
                res.image_metadata.photo_position.lng,
                res.image_metadata.photo_position.abs_alt,
                res.image_metadata.photo_position.rel_alt,
                res.image_metadata.shoot_position.lat,
                res.image_metadata.shoot_position.lng,
                res.image_metadata.shoot_position.abs_alt,
                res.image_metadata.shoot_position.rel_alt,
                res.video_metadata.duration,
                res.video_metadata.encoder.c_str(),
                res.video_metadata.format_name.c_str(),
                res.video_metadata.format_long_name.c_str(),
                res.video_metadata.width,
                res.video_metadata.height
            );

            // Notify data;
            if (callback) {
                callback(res);
            } else {
                LOG_ERROR("[MediaServiceModuleImpl] ListenMediaFilePostProcessMessage(), error: callback is NULL !");
            }
        },
        [wk_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(wk_ptr);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[MediaServiceModuleImpl] ListenMediaFilePostProcessMessage(), error: %lld", (long long)(error_code));
            }
        },
        device_sn,
        notification_center::NotificationFrequency_OnChanged
    );
    } else {
        LOG_ERROR("[session_bm] null, skip Listen for session");
    }

    // 打印监听 id;
    LOG_INFO("[MediaServiceModuleImpl] ListenMediaFilePostProcessMessage(), listen_id: %d", listen_id);

    return listen_id;
}


void MediaServiceModuleImpl::cancelListenMediaFilePostProcessMessage(int listen_id) {
    auto bm = ResolveBusinessManager(session_id_);
    LOG_INFO("MediaServiceModuleImpl::cancelListenMediaFilePostProcessMessage() with param: listen_id: %d", listen_id);

    if (listen_id >= 0) {
        if (bm) {
            bm->CancelObserve(listen_id);
        }
        LOG_INFO("MediaServiceModuleImpl::cancelListenMediaFilePostProcessMessage(), BusinessManager CancelObserve() invoked with param: listen_id: %d", listen_id);
    }
}


// 监听/取消监听消息;
// "topic_media_file_device_upload";
// 设备媒体回传通知;
int MediaServiceModuleImpl::listenMediaFileDeviceUploadMessage(const std::string &device_sn, const std::function<void(const MediaFileDeviceUpload& msg)>& callback) {
    auto bm = ResolveBusinessManager(session_id_);
    // callback 检查;
    if (!callback) {
        LOG_ERROR("listenMediaFileDeviceUploadMessage(): callback is NULL !");
        return -1;
    }

    // protection use dummy weak_ptr;
    WeakDummy(wk_ptr);

    // init listen id;
    auto listen_id = -1;
    if (bm) {
        listen_id = bm->ListenMediaFileDeviceUploadMessage(
        [wk_ptr, callback, session_id_ = session_id_](const notification_center::MediaFileDeviceUpload& msg)-> void {
            WeakDummyReturn(wk_ptr);

            // 将 NC SDK 消息转换成 FH SDK 消息;
            flight_hub_sdk::MediaFileDeviceUpload res;

            res.file_id = static_cast<long long>(msg.file_id);          // 文件 id;
            res.flight_id = msg.flight_id;                              // 飞行任务 id;
            res.device_sn = msg.device_sn;                              // 设备序列号;
            res.object_key = msg.object_key;                            // 文件对象 key;
            res.object_size = static_cast<long long>(msg.object_size);  // 文件大小(字节);
            res.file_type = msg.file_type;                              // 文件类型, 2: 图片、3: 视频;

            LOG_DEBUG("[MediaServiceModuleImpl] ListenMediaFileDeviceUploadMessage() flight_hub_sdk::MediaFileDeviceUpload res: {"
                "file_id=%lld, flight_id=%s, device_sn=%s, object_key=%s, object_size=%lld, file_type=%d }\n",
                res.file_id,
                res.flight_id.c_str(),
                res.device_sn.c_str(),
                res.object_key.c_str(),
                res.object_size,
                res.file_type
            );

            // Notify data;
            if (callback) {
                callback(res);
            } else {
                LOG_ERROR("[MediaServiceModuleImpl] listenMediaFileDeviceUploadMessage(), error: callback is NULL !");
            }
        },
        [wk_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(wk_ptr);

            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[MediaServiceModuleImpl] ListenMediaFileDeviceUploadMessage(), error: %lld", (long long)error_code);
            }
        },
        device_sn,
        notification_center::NotificationFrequency_OnChanged
    );
    } else {
        LOG_ERROR("[session_bm] null, skip Listen for session");
    }

    // 打印监听 id;
    LOG_INFO("[MediaServiceModuleImpl] ListenMediaFileDeviceUploadMessage(), listen_id: %d", listen_id);

    return listen_id;
}


void MediaServiceModuleImpl::cancelListenMediaFileDeviceUploadMessage(int listen_id) {
    auto bm = ResolveBusinessManager(session_id_);
    LOG_INFO("MediaServiceModuleImpl::cancelListenMediaFileDeviceUploadMessage() with param: listen_id: %d", listen_id);

    if (listen_id >= 0) {
        if (bm) {
            bm->CancelObserve(listen_id);
        }
        LOG_INFO("MediaServiceModuleImpl::cancelListenMediaFileDeviceUploadMessage(), BusinessManager CancelObserve() invoked with param: listen_id: %d", listen_id);
    }
}

// 获取媒体上传 STS 临时凭证
void MediaServiceModuleImpl::getMediaUploadSts(
    const GetMediaUploadStsParams& params,
    const std::function<void(FlightHubError err, const GetMediaUploadStsResult& result)>& callback) {
    if (!callback) { LOG_ERROR("getMediaUploadSts(): callback is NULL !"); return; }
    if (params.file_path.empty()) {
        LOG_ERROR("getMediaUploadSts(): file_path is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 获取 OSS STS 临时上传凭证(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MEDIA_GET_UPLOAD_STS, param_err, session_id_, "[MediaServiceModuleImpl]");
        callback(param_err, {});
        return;
    }
    WeakDummy(weak_ptr);

    GetMediaUploadStsRequest request;
    request.file_path = params.file_path;
    request.expire = params.expire;
    request.data.sn = params.sn;
    request.data.module = params.module;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        get_media_upload_sts(request,
            [weak_ptr, callback, this](const GetMediaUploadStsResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                    // 埋点: 获取 OSS STS 临时上传凭证; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_MEDIA_GET_UPLOAD_STS, err, session_id_, "[MediaServiceModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        LOG_ERROR("getMediaUploadSts(): err_code: %d", static_cast<int>(err.error_code));
                        callback(err, {});
                        return;
                    }
                    GetMediaUploadStsResult result;
                    result.bucket = data.bucket;
                    result.credentials.access_key_id = data.credentials.access_key_id;
                    result.credentials.access_key_secret = data.credentials.access_key_secret;
                    result.credentials.expire = data.credentials.expire;
                    result.credentials.expiration = data.credentials.expiration;
                    result.credentials.platform = data.credentials.platform;
                    result.credentials.security_token = data.credentials.security_token;
                    result.object_key_prefix = data.object_key_prefix;
                    result.endpoint = data.endpoint;
                    result.provider = data.provider;
                    result.region = data.region;
                    result.ext = data.ext;
                    callback(err, result);
                });
            }, session_id_);
    });
}

// 媒体上传完成回调
void MediaServiceModuleImpl::mediaUploadCallback(
    const MediaUploadCallbackParams& params,
    const std::function<void(FlightHubError err, const MediaUploadCallbackResult& result)>& callback) {
    if (!callback) { LOG_ERROR("mediaUploadCallback(): callback is NULL !"); return; }
    if (params.file.object_key.empty() || params.file.name.empty()) {
        LOG_ERROR("mediaUploadCallback(): file.object_key or file.name is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 媒体文件上传完成后通知服务端(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MEDIA_UPLOAD_CALLBACK, param_err, session_id_, "[MediaServiceModuleImpl]");
        callback(param_err, {});
        return;
    }
    WeakDummy(weak_ptr);

    MediaUploadCallbackRequest request;
    request.data.sn = params.sn;
    request.data.result = params.result;
    request.data.progress = params.progress;
    request.biz_code = params.biz_code;
    request.version = params.version;
    // file
    request.data.file.object_key = params.file.object_key;
    request.data.file.fingerprint = params.file.fingerprint;
    request.data.file.name = params.file.name;
    request.data.file.type = params.file.type;
    request.data.file.cloud_to_cloud_id = params.file.cloud_to_cloud_id;
    request.data.file.created_time = params.file.created_time;
    request.data.file.source = params.file.source;
    // file.ext
    request.data.file.ext.wayline_id = params.file.ext.wayline_id;
    request.data.file.ext.wayline_action_uuid = params.file.ext.wayline_action_uuid;
    request.data.file.ext.prj_uuid = params.file.ext.prj_uuid;
    request.data.file.ext.workspace_id = params.file.ext.workspace_id;
    request.data.file.ext.flight_id = params.file.ext.flight_id;
    request.data.file.ext.drone_model_key = params.file.ext.drone_model_key;
    request.data.file.ext.payload_model_key = params.file.ext.payload_model_key;
    request.data.file.ext.is_original = params.file.ext.is_original;
    request.data.file.ext.task_name = params.file.ext.task_name;
    request.data.file.ext.sn = params.file.ext.sn;
    request.data.file.ext.user_id = params.file.ext.user_id;
    request.data.file.ext.tinny_fingerprint = params.file.ext.tinny_fingerprint;
    request.data.file.ext.fast_reconstruction_media_type = params.file.ext.fast_reconstruction_media_type;
    request.data.file.ext.media_index = params.file.ext.media_index;
    request.data.file.ext.expected_file_count = params.file.ext.expected_file_count;
    request.data.file.ext.pano_total_count = params.file.ext.pano_total_count;
    // file.metadata
    request.data.file.metadata.absolute_altitude = params.file.metadata.absolute_altitude;
    request.data.file.metadata.relative_altitude = params.file.metadata.relative_altitude;
    request.data.file.metadata.gimbal_yaw_degree = params.file.metadata.gimbal_yaw_degree;
    request.data.file.metadata.created_time = params.file.metadata.created_time;
    request.data.file.metadata.shoot_position.lat = params.file.metadata.shoot_position.lat;
    request.data.file.metadata.shoot_position.lng = params.file.metadata.shoot_position.lng;
    // flight_task
    request.data.flight_task.expected_file_count = params.expected_file_count;
    request.data.flight_task.uploaded_file_count = params.uploaded_file_count;
    request.data.flight_task.flight_type = params.flight_type;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        media_upload_callback(request,
            [weak_ptr, callback, this](const MediaUploadCallbackResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                    // 埋点: 媒体文件上传完成后通知服务端; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_MEDIA_UPLOAD_CALLBACK, err, session_id_, "[MediaServiceModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        LOG_ERROR("mediaUploadCallback(): err_code: %d", static_cast<int>(err.error_code));
                        callback(err, {});
                        return;
                    }
                    MediaUploadCallbackResult result;
                    result.object_key = data.object_key;
                    result.filename = data.filename;
                    result.flight_id = data.flight_id;
                    result.uuid = data.uuid;
                    result.id = data.id;
                    result.media_id = data.media_id;
                    callback(err, result);
                });
            }, session_id_);
    });
}

// 上传地理标注媒体资源（图片备注）
void MediaServiceModuleImpl::uploadGeoMediaFile(
    const UploadGeoMediaFileParams& params,
    const std::function<void(FlightHubError err, const UploadGeoMediaFileResult& result)>& callback) {
    if (!callback) { LOG_ERROR("uploadGeoMediaFile(): callback is NULL !"); return; }
    if (params.name.empty() || params.object_key.empty()) {
        LOG_ERROR("uploadGeoMediaFile(): name or object_key is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 上传地理标注媒体资源（图片备注上传回调）(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MEDIA_UPLOAD_GEO_FILE, param_err, session_id_, "[MediaServiceModuleImpl]");
        callback(param_err, {});
        return;
    }
    WeakDummy(weak_ptr);

    UploadGeoMediaFileRequest request;
    request.name        = params.name;
    request.object_key  = params.object_key;
    if (!params.fingerprint.empty()) request.fingerprint = params.fingerprint;
    request.media_source = params.media_source;

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        upload_geo_media_file(request,
            [weak_ptr, callback, this](const UploadGeoMediaFileResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                    // 埋点: 上传地理标注媒体资源（图片备注上传回调）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_MEDIA_UPLOAD_GEO_FILE, err, session_id_, "[MediaServiceModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        LOG_ERROR("uploadGeoMediaFile(): err_code: %d", static_cast<int>(err.error_code));
                        callback(err, {});
                        return;
                    }
                    UploadGeoMediaFileResult result;
                    result.file_id       = data.file_id;
                    result.file_uuid     = data.file_uuid;
                    result.media_file_id = data.media_file_id;
                    callback(err, result);
                });
            }, session_id_);
    });
}

// 根据文件 ID 获取媒体文件详情（轮询文件 URL）
void MediaServiceModuleImpl::getMediaFileDetailById(
    int file_id,
    const std::function<void(FlightHubError err, const GetMediaFileDetailByIdResult& result)>& callback) {
    if (!callback) { LOG_ERROR("getMediaFileDetailById(): callback is NULL !"); return; }
    if (file_id <= 0) {
        LOG_ERROR("getMediaFileDetailById(): file_id is invalid !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 根据文件 ID 获取媒体文件详情（轮询文件 URL）(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MEDIA_GET_FILE_DETAIL_BY_ID, param_err, session_id_, "[MediaServiceModuleImpl]");
        callback(param_err, {});
        return;
    }
    WeakDummy(weak_ptr);

    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_id, callback]() {
        WeakDummyReturn(weak_ptr);
        get_media_file_detail_by_id(file_id,
            [weak_ptr, callback, this](const GetMediaFileDetailByIdResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                    // 埋点: 根据文件 ID 获取媒体文件详情（轮询文件 URL）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_MEDIA_GET_FILE_DETAIL_BY_ID, err, session_id_, "[MediaServiceModuleImpl]");
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        LOG_ERROR("getMediaFileDetailById(): err_code: %d", static_cast<int>(err.error_code));
                        callback(err, {});
                        return;
                    }
                    GetMediaFileDetailByIdResult result;
                    result.url             = data.url;
                    result.thumbnail_url   = data.thumbnail_url;
                    result.alert_image_url = data.alert_image_url;
                    result.name            = data.name;
                    result.suffix          = data.suffix;
                    result.size            = data.size;
                    result.create_at       = data.create_at;
                    result.ext.width       = data.ext.width;
                    result.ext.height      = data.ext.height;
                    result.ext.photo_position.lat     = data.ext.photo_position.lat;
                    result.ext.photo_position.lng     = data.ext.photo_position.lng;
                    result.ext.photo_position.abs_alt = data.ext.photo_position.abs_alt;
                    result.ext.photo_position.rel_alt = data.ext.photo_position.rel_alt;
                    result.ext.resolution = data.ext.resolution;
                    result.ext.photographer = data.ext.photographer;
                    result.ext.task_name = data.ext.task_name;
                    result.ext.line_name = data.ext.line_name;
                    result.ext.wayline_uuid = data.ext.wayline_uuid;
                    result.ext.origin_wayline_uuid = data.ext.origin_wayline_uuid;
                    result.ext.wayline_action_uuid = data.ext.wayline_action_uuid;
                    result.ext.payload_model_key = data.ext.payload_model_key;
                    result.ext.drone_model_key = data.ext.drone_model_key;
                    result.ext.is_original = data.ext.is_original;
                    result.ext.digital_zoom_ratio = data.ext.digital_zoom_ratio;
                    result.ext.length_in35mm_film = data.ext.length_in35mm_film;
                    result.ext.gimbal_roll = data.ext.gimbal_roll;
                    result.ext.gimbal_yaw = data.ext.gimbal_yaw;
                    result.ext.gimbal_yaw_degree = data.ext.gimbal_yaw_degree;
                    result.ext.flight_id = data.ext.flight_id;
                    result.ext.duration = data.ext.duration;
                    result.ext.shoot_position.lat = data.ext.shoot_position.lat;
                    result.ext.shoot_position.lng = data.ext.shoot_position.lng;
                    result.ext.shoot_position.abs_alt = data.ext.shoot_position.abs_alt;
                    result.ext.shoot_position.rel_alt = data.ext.shoot_position.rel_alt;
                    result.ext.absolute_altitude = data.ext.absolute_altitude;
                    result.ext.relative_altitude = data.ext.relative_altitude;
                    result.ext.created_time = data.ext.created_time;
                    result.ext.creation_time = data.ext.creation_time;
                    result.ext.create_time = data.ext.create_time;
                    result.ext.encoder = data.ext.encoder;
                    result.ext.format_long_name = data.ext.format_long_name;
                    result.ext.format_name = data.ext.format_name;
                    result.ext.ext_size = data.ext.ext_size;
                    result.ext.flight_yaw = data.ext.flight_yaw;
                    result.ext.focal_length = data.ext.focal_length;
                    result.ext.gimbal_pitch = data.ext.gimbal_pitch;
                    result.ext.image_type = data.ext.image_type;
                    result.pano_stitch_state = data.pano_stitch_state;
                    result.pano_stitch_progress = data.pano_stitch_progress;
                    result.pano_error_code = data.pano_error_code;
                    callback(err, result);
                });
            }, session_id_);
    });
}

// ── 3X 超清全景拼接 ──────────────────────────────────────────────────────

/// 辅助宏：pano 5 个 HTTP 方法的公共结构（WeakDummy + IO post + Main callback）
#define PANO_HTTP_ACTION_IMPL(method_name, http_func)                                          \
void MediaServiceModuleImpl::method_name(                                                      \
    long long file_id,                                                                         \
    const std::function<void(FlightHubError err)>& callback) {                                 \
    if (!callback) { LOG_ERROR(#method_name "(): callback is NULL !"); return; }               \
    if (file_id <= 0) {                                                                        \
        LOG_ERROR(#method_name "(): file_id is invalid !");                                    \
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM));        \
        return;                                                                                \
    }                                                                                          \
    int sid = session_id_;                                                                     \
    WeakDummy(weak_ptr);                                                                       \
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_id, callback, sid]() {        \
        WeakDummyReturn(weak_ptr);                                                             \
        http_func(file_id,                                                                     \
            [weak_ptr, callback, sid](FlightHubError err) {                                    \
                WeakDummyReturn(weak_ptr);                                                     \
                drc_sdk::common::Executors::Main()->Post([err, callback, sid]() {              \
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {                    \
                        LOG_ERROR(#method_name "(): err_code: %d", (int)err.error_code);       \
                    }                                                                          \
                    callback(err);                                                             \
                });                                                                            \
            }, sid);                                                                           \
    });                                                                                        \
}

PANO_HTTP_ACTION_IMPL(startPanoStitch,  post_invoke_pano_stitch)
PANO_HTTP_ACTION_IMPL(stopPanoStitch,   post_cancel_pano_stitch)
PANO_HTTP_ACTION_IMPL(retryPanoStitch,  post_retry_pano_stitch)
PANO_HTTP_ACTION_IMPL(startPanoTier,    post_invoke_pano_tier)

#undef PANO_HTTP_ACTION_IMPL

void MediaServiceModuleImpl::getPanoTierTileUrl(
    long long file_id,
    const std::function<void(FlightHubError err, const GetPanoTierTileUrlResult& result)>& callback) {
    if (!callback) { LOG_ERROR("getPanoTierTileUrl(): callback is NULL !"); return; }
    if (file_id <= 0) {
        LOG_ERROR("getPanoTierTileUrl(): file_id is invalid !");
        callback(FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM), {});
        return;
    }
    int sid = session_id_;
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, file_id, callback, sid]() {
        WeakDummyReturn(weak_ptr);
        get_pano_tier_tile_url(file_id,
            [weak_ptr, callback, sid](const GetPanoTierTileUrlResponseData& data, FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, data, sid]() {
                    if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                        LOG_ERROR("getPanoTierTileUrl(): err_code: %d", (int)err.error_code);
                        callback(err, {});
                        return;
                    }
                    GetPanoTierTileUrlResult result;
                    result.state    = data.state;
                    result.base_url = data.base_url;
                    // cookies: PaaS 后端通过 HTTP Set-Cookie 响应头下发，body 中无此字段；
                    // SDK 底层 HttpHeaders(std::map) 不支持多值 key，PaaS 场景 cookies 不填充。
                    // Native/Java ISV 请使用 query_params 完成 CDN 鉴权（Key/Value 与 cookies 相同）。
                    for (const auto& c : data.cookies) {
                        PanoTierCookie cookie;
                        cookie.name  = c.name;
                        cookie.value = c.value;
                        cookie.path  = c.path;
                        cookie.expires_unix_sec = 0;
                        result.cookies.push_back(std::move(cookie));
                    }
                    result.query_params = data.query_params;
                    callback(err, result);
                });
            }, sid);
    });
}

int MediaServiceModuleImpl::subscribePanoStitchProgress(
    const std::function<void(const PanoStitchProgressMsg& msg)>& callback) {
    if (!callback) { LOG_ERROR("subscribePanoStitchProgress(): callback is NULL !"); return -1; }
    auto bm = ResolveBusinessManager(session_id_);
    if (!bm) {
        LOG_ERROR("subscribePanoStitchProgress(): BusinessManager not found, session_id=%d", session_id_);
        return -1;
    }
    WeakDummy(weak_ptr);
    return bm->ListenPanoStitchProgress(
        [weak_ptr, callback](const notification_center::PanoStitchProgress& msg) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([callback, msg]() {
                PanoStitchProgressMsg out;
                out.workspace_id         = msg.workspace_id;
                out.file_id              = msg.file_id;
                out.name                 = msg.name;
                out.pano_stitch_state    = msg.pano_stitch_state;
                out.pano_stitch_progress = msg.pano_stitch_progress;
                out.pano_error_code      = msg.pano_error_code;
                callback(out);
            });
        },
        [](const notification_center::NotificationCenterErrorCode& code) {
            LOG_ERROR("subscribePanoStitchProgress(): NC error: %d", (int)code);
        },
        // PaaS 全景拼接进度不按设备 SN/workspace 过滤，后端以空 device_sn/workspace_id 推送。
        // 固定传空字符串订阅全局 topic_pano_stitch_progress。
        "",
        notification_center::NotificationFrequency_OnChanged);
}

void MediaServiceModuleImpl::cancelListenPanoStitchProgress(int listen_id) {
    if (listen_id < 0) return;
    auto bm = ResolveBusinessManager(session_id_);
    if (bm) {
        bm->CancelObserve(listen_id);
    }
}

}  // namespace flight_hub_sdk
