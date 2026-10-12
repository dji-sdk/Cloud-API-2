// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <memory>
#include "SDKManager.h"
#include "APIExport.h"

namespace notification_center {
    // SDK工厂类，负责创建和管理SDK实例
    class NOTIFICATION_CENTER_API SDKFactory {
    public:
        /**
         * 创建SDK管理器实例
         * @return SDK管理器实例
         */
        static std::shared_ptr<SDKManager> createSDKManager();

        /**
         * 销毁SDK管理器实例
         * @param sdk SDK管理器实例
         */
        static void destroySDKManager(std::shared_ptr<SDKManager> sdk);

    private:
        SDKFactory() = default;
        ~SDKFactory() = default;
    };
}
