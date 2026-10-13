// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include <cstdint>
#include <functional>
#include <map>
#include <mutex>
#include <atomic>
#include <string>
#include <memory>
#include "TopicMessageWrapper.h"
#include "base/async/timer.h"
#include "websocket/WebSocketHolder.h"
#include "common/util/async_capture_protect.h"

namespace notification_center {
    using PublishTopicCallback = std::function<void(std::shared_ptr<PublishTopicWrapper>)>;
    using SendTopicCallback = std::function<void(int err, std::shared_ptr<TopicMessageWrapper>)>;
    using SubscribeResultCallback = std::function<void(int err)>;

    // TopicManager 内部错误码, 不会对外暴露
    enum TopicManagerErrorCode {
        TopicManager_NoError = 0,
        TopicManager_ErrorNoWebsocket = -1,
        TopicManager_ErrorTimeout = -2,
        TopicManager_ErrorServerError = -3,
        TopicManager_ErrorWebsocketNotOpen = -4,
    };

    // 订阅消息三元组, 每一个 subscribe_id 对应一个三元组
    class SubscribeTopicTriple {
    public:
        std::string sn;
        std::string topic;
        NotificationFrequency freq;

        AIGC_JSON_HELPER(sn, topic, freq);

        bool operator==(const SubscribeTopicTriple &other) const {
            return sn == other.sn && topic == other.topic && freq == other.freq;
        }
    };
}

// std::hash 特化必须在类定义之后, 但在使用该类型的容器之前
namespace std {
    template<>
    struct hash<notification_center::SubscribeTopicTriple> {
        std::size_t operator()(const notification_center::SubscribeTopicTriple &k) const noexcept {
            return std::hash<std::string>()(k.sn) ^ (std::hash<std::string>()(k.topic) << 1) ^ (std::hash<int>()(k.freq) << 2);
        }
    };
}

namespace notification_center {
    // 消息匹配
    class TopicManager : public drc_sdk::common::AsyncCaptureProtect {
    public:
        TopicManager() = default;

        ~TopicManager() override = default;

        void setWebSocketHolder(const std::weak_ptr<WebSocketHolder> &holder);

        int64_t Observe(const std::string& sn, const std::string& topic, NotificationFrequency freq, PublishTopicCallback cb, const SubscribeResultCallback &result_cb = nullptr);

        void CancelObserve(int64_t listen_id);

    private:
        /// websocket
        // websocket callback
        void OnWebSocketMessage(const std::string &json);
        void OnWebsocketOpen();
        void OnWebsocketClose();
        // websocket operator Wrapper
        int SendMessage(const std::shared_ptr<TopicMessageWrapper> &msg, SendTopicCallback cb = nullptr);

        /// notificationcenter server
        int SendSubscribe(int64_t subscribe_id, const SubscribeTopicTriple& triple, const SubscribeResultCallback &result_cb); // 发起订阅请求
        void SendUnsubscribe(int64_t subscribe_id, const SubscribeTopicTriple &topic_tuple); // 取消订阅
        void SendPublishAck(const std::shared_ptr<PublishTopicWrapper>& message); // publish ack 回复确认
        void SendPing(); // 发送 ping 消息

        void OnSubscribe(const std::string &json); // 收到订阅请求的回复
        void OnUnSubscribe(const std::string &json); // 收到取消订阅请求的回复
        void OnPublish(const std::string &json); // 收到 publish 的消息
        void OnPing(const std::string &json); // 收到来自服务器的 ping 消息
        void OnPong(const std::string &json); // 收到自己发送的 ping 消息的 pong 回复

        void RecoverySubscribe(); // 恢复已订阅消息

        drc_sdk::Timer ping_task_timer_;
        drc_sdk::Timer recovery_task_timer_;

        std::atomic<int64_t> next_listen_id_{1};
        std::mutex mtx_;

        // 当前已订阅的监听回调
        std::unordered_map<int64_t, PublishTopicCallback> subscribe_id_to_callback_; // listen_id -> 回调
        std::unordered_map<int64_t, SubscribeTopicTriple> subscribe_id_to_tuple_; // listen_id -> 订阅三元组

        // 待从断连重连中恢复的订阅
        std::unordered_map<int64_t, PublishTopicCallback> recovery_subscribe_id_to_callback_; // listen_id -> 回调
        std::unordered_map<int64_t, SubscribeTopicTriple> recovery_subscribe_id_to_tuple_; // listen_id -> 订阅三元组

        std::unordered_map<std::string, SendTopicCallback> pending_requests_; // uuid -> 回调

        std::weak_ptr<WebSocketHolder> ws_holder_;
    };
}
