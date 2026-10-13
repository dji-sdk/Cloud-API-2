// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "BusinessManagerImpl.h"

#include <utility>
#include <cinttypes>

#include "base/common/logger.h"

using namespace notification_center;

BusinessManagerImpl::BusinessManagerImpl(std::weak_ptr<TopicManager> topic_mgr)
    : topic_mgr_(std::move(topic_mgr)) {
    LOG_INFO("[BusinessManagerImpl] ctor");
}

void BusinessManagerImpl::CancelObserve(int64_t listen_id) {
    auto strong_topic_mgr = topic_mgr_.lock();
    if (strong_topic_mgr == nullptr) {
        LOG_INFO("[BusinessManagerImpl] CancelObserve: %" PRId64 ", failed", listen_id);
        return;
    }

    LOG_INFO("[BusinessManagerImpl] CancelObserve: %" PRId64 "", listen_id);

    strong_topic_mgr->CancelObserve(listen_id);
}
