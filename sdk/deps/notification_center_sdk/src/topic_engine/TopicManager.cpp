// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "TopicManager.h"
#include <memory>
#include <string>
#include <utility>
#include <charconv>

#include "Common.h"
#include "base/common/logger.h"
#include "base_utils/utils/json_utils.h"
#include "common/executors/executors.h"


using namespace notification_center;

constexpr auto kWebsocketTimeout = 3000; // websocket 消息超时时间 ms
constexpr auto kWebsocketDevicePingPongInterval = 60000; // websocket 设备 ping 服务器时间 ms
constexpr auto kWebsocketRecoverySubscribeInterval = 5000; // websocket 异常恢复后重连时间 ms

inline bool parse_int64(const std::string& s, std::int64_t& out) {
    const char* begin = s.data();
    const char* end = s.data() + s.size();
    std::from_chars_result res = std::from_chars(begin, end, out);

    if (res.ec == std::errc()) {
        // 成功解析, 但需确保整个字符串都被解析(没有多余字符)
        return (res.ptr == end);
    }
    return false;
}

void TopicManager::setWebSocketHolder(const std::weak_ptr<WebSocketHolder> &holder) {
    ws_holder_ = holder;
    auto strong_ws_holder = ws_holder_.lock();
    if (strong_ws_holder == nullptr) {
        LOG_INFO("[TopicManager] setWebSocketHolder failed");
        return;
    }
    WeakDummy(weak_ptr);
    strong_ws_holder->setOnMessage([weak_ptr, this](const std::string &message)-> void {
        WeakDummyReturn(weak_ptr);
        OnWebSocketMessage(message);
    });
    strong_ws_holder->setOnOpen([weak_ptr, this]()-> void {
        WeakDummyReturn(weak_ptr);
        LOG_INFO("[TopicManager] setWebSocketHolder OnOpen");
        OnWebsocketOpen();
    });
    strong_ws_holder->setOnClose([weak_ptr, this]()-> void {
        WeakDummyReturn(weak_ptr);
        LOG_ERROR("[TopicManager] setWebSocketHolder OnClose");
        OnWebsocketClose();
    });
    strong_ws_holder->setOnError([weak_ptr, this](const std::string& error_msg)-> void {
        WeakDummyReturn(weak_ptr);
        LOG_ERROR("[TopicManager] setWebSocketHolder OnError: %s", error_msg.c_str());
        OnWebsocketClose();
    });
}

void TopicManager::OnWebsocketOpen() {
    WeakDummy(weak_ptr);
    ping_task_timer_.Start(drc_sdk::common::Executors::Main(), [this, weak_ptr]()->void {
        WeakDummyReturn(weak_ptr);
        SendPing();
    }, std::chrono::milliseconds(kWebsocketDevicePingPongInterval), true);

    if (recovery_subscribe_id_to_tuple_.empty()) {
        // 当前没有任何已订阅的消息, 不需要恢复订阅
        return;
    }
    // 重新监听之前已经订阅的消息
    recovery_task_timer_.Start(drc_sdk::common::Executors::Main(), [this, weak_ptr]()->void {
        WeakDummyReturn(weak_ptr);
        RecoverySubscribe();
    }, std::chrono::milliseconds(kWebsocketRecoverySubscribeInterval), true);
}

