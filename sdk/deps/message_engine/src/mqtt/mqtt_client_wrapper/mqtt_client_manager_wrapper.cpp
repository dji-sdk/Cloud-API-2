// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "mqtt_client_manager_wrapper.h"

#include <base/common/logger.h>

namespace message_engine {

MqttClientManagerWrapper::MqttClientManagerWrapper(
    const std::shared_ptr<drc_sdk::Executor>& executor,
    const std::shared_ptr<TransportManager>& transport_manager,
    std::shared_ptr<IMqttClientManagerBridge> mqtt_client_manager_bridge)
    : executor_(executor),
      transport_manager_(transport_manager),
      mqtt_client_manager_bridge_(std::move(mqtt_client_manager_bridge)) {
}

MeErrorCode MqttClientManagerWrapper::AddMqttClient(const TransportIdType& transport_id, const AddMqttClientParams& params) {
    if (!mqtt_client_manager_bridge_ || !transport_manager_) {
        LOG_ERROR("[MqttClientManagerWrapper] AddMqttClient failed: bridge or transport_manager is null");
        return ERROR_TRANSPORT_NOT_FOUND;
    }

    if (mqtt_clients_.contains(transport_id)) {
        return ME_NO_ERROR;
    }

    auto err = mqtt_client_manager_bridge_->AddMqttClient(transport_id, params);
    if (err != ME_NO_ERROR) {
        LOG_ERROR("[MqttClientManagerWrapper] AddMqttClient bridge failed, transport_id: %s, err: %lld",
                  transport_id.c_str(), static_cast<long long>(err));
        return err;
    }

    auto mqtt_client_bridge = mqtt_client_manager_bridge_->GetMqttClient(transport_id).lock();
    if (!mqtt_client_bridge) {
        LOG_ERROR("[MqttClientManagerWrapper] GetMqttClient bridge failed, transport_id: %s", transport_id.c_str());
        return ERROR_TRANSPORT_NOT_FOUND;
    }

    auto mqtt_client = std::make_shared<MqttClientWrapper>(
        executor_,
        params.topic_method_map,
        params.topics_pub,
        params.topics_sub,
        transport_id,
        mqtt_client_bridge);
    mqtt_client->SetObserveCallbackManager(transport_manager_->GetObserveCallbackManager());
    mqtt_clients_[transport_id] = mqtt_client;
    transport_manager_->AddTransport(transport_id, mqtt_client);
    return ME_NO_ERROR;
}

MeErrorCode MqttClientManagerWrapper::RemoveMqttClient(const TransportIdType& transport_id) {
    if (!mqtt_client_manager_bridge_ || !transport_manager_) {
        LOG_ERROR("[MqttClientManagerWrapper] RemoveMqttClient failed: bridge or transport_manager is null");
        return ERROR_TRANSPORT_NOT_FOUND;
    }

    mqtt_client_manager_bridge_->RemoveMqttClient(transport_id);
    mqtt_clients_.erase(transport_id);
    transport_manager_->RemoveTransport(transport_id);
    return ME_NO_ERROR;
}

std::weak_ptr<IMqttClient> MqttClientManagerWrapper::GetMqttClient(const TransportIdType& transport_id) {
    if (!mqtt_clients_.contains(transport_id)) {
        return {};
    }
    return mqtt_clients_[transport_id];
}

bool MqttClientManagerWrapper::IsMqttClientExisted(const TransportIdType& transport_id) {
    return mqtt_clients_.contains(transport_id);
}

}  // namespace message_engine
