// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "message_scheduler.h"

#include <base/common/logger.h>

using namespace message_engine;

MessageScheduler::MessageScheduler(const std::shared_ptr<drc_sdk::Executor>& callback_executor, const std::string& log_tag) {
    // Linux的pthread库限制线程名称的长度为16个字符，包括终止的空字符。还要给消息队列的日志预留2字节，因此传入的log_tag不能超过13字节
    if (log_tag.size() > 13) {
        LOG_INFO("[MessageScheduler-%s] MessageScheduler, log_tag too long, shrink to 13 words", log_tag_.c_str());
        log_tag_ = log_tag.substr(0, 13);
    } else {
        log_tag_ = log_tag;
    }
    // 从用户线程调用
    LOG_INFO("[MessageScheduler-%s] MessageScheduler", log_tag_.c_str());
    callback_executor_ = callback_executor;
    send_callback_manager_ = std::make_unique<SendCallbackManager>();

    send_message_queue_ = std::make_shared<MessageQueue>();
#ifdef __EMSCRIPTEN__
    send_thread_ = drc_sdk::Executor::CreateEmscriptenEventExecutor(log_tag_ + "-Q");
#else
    send_thread_ = drc_sdk::Executor::CreateSingleThreadExecutor(log_tag_ + "-Q");
#endif
}

void MessageScheduler::AddListener(const std::shared_ptr<MessageSchedulerListener>& listener) {
    listener_ = listener;
}

MessageScheduler::~MessageScheduler() {
    LOG_INFO("[MessageScheduler-%s] ~MessageScheduler", log_tag_.c_str());
    Stop();
}

bool MessageScheduler::Start() {
    if (is_running_) {
        LOG_WARN("[MessageScheduler-%s] Already Start", log_tag_.c_str());
        return false;
    }
    // 从用户线程调用
    LOG_INFO("[MessageScheduler-%s] Start", log_tag_.c_str());

    is_running_ = true;
    
    // 启动消息发送队列
    WeakDummy(weak_ptr);
    send_thread_->Post([this, weak_ptr]() -> void {
        WeakDummyReturn(weak_ptr);
        SendQueueRunLoop();
    });

    LOG_INFO("[MessageScheduler-%s] Start done", log_tag_.c_str());
    return true;
}

bool MessageScheduler::Stop() {
    if (is_running_ == false) {
        LOG_WARN("[MessageScheduler-%s] Already Stop", log_tag_.c_str());
        return false;
    }
    LOG_INFO("[MessageScheduler-%s] Stop", log_tag_.c_str());
    is_running_ = false;
#ifndef __EMSCRIPTEN__
    message_cv_.notify_all();
#endif
    send_thread_->Stop();
    return true;
}

void MessageScheduler::SendQueueRunLoop() {
    if (is_running_ == false) {
        LOG_INFO("[MessageScheduler-%s] SendQueueRunLoop stop running", log_tag_.c_str());
        return;
    }

#ifdef __EMSCRIPTEN__
    // Emscripten 单线程：非阻塞轮询，通过 emscripten_async_call 调度
    auto now = std::chrono::steady_clock::now();
    while (send_message_queue_->NextExecuteTime() <= now && !send_message_queue_->IsEmpty()) {
        auto msg = send_message_queue_->Pull();
        if (msg) {
            OnMessageQueueFired(std::dynamic_pointer_cast<MessageWrapper>(msg));
        }
        now = std::chrono::steady_clock::now();
    }
    if (!send_message_queue_->IsEmpty()) {
        auto next_time = send_message_queue_->NextExecuteTime();
        send_thread_->PostAtTime([this]() -> void { SendQueueRunLoop(); }, next_time);
    }
#else
    // 原生多线程：阻塞等待 condition_variable
    auto&& target_execute_time = send_message_queue_->NextExecuteTime();
    if (target_execute_time > std::chrono::steady_clock::now()) {
        std::unique_lock<std::mutex> lock(queue_mutex_);
        message_cv_.wait_until(lock, target_execute_time);
    }

    if (is_running_ == false) {
        LOG_INFO("[MessageScheduler-%s] SendQueueRunLoop stop running", log_tag_.c_str());
        return;
    }

    if (send_message_queue_->NextExecuteTime() <= std::chrono::steady_clock::now()) {
        auto msg = send_message_queue_->Pull();
        OnMessageQueueFired(std::dynamic_pointer_cast<MessageWrapper>(msg));
    }
    // 避免递归
    send_thread_->Post([this]() -> void { SendQueueRunLoop(); });
#endif
}

