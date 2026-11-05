// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "cloud_storage_interface.h"
#include <memory>
#include <mutex>

namespace flight_hub_sdk {

class CloudStorageFactory {
public:
    /// SDK 初始化阶段统一下发（与 Network::SetTlsConfig 同点位）；线程安全
    static void SetTlsConfig(bool verify_ssl, const std::string& ca_path);
    static CloudStorageTlsConfig GetTlsConfig();

    static std::unique_ptr<ICloudStorageUploader> createUploader(const std::string& provider);
    static bool isProviderSupported(const std::string& provider);

private:
    CloudStorageFactory() = default;
    ~CloudStorageFactory() = default;
    static std::mutex tls_mu_;
    static CloudStorageTlsConfig tls_config_;
};

}  // namespace flight_hub_sdk
