// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/i_message_engine.h"
#include "base/async/executor.h"
#include "transport/transport_manager.h"

namespace message_engine {

class MessageEngineImpl : public IMessageEngine {
public:
    MessageEngineImpl(const std::shared_ptr<::drc_sdk::Executor> &working_executor);
    ~MessageEngineImpl();

    bool initialize() override;
    bool setRtcChannelManager(const std::shared_ptr<IRtcChannelManagerBridge>& channel_manager_bridge) override;
    bool setMqttClientManager(const std::shared_ptr<IMqttClientManagerBridge>& mqtt_client_manager_bridge) override;
    
    std::shared_ptr<ICloudMessageManager> getCloudMessageManager() override;
    std::shared_ptr<IRtcChannelManager> getRtcChannelManager() override;
    std::shared_ptr<IMqttClientManager> getMqttClientManager() override;
    std::shared_ptr<ITransportManager> getTransportManager() override;

    void destroy() override;

private:
    std::shared_ptr<IRtcChannelManager> rtc_channel_manager_ = nullptr;
    std::shared_ptr<IMqttClientManager> mqtt_client_manager_ = nullptr;
    std::shared_ptr<ICloudMessageManager> cloud_message_manager_ = nullptr;
    std::shared_ptr<TransportManager> transport_manager_ = nullptr;
private:
    bool initialize_ = false;
    std::shared_ptr<::drc_sdk::Executor> working_executor_;
};
} // namespace message_engine