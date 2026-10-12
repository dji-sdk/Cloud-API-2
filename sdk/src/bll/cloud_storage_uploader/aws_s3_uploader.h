// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "cloud_storage_interface.h"
#include <base/common/logger.h>

#ifdef USE_AWS_SDK
#include <aws/core/Aws.h>
#include <aws/s3/S3Client.h>
#include <aws/s3/model/PutObjectRequest.h>
#include <aws/core/auth/AWSCredentialsProvider.h>
#include <aws/core/utils/threading/Executor.h>
#include <aws/core/utils/logging/LogSystemInterface.h>
#include <aws/core/utils/logging/LogLevel.h>
#endif

#include <memory>
#include <cstdarg>
#include <cstdio>

namespace flight_hub_sdk {

#ifdef USE_AWS_SDK
/**
 * @brief 自定义AWS日志系统，使用现有的LOG_*宏
 */
class CustomAwsLogSystem : public Aws::Utils::Logging::LogSystemInterface {
public:
    CustomAwsLogSystem(Aws::Utils::Logging::LogLevel logLevel) : m_logLevel(logLevel) {}
    
    Aws::Utils::Logging::LogLevel GetLogLevel(void) const override {
        return m_logLevel;
    }
    
    void Log(Aws::Utils::Logging::LogLevel logLevel, const char* tag, const char* formatStr, ...) override {
        if (logLevel > m_logLevel) {
            return;
        }
        
        va_list args;
        va_start(args, formatStr);
        vaLog(logLevel, tag, formatStr, args);
        va_end(args);
    }
    
    void vaLog(Aws::Utils::Logging::LogLevel logLevel, const char* tag, const char* formatStr, va_list args) override {
        if (logLevel > m_logLevel) {
            return;
        }
        
        // 格式化消息
        char buffer[1024];
        vsnprintf(buffer, sizeof(buffer), formatStr, args);
        
        // 根据日志级别使用相应的LOG_*宏
        switch (logLevel) {
            case Aws::Utils::Logging::LogLevel::Error:
                LOG_ERROR("[AWS-SDK] %s: %s", tag, buffer);
                break;
            case Aws::Utils::Logging::LogLevel::Warn:
                LOG_WARN("[AWS-SDK] %s: %s", tag, buffer);
                break;
            case Aws::Utils::Logging::LogLevel::Info:
                LOG_INFO("[AWS-SDK] %s: %s", tag, buffer);
                break;
            case Aws::Utils::Logging::LogLevel::Debug:
                LOG_DEBUG("[AWS-SDK] %s: %s", tag, buffer);
                break;
            case Aws::Utils::Logging::LogLevel::Trace:
                LOG_DEBUG("[AWS-SDK-TRACE] %s: %s", tag, buffer);
                break;
            default:
                break;
        }
    }
    
    void LogStream(Aws::Utils::Logging::LogLevel logLevel, const char* tag, const Aws::OStringStream& messageStream) override {
        Log(logLevel, tag, "%s", messageStream.str().c_str());
    }
    
    void Flush() override {
        // 不需要特殊处理，LOG_*宏会立即输出
    }

private:
    Aws::Utils::Logging::LogLevel m_logLevel;
};
#endif

/**
 * @brief AWS S3 云存储上传器实现
 */
class AwsS3Uploader : public ICloudStorageUploader {
public:
    AwsS3Uploader();
    ~AwsS3Uploader() override;

    CloudStorageUploadResult uploadFile(
        const std::string& file_path,
        const std::string& object_key,
        const STSInfo& sts_info,
        CloudStorageProgressCallback progress_callback = nullptr) override;

    void uploadFileAsync(
        const std::string& file_path,
        const std::string& object_key,
        const STSInfo& sts_info,
        std::function<void(const CloudStorageUploadResult&)> callback,
        CloudStorageProgressCallback progress_callback = nullptr) override;

    bool isProviderSupported(const std::string& provider) const override;

private:
    /**
     * @brief 创建 S3 客户端
     * @param sts_info STS 凭证信息
     * @return S3 客户端指针
     */
#ifdef USE_AWS_SDK
    std::shared_ptr<Aws::S3::S3Client> createS3Client(const STSInfo& sts_info);
#else
    void* createS3Client(const STSInfo& sts_info);
#endif

    /**
     * @brief 执行实际的文件上传
     * @param file_path 本地文件路径
     * @param object_key 云存储中的对象键
     * @param sts_info STS 凭证信息
     * @param progress_callback 进度回调
     * @return 上传结果
     */
    CloudStorageUploadResult doUploadFile(
        const std::string& file_path,
        const std::string& object_key,
        const STSInfo& sts_info,
        CloudStorageProgressCallback progress_callback);

    /**
     * @brief 生成完整的对象键（包含前缀）
     * @param object_key 原始对象键
     * @param sts_info STS 信息
     * @return 完整的对象键
     */
    std::string generateFullObjectKey(const std::string& object_key, const STSInfo& sts_info);

    /**
     * @brief 生成对象URL
     * @param bucket 存储桶名称
     * @param object_key 对象键
     * @param endpoint 端点
     * @return 对象URL
     */
    std::string generateObjectUrl(const std::string& bucket, const std::string& object_key, const std::string& endpoint);

    bool aws_sdk_initialized_;
#ifdef USE_AWS_SDK
    std::shared_ptr<Aws::Utils::Threading::Executor> executor_;
#else
    void* executor_;
#endif
};

} // namespace flight_hub_sdk
