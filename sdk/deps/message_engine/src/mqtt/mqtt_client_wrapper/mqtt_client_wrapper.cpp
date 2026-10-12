// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "mqtt_client_wrapper.h"

#include <base/common/logger.h>

namespace message_engine {

MqttClientWrapper::MqttClientWrapper(
    const std::shared_ptr<drc_sdk::Executor>& executor,
    std::unordered_map<std::string, std::string> topic_method_map,
    const std::vector<TopicInfo>& topics_pub,
    const std::vector<TopicInfo>& topics_sub,
    std::string log_tag,
    std::shared_ptr<IMqttClientBridge> mqtt_client_bridge)
    : Super(executor, std::move(topic_method_map), topics_pub, topics_sub, std::move(log_tag)),
      mqtt_client_bridge_(std::move(mqtt_client_bridge)) {
}

MeErrorCode MqttClientWrapper::SetUp() {
    if (!mqtt_client_bridge_) {
        LOG_ERROR("[MqttClientWrapper-%s] SetUp failed: mqtt_client_bridge_ is null", log_tag_.c_str());
        return ERROR_TRANSPORT_NOT_FOUND;
    }

    const auto super_err = Super::SetUp();
    if (super_err != ME_NO_ERROR) {
        return super_err;
    }

    auto err = mqtt_client_bridge_->SetConnectStateCallback([this](const MQTT_CONNECT_STATUS& state) {
        CloudConnectStateCallback(static_cast<int>(state));
        if (state == MQTT_CONNECT_STATUS::CONNECTED) {
            TopicRegister();
        }
    });
    if (err != ME_NO_ERROR) {
        LOG_ERROR("[MqttClientWrapper-%s] SetConnectStateCallback failed, err: %lld", log_tag_.c_str(), static_cast<long long>(err));
        return err;
    }

    connect_state_listen_id_ = 1;  // JS bridge当前仅支持单连接状态回调，预留值用于清理

    err = mqtt_client_bridge_->SetMessageCallback([this](const std::string& topic, const std::string& payload) {
        MessageCallback(topic, payload);
    });
    if (err != ME_NO_ERROR) {
        LOG_ERROR("[MqttClientWrapper-%s] SetMessageCallback failed, err: %lld", log_tag_.c_str(), static_cast<long long>(err));
        return err;
    }

    message_listen_id_ = 1;  // JS bridge当前仅支持单消息回调，预留值用于清理

    err = mqtt_client_bridge_->Create();
    if (err != ME_NO_ERROR) {
        LOG_ERROR("[MqttClientWrapper-%s] Create failed, err: %lld", log_tag_.c_str(), static_cast<long long>(err));
        return err;
    }

    err = mqtt_client_bridge_->Connect();
    if (err != ME_NO_ERROR) {
        LOG_ERROR("[MqttClientWrapper-%s] Connect failed, err: %lld", log_tag_.c_str(), static_cast<long long>(err));
        return err;
    }

    return ME_NO_ERROR;
}

MeErrorCode MqttClientWrapper::CleanUp() {
    if (mqtt_client_bridge_) {
        if (connect_state_listen_id_ > 0) {
            mqtt_client_bridge_->RemoveConnectStateCallback(connect_state_listen_id_);
            connect_state_listen_id_ = 0;
        }
        if (message_listen_id_ > 0) {
            mqtt_client_bridge_->RemoveMessageCallback(message_listen_id_);
            message_listen_id_ = 0;
        }
        mqtt_client_bridge_->Disconnect();
        mqtt_client_bridge_->Destroy();
    }
    return Super::CleanUp();
}

MeErrorCode MqttClientWrapper::ConnectParamsConfig(
    const std::string& host,
    const std::string& username,
    const std::string& password,
    const std::string& client_id,
    const std::string& sn) {
    const auto err = Super::ConnectParamsConfig(host, username, password, client_id, sn);
    if (err != ME_NO_ERROR) {
        return err;
    }
    if (!mqtt_client_bridge_) {
        LOG_ERROR("[MqttClientWrapper-%s] ConnectParamsConfig failed: mqtt_client_bridge_ is null", log_tag_.c_str());
        return ERROR_TRANSPORT_NOT_FOUND;
    }
    return mqtt_client_bridge_->SetConnectParams(host, username, password, client_id, sn);
}

void MqttClientWrapper::TopicRegister() {
    if (!mqtt_client_bridge_) {
        LOG_ERROR("[MqttClientWrapper-%s] TopicRegister failed: mqtt_client_bridge_ is null", log_tag_.c_str());
        return;
    }
    for (const auto& topic : subscribe_topics_) {
        std::string real_topic = topic.topic_name;
        auto pos = real_topic.find("{device_sn}");
        if (pos != std::string::npos) {
            real_topic = real_topic.substr(0, pos) + product_sn_ + real_topic.substr(pos + 11);
        }
        LOG_INFO("[MqttClientWrapper-%s] Subscribe -> %s", log_tag_.c_str(), real_topic.c_str());
        const auto err = mqtt_client_bridge_->Subscribe(real_topic);
        if (err != ME_NO_ERROR) {
            LOG_ERROR("[MqttClientWrapper-%s] Subscribe failed, topic: %s, err: %lld",
                      log_tag_.c_str(), real_topic.c_str(), static_cast<long long>(err));
        }
    }
}

void MqttClientWrapper::Publish(const std::string& topic, const std::string& payload) {
    if (!mqtt_client_bridge_) {
        LOG_ERROR("[MqttClientWrapper-%s] Publish failed: mqtt_client_bridge_ is null", log_tag_.c_str());
        return;
    }
    std::string real_topic = topic;
    auto pos = real_topic.find("{device_sn}");
    if (pos != std::string::npos) {
        real_topic = real_topic.substr(0, pos) + product_sn_ + real_topic.substr(pos + 11);
    }
    const auto err = mqtt_client_bridge_->Publish(real_topic, payload);
    if (err != ME_NO_ERROR) {
        LOG_ERROR("[MqttClientWrapper-%s] Publish failed, topic: %s, err: %lld",
                  log_tag_.c_str(), real_topic.c_str(), static_cast<long long>(err));
    }
}

}  // namespace message_engine
