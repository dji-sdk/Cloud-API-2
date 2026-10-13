// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <base/async/executor.h>
#include <common/util/async_capture_protect.h>
#include <i_transport.h>
#include <message_callback.h>
#include <message_queue.h>
#include <condition_variable>

namespace message_engine {

/* MessageScheduler:
 * 消息发送队列+消息匹配 + 消息分发（reponse、observe）
 * 对于每一条发送的MessageWrapper消息，放入一个优先队列中，根据时间戳和优先级进行排序，带有重试机制
 * 对于收到的消息，匹配对应的callback进行回调
 */
class MessageSchedulerListener {
public:
    virtual ~MessageSchedulerListener() = default;
    virtual void OnMessageNeedToBeSent(const std::shared_ptr<MessageWrapper>& message) = 0;
};

class MessageScheduler : public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit MessageScheduler(const std::shared_ptr<drc_sdk::Executor>& callback_executor, const std::string& log_tag);
    ~MessageScheduler() override;

public:
    bool Start();
    bool Stop();
    void AddListener(const std::shared_ptr<MessageSchedulerListener>& listener);  // Transport调用此接口注册监听，当消息队列中有需要发送的消息时，回调（发包能力在Transport）

public:
    MeErrorCode ProcessSendMessage(const std::shared_ptr<MessageWrapper>& message);  // 用户发送消息，不关心回复
    MeErrorCode ProcessSendMessage(const std::shared_ptr<MessageWrapper>& message, const MeResponse& response, const std::shared_ptr<drc_sdk::Executor>& user_define_executor = nullptr);  // 用户发送消息，需要对端回复
    ListenIdCode AddObserve(const std::shared_ptr<ObserveCallback>& callback);
    void CancelObserve(const ListenIdCode& listen_id);
    void ProcessReceivedMessage(const std::shared_ptr<MessageWrapper>& message);   // tranport 收到消息后，传入scheduler，scheduler完成消息匹配后回调用户
    void SetObserveCallbackManager(const std::shared_ptr<ObserveCallbackManager>& observe_callback_manager);
private:
    /// 消息等待发送队列
    void SendQueueRunLoop();
    /// 消息处理
    void OnMessageQueueFired(const std::shared_ptr<MessageWrapper>& message);
    /// 通知发送队列有新消息（Emscripten 用 Post，原生用 condition_variable）
    void ScheduleSendQueue();

private:
    std::string log_tag_;  // 日志tag，因为有多个Transport，每个scheduler运行在Transport下
    std::atomic<bool> is_running_ = false;

    /// 消息发送队列
    std::condition_variable message_cv_;
    std::mutex queue_mutex_;
    std::shared_ptr<MessageQueue> send_message_queue_;  // 消息等待发送队列，用户发送的消息先丢到这个队列中进行优先级排序，按顺序消费
    std::shared_ptr<drc_sdk::Executor> send_thread_;// 消息等待发送队列线程，内部线程

    /// 管理用户传入的 callback
    std::unique_ptr<SendCallbackManager> send_callback_manager_;
    std::shared_ptr<ObserveCallbackManager> observe_callback_manager_ = nullptr;
    std::shared_ptr<drc_sdk::Executor> callback_executor_; // 回调用户线程使用的executor，用户调用Send/Observe接口时如果未传入executor，则默认用这个executor

    /// Transport 注册的监听，当发送队列的消息需要发送时，回调给此监听者
    std::weak_ptr<MessageSchedulerListener> listener_;
};

} // namespace message_engine