void TopicManager::RecoverySubscribe() {
    if (recovery_subscribe_id_to_callback_.empty()) {
        // 没有需要异常恢复的订阅
        recovery_task_timer_.Cancel(); // 不再需要重试了
        return;
    }
    LOG_INFO("[TopicManager] RecoverySubscribe, current recovery subscribe size: %d", recovery_subscribe_id_to_tuple_.size());
    // 重新发起订阅
    WeakDummy(weak_ptr);
    for (const auto& [id, triple] : recovery_subscribe_id_to_tuple_) {
        auto ret = SendSubscribe(id, triple, [this, weak_ptr, id](int err)->void {
            WeakDummyReturn(weak_ptr);
            if (err != TopicManager_NoError) {
                // 订阅失败, 无所谓, 会继续重试
                return;
            }
            // 订阅成功, 从 recovery 中移动到已订阅的消息中
            LOG_DEBUG("[TopicManager] RecoverySubscribe, current recovery subscribe success: %d", id);
            {
                std::lock_guard<std::mutex> lock(mtx_);
                subscribe_id_to_callback_[id] = std::move(recovery_subscribe_id_to_callback_[id]);
                subscribe_id_to_tuple_[id] = std::move(recovery_subscribe_id_to_tuple_[id]);
                recovery_subscribe_id_to_callback_.erase(id);
                recovery_subscribe_id_to_tuple_.erase(id);
            }
        });
        if (ret != TopicManager_NoError) {
            // 订阅失败, 无所谓, 会继续重试
        }
    }
}

void TopicManager::OnWebsocketClose() {
    ping_task_timer_.Cancel();
    recovery_task_timer_.Cancel();

    LOG_INFO("[TopicManager] OnWebsocketClose, current subscribe size: %d", subscribe_id_to_tuple_.size());
    for (const auto &[id, triple] : subscribe_id_to_tuple_) {
        LOG_DEBUG("[TopicManager] OnWebsocketClose, current subscribe [%lld] sn: %s, topic: %s, freq: %d", id, triple.sn.c_str(), triple.topic.c_str(), triple.freq);
    }
    LOG_INFO("[TopicManager] OnWebsocketClose, current recovery subscribe size: %d", recovery_subscribe_id_to_tuple_.size());
    for (const auto &[id, triple] : recovery_subscribe_id_to_tuple_) {
        LOG_DEBUG("[TopicManager] OnWebsocketClose, current recovery subscribe [%lld] sn: %s, topic: %s, freq: %d", id, triple.sn.c_str(), triple.topic.c_str(), triple.freq);
    }
    {
        std::lock_guard<std::mutex> lock(mtx_);
        // 追加到 recovery 变量中, 保留原有内容
        recovery_subscribe_id_to_callback_.insert(subscribe_id_to_callback_.begin(), subscribe_id_to_callback_.end());
        recovery_subscribe_id_to_tuple_.insert(subscribe_id_to_tuple_.begin(), subscribe_id_to_tuple_.end());
        subscribe_id_to_callback_.clear();
        subscribe_id_to_tuple_.clear();
    }
}

void TopicManager::SendPing() {
    auto ping_msg = std::make_shared<PingTopicWrapper>();
    auto ret = SendMessage(ping_msg, nullptr);
    if (ret != TopicManager_NoError) {
        LOG_ERROR("[TopicManager] SendPing, ping error: %d", ret);
    }
}

int64_t TopicManager::Observe(const std::string& sn, const std::string& topic, NotificationFrequency freq, PublishTopicCallback cb, const SubscribeResultCallback &result_cb) {
    SubscribeTopicTriple triple{
        .sn = sn,
        .topic = topic,
        .freq = freq,
    };
    int64_t id;
    {
        std::lock_guard<std::mutex> lock(mtx_);
        id = next_listen_id_++;
        subscribe_id_to_callback_[id] = std::move(cb);
        subscribe_id_to_tuple_[id] = triple;
    }

    WeakDummy(weak_ptr);
    auto ret = SendSubscribe(id, triple, [this, weak_ptr, result_cb](int err)->void {
        WeakDummyReturn(weak_ptr);
        // 订阅成功
        if (err == TopicManager_NoError) {
            if (result_cb) {
                result_cb(NotificationCenterErrorCode_NoError);
            }
            return;
        }
        // 订阅失败
        if (result_cb) {
            result_cb(NotificationCenterErrorCode_SubscribeError);
        }
    });
    if (ret != TopicManager_NoError) {
        // 订阅失败, 清理监听回调
        {
            std::lock_guard<std::mutex> lock(mtx_);
            subscribe_id_to_callback_.erase(id);
            subscribe_id_to_tuple_.erase(id);
        }
        return NotificationCenterErrorCode_SendError; // 返回错误ID
    }

    return id;
}

