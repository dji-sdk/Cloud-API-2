// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "cloud_storage_interface.h"
#include <memory>

namespace flight_hub_sdk {

/**
 * @brief 云存储上传器工厂类
 */
class CloudStorageFactory {
public:
    /**
     * @brief 创建云存储上传器
     * @param provider 云存储提供商（如 "aws", "minio", "ali" 等）
     * @return 云存储上传器指针，如果提供商不支持则返回 nullptr
     */
    static std::unique_ptr<ICloudStorageUploader> createUploader(const std::string& provider);

    /**
     * @brief 检查提供商是否支持
     * @param provider 云存储提供商
     * @return 是否支持
     */
    static bool isProviderSupported(const std::string& provider);

private:
    CloudStorageFactory() = default;
    ~CloudStorageFactory() = default;
};

} // namespace flight_hub_sdk
