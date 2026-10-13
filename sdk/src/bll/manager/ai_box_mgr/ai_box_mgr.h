// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "cloud_storage_uploader/cloud_storage_interface.h"
#include "http_interface/response_message.h"
#include "message.h"

#include <functional>
#include <memory>

namespace flight_hub_sdk {

class AIBoxManager: public drc_sdk::common::AsyncCaptureProtect {
public:
    // ctor;
    AIBoxManager();
    // dtor;
    virtual ~AIBoxManager();


    /// AI Box: 获取 AI Box 信息;
    /// 获取EA700 AI Box的设备信息，包括固件版本、存储状态等;
    void fetchAIBoxInfo(
        const std::string & device_sn,
        const std::function<void(FlightHubError err, const AIBoxInfoListResults& results)>& callback
    );

    /// AI Box: 获取文件下载 URL;
    void FetchFileDownloadUrl(
        const FetchFileDownloadUrlParams &params,
        const std::function<void(FlightHubError err, const FileDownloadUrlListResults& results)>& callback
    );

    ///  AI Box: 获取应用列表;
    void fetchAppList(
        const FetchAppListParam &param,
        const std::function<void(FlightHubError err, const FetchAppListResults& results)>& callback
    );

    /// AI Box: 查询应用列表进度;
    void fetchAppListProgress(
        const FetchAppListProgressParam &param,
        const std::function<void(FlightHubError err, const FetchAppListProgressResults& results)>& callback
    );

    /// AI Box: 导出文件;
    void exportFile(
        const ExportFileParam &param,
        const std::function<void(FlightHubError err, const ExportFileResults& results)>& callback
    );

    /// AI Box: 查询文件导出进度;
    void exportFileProgress(
        const ExportFileProgressParam &param,
        const std::function<void(FlightHubError err, const ExportFileProgressResults& results)>& callback
    );

    /// AI Box: upload 应用;
    void uploadFile(
        const UploadFileParam &param,
        const std::function<void(FlightHubError err, const UploadFileResults& results)>& callback
    );

    /// AI Box: 安装应用;
    void installApp(
        const InstallAppParam &param,
        const std::function<void(FlightHubError err, const InstallAppResults& results)>& callback
    );

    /// AI Box: 查询应用安装进度;
    void installAppProgress(
        const InstallAppProgressParam &param,
        const std::function<void(FlightHubError err, const InstallAppProgressResults& results)>& callback
    );

    /// AI Box: 操作应用;
    /// 对EA700 AI Box上的应用执行卸载、启动、停止或清除操作;
    void operateApp(
        const OperateAppParam &param,
        const std::function<void(FlightHubError err, const OperateAppResults &results)> &callback
    );


private:

    // 根据设备SN(device_sn)和 AI Box SN(box_sn) 获取OSS上传所需的STS临时凭证, 用于前端直传文件到OSS;
    void getUploadStsToken(
        const std::string &device_sn,
        const std::string &box_sn,
        const std::function<void(FlightHubError err, const GetUploadStsResponse& response)>& callback
    );

    std::unique_ptr<ICloudStorageUploader> getCloudStorageUploader(const std::string& provider);
};

}  // namespace flight_hub_sdk