void TopicManager::CancelObserve(int64_t listen_id) {
    SubscribeTopicTriple triple;
    {
        std::lock_guard<std::mutex> lock(mtx_);
        // 修复: 改用 find() 替代 operator[]。
        // operator[] 在 key 不存在时会默认构造并插入 SubscribeTopicTriple{}, 
        // 随后 erase 虽会删除该条目, 但 triple 已是空值(sn="", topic=""), 
        // 导致 SendUnsubscribe 向服务器发送非法的 unsubscribe 消息。
        auto it = subscribe_id_to_tuple_.find(listen_id);
        if (it == subscribe_id_to_tuple_.end()) {
            LOG_WARN("[TopicManager] CancelObserve: listen_id %lld not found, skip", listen_id);
            return;
        }
        triple = it->second;
        subscribe_id_to_callback_.erase(listen_id);
        subscribe_id_to_tuple_.erase(it);
    }
    SendUnsubscribe(listen_id, triple);
}

int TopicManager::SendMessage(const std::shared_ptr<TopicMessageWrapper> &msg, SendTopicCallback cb) {
    // LOG_DEBUG("[TopicManager] SendMessage: %s", msg->ToJsonString().c_str());
    auto strong_ws_holder = ws_holder_.lock();
    if (strong_ws_holder == nullptr) {
        LOG_ERROR("[TopicManager] SendMessage: ws is nullptr");
        return TopicManager_ErrorNoWebsocket;
    }
    auto *ws = strong_ws_holder->getWebSocket();
    if (!ws) {
        return TopicManager_ErrorNoWebsocket;
    }
    // 先检查 WebSocket 是否打开, 避免添加无效的回调
    if (!ws->isOpen()) {
        LOG_ERROR("[TopicManager] SendMessage: ws is not open");
        return TopicManager_ErrorWebsocketNotOpen;
    }

    // 修复: 将外部函数调用 ws->send() 移出 mtx_ 锁范围。
    // 原代码在持 mtx_ 时调用 ws->send(), 而 send() 内部会获取 CppWebSocket::sendMutex, 
    // 形成锁链 mtx_ → sendMutex。当前无反向路径, 不会死锁, 但这是潜在的锁顺序风险, 
    // 一旦未来有代码从 sendMutex 回调到 TopicManager 就会死锁。
    // 修复方案: 锁内仅操作内存结构(pending_requests_ 插入 + 序列化), 锁外再执行 I/O。
    std::string json_to_send;
    {
        std::lock_guard<std::mutex> lock(mtx_);
        if (cb != nullptr && msg->need_replay == true) {
            // 消息发出后需要等待回复
            pending_requests_[msg->message_id] = std::move(cb);
            WeakDummy(weak_ptr);
            drc_sdk::common::Executors::Main()->PostDelay([this, weak_ptr, msg_uuid = msg->message_id]()-> void {
                WeakDummyReturn(weak_ptr);
                // 修复: 改用 find() 替代原来的 contains() + operator[] 两步操作。
                // 原 contains()(无锁检查)+ operator[](无锁写, 且 key 不存在时会默认构造插入)
                // 存在隐式副作用。find() 一步完成查找, 无副作用, 更安全。
                // 注意：所有 TopicManager 回调均在 Main 线程串行执行, 实际无并发风险, 
                // 但统一用 find() 消除隐式插入隐患, 保持与 CancelObserve 的一致性。
                auto it = pending_requests_.find(msg_uuid);
                if (it != pending_requests_.end()) {
                    auto callback = std::move(it->second);
                    pending_requests_.erase(it);
                    callback(TopicManager_ErrorTimeout, nullptr);
                }
            }, std::chrono::milliseconds(kWebsocketTimeout));
        }
        json_to_send = msg->ToJsonString();  // 序列化在锁内完成, msg 只读, 无副作用
    }
    // mtx_ 已释放, 安全调用外部函数, 不再持有任何锁
    ws->send(json_to_send);
    return TopicManager_NoError;
}

void TopicManager::OnWebSocketMessage(const std::string &json) {
    if (isValidJson(json) == false) {
        LOG_ERROR("[TopicManager] OnWebSocketMessage, recv invalid json string: %s", json.c_str());
        return;
    }

    auto msg = std::make_shared<TopicMessageWrapper>();
    msg->FromJsonString(json);

    if (msg->message_type == "subscribe") {
        OnSubscribe(json);
        return;
    }
    if (msg->message_type == "unsubscribe") {
        OnUnSubscribe(json);
        return;
    }
    if (msg->message_type == "ping") {
        OnPing(json);
        return;
    }
    if (msg->message_type == "pong") {
        OnPong(json);
        return;
    }
    if (msg->message_type == "publish") {
        OnPublish(json);
        return;
    }
    LOG_ERROR("[TopicManager] OnWebSocketMessage message_type unknown: %s", msg->message_type.c_str());
}

void TopicManager::OnSubscribe(const std::string &json) {
    // 收到 subscribe 消息, 是来自服务端对 SDK 发起的 subscribe 的回复
    // 匹配之前发出的消息
    auto subscribe_msg = std::make_shared<SubscribeTopicReplyWrapper>();
    subscribe_msg->FromJsonString(json);

    LOG_DEBUG("[TopicManager] OnSubscribe subscribe_msg: %s", subscribe_msg->ToJsonString().c_str());

    if (subscribe_msg->isValid() == false) {
        LOG_ERROR("[TopicManager] OnSubscribe unknown: %s", json.c_str());
        return;
    }
    // 判断订阅是否成功, 现在是只要服务端返回数据就说明订阅成功
    TopicManagerErrorCode error_code = TopicManager_NoError;
    if (subscribe_msg->message_data.code != 0) {
        LOG_ERROR("[TopicManager] OnSubscribe error");
        error_code = TopicManager_ErrorServerError;
    }
    // subscribe 是服务端回复的, 没有人监听, 只会先发起, 然后等待回复
    // 1. 响应回调
    if (pending_requests_.contains(subscribe_msg->message_id)) {
        SendTopicCallback cb;
        {
            std::lock_guard<std::mutex> lock(mtx_);
            cb = pending_requests_[subscribe_msg->message_id];
            pending_requests_.erase(subscribe_msg->message_id);
        }
        cb(error_code, subscribe_msg);
    }
}

void TopicManager::OnUnSubscribe(const std::string &json) {
    // 收到 unsubscribe 消息, 是来自服务端对 SDK 发起的 unsubscribe 的回复
    // 匹配之前发出的消息
    auto unsubscribe_msg = std::make_shared<TopicMessageWrapper>();
    unsubscribe_msg->FromJsonString(json);

    LOG_DEBUG("[TopicManager] OnUnSubscribe unsubscribe_msg: %s", unsubscribe_msg->ToJsonString().c_str());

    if (unsubscribe_msg->isValid() == false) {
        LOG_ERROR("[TopicManager] OnUnSubscribe unknown: %s", json.c_str());
        return;
    }
    // unsubscribe 是服务端回复的, 没有人监听, 只会先发起, 然后等待回复
    // 1. 响应回调
    if (pending_requests_.contains(unsubscribe_msg->message_id)) {
        SendTopicCallback cb; {
            std::lock_guard<std::mutex> lock(mtx_);
            cb = pending_requests_[unsubscribe_msg->message_id];
            pending_requests_.erase(unsubscribe_msg->message_id);
        }
        cb(TopicManager_NoError, unsubscribe_msg);
        return;
    }
    
    // 如果没有找到对应的请求, 记录警告日志
    LOG_WARN("[TopicManager] OnUnSubscribe, no pending request found for message_id: %s", unsubscribe_msg->message_id.c_str());
}

