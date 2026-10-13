// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "aws_s3_uploader.h"
#include <base/common/logger.h>
#include <filesystem>
#include <fstream>

#ifdef USE_AWS_SDK
#include <aws/core/utils/logging/LogLevel.h>
#include <aws/core/utils/logging/ConsoleLogSystem.h>
#include <aws/core/auth/AWSCredentialsProvider.h>
#include <aws/core/utils/threading/DefaultExecutor.h>
#include <aws/core/client/ClientConfiguration.h>
#endif

namespace flight_hub_sdk {

AwsS3Uploader::AwsS3Uploader() : aws_sdk_initialized_(false) {
    LOG_INFO("[AwsS3Uploader] AwsS3Uploader ctor ENTER this=%p", static_cast<void*>(this));
#ifdef USE_AWS_SDK
    // 延迟初始化SDK，在实际需要时再初始化，避免全局状态问题
    executor_ = Aws::MakeShared<Aws::Utils::Threading::DefaultExecutor>("AwsS3Uploader");
#else
    executor_ = nullptr;
    LOG_ERROR("[AwsS3Uploader] AWS SDK not enabled, please set USE_AWS_SDK=ON");
#endif
    LOG_INFO("[AwsS3Uploader] AwsS3Uploader ctor LEAVE this=%p", static_cast<void*>(this));
}

AwsS3Uploader::~AwsS3Uploader() {
    // 生命周期探针：对照 Main join bug —— 期望 success 之后才进析构；
    // unique_ptr 误用时会在 Main 上于上传未完成时进入此处并 join。
    LOG_INFO("[AwsS3Uploader] ~AwsS3Uploader ENTER this=%p", static_cast<void*>(this));
#ifdef USE_AWS_SDK
    // 显式释放 executor，把 DefaultExecutor::join 夹在两条日志之间，便于确认耗时发生点
    if (executor_) {
        LOG_INFO("[AwsS3Uploader] ~AwsS3Uploader releasing executor_ (may join upload threads) this=%p",
                 static_cast<void*>(this));
        executor_.reset();
        LOG_INFO("[AwsS3Uploader] ~AwsS3Uploader executor_ released this=%p", static_cast<void*>(this));
    }
    if (aws_sdk_initialized_) {
        Aws::ShutdownAPI(Aws::SDKOptions{});
    }
#endif
    LOG_INFO("[AwsS3Uploader] ~AwsS3Uploader LEAVE this=%p", static_cast<void*>(this));
}


#ifdef USE_AWS_SDK
std::shared_ptr<Aws::S3::S3Client> AwsS3Uploader::createS3Client(const STSInfo& sts_info) {
#else
void* AwsS3Uploader::createS3Client(const STSInfo& sts_info) {
#endif
#ifdef USE_AWS_SDK
    // 延迟初始化SDK，避免构造函数中的全局状态问题
    if (!aws_sdk_initialized_) {
        LOG_DEBUG("[AwsS3Uploader] Initializing AWS SDK");
        
        Aws::SDKOptions options;
        options.loggingOptions.logLevel = Aws::Utils::Logging::LogLevel::Warn;
        
        // 使用自定义日志系统，不生成单独的日志文件
        options.loggingOptions.logger_create_fn = [options]() {
            return Aws::MakeShared<CustomAwsLogSystem>("CustomAwsLogSystem", options.loggingOptions.logLevel);
        };
        
        Aws::InitAPI(options);
        aws_sdk_initialized_ = true;
        LOG_DEBUG("[AwsS3Uploader] AWS SDK initialized with custom logging");
    }

    LOG_DEBUG("[AwsS3Uploader] Creating S3 client configuration");

    // 参考demo代码，临时禁用EC2元数据服务避免阻塞，然后立即恢复
    setenv("AWS_EC2_METADATA_DISABLED", "true", 1);
    Aws::Client::ClientConfiguration config;
    setenv("AWS_EC2_METADATA_DISABLED", "false", 1);
    
    // 根据不同提供商设置配置
    if (sts_info.provider == "aws") {
        // AWS S3 标准配置
        config.region = sts_info.region.empty() ? "us-east-1" : sts_info.region;
        if (!sts_info.endpoint.empty()) {
            config.endpointOverride = Aws::String(sts_info.endpoint.c_str());
        }
    } else if (sts_info.provider == "minio") {
        // MinIO 配置：跟随 STS 下发的 region，避免 SigV4 签名 region 与服务端不一致导致 AuthorizationHeaderMalformed
        config.region = sts_info.region.empty() ? "us-east-1" : sts_info.region;
        config.endpointOverride = Aws::String(sts_info.endpoint);
        config.disableExpectHeader = true;

        // 按后端下发的 endpoint 协议决定传输与校验策略。
        // https:// -> HTTPS + verifySSL=true
        // http://  -> HTTP  + verifySSL=false
        // 无 scheme 历史兼容：按 HTTP 处理，避免私有化旧配置回归。
        if (sts_info.endpoint.rfind("https://", 0) == 0) {
            config.scheme = Aws::Http::Scheme::HTTPS;
            config.verifySSL = true;
        } else {
            config.scheme = Aws::Http::Scheme::HTTP;
            config.verifySSL = false;
        }
    }
    
    config.connectTimeoutMs = 30000;
    config.requestTimeoutMs = 60000;
    
    LOG_DEBUG("[AwsS3Uploader] Creating S3 client with credentials");

    // 直接使用凭证创建客户端，避免环境变量污染
    Aws::Auth::AWSCredentials credentials(
        Aws::String(sts_info.credentials.access_key_id.c_str()),
        Aws::String(sts_info.credentials.access_key_secret.c_str()),
        Aws::String(sts_info.credentials.security_token.c_str())
    );

    // 使用凭证创建客户端，对MinIO设置 useVirtualAddressing = false
    bool useVirtualAddressing = (sts_info.provider != "minio");
    return Aws::MakeShared<Aws::S3::S3Client>(
        "S3Client", 
        credentials, 
        config, 
        Aws::Client::AWSAuthV4Signer::PayloadSigningPolicy::Never, 
        useVirtualAddressing
    );
#else
    LOG_INFO("[AwsS3Uploader] createS3Client nullptr");
    return nullptr;
#endif
}

std::string AwsS3Uploader::generateFullObjectKey(const std::string& object_key, const STSInfo& sts_info) {
    if (sts_info.object_key_prefix.empty()) {
        return object_key;
    }
    
    // 确保前缀以 / 结尾，对象键不以 / 开头
    std::string prefix = sts_info.object_key_prefix;
    if (prefix.back() != '/') {
        prefix += '/';
    }
    
    std::string key = object_key;
    if (key.front() == '/') {
        key = key.substr(1);
    }
    
    return prefix + key;
}

std::string AwsS3Uploader::generateObjectUrl(const std::string& bucket, const std::string& object_key, const std::string& endpoint) {
    std::string url = endpoint;
    if (url.back() != '/') {
        url += '/';
    }
    url += bucket + "/" + object_key;
    return url;
}

CloudStorageUploadResult AwsS3Uploader::doUploadFile(
    const std::string& file_path,
    const std::string& object_key,
    const STSInfo& sts_info,
    CloudStorageProgressCallback progress_callback) {
    
#ifdef USE_AWS_SDK
    LOG_INFO("[AwsS3Uploader] Starting upload: %s -> %s/%s", file_path.c_str(), sts_info.bucket.c_str(), object_key.c_str());

    // 检查文件是否存在
    if (!std::filesystem::exists(file_path)) {
        std::string error_msg = "File does not exist: " + file_path;
        LOG_ERROR("[AwsS3Uploader] %s", error_msg.c_str());
        return CloudStorageUploadResult(false, error_msg);
    }

    // 获取文件大小
    auto file_size = std::filesystem::file_size(file_path);
    LOG_DEBUG("[AwsS3Uploader] File size: %zu bytes", file_size);

    // 创建 S3 客户端
    auto s3_client = createS3Client(sts_info);
    if (!s3_client) {
        std::string error_msg = "Failed to create S3 client";
        LOG_ERROR("[AwsS3Uploader] %s", error_msg.c_str());
        return CloudStorageUploadResult(false, error_msg);
    }

    // 生成完整的对象键
    std::string full_object_key = generateFullObjectKey(object_key, sts_info);
    LOG_DEBUG("[AwsS3Uploader] Full object key: %s", full_object_key.c_str());

    // 创建 PutObject 请求
    Aws::S3::Model::PutObjectRequest put_object_request;
    put_object_request.SetBucket(sts_info.bucket);
    put_object_request.SetKey(full_object_key);

    // 设置文件流
    auto file_stream = Aws::MakeShared<Aws::FStream>("PutObjectInputStream", file_path.c_str(), std::ios_base::in | std::ios_base::binary);
    if (!file_stream->is_open()) {
        std::string error_msg = "Failed to open file: " + file_path;
        LOG_ERROR("[AwsS3Uploader] %s", error_msg.c_str());
        return CloudStorageUploadResult(false, error_msg);
    }

    put_object_request.SetBody(file_stream);

    // 设置内容类型
    put_object_request.SetContentType("application/octet-stream");

    // 设置进度回调
    if (progress_callback) {
        put_object_request.SetDataSentEventHandler([progress_callback, file_size](const Aws::Http::HttpRequest*, long long bytes_sent) {
            progress_callback(static_cast<size_t>(bytes_sent), static_cast<size_t>(file_size));
        });
    }

    // 执行上传
    auto outcome = s3_client->PutObject(put_object_request);

    if (outcome.IsSuccess()) {
        std::string object_url = generateObjectUrl(sts_info.bucket, full_object_key, sts_info.endpoint);
        std::string etag = outcome.GetResult().GetETag();
        
        LOG_INFO("[AwsS3Uploader] Upload successful: %s", object_url.c_str());
        LOG_INFO("[AwsS3Uploader] ETag: %s", etag.c_str());
        
        return CloudStorageUploadResult(true, "", object_url, etag);
    } else {
        std::string error_msg = "Upload failed: " + outcome.GetError().GetMessage();
        LOG_ERROR("[AwsS3Uploader] %s", error_msg.c_str());
        return CloudStorageUploadResult(false, error_msg);
    }
#else
    LOG_ERROR("[AwsS3Uploader] AWS SDK not available");
    return CloudStorageUploadResult(false, "AWS SDK not available");
#endif
}

CloudStorageUploadResult AwsS3Uploader::uploadFile(
    const std::string& file_path,
    const std::string& object_key,
    const STSInfo& sts_info,
    CloudStorageProgressCallback progress_callback) {
    
    return doUploadFile(file_path, object_key, sts_info, progress_callback);
}

void AwsS3Uploader::uploadFileAsync(
    const std::string& file_path,
    const std::string& object_key,
    const STSInfo& sts_info,
    std::function<void(const CloudStorageUploadResult&)> callback,
    CloudStorageProgressCallback progress_callback) {
    
#ifdef USE_AWS_SDK
    if (!executor_) {
        callback(CloudStorageUploadResult(false, "Executor not available"));
        return;
    }

    // 在后台线程中执行上传
    auto executor = static_cast<std::shared_ptr<Aws::Utils::Threading::Executor>*>(&executor_);
    (*executor)->Submit([this, file_path, object_key, sts_info, callback, progress_callback]() {
        auto result = doUploadFile(file_path, object_key, sts_info, progress_callback);
        callback(result);
    });
#else
    callback(CloudStorageUploadResult(false, "AWS SDK not available"));
#endif
}

bool AwsS3Uploader::isProviderSupported(const std::string& provider) const {
    return provider == "aws" || provider == "minio";
}

} // namespace flight_hub_sdk
