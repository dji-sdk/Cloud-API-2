// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/bridge/i_mqtt_client_manager_bridge.h"
#include "api/i_mqtt_client_manager.h"
#include "mqtt_client_wrapper.h"
#include "transport/transport_manager.h"

namespace message_engine {

class MqttClientManagerWrapper : public IMqttClientManager {
public:
    MqttClientManagerWrapper(
        const std::shared_ptr<drc_sdk::Executor>& executor,
        const std::shared_ptr<TransportManager>& transport_manager,
        std::shared_ptr<IMqttClientManagerBridge> mqtt_client_manager_bridge);
    ~MqttClientManagerWrapper() override = default;

    MeErrorCode AddMqttClient(const TransportIdType& transport_id, const AddMqttClientParams& params) override;
    MeErrorCode RemoveMqttClient(const TransportIdType& transport_id) override;

    std::weak_ptr<IMqttClient> GetMqttClient(const TransportIdType& transport_id) override;
    bool IsMqttClientExisted(const TransportIdType& transport_id) override;

private:
    std::shared_ptr<drc_sdk::Executor> executor_;
    std::shared_ptr<TransportManager> transport_manager_;
    std::shared_ptr<IMqttClientManagerBridge> mqtt_client_manager_bridge_;
    std::unordered_map<TransportIdType, std::shared_ptr<MqttClientWrapper>> mqtt_clients_;
};

}  // namespace message_engine
