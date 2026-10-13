// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "i_message_engine.h"
#include "api/stream.h"
#include "api/custom_sei.h"
#include "api/pull_stream.h"
#include "bridge/i_rtc_channel_manager_bridge.h"
#include "bridge/i_mqtt_client_manager_bridge.h"
#include <unordered_map>
#include <mutex>
#include <string>
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class StreamImpl : public IStream, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit StreamImpl(
        const std::shared_ptr<message_engine::IRtcChannelManagerBridge>& rtc_channel_manager_bridge = nullptr,
        const std::shared_ptr<message_engine::IMqttClientManagerBridge>& mqtt_client_manager_bridge = nullptr);
    ~StreamImpl() override = default;

    std::shared_ptr<ICustomSEIModule> getCustomSEIModule() override;
    std::shared_ptr<IPullStreamModule> getPullStreamModule(const std::string& stream_id) override;

private:
    std::shared_ptr<ICustomSEIModule> custom_sei_module_;
    std::unordered_map<std::string, std::shared_ptr<IPullStreamModule>> pull_stream_modules_;
    std::mutex pull_stream_modules_mutex_;

    /// message engine
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;
    std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc_channel_manager_bridge_;
    std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt_client_manager_bridge_;
};

} // namespace flight_hub_sdk
