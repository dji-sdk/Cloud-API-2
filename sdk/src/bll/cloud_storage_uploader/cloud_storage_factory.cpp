// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "cloud_storage_factory.h"
#include "aws_s3_uploader.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

std::unique_ptr<ICloudStorageUploader> CloudStorageFactory::createUploader(const std::string& provider) {
    LOG_INFO("[CloudStorageFactory] Creating uploader for provider: %s", provider.c_str());
    
    if (provider == "aws" || provider == "minio") {
        return std::make_unique<AwsS3Uploader>();
    }
    
    // 未来可以在这里添加其他云存储提供商的实现
    // else if (provider == "ali") {
    //     return std::make_unique<AliyunOssUploader>();
    // }
    
    LOG_ERROR("[CloudStorageFactory] Unsupported provider: %s", provider.c_str());
    return nullptr;
}

bool CloudStorageFactory::isProviderSupported(const std::string& provider) {
    return provider == "aws" || provider == "minio";
}

} // namespace flight_hub_sdk
