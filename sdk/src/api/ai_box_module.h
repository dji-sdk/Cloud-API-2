// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>
#include <memory>
#include <string>

namespace flight_hub_sdk {

/// @brief AI Box 模块接口，提供盒子信息查询、应用安装/操作、文件导入导出等功能。
class IAIBoxModule {
public:
    virtual ~IAIBoxModule() = default;

    /// @brief 查询 AI Box 信息
    /// @param device_sn 设备序列号
    /// @param callback 回调，返回 AI Box 信息列表与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/aibox/ai-box-info
    /// @endinternal
    virtual void fetchAIBoxInfo(const std::string& device_sn, std::function<void(FlightHubError err, const AIBoxInfoListResults& results)> callback) = 0;

    /// @brief 获取文件下载链接
    /// @param params 请求参数
    /// @param callback 回调，返回下载链接列表与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/aibox/file-download-url
    /// @endinternal
    virtual void fetchFileDownloadUrl(const FetchFileDownloadUrlParams& params, std::function<void(FlightHubError err, const FileDownloadUrlListResults& results)> callback) = 0;

    /// @brief 获取 AI Box 应用列表
    /// @param param 请求参数
    /// @param callback 回调，返回应用列表与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/aibox/app-list
    /// @endinternal
    virtual void fetchAppList(const FetchAppListParam& param, std::function<void(FlightHubError err, const FetchAppListResults& results)> callback) = 0;

    /// @brief 查询获取应用列表进度
    /// @param param 请求参数
    /// @param callback 回调，返回进度信息与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/aibox/app-list-progress
    /// @endinternal
    virtual void fetchAppListProgress(const FetchAppListProgressParam& param, std::function<void(FlightHubError err, const FetchAppListProgressResults& results)> callback) = 0;

    /// @brief 导出 AI Box 文件
    /// @param param 导出参数
    /// @param callback 回调，返回导出结果与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/aibox/export-file
    /// @endinternal
    virtual void exportFile(const ExportFileParam& param, std::function<void(FlightHubError err, const ExportFileResults& results)> callback) = 0;

    /// @brief 查询导出文件进度
    /// @param param 请求参数
    /// @param callback 回调，返回导出进度与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/aibox/export-file-progress
    /// @endinternal
    virtual void exportFileProgress(const ExportFileProgressParam& param, std::function<void(FlightHubError err, const ExportFileProgressResults& results)> callback) = 0;

    /// @brief 上传 AI Box 文件
    /// @param param 上传参数
    /// @param callback 回调，返回上传结果与错误信息
    /// @internal
    /// 链路: SDK 内部 | 获取 STS 凭证后上传 AI Box 文件
    /// @endinternal
    virtual void uploadAIBoxFile(const UploadFileParam& param, std::function<void(FlightHubError err, const UploadFileResults& results)> callback) = 0;

    /// @brief 安装 AI Box 应用
    /// @param param 安装参数
    /// @param callback 回调，返回安装结果与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/aibox/install-app
    /// @endinternal
    virtual void installApp(const InstallAppParam& param, std::function<void(FlightHubError err, const InstallAppResults& results)> callback) = 0;

    /// @brief 查询安装应用进度
    /// @param param 请求参数
    /// @param callback 回调，返回安装进度与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/aibox/install-app-progress
    /// @endinternal
    virtual void installAppProgress(const InstallAppProgressParam& param, std::function<void(FlightHubError err, const InstallAppProgressResults& results)> callback) = 0;

    /// @brief 操作 AI Box 应用（启动/停止等）
    /// @param param 操作参数
    /// @param callback 回调，返回操作结果与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/aibox/operate-app
    /// @endinternal
    virtual void operateApp(const OperateAppParam& param, std::function<void(FlightHubError err, const OperateAppResults& results)> callback) = 0;
};

}  // namespace flight_hub_sdk