void TopicManager::OnPublish(const std::string &json) {
    auto publish_msg = std::make_shared<PublishTopicWrapper>();
    publish_msg->FromJsonString(json);

    LOG_DEBUG("[TopicManager] OnPublish publish_msg: %s", json.c_str());

    if (publish_msg->isValid() == false) {
        // 收到非法 publish 消息
        LOG_ERROR("[TopicManager] OnPublish unknown: %s", json.c_str());
        return;
    }

    int64_t subscribe_id = -1;
    if (parse_int64(publish_msg->subscribe_id, subscribe_id) == false) {
        LOG_ERROR("[TopicManager] OnPublish unknown subscribe_id: %s (length: %d)", publish_msg->subscribe_id.c_str(), publish_msg->subscribe_id.length());
        return;
    }

    // 修复: 在单次加锁中完成全部验证, 并同步取出回调, 验证全部通过后再发 ACK。
    // 原代码先发 ACK 再验证, 导致对 unknown subscribe_id / topic mismatch 等无效消息
    // 也发出 ACK, 重连期间服务器持续推送旧订阅数据时会产生大量无效 ACK 占用发送队列。
    // 同时原代码对 subscribe_id_to_callback_ 使用 contains()(无锁)+ operator[](有锁), 
    // operator[] 在 key 不存在时会插入默认空值, 改用 find() 消除隐式副作用。
    SubscribeTopicTriple subscribed_triple;
    PublishTopicCallback cb;
    {
        std::lock_guard<std::mutex> lock(mtx_);
        auto it_tuple = subscribe_id_to_tuple_.find(subscribe_id);
        if (it_tuple == subscribe_id_to_tuple_.end()) {
            LOG_WARN("[TopicManager] OnPublish, unknown subscribe_id: %lld", subscribe_id);
            return;
        }
        subscribed_triple = it_tuple->second;

        // 顺带取出回调, 与 tuple 查找合并到同一把锁内, 避免两次加锁的 TOCTOU
        auto it_cb = subscribe_id_to_callback_.find(subscribe_id);
        if (it_cb != subscribe_id_to_callback_.end()) {
            cb = it_cb->second;
        }
    }

    // 验证主题是否匹配
    if (subscribed_triple.sn != publish_msg->device_sn || subscribed_triple.topic != publish_msg->message_topic) {
        LOG_WARN("[TopicManager] OnPublish, topic mismatch: subscribed(sn=%s, topic=%s) vs received(sn=%s, topic=%s)",
            subscribed_triple.sn.c_str(), subscribed_triple.topic.c_str(), publish_msg->device_sn.c_str(), publish_msg->message_topic.c_str());
        return;
    }

    // 所有验证通过后, 再发 ACK
    if (publish_msg->need_replay == true) {
        SendPublishAck(publish_msg);
    }

    // 回调给该 subscribe_id 的监听者
    if (cb) {
        cb(publish_msg);
    }
}

void TopicManager::OnPing(const std::string &json) {
    // 收到服务端发送的 ping, 原样回复一个 pong
    auto ping_msg = std::make_shared<PingTopicWrapper>();
    ping_msg->FromJsonString(json);
    LOG_DEBUG("[TopicManager] OnPing, uuid: %s, ts: %llu", ping_msg->message_id.c_str(), ping_msg->timestamp);

    auto pong_msg = std::make_shared<PongTopicWrapper>(ping_msg);
    auto ret = SendMessage(pong_msg, nullptr);
    if (ret != TopicManager_NoError) {
        LOG_ERROR("[TopicManager] OnPing, pong error: %d", ret);
    }
}

