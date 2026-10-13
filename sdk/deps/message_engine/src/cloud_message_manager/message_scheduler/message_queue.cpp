// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "message_queue.h"

using namespace message_engine;

void MessageQueue::Push(const std::shared_ptr<MessageWrapper>& msg) {
    std::lock_guard<std::mutex> lk(send_queue_mutex_);
    send_queue_.push(msg);
}

std::shared_ptr<MessageWrapper> MessageQueue::Pull() {
    std::lock_guard<std::mutex> lk(send_queue_mutex_);
    if (send_queue_.empty()) {
        return nullptr;
    }
    const auto msg = send_queue_.top();
    send_queue_.pop();
    return msg;
}

std::chrono::steady_clock::time_point MessageQueue::NextExecuteTime() {
    std::lock_guard<std::mutex> lk(send_queue_mutex_);
    if (send_queue_.empty()) {
        return std::chrono::steady_clock::time_point::max();
    }
    const auto msg = send_queue_.top()->next_send_time;
    return msg;
}

bool MessageQueue::IsEmpty() {
    std::lock_guard<std::mutex> lk(send_queue_mutex_);
    return send_queue_.empty();
}

void MessageQueue::Remove(const std::string& uuid) {
    std::lock_guard<std::mutex> lk(send_queue_mutex_);
    send_queue_.remove(uuid);
}
