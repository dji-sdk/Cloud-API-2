// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "mqtt_client.h"

#include <base/common/logger.h>
#include <json11.hpp>
#include <message_v1.h>
#include <message_v2.h>
#include <message_v3.h>
#include <message_decoder.h>

namespace message_engine {

MqttClient::MqttClient(const std::shared_ptr<drc_sdk::Executor>& executor, std::unordered_map<std::string, std::string> topic_method_map, 
                             const std::vector<TopicInfo>& topics_pub, const std::vector<TopicInfo>& topics_sub, std::string log_tag) {
    executor_ = executor;
    topic_method_map_ = std::move(topic_method_map);
    publish_topics_ = std::move(topics_pub);
    subscribe_topics_ = std::move(topics_sub);
    log_tag_ = log_tag;
    scheduler_ = std::make_shared<MessageScheduler>(executor_, log_tag_);
    LOG_INFO("[MqttClient-%s] ctor", log_tag_.c_str());
}

MqttClient::~MqttClient() {
    LOG_INFO("[MqttClient-%s] ~MqttClient", log_tag_.c_str());
}

MeErrorCode MqttClient::SetUp() {
    LOG_INFO("[MqttClient-%s] SetUp", log_tag_.c_str());
    scheduler_->AddListener(shared_from_this());
    if (!scheduler_->Start()) {
        LOG_ERROR("[MqttClient-%s] SetUp failed", log_tag_.c_str());
        return ERROR_TRANSPORT_ERROR;
    }
    return ME_NO_ERROR;
}

MeErrorCode MqttClient::CleanUp() {
    LOG_INFO("[MqttClient-%s] CleanUp", log_tag_.c_str());
    if (scheduler_) {
        scheduler_->Stop();
    }
    product_sn_.clear();
    user_name_.clear();
    password_.clear();
    host_.clear();
    log_tag_.clear();
    scheduler_ = nullptr;
    executor_ = nullptr;
    topic_method_map_.clear();
    publish_topics_.clear();
    subscribe_topics_.clear();
    connect_state_callbacks_.clear();
    return ME_NO_ERROR;
}

MeErrorCode MqttClient::ConnectParamsConfig(const std::string& host, const std::string& username, const std::string& password, const std::string& client_id, const std::string& sn) {
    host_ = host;
    user_name_ = username;
    password_ = password;
    product_sn_ = sn;
    client_id_ = client_id;
    // 密码脱敏: 仅打印 set/empty 状态, 不落明文(参照 live_module_impl.cpp 对 token 的处理)
    LOG_INFO("[MqttClient-%s] ConnectParamsConfig, host: %s, username: %s, password: %s, sn: %s", log_tag_.c_str(), host_.c_str(), user_name_.c_str(), password_.empty() ? "empty" : "set", product_sn_.c_str());
    return ME_NO_ERROR;
}

/*---------------------- observe ------------------*/
ListenIdCode MqttClient::Observe(const std::shared_ptr<ObserveCallback>& callback) {
    return scheduler_->AddObserve(callback);
}

void MqttClient::CancelObserve(const ListenIdCode& listen_id) {
    scheduler_->CancelObserve(listen_id);
}

/* --------------------- Send -------------------- */
MeErrorCode MqttClient::Send(const std::shared_ptr<MessageWrapper>& message) {
    if (connect_state_ != MQTT_CONNECT_STATUS::CONNECTED) {
        return MeErrorCode::ERROR_CONNECT_ERROR;
    }
    return scheduler_->ProcessSendMessage(message);
}

MeErrorCode MqttClient::Send(const std::shared_ptr<MessageWrapper>& message, const MeResponse& response, const std::shared_ptr<drc_sdk::Executor> &user_define_executor) {
    if (connect_state_ != MQTT_CONNECT_STATUS::CONNECTED) {
        return MeErrorCode::ERROR_CONNECT_ERROR;
    }
    return scheduler_->ProcessSendMessage(message, response, user_define_executor);
}

void MqttClient::OnMessageNeedToBeSent(const std::shared_ptr<MessageWrapper>&message) {
    WeakDummy(weak_ptr);
    executor_->Dispatch([this, weak_ptr, message]() {
        switch (message->GetMessageProtocolType()) {
            case MessageProtocolType::V1: {
                OnSentV1Message(message);
                break;
            }
            case MessageProtocolType::V2: {
                OnSentV2Message(message);
                break;
            }
            case MessageProtocolType::V3: {
                OnSentV3Message(message);
                break;
            }
            default:
                break;
        }
    });
}

void MqttClient::OnSentV1Message(const std::shared_ptr<MessageWrapper> &message) {
    const auto v1_message = std::dynamic_pointer_cast<MessageV1>(message);
    // V1消息 转 mqtt 消息
    std::string error;
    json11::Json::object content;
    content["tid"] = v1_message->GetTid();
    content["bid"] = v1_message->GetBid();
    content["timestamp"] = static_cast<double>(v1_message->GetTimestamp());
    content["method"] = v1_message->GetMethod();
    content["data"] = json11::Json::parse(v1_message->ToJsonString(), error);
    if (v1_message->GetIsNeedReply().has_value()) {
        content["need_reply"] = (int)v1_message->GetIsNeedReply().value();
    }
    if (!error.empty()) {
        LOG_ERROR("[MqttClient] OnSentV1Message Failed to parse message: %s, error %s", v1_message->ToJsonString().c_str(), error.c_str());
        return;
    }
    const auto message_string = json11::Json(content).dump();
    this->Publish(v1_message->GetTopic(), message_string);
}

void MqttClient::OnSentV2Message(const std::shared_ptr<MessageWrapper>& message) {
    const auto v2_message = std::dynamic_pointer_cast<MessageV2>(message);
    LOG_DEBUG("[MqttClient-%s] OnSentV2Message, method: %s, seq: %lld, uuid: %s", log_tag_.c_str(), v2_message->GetMethod().c_str(), v2_message->GetSeq(), v2_message->GetUuid().c_str());

    std::string error;
    json11::Json::object content;
    content["seq"] = static_cast<double>(v2_message->GetSeq());
    content["method"] = v2_message->GetMethod();
    content["data"] = json11::Json::parse(v2_message->ToJsonString(), error);
    if (!error.empty()) {
        LOG_ERROR("[MqttClient] OnSentV2Message Failed to parse message: %s, error %s", v2_message->ToJsonString().c_str(), error.c_str());
        return;
    }
    const auto message_string = json11::Json(content).dump();
    this->Publish(v2_message->GetDownTopic(), message_string);
}

void MqttClient::OnSentV3Message(const std::shared_ptr<MessageWrapper>& message) {
    const auto v3_message = std::dynamic_pointer_cast<MessageV3>(message);
    // todo:mqtt 链路不支持发送 V3 Message
    LOG_ERROR("[MqttClient-%s] OnSentV3Message, not support", log_tag_.c_str());
}

/*----------------  Recive  ------------------*/
void MqttClient::MessageCallback(const std::string& topic, const std::string& data) {
    if (!executor_) {
        LOG_ERROR("[MqttClient-%s] MessageCallback executor is null.", log_tag_.c_str());
        return;
    }
    // LOG_DEBUG("[MqttClient-%s] MessageCallback %s %s", log_tag_.c_str(), topic.c_str(), data.c_str());
    WeakDummy(p);
    executor_->Dispatch([this, topic, data, p]() {
        WeakDummyReturn(p);
        OnReceivedMessage(topic, data);
    });
}

void MqttClient::OnReceivedMessage(const std::string& topic, const std::string& data) {
    auto message = MessageDecoder::Decode(data);
    if (message == nullptr) {
        LOG_ERROR("[MqttClient-%s] OnReceivedMessage failed %s %s", log_tag_.c_str(), topic.c_str(), data.c_str());
        return;
    }
    if (message->GetMessageProtocolType() == MessageProtocolType::V1) {
        auto message_v1 = std::dynamic_pointer_cast<MessageV1>(message);
        message_v1->SetMethod(topic_method_map_[topic]);
        message_v1->SetTopic(topic);
        scheduler_->ProcessReceivedMessage(message_v1);
        LOG_DEBUG("[MqttClient-%s] OnReceivedMessage successfully %s %s", log_tag_.c_str(), message->GetMethod().c_str(), topic.c_str());
        return;
    }
    if (message->GetMessageProtocolType() == MessageProtocolType::V2) {
        auto message_v2 = std::dynamic_pointer_cast<MessageV2>(message);
        scheduler_->ProcessReceivedMessage(message_v2);
        return;
    }
    if (message->GetMessageProtocolType() == MessageProtocolType::V3) {
        // todo:不支持V3
        LOG_ERROR("[MqttClient-%s] OnReceivedMessage, not support v3", log_tag_.c_str());
        return;
    }
}

/*----------------  Cloud Connect State  ------------------*/
void MqttClient::CloudConnectStateCallback(int state) {
    if (!executor_) {
        LOG_INFO("[MqttClient-%s] CloudConnectStateCallback executor is null.", log_tag_.c_str());
        return;
    }
    WeakDummy(p);
    connect_state_ = MQTT_CONNECT_STATUS(state);
    executor_->Dispatch([this, state, p]() {
        WeakDummyReturn(p);
        LOG_INFO("[MqttClient-%s] CloudConnectStateCallback changed %d.", log_tag_.c_str(), state);
        for (const auto& [listen_id, callback]: connect_state_callbacks_) {
            if (callback) {
                callback((MQTT_CONNECT_STATUS)(state));
            }
        }
    });
}

uint32_t MqttClient::AddConnectStateCallback(const MqttConnectStateCallback& callback) {
    if (!executor_) {
        LOG_INFO("[MqttClient-%s] MqttClient::AddConnectStateCallback executor_ nullptr", log_tag_.c_str());
        return -1;
    }
    auto listen_id = GenerateListenId();
    WeakDummy(p);
    executor_->Dispatch([this, callback = std::move(callback), listen_id, p]() {
        WeakDummyReturn(p)
        connect_state_callbacks_[listen_id] = callback;
        if (callback) {
            callback(connect_state_);
        }
    });
    return listen_id;
}

void MqttClient::RemoveConnectStateCallback(uint32_t listen_id) {
    if (!executor_) {
        LOG_INFO("[MqttClient-%s] MqttClient::AddConnectStateCallback executor_ nullptr", log_tag_.c_str());
        return;
    }
    WeakDummy(p);
    executor_->Dispatch([this, listen_id, p]() {
        WeakDummyReturn(p)
        if (connect_state_callbacks_.count(listen_id) > 0) {
            connect_state_callbacks_.erase(listen_id);
        }
    });
}

void MqttClient::SetObserveCallbackManager(const std::shared_ptr<ObserveCallbackManager>& observe_callback_manager) {
    if (scheduler_ != nullptr) {
        scheduler_->SetObserveCallbackManager(observe_callback_manager);
    } else {
        LOG_ERROR("[MqttClient-%s] SetObserveCallbackManager, scheduler_ is nullptr", log_tag_.c_str());
    }
}
}  // namespace message_engine