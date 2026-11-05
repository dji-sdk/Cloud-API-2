// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/airsense_warning_module.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class AirsenseWarningModuleImpl : public IAirsenseWarningModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit AirsenseWarningModuleImpl(const std::string& drone_sn, const std::string& gateway_sn, int session_id);
    ~AirsenseWarningModuleImpl() override;

    int listenAirsenseWarning(
        const std::function<void(const AirsenseWarning& msg)>& callback) override;

    void cancelListenAirsenseWarning(int listen_id) override;

private:
    void registerNotificationCenterListener();

    std::string drone_sn_;
    std::string gateway_sn_;
    int session_id_;
    CallbackContainer<AirsenseWarning, false> airsense_callbacks_;  // UseCache=false：事件流不去重
    int airsense_listen_id_ = -1;
};

} // namespace flight_hub_sdk