MeErrorCode MessageScheduler::ProcessSendMessage(const std::shared_ptr<MessageWrapper>& message) {
    if (message == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    send_message_queue_->Push(message);
    ScheduleSendQueue();
    return MeErrorCode::ME_NO_ERROR;
}

MeErrorCode MessageScheduler::ProcessSendMessage(const std::shared_ptr<MessageWrapper>& message, const MeResponse& response, const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    if (message == nullptr) {
        return MeErrorCode::ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE;
    }
    auto executor = user_define_executor;
    if (executor == nullptr) {
        executor = callback_executor_;
    }
    std::shared_ptr<SendCallback> send_ballback = std::make_shared<SendCallback>(message->GetMethod(), executor, message->GetUuid(), response);
    send_callback_manager_->AddSendCallback(send_ballback);
    send_message_queue_->Push(message);
    ScheduleSendQueue();
    return MeErrorCode::ME_NO_ERROR;
}

ListenIdCode MessageScheduler::AddObserve(const std::shared_ptr<ObserveCallback>& callback) {
    // 目前监听的注册，都放在全局的ObserveCallbackManager中，后续如果需要使用内部的mgr，可以再修复
    // ObserveCallbackManager::GetInstance().AddObserveCallback(callback);
    return callback->GetListenId();
}

void MessageScheduler::CancelObserve(const ListenIdCode& listen_id) {
    // ObserveCallbackManager::GetInstance().RemoveObserveCallback(listen_id);
    if (observe_callback_manager_) {
        observe_callback_manager_->RemoveObserveCallback(listen_id);
    }
}

void MessageScheduler::OnMessageQueueFired(const std::shared_ptr<MessageWrapper>& message) {
    if (message == nullptr) {
        return;
    }
    LOG_DEBUG("[MessageScheduler-%s] OnMessageQueueFired, method: %s, uuid: %s, remain_times: %d", log_tag_.c_str(), message->GetMethod().c_str(), message->GetUuid().c_str(), message->remaining_send_times_);
    if (message->remaining_send_times_ <= 0) {
        // 消息重试次数用尽，但还在队列，说明消息超时，找对应callback回调
        bool is_need_callback = send_callback_manager_->IsNeedCallback(message->GetUuid());
        if (is_need_callback == false) {
            return;
        }

        auto callback = send_callback_manager_->GetSendCallback(message->GetUuid());
        if (callback) {
            callback->Call(MeErrorCode::ERROR_SCHEDULER_WAIT_RESPONSE_TIME_OUT, nullptr);
        }
        send_callback_manager_->RemoveSendCallback(message->GetUuid());
        return;
    }
    // 通知发送
    WeakDummy(weak_ptr);
    if (auto listener = listener_.lock()) {
        callback_executor_->Post([this, weak_ptr, listener, message]() -> void {
            WeakDummyReturn(weak_ptr);
            listener->OnMessageNeedToBeSent(message);
        });
    }

    // 消息超时/或为第一次发送，但消息还有剩余重试次数，一边继续丢回队列中重试，一边继续发送
    message->remaining_send_times_--;
    message->next_send_time = std::chrono::steady_clock::now() + std::chrono::milliseconds(message->send_interval_millisecond);
    send_message_queue_->Push(message);
    ScheduleSendQueue();
}

// 1.observe 2.response
void MessageScheduler::ProcessReceivedMessage(const std::shared_ptr<MessageWrapper>& message) {
    // LOG_DEBUG("[MessageScheduler-%s] ProcessReceivedMessage, method: %s, uuid: %s", log_tag_.c_str(), message->GetMethod().c_str(), message->GetUuid().c_str());
    if (message == nullptr) {
        return;
    }

    // 1.response
    auto is_send_response = send_callback_manager_->IsNeedCallback(message->GetUuid());
    if (is_send_response) {
        // 发送消息的回包，仅回调给发送者
        auto callback = send_callback_manager_->GetSendCallback(message->GetUuid());
        if (callback != nullptr) {
            callback->Call(MeErrorCode::ME_NO_ERROR, message);
        }
        // 收到一条消息，如果发送队列中有这个uuid的消息，那就不必再重试发送了
        send_message_queue_->Remove(message->GetUuid());
        send_callback_manager_->RemoveSendCallback(message->GetUuid());
        return;
    }

    // 2.observe
    // std::list<std::shared_ptr<ObserveCallback>> callbacks = ObserveCallbackManager::GetInstance().GetObserveCallbacks(message->GetMethod());
    // LOG_INFO("[MessageScheduler-%s] Global ProcessReceivedMessage, method: %s, callbacks size: %d", log_tag_.c_str(), message->GetMethod().c_str(), callbacks.size());
    // for (const auto& callback : callbacks) {
    //     if (callback != nullptr) {
    //         LOG_INFO("[MessageScheduler-%s] Global ProcessReceivedMessage, method: %s, callback id: %d", log_tag_.c_str(), message->GetMethod().c_str(), callback->GetListenId());
    //         callback->Call(message);
    //     }
    // }

    if (observe_callback_manager_) {
        std::list<std::shared_ptr<ObserveCallback>> callbacks = observe_callback_manager_->GetObserveCallbacks(message->GetMethod());
        // LOG_DEBUG("[MessageScheduler-%s] Inner ProcessReceivedMessage, method: %s, callbacks size: %d", log_tag_.c_str(), message->GetMethod().c_str(), callbacks.size());
        for (const auto& callback : callbacks) {
            if (callback != nullptr) {
                // LOG_DEBUG("[MessageScheduler-%s] Inner ProcessReceivedMessage, method: %s, callback id: %d", log_tag_.c_str(), message->GetMethod().c_str(), callback->GetListenId());
                callback->Call(message);
            }
        }
    } else {
        LOG_ERROR("[MessageScheduler-%s] ProcessReceivedMessage, observe_callback_manager_ is nullptr", log_tag_.c_str());
    }
}

void MessageScheduler::SetObserveCallbackManager(const std::shared_ptr<ObserveCallbackManager>& observe_callback_manager) {
    observe_callback_manager_ = observe_callback_manager;
}

void MessageScheduler::ScheduleSendQueue() {
#ifdef __EMSCRIPTEN__
    // Emscripten：通过 Post 触发 SendQueueRunLoop
    send_thread_->Post([this]() -> void { SendQueueRunLoop(); });
#else
    message_cv_.notify_one();
#endif
}