void TopicManager::OnPong(const std::string &json) {
    auto pong_msg = std::make_shared<PongTopicWrapper>(json);
    pong_msg->FromJsonString(json);

    // 验证 pong 消息是否有效
    if (!pong_msg->isValid()) {
        LOG_ERROR("[TopicManager] OnPong, invalid pong message: %s", json.c_str());
        return;
    }

    auto time_now = std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::system_clock::now().time_since_epoch()).count();
    
    // 检查时间戳的有效性, 避免下溢
    if (pong_msg->timestamp > time_now) {
        LOG_WARN("[TopicManager] OnPong, invalid timestamp: pong_ts=%llu, now_ts=%llu", pong_msg->timestamp, time_now);
        return;
    }
    
    auto time_diff = time_now - pong_msg->timestamp;

    LOG_INFO("[TopicManager] OnPong, uuid: %s, rtt: %llu(ms)", pong_msg->message_id.c_str(), time_diff);
}

int TopicManager::SendSubscribe(int64_t subscribe_id, const SubscribeTopicTriple& triple, const SubscribeResultCallback &result_cb) {
    // 发起订阅请求, 可能是首次订阅, 也可能是从断连重连后回复订阅
    LOG_DEBUG("[TopicManager] SendSubscribe, subscribe_id: %lld, sn: %s, topic: %s, freq: %d", subscribe_id, triple.sn.c_str(), triple.topic.c_str(), triple.freq);

    auto subscribe_msg = std::make_shared<SubscribeTopicWrapper>(triple.sn, triple.topic, triple.freq, std::to_string(subscribe_id));

    WeakDummy(weak_ptr);
    auto ret = SendMessage(subscribe_msg,
        [this, weak_ptr, result_cb, subscribe_id, triple](int err, const std::shared_ptr<TopicMessageWrapper> &message)-> void {
            WeakDummyReturn(weak_ptr);
            LOG_DEBUG("[TopicManager] SendSubscribe, sn: %s, topic: %s, error: %d", triple.sn.c_str(), triple.topic.c_str(), err);
            if (result_cb) {
                result_cb(err);
            }
        });
    return ret;
}

void TopicManager::SendUnsubscribe(int64_t subscribe_id, const SubscribeTopicTriple &topic_tuple) {
    auto unsubscribe_msg = std::make_shared<UnSubscribeTopicWrapper>(topic_tuple.sn, topic_tuple.topic, topic_tuple.freq, std::to_string(subscribe_id));
    WeakDummy(weak_ptr);
    auto ret = SendMessage(unsubscribe_msg,
        [this, weak_ptr, topic_tuple](int err, const std::shared_ptr<TopicMessageWrapper> &unsubscribe_reply)-> void {
            WeakDummyReturn(weak_ptr);
            if (err != TopicManager_NoError) {
                // 取消订阅失败就不管了, 就算收到了推送数据, 由于 callback 已经删除, 也没有什么影响
                LOG_ERROR("[TopicManager] SendUnsubscribe [%s - %s] reply error: %d", topic_tuple.sn.c_str(), topic_tuple.topic.c_str(), err);
                return;
            }
            LOG_DEBUG("[TopicManager] SendUnsubscribe [%s - %s] success", topic_tuple.sn.c_str(), topic_tuple.topic.c_str());
    });
    if (ret != TopicManager_NoError) {
        LOG_ERROR("[TopicManager] SendUnsubscribe [%s - %s] error: %d", topic_tuple.sn.c_str(), topic_tuple.topic.c_str(), ret);
    }
}

void TopicManager::SendPublishAck(const std::shared_ptr<PublishTopicWrapper>& message) {
    LOG_DEBUG("[TopicManager] SendPublishAck [%s - %s - %s]", message->message_topic.c_str(), message->device_sn.c_str(), message->message_id.c_str());

    auto publish_ack_msg = std::make_shared<PublishAckTopicWrapper>();
    publish_ack_msg->message_id = message->message_id;

    WeakDummy(weak_ptr);
    auto ret = SendMessage(publish_ack_msg);
    if (ret != TopicManager_NoError) {
        LOG_ERROR("[TopicManager] SendPublishAck [%s - %s - %s] error: %d", message->message_topic.c_str(), message->device_sn.c_str(), message->message_id.c_str(), ret);
    }
}
