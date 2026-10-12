// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/media_service_module.h"
#include "tool/callback_container.h"
#include "common/util/async_capture_protect.h"
#include "BusinessManager.h"

#include <unordered_map>
#include <vector>
#include <string>
#include <functional>



namespace flight_hub_sdk {

class MediaServiceModuleImpl : public IMediaServiceModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit MediaServiceModuleImpl();

    // 析构函数;
    ~MediaServiceModuleImpl();


    // [媒体服务]
    // 获取媒体文件列表;
    void getMediaFilesList(
        const GetMediaFilesListParams &request_params,
        const std::function<void(FlightHubError err, const GetMediaFilesListResults& results)>& callback) override;


    // [媒体服务]
    // 批量删除媒体文件;
    void deleteMediaFiles(
        const std::vector<long long> &file_ids,    // 文件 id 列表;
        const std::function<void(FlightHubError err)>& callback) override;


    // [媒体服务]
    // 获取媒体文件下载地址;
    void getMediaFilesDownloadURL(
        long long file_id,  // 文件 id;
        const std::function<void(FlightHubError err, const GetMediaFilesDownloadUrlResults& results)>& callback) override;

    // [媒体服务]
    // 获取媒体文件下载地址;
    void getMediaFilesDownloadURL(
        const std::vector<std::string> & object_keys,
        const std::function<void(FlightHubError err, const GetMediaFilesDownloadUrlListResults& results)>& callback) override;


    // 监听/取消监听消息;
    // "topic_media_file_post_process";
    // 设备媒体后处理完成通知;
    int listenMediaFilePostProcessMessage(const std::string &device_sn, const std::function<void(const MediaFilePostProcess& msg)>& callback) override;
    void cancelListenMediaFilePostProcessMessage(int listen_id) override;

    // 监听/取消监听消息;
    // "topic_media_file_device_upload";
    // 设备媒体回传通知;
    int listenMediaFileDeviceUploadMessage(const std::string &device_sn, const std::function<void(const MediaFileDeviceUpload& msg)>& callback) override;
    void cancelListenMediaFileDeviceUploadMessage(int listen_id) override;

    // [媒体上传]
    void getMediaUploadSts(
        const GetMediaUploadStsParams& params,
        const std::function<void(FlightHubError err, const GetMediaUploadStsResult& result)>& callback) override;

    void mediaUploadCallback(
        const MediaUploadCallbackParams& params,
        const std::function<void(FlightHubError err, const MediaUploadCallbackResult& result)>& callback) override;

    // [图片备注]
    void uploadGeoMediaFile(
        const UploadGeoMediaFileParams& params,
        const std::function<void(FlightHubError err, const UploadGeoMediaFileResult& result)>& callback) override;

    void getMediaFileDetailById(
        int file_id,
        const std::function<void(FlightHubError err, const GetMediaFileDetailByIdResult& result)>& callback) override;

private:

};

} // namespace flight_hub_sdk