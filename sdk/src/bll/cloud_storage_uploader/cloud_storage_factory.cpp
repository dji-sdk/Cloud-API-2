// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "cloud_storage_factory.h"
#include "aws_s3_uploader.h"
#include <base/common/logger.h>
#include <filesystem>

namespace flight_hub_sdk {

std::mutex CloudStorageFactory::tls_mu_;
CloudStorageTlsConfig CloudStorageFactory::tls_config_;

void CloudStorageFactory::SetTlsConfig(bool verify_ssl, const std::string& ca_path) {
    std::lock_guard<std::mutex> lock(tls_mu_);
    if (!verify_ssl) {
        LOG_WARN("[CloudStorageFactory] S3 upload channel TLS verify disabled (unsafe, test only)");
    }
    if (!ca_path.empty()) {
        std::error_code ec;
        if (!std::filesystem::is_regular_file(ca_path, ec)) {
            LOG_ERROR("[CloudStorageFactory] ssl_ca_path is not a regular file: %s (ec=%s), "
                      "fall back to system CA store",
                      ca_path.c_str(), ec.message().c_str());
            tls_config_ = {verify_ssl, ""};
            return;
        }
    }
    tls_config_ = {verify_ssl, ca_path};
}

CloudStorageTlsConfig CloudStorageFactory::GetTlsConfig() {
    std::lock_guard<std::mutex> lock(tls_mu_);
    return tls_config_;
}

std::unique_ptr<ICloudStorageUploader> CloudStorageFactory::createUploader(const std::string& provider) {
    LOG_INFO("[CloudStorageFactory] Creating uploader for provider: %s", provider.c_str());

    if (provider == "aws" || provider == "minio") {
        return std::make_unique<AwsS3Uploader>(GetTlsConfig());
    }

    LOG_ERROR("[CloudStorageFactory] Unsupported provider: %s", provider.c_str());
    return nullptr;
}

bool CloudStorageFactory::isProviderSupported(const std::string& provider) {
    return provider == "aws" || provider == "minio";
}

}  // namespace flight_hub_sdk
