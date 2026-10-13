// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "mqtt_client_manager.h"
#include <base/common/logger.h>
#include <transport_manager.h>
#include "paho_mqtt_client.h"

using namespace message_engine;

MqttClientManager::MqttClientManager(const std::shared_ptr<drc_sdk::Executor>& executor, const std::shared_ptr<TransportManager>& transport_manager) : executor_(executor), transport_manager_(transport_manager) {
    LOG_INFO("[MqttClientManager] ctor");
}

MqttClientManager::~MqttClientManager() {
    LOG_INFO("[MqttClientManager] dtor");
}

MeErrorCode MqttClientManager::AddMqttClient(const TransportIdType& transport_id, const AddMqttClientParams& params) {
    LOG_INFO("[MqttClientManager] AddMqttClient, transport_id: %s", transport_id.c_str());
    
    if (mp_transport_id_to_mqtt_client_.contains(transport_id)) {
        LOG_WARN("[MqttClientManager] AddMqttClient, Mqtt client already exists, transport_id: %s", transport_id.c_str());
        return ME_NO_ERROR;
    }

    std::shared_ptr<MqttClient> mqtt_client = nullptr;

    if (transport_id == DDSAL_BASE_TRANSPORT_ID) {
        LOG_ERROR("[MqttClientManager] AddMqttClient, DDSAL is not enabled");
        return ERROR_TRANSPORT_NOT_FOUND;
    } else if (transport_id == PAHO_MQTT_BASE_TRANSPORT_ID || transport_id == PAHO_MQTT_DRC_TRANSPORT_ID) {
#ifdef BUILD_WITH_PAHO_MQTT
        mqtt_client = std::make_shared<PahoMqttClient>(executor_, params.topic_method_map, params.topics_pub, params.topics_sub, transport_id);
#else
        LOG_ERROR("[MqttClientManager] AddMqttClient, PahoMQTT is not enabled");
        return ERROR_TRANSPORT_NOT_FOUND;
#endif
    } else {
        LOG_ERROR("[MqttClientManager] AddMqttClient, Unknown transport_id: %s", transport_id.c_str());
        return ERROR_TRANSPORT_NOT_FOUND;
    }

    // 设置ObserveCallbackManager
    mqtt_client->SetObserveCallbackManager(transport_manager_->GetObserveCallbackManager());

    mp_transport_id_to_mqtt_client_[transport_id] = mqtt_client;
    LOG_INFO("[MqttClientManager] AddMqttClient: %p", mp_transport_id_to_mqtt_client_[transport_id].get());
    transport_manager_->AddTransport(transport_id, mqtt_client);
    return ME_NO_ERROR;
}

MeErrorCode MqttClientManager::RemoveMqttClient(const TransportIdType& transport_id) {
    LOG_INFO("[MqttClientManager] RemoveMqttClient, transport_id: %s", transport_id.c_str());
    if (!mp_transport_id_to_mqtt_client_.contains(transport_id)) {
        LOG_WARN("[MqttClientManager] AddMqttClient, Mqtt client do not exists, transport_id: %s", transport_id.c_str());
        return ERROR_TRANSPORT_NOT_FOUND;
    }
    LOG_INFO("[MqttClientManager] RemoveMqttClient: %p, use_count before erase: %ld",
        mp_transport_id_to_mqtt_client_[transport_id].get(), mp_transport_id_to_mqtt_client_[transport_id].use_count());
    mp_transport_id_to_mqtt_client_.erase(transport_id);
    transport_manager_->RemoveTransport(transport_id);
    return ME_NO_ERROR;
}

std::weak_ptr<IMqttClient> MqttClientManager::GetMqttClient(const TransportIdType& transport_id) {
    if (!mp_transport_id_to_mqtt_client_.contains(transport_id)) {
        return {};
    }
    LOG_INFO("[MqttClientManager] GetMqttClient: %p", mp_transport_id_to_mqtt_client_[transport_id].get());
    return mp_transport_id_to_mqtt_client_[transport_id];
}

bool MqttClientManager::IsMqttClientExisted(const TransportIdType& transport_id) {
    return mp_transport_id_to_mqtt_client_.contains(transport_id);
}