// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <removable_priority_queue.h>
#include <mutex>
#include <message_wrapper.h>

namespace message_engine {

class MessageQueue {
public:
    void Push(const std::shared_ptr<MessageWrapper>& msg);
    std::shared_ptr<MessageWrapper> Pull();
    std::chrono::steady_clock::time_point NextExecuteTime();
    bool IsEmpty();
    void Remove(const std::string& uuid);

private:

    std::mutex send_queue_mutex_;
    RemovablePriorityQueue send_queue_;
};

} // namespace message_engine
