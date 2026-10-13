// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <functional>
#include "message.h"

namespace flight_hub_sdk {

/**
 * @brief 云存储上传结果
 */
struct CloudStorageUploadResult {
    bool success;
    std::string error_message;
    std::string object_url;  // 上传成功后的对象URL
    std::string etag;        // 上传成功后的ETag
    
    CloudStorageUploadResult() : success(false) {}
    CloudStorageUploadResult(bool s, const std::string& msg) 
        : success(s), error_message(msg) {}
    CloudStorageUploadResult(bool s, const std::string& msg, const std::string& url, const std::string& etag_val)
        : success(s), error_message(msg), object_url(url), etag(etag_val) {}
};

/**
 * @brief 云存储上传进度回调
 * @param bytes_uploaded 已上传字节数
 * @param total_bytes 总字节数
 */
using CloudStorageProgressCallback = std::function<void(size_t bytes_uploaded, size_t total_bytes)>;

/**
 * @brief 云存储上传器抽象接口
 */
class ICloudStorageUploader {
public:
    virtual ~ICloudStorageUploader() = default;

    /**
     * @brief 上传文件到云存储
     * @param file_path 本地文件路径
     * @param object_key 云存储中的对象键
     * @param sts_info STS凭证信息
     * @param progress_callback 进度回调（可选）
     * @return 上传结果
     */
    virtual CloudStorageUploadResult uploadFile(
        const std::string& file_path,
        const std::string& object_key,
        const STSInfo& sts_info,
        CloudStorageProgressCallback progress_callback = nullptr) = 0;

    /**
     * @brief 异步上传文件到云存储
     * @param file_path 本地文件路径
     * @param object_key 云存储中的对象键
     * @param sts_info STS凭证信息
     * @param callback 完成回调
     * @param progress_callback 进度回调（可选）
     */
    virtual void uploadFileAsync(
        const std::string& file_path,
        const std::string& object_key,
        const STSInfo& sts_info,
        std::function<void(const CloudStorageUploadResult&)> callback,
        CloudStorageProgressCallback progress_callback = nullptr) = 0;

    /**
     * @brief 检查云存储提供商是否支持
     * @param provider 提供商名称（如 "aws", "aliyun" 等）
     * @return 是否支持
     */
    virtual bool isProviderSupported(const std::string& provider) const = 0;
};

} // namespace flight_hub_sdk
