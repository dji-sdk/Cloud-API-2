// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "SDKFactory.h"
#include "SDKManagerImpl.h"

namespace notification_center {
    std::shared_ptr<SDKManager> SDKFactory::createSDKManager() {
        return std::make_shared<SDKManagerImpl>();
    }

    void SDKFactory::destroySDKManager(std::shared_ptr<SDKManager> sdk) {
        // 智能指针会自动管理内存，这里可以添加清理逻辑
        sdk.reset();
    }
}
