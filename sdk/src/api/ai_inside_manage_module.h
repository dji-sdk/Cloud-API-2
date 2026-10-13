// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>
#include <string>
#include <vector>

namespace flight_hub_sdk {

class IAIInsideManageModule {
public:
    virtual ~IAIInsideManageModule() = default;

    /// @brief 获取组织下机载算法列表
    /// @note GET /gateway/manage/api/v1/ai-inside/models
    virtual void getAIInsideModels(
        std::function<void(FlightHubError err, GetAIInsideModelsResult result)> callback) = 0;

    /// @brief 获取指定算法绑定的设备详情（分页）
    /// @note GET /gateway/manage/api/v1/ai-inside/models/{signed_id}/devices
    virtual void getAIInsideModelDevices(
        const GetAIInsideModelDevicesParam& param,
        std::function<void(FlightHubError err, GetAIInsideModelDevicesResult result)> callback) = 0;

    /// @brief 获取指定算法在指定操作类型下的可操作设备数量统计
    /// @note GET /gateway/manage/api/v1/ai-inside/models/{signed_id}/devices/summary
    ///       query operation_type: load | update
    virtual void getAIInsideModelDevicesSummary(
        const GetAIInsideModelDevicesSummaryParam& param,
        std::function<void(FlightHubError err, GetAIInsideModelDevicesSummaryResult result)> callback) = 0;

    /// @brief 批量下发 Load / Update / Unload 操作指令
    /// @note POST /gateway/manage/api/v1/ai-inside/models/{signed_id}/operation
    virtual void operateAIInsideModel(
        const OperateAIInsideModelParam& param,
        std::function<void(FlightHubError err, OperateAIInsideModelResult result)> callback) = 0;

    /// @brief 创建算法包上传凭证（私有化），返回 MinIO STS 凭证
    /// @note POST /gateway/manage/api/v1/ai-inside-algorithm/create
    virtual void createAIInsideAlgorithm(
        const CreateAIInsideAlgorithmParam& param,
        std::function<void(FlightHubError err, CreateAIInsideAlgorithmResult result)> callback) = 0;

    /// @brief 处理已上传的算法文件，后端解压解析并写入 DB
    /// @note POST /gateway/manage/api/v1/ai-inside-algorithm/file-upload
    virtual void fileUploadAIInsideAlgorithm(
        const FileUploadAIInsideAlgorithmParam& param,
        std::function<void(FlightHubError err, FileUploadAIInsideAlgorithmResult result)> callback) = 0;

    /// @brief 删除指定算法，同步清理存储和 DB 记录
    /// @note DELETE /gateway/manage/api/v1/ai-inside-algorithm/delete
    virtual void deleteAIInsideAlgorithm(
        const DeleteAIInsideAlgorithmParam& param,
        std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 一站式算法文件上传（私有化）
    /// 内部自动串联三步：获取 STS 上传凭证 → 上传 ZIP 文件到 MinIO → 通知后端解压入库
    /// @param param 上传参数（file_path: 本地 ZIP 文件绝对路径）
    /// @param callback 回调，err 为 SUCCESS 时 result 含 object_key 和完整算法详情
    virtual void uploadAlgorithmFile(
        const UploadAIInsideAlgorithmFileParam& param,
        std::function<void(FlightHubError err, UploadAIInsideAlgorithmFileResult result)> callback) = 0;
};

}  // namespace flight_hub_sdk
