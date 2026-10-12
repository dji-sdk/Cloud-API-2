// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "message_callback.h"

using namespace message_engine;

/// SendCallback
SendCallback::SendCallback(MethodType method, std::shared_ptr<drc_sdk::Executor> executor, std::string uuid, MeResponse response)
    : method_(std::move(method)), executor_(std::move(executor)), uuid_(std::move(uuid)), response_(std::move(response)) {}

void SendCallback::Call(const MeErrorCode& error_code, const std::shared_ptr<MessageWrapper>& message) {
    if (called_.exchange(true)) {
        return;
    }
    executor_->Post([cb = response_, error_code, message]() -> void {
        cb(error_code, message);
    });
}

std::string SendCallback::GetUuid() const {
    return uuid_;
}

/// ObserveCallback
ObserveCallback::ObserveCallback(MethodType method, std::shared_ptr<drc_sdk::Executor> executor, ListenIdCode listen_id, MeObserveResults observe_results)
    : method_(std::move(method)), executor_(std::move(executor)), listen_id_(listen_id), observe_result_(std::move(observe_results)) {}

void ObserveCallback::Call(const std::shared_ptr<MessageWrapper>& message) {
    WeakDummy(weak_ptr);
    executor_->Post([this, weak_ptr, message]() -> void {
        WeakDummyReturn(weak_ptr);
        if (observe_result_) {
            observe_result_(message);
        }
    });
}

ListenIdCode ObserveCallback::GetListenId() const {
    return listen_id_;
}

MethodType ObserveCallback::GetMethod() const {
    return method_;
}

/// SendCallbackManager
void SendCallbackManager::AddSendCallback(const std::shared_ptr<SendCallback>& callback) {
    std::lock_guard lock(mutex_);
    send_callbacks_[callback->GetUuid()] = callback;
}

bool SendCallbackManager::IsNeedCallback(const std::string& message_uuid) {
    std::lock_guard lock(mutex_);
    return send_callbacks_.contains(message_uuid);
}

void SendCallbackManager::RemoveSendCallback(const std::string& message_uuid) {
    std::lock_guard lock(mutex_);
    send_callbacks_.erase(message_uuid);
}
std::shared_ptr<SendCallback> SendCallbackManager::GetSendCallback(const std::string& message_uuid) {
    std::lock_guard lock(mutex_);
    if (send_callbacks_.contains(message_uuid)) {
        return send_callbacks_[message_uuid];
    }
    return nullptr;
}

/// ObserveCallbackManager
void ObserveCallbackManager::AddObserveCallback(const std::shared_ptr<ObserveCallback>& callback) {
    std::lock_guard lock(mutex_);
    observe_callbacks_[callback->GetMethod()].emplace_back(callback);
}

void ObserveCallbackManager::RemoveObserveCallback(ListenIdCode listen_id) {
    std::lock_guard lock(mutex_);
    for (auto& [_, callback_list] : observe_callbacks_) {
        callback_list.remove_if([listen_id](const std::shared_ptr<ObserveCallback>& callback) -> bool {
            if (callback->GetListenId() == listen_id) {
                return true;
            }
            return false;
        });
    }
}

std::list<std::shared_ptr<ObserveCallback>> ObserveCallbackManager::GetObserveCallbacks(const MethodType& method) {
    std::lock_guard lock(mutex_);
    return observe_callbacks_[method];
}
