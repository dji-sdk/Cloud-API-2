// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "paho_mqtt_client.h"
#include <base/common/logger.h>
#include <common/log_safe.h>
#include <json11.hpp>
namespace message_engine {

extern "C" {
    void PahoMqttOnConnect(void* context, MQTTAsync_successData* data) {
        LOG_INFO("[PahoMqttClient] [PahoMqttOnConnect] Connect success");
        if (!context) {
            LOG_INFO("PahoMqttOnConnect param nullptr");
            return;
        }
        auto paho_mqtt_client = (PahoMqttClient*)context;
        paho_mqtt_client->TopicRegister();
        paho_mqtt_client->CloudConnectStateCallback((int)MQTT_CONNECT_STATUS::CONNECTED);
    }


    void PahoMqttOnConnectFailure(void* context, MQTTAsync_failureData* data) {
        // 连接失败后，需要立即重新连接
        LOG_INFO("[PahoMqttClient] [PahoMqttOnConnectFailure] Connect failed, reason code: %d, %s", data->code, data->message ? data->message : "");
        if (!context) {
            LOG_INFO("PahoMqttOnConnect param nullptr");
            return;
        }
        auto paho_mqtt_client = (PahoMqttClient*)context;
        paho_mqtt_client->CloudConnectStateCallback((int)MQTT_CONNECT_STATUS::DISCONNECTED);
        paho_mqtt_client->Connect();
    }

    void PahoMqttOnDeliveryComplete(void* context, MQTTAsync_token token) {
        LOG_DEBUG("[PahoMqttClient] [PahoMqttOnDeliveryComplete] deliveryComplete, token: %d", token);
    }

    void PahoMqttConnectLost(void* context, char* cause) {
        // 丢失连接后需要立即重新连接
        LOG_INFO("[PahoMqttClient] connlost, Connection lost, cause: %s", cause ? cause : "");
        if (!context) {
            LOG_INFO("[PahoMqttClient] PahoMqttOnConnect param nullptr");
            return;
        }
        auto paho_mqtt_client = (PahoMqttClient*)context;
        paho_mqtt_client->CloudConnectStateCallback((int)MQTT_CONNECT_STATUS::DISCONNECTED);
        paho_mqtt_client->Connect();
    }

    int PahoMqttmessageCallbackImpl(void* context, char* topic_name, int topic_len, MQTTAsync_message* payload) {
        if (!topic_name || !payload) {
            LOG_INFO("[PahoMqttClient] PahoMqttmessageCallbackImpl param nullptr");
            return -1;
        }
        // 打印原始数据
        const auto topic = std::string(topic_name, topic_len);
        const auto body = std::string((char*)payload->payload, payload->payloadlen);
        // LOG_DEBUG("[PahoMqttClient] PahoMqttmessageCallbackImpl recv topic: %s, paylod: %s", topic.c_str(), drc_sdk::common::log_safe(body).c_str());


        // 解析payload，提取method
        std::string error;
        auto payload_json = json11::Json::parse(body, error);
        if (!error.empty()) {
            LOG_ERROR("[PahoMqttClient] PahoMqttmessageCallbackImpl recv parse payload error %s", error.c_str());
            return -1;
        }
        auto method = payload_json["method"];
        std::string add_method = "";
        if (method.is_string()) {
            add_method = method.string_value().empty() ? "" : "/" + method.string_value();
        }

        // 查找sn，替换为uav
        auto paho_mqtt_client = (PahoMqttClient*)context;
        auto sn_start_pos = topic.find(paho_mqtt_client->product_sn_);
        if (sn_start_pos == std::string::npos) {
            LOG_ERROR("[PahoMqttClient] PahoMqttmessageCallbackImpl sn no find, product_sn: %s", paho_mqtt_client->product_sn_.c_str());
            return -1;
        }
        
        // 组装外部认识的topic
        auto topic_name_head = topic.substr(0, sn_start_pos);
        auto topic_name_tail = topic.substr(sn_start_pos + paho_mqtt_client->product_sn_.size());
        const auto real_topic = topic_name_head + "{device_sn}" + topic_name_tail + add_method;
        // LOG_DEBUG("[PahoMqttClient] PahoMqttmessageCallbackImpl recv real_topic: %s", real_topic.c_str());
        paho_mqtt_client->MessageCallback(real_topic, body);
        return 1;
    }

    void PahoMqttOnDisconnectSuccess(void* context, MQTTAsync_successData* response) {
        LOG_INFO("[PahoMqttClient] PahoMqttOnDisconnectSuccess Disconnected successfully");
    }

    void PahoMqttOnDisconnectFailure(void* context, MQTTAsync_failureData* response) {
        LOG_ERROR("[PahoMqttClient] PahoMqttOnDisconnectFailure Disconnect failed, return code: %d", response ? response->code : -1);
    } 
}

PahoMqttClient::PahoMqttClient(const std::shared_ptr<drc_sdk::Executor>& executor, std::unordered_map<std::string, std::string> topic_method_map, 
                               const std::vector<TopicInfo>& topics_pub, const std::vector<TopicInfo>& topics_sub, std::string log_tag) :
                               MqttClient(executor, topic_method_map, topics_pub, topics_sub, log_tag) {}

PahoMqttClient::~PahoMqttClient() {
    LOG_INFO("[PahoMqttClient-%s] ~PahoMqttClient", log_tag_.c_str());
    CleanUp();
}

MeErrorCode PahoMqttClient::SetUp() {
    Super::SetUp();
    if (Create() != 0) {
        LOG_ERROR("[PahoMqttClient-%s] SetUp, Create failed", log_tag_.c_str());
        return ERROR_TRANSPORT_ERROR;
    }
    if (Connect() != 0) {
        LOG_ERROR("[PahoMqttClient-%s] SetUp, Connect failed", log_tag_.c_str());
        return ERROR_TRANSPORT_ERROR;
    }
    return ME_NO_ERROR;
}

MeErrorCode PahoMqttClient::CleanUp() {
    LOG_INFO("[PahoMqttClient-%s] CleanUp", log_tag_.c_str());
    Disconnect();
    if (client_ != nullptr) {
        MQTTAsync_destroy(&client_);
        client_ = nullptr;
    }
    real_subscribe_topics_.clear();
    Super::CleanUp();
    return ME_NO_ERROR;
}

int PahoMqttClient::Create() {
    LOG_INFO("[PahoMqttClient-%s] Create", log_tag_.c_str());
    int ret = 0;
    if (client_ != nullptr) {
        LOG_INFO("[PahoMqttClient-%s] Create, already done", log_tag_.c_str());
        return 0;
    }
    if (product_sn_.empty() || host_.empty()) {
        LOG_INFO("[PahoMqttClient-%s] Create fail, product_sn or host is empty.", log_tag_.c_str());
        return -1;
    }
    LOG_INFO("[PahoMqttClient-%s] Create, product_sn: %s, host: %s, user_name: %s, password: %s, client_id: %s", log_tag_.c_str(), product_sn_.c_str(), host_.c_str(), user_name_.c_str(), password_.empty() ? "empty" : "set", client_id_.c_str());
    // creat
    MQTTAsync_createOptions create_opts = MQTTAsync_createOptions_initializer;
    create_opts.sendWhileDisconnected = true;
    create_opts.maxBufferedMessages = 100;
    ret = MQTTAsync_createWithOptions(&client_, host_.c_str(), client_id_.c_str(), MQTTCLIENT_PERSISTENCE_NONE, nullptr, &create_opts);
    if (ret != MQTTASYNC_SUCCESS) {
        LOG_ERROR("[PahoMqttClient-%s] Init, Failed to create client object, return code %d", log_tag_.c_str(), ret);
        return ret;
    }

    // set callback
    ret = MQTTAsync_setCallbacks(client_, this, PahoMqttConnectLost, PahoMqttmessageCallbackImpl, PahoMqttOnDeliveryComplete);
    if (ret != 0) {
        LOG_ERROR("[PahoMqttClient-%s] Init, Failed to set callback, return code %d", log_tag_.c_str(), ret);
        return ret;
    }
    LOG_INFO("[PahoMqttClient-%s] Create, done: %d", log_tag_.c_str(), ret);
    return ret;
}

int PahoMqttClient::Connect() {
    int ret = 0;
    if (client_ == nullptr) {
        LOG_INFO("[PahoMqttClient-%s] Connect, client is nullptr", log_tag_.c_str());
        return 0;
    }
    if (user_name_.empty()) {
        LOG_INFO("[PahoMqttClient-%s] Connect, user name is empty", log_tag_.c_str());
        return 0;
    }
    MQTTAsync_connectOptions conn_opts = MQTTAsync_connectOptions_initializer;
    conn_opts.keepAliveInterval = 10;
    conn_opts.cleansession = 1;
    conn_opts.username = user_name_.c_str();
    conn_opts.password = password_.c_str();
    conn_opts.connectTimeout = 10;
    conn_opts.retryInterval = 5;
    conn_opts.automaticReconnect = false;
    conn_opts.context = this;
    conn_opts.onSuccess = PahoMqttOnConnect;
    conn_opts.onFailure = PahoMqttOnConnectFailure;
    
    // 配置SSL选项（用于WSS连接）
    MQTTAsync_SSLOptions ssl_opts = MQTTAsync_SSLOptions_initializer;
    ssl_opts.enableServerCertAuth = false;  // 禁用服务器证书验证（如果使用自签名证书）
    ssl_opts.verify = false;               // 禁用SSL验证
    conn_opts.ssl = &ssl_opts;
    if ((ret = MQTTAsync_connect(client_, &conn_opts)) != MQTTASYNC_SUCCESS) {
        LOG_INFO("[PahoMqttClient-%s] Connect, Failed to start connect, return code %d", log_tag_.c_str(), ret);
        return ret;
    }
    LOG_INFO("[PahoMqttClient-%s] Connect, done: %d", log_tag_.c_str(), ret);
    return ret;
}

void PahoMqttClient::TopicRegister() {
    // 下行消息类型：services, events_reply, requests_reply, state_reply, property/set, status_reply
    // TODO: 需要根据上下行修改topic
    subscribe_topics_ = {
            // {"thing/product/{device_sn}/services_reply", "JsonString", 1},
            // {"thing/product/{device_sn}/events", "JsonString", 1},
            // {"thing/product/{device_sn}/requests", "JsonString", 1},
            // {"thing/product/{device_sn}/state", "JsonString", 0},
            // {"thing/product/{device_sn}/property/set_reply", "JsonString", 1},
            {"thing/product/{device_sn}/drc/up", "JsonString", 0},
            // {"sys/product/{device_sn}/status", "JsonString", 1},
    };
    real_subscribe_topics_.clear();
    for (auto subscribe : subscribe_topics_) {
        LOG_INFO("[PahoMqttClient-%s] TopicRegister subscribe: %s, type: %s, qos: %d", log_tag_.c_str(), subscribe.topic_name.c_str(), subscribe.type.c_str(), subscribe.qos);
        auto pos = subscribe.topic_name.find("{device_sn}");
        if (pos != std::string::npos) {
            auto topic_name_head = subscribe.topic_name.substr(0, pos);
            auto topic_name_tail = subscribe.topic_name.substr(pos + 11);
            auto real_topic_name = topic_name_head + product_sn_ + topic_name_tail;
            real_subscribe_topics_.emplace_back(TopicInfo{real_topic_name, subscribe.type, subscribe.qos});
        }
    }
    for (auto subscribe : real_subscribe_topics_) {
        MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;
        LOG_INFO("[PahoMqttClient-%s] Subscribe ->  %s, qos %d", log_tag_.c_str(), subscribe.topic_name.c_str(), subscribe.qos);
        int ret = MQTTAsync_subscribe(client_, subscribe.topic_name.c_str(), subscribe.qos, &opts);
        if (ret != MQTTASYNC_SUCCESS) {
            LOG_ERROR("[PahoMqttClient-%s] %s, Failed to subscribe, return code %d", log_tag_.c_str(), subscribe.topic_name.c_str(), ret);
        }
    }
    LOG_INFO("[PahoMqttClient-%s] TopicRegister end.", log_tag_.c_str());
}

int PahoMqttClient::Disconnect() {
    if (client_ == nullptr) {
        return 0;
    }

    int ret = 0;
    MQTTAsync_token* tokens = nullptr;
    ret = MQTTAsync_getPendingTokens(client_, &tokens);
    if (ret != MQTTASYNC_SUCCESS) {
        LOG_INFO("[PahoMqttClient-%s] DisconnectRemote, MQTTAsync_getPendingTokens error: client: %p, ret = %d", log_tag_.c_str(), client_, ret);
        return ret;
    }

    if (tokens != nullptr) {
        MQTTAsync_free(tokens);
        tokens = nullptr;
    }

    MQTTAsync_disconnectOptions disc_opts = MQTTAsync_disconnectOptions_initializer;
    disc_opts.timeout = 5000;
    disc_opts.context = this;
    disc_opts.onSuccess = PahoMqttOnDisconnectSuccess;
    disc_opts.onFailure = PahoMqttOnDisconnectFailure;

    ret = MQTTAsync_disconnect(client_, &disc_opts);
    if (ret != MQTTASYNC_SUCCESS) {
        LOG_INFO("[PahoMqttClient-%s] DisconnectRemote, MQTTAsync_disconnect error: client: %p, ret = %d", log_tag_.c_str(), client_, ret);
        return ret;
    }
    return ret;
}

void PahoMqttClient::Publish(const std::string &topic, const std::string &payload) {
    auto qos = MQTTQos::DJI_MQTT_QOS_UNKNOWN;
    std::string real_topic_name;
    for (const auto& item : publish_topics_) {
        if (item.topic_name == topic) {
            real_topic_name = item.topic_name;
            real_topic_name = real_topic_name.replace(real_topic_name.find("{device_sn}"), 11, product_sn_);
            qos = (MQTTQos)item.qos;
            break;
        }
    }
    if (qos == MQTTQos::DJI_MQTT_QOS_UNKNOWN) {
        LOG_ERROR("[PahoMqttClient-%s] Publish, topic error %s", log_tag_.c_str(), topic.c_str());
        return;
    }

    // 上行消息类型：requests, events, services_reply, osd, state, status, set_reply
    // requests, events, services_reply 外部订阅的topic都带了method，但IOT不认识加method的topic，需要移除
    auto pos = real_topic_name.find_last_of("/");
    if (pos != std::string::npos) {
        auto real_topic_tail = real_topic_name.substr(pos + 1);
        if (real_topic_tail != "osd" && real_topic_tail != "state" && 
            real_topic_tail != "status" && real_topic_tail != "set_reply" 
            && real_topic_tail != "up" && real_topic_tail != "down") {
            real_topic_name = real_topic_name.substr(0, pos);
        }
    }
    if (connect_state_ != MQTT_CONNECT_STATUS::CONNECTED) {
        LOG_ERROR("[PahoMqttClient-%s] Failed to sendMessage, topic %s", log_tag_.c_str(), real_topic_name.c_str());
        return;
    }
    LOG_DEBUG("[PahoMqttClient-%s] sendMessage topic: %s, paylod: %s", log_tag_.c_str(), real_topic_name.c_str(), payload.c_str());
    MQTTAsync_message pubmsg = MQTTAsync_message_initializer;
    MQTTAsync_responseOptions opts = MQTTAsync_responseOptions_initializer;

    pubmsg.payload = const_cast<char*>(payload.c_str());
    pubmsg.payloadlen = static_cast<int>(payload.length());
    pubmsg.qos = qos;
    pubmsg.retained = 0;

    int ret = MQTTAsync_sendMessage(client_, real_topic_name.c_str(), &pubmsg, &opts);
    if (ret != 0) {
        LOG_ERROR("[PahoMqttClient-%s] Failed to start sendMessage, return code %d, %s", log_tag_.c_str(), ret, real_topic_name.c_str());
    }
}

MeErrorCode PahoMqttClient::ConnectParamsConfig(const std::string& host, const std::string& username, const std::string& password, const std::string& client_id, const std::string& sn) {
    LOG_INFO("[PahoMqttClient-%s] host:%s", log_tag_.c_str(), host.c_str());
    LOG_INFO("[PahoMqttClient-%s] user_name:%s", log_tag_.c_str(), username.c_str());
    LOG_INFO("[PahoMqttClient-%s] sn:%s", log_tag_.c_str(), sn.c_str());
    LOG_INFO("[PahoMqttClient-%s] password:%s", log_tag_.c_str(), password.empty() ? "empty" : "set");
    LOG_INFO("[PahoMqttClient-%s] client_id:%s", log_tag_.c_str(), client_id.c_str());
    host_ = host;
    product_sn_ = sn;
    user_name_ = username;
    password_ = password;
    client_id_ = client_id;
    return ME_NO_ERROR;
}

}  // namespace message_engine