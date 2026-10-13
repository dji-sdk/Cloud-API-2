// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <queue>
#include <message_wrapper.h>

namespace message_engine {

struct MessageQueueSortingRule {
    bool operator()(const std::shared_ptr<MessageWrapper>& a, const std::shared_ptr<MessageWrapper>& b) const {
        //返回false时，a的优先级高于b的优先级（a排在b的前面）
        if (a->next_send_time < b->next_send_time) {
            return false;
        }
        if (a->priority > b->priority) {
            return false;
        }
        return true;
    }
};

class RemovablePriorityQueue : public std::priority_queue<std::shared_ptr<MessageWrapper>, std::vector<std::shared_ptr<MessageWrapper>>, MessageQueueSortingRule> {
public:
    bool remove(const std::string& uuid) {
        auto it = std::find_if(this->c.begin(), this->c.end(), [uuid](const std::shared_ptr<MessageWrapper>& item)->bool {
            if (item->GetUuid() == uuid) {
                return true;
            }
            return false;
        });

        if (it == this->c.end()) {
            return false;
        }
        if (it == this->c.begin()) {
            // deque the top element
            this->pop();
        } else {
            // remove element and re-heap
            this->c.erase(it);
            std::make_heap(this->c.begin(), this->c.end(), this->comp);
        }
        return true;
    }
};

} // namespace message_engine