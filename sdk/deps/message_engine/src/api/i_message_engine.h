// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include "i_cloud_message_manager.h"
#include "i_rtc_channel_manager.h"
#include "i_mqtt_client_manager.h"
#include "i_transport_manager.h"
#include "bridge/i_rtc_channel_manager_bridge.h"
#include "bridge/i_mqtt_client_manager_bridge.h"


namespace message_engine {

class IMessageEngine {
public:
    virtual ~IMessageEngine() = default;
    virtual bool initialize() = 0;
    virtual bool setRtcChannelManager(const std::shared_ptr<IRtcChannelManagerBridge>& channel_manager_bridge) = 0;
    virtual bool setMqttClientManager(const std::shared_ptr<IMqttClientManagerBridge>& mqtt_client_manager_bridge) = 0;
    
    virtual std::shared_ptr<ICloudMessageManager> getCloudMessageManager() = 0;
    virtual std::shared_ptr<IRtcChannelManager> getRtcChannelManager() = 0;
    virtual std::shared_ptr<IMqttClientManager> getMqttClientManager() = 0;
    virtual std::shared_ptr<ITransportManager> getTransportManager() = 0;
    virtual void destroy() = 0;
};

} // namespace message_engine