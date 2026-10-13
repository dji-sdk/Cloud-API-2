// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <base/async/executor.h>
#include <common/util/async_capture_protect.h>
#include "api/i_mqtt_client.h"
#include "i_transport.h"
#include "message_scheduler.h"

namespace message_engine {

typedef enum {
    DJI_MQTT_QOS_0 = 0,        /**< Same as MQTT QoS0, at most once */
    DJI_MQTT_QOS_1 = 1,        /**< Same as MQTT QoS1, at least once */
    DJI_MQTT_QOS_2 = 2,        /**< Same as MQTT QoS2, exactly once */
    DJI_MQTT_QOS_ANY = 3,      /**< Match all of the QoS, maybe used when subscribe topic */
    DJI_MQTT_QOS_UNKNOWN = 0xFF
} MQTTQos;

class MqttClient : public IMqttClient, public ITransport, public MessageSchedulerListener, public drc_sdk::common::AsyncCaptureProtect, public std::enable_shared_from_this<MqttClient> {
public:
    explicit MqttClient(const std::shared_ptr<drc_sdk::Executor>& executor, std::unordered_map<std::string, std::string> topic_method_map, 
                           const std::vector<TopicInfo>& topics_pub, const std::vector<TopicInfo>& topics_sub, std::string log_tag);

    MqttClient() = delete;
    ~MqttClient() override;

public:
    /// ITransport
    MeErrorCode Send(const std::shared_ptr<MessageWrapper>& message) override;
    MeErrorCode Send(const std::shared_ptr<MessageWrapper>& message, const MeResponse& response, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) override;
    ListenIdCode Observe(const std::shared_ptr<ObserveCallback>& callback) override;
    void CancelObserve(const ListenIdCode& listen_id) override;

    /// MessageSchedulerListener
    void OnMessageNeedToBeSent(const std::shared_ptr<MessageWrapper> &message) override;
    void OnSentV1Message(const std::shared_ptr<MessageWrapper>& message);
    void OnSentV2Message(const std::shared_ptr<MessageWrapper>& message);
    void OnSentV3Message(const std::shared_ptr<MessageWrapper>& message);
    void MessageCallback(const std::string& topic, const std::string& data);
    
public:
    // IMqttClient
    virtual MeErrorCode SetUp() override;
    virtual MeErrorCode CleanUp() override;
    virtual MeErrorCode ConnectParamsConfig(const std::string& host, const std::string& username, const std::string& password, const std::string& client_id, const std::string& sn) override;
    virtual uint32_t AddConnectStateCallback(const MqttConnectStateCallback& callback) override;
    virtual void RemoveConnectStateCallback(uint32_t listen_id) override;

    // Enable Multi Mqtt Client
    virtual void MultiPathParamsConfig(const MqttMultiPathParams& params) override {};
    virtual void UpdateBestLink(std::string interface_name) override {};
    virtual void UpdateInterfaceMap(std::unordered_map<std::string, std::string> interface_map) override {};

public:
    virtual void TopicRegister() = 0;
    virtual void Publish(const std::string &topic, const std::string &payload) = 0;
    // void SetSSLConfig(const SSLConfig& ssl_config);
    void CloudConnectStateCallback(int state);

    void SetObserveCallbackManager(const std::shared_ptr<ObserveCallbackManager>& observe_callback_manager);
public:
    std::string product_sn_;
    std::string user_name_;
    std::string password_;
    std::string client_id_;
    std::string host_;
    std::string log_tag_;
    std::shared_ptr<MessageScheduler> scheduler_ = nullptr;
    std::shared_ptr<drc_sdk::Executor> executor_ = nullptr;
    std::unordered_map<std::string, std::string> topic_method_map_;
    std::vector<TopicInfo> publish_topics_;
    std::vector<TopicInfo> subscribe_topics_;
    std::unordered_map<uint32_t, MqttConnectStateCallback> connect_state_callbacks_;
    MQTT_CONNECT_STATUS connect_state_ = MQTT_CONNECT_STATUS::IDLE;

private:
    void OnReceivedMessage(const std::string& topic, const std::string& data);
    uint32_t GenerateListenId() {
        static std::atomic<uint32_t> id = 0;
        return id++;
    }
};

}  // namespace message_engine