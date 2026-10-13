// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "real_time_channel.h"

#include <base/common/logger.h>
#include <utility>

using namespace message_engine;

RealTimeChannel::RealTimeChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<::drc_sdk::Executor> &working_executor) :
    Channel(channel_info, token, working_executor) {
}

void RealTimeChannel::SetReceiveBinaryMessageCallback(std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)>&& callback)  {
    receive_binary_message_callback_ = std::move(callback);
}

void RealTimeChannel::SetReceiveStringMessageCallback(std::function<void(const std::string &source_user_id, const std::string &message)>&& callback)  {
    receive_string_message_callback_ = std::move(callback);
}

void RealTimeChannel::SetObserveCallbackManager(const std::shared_ptr<ObserveCallbackManager>& observe_callback_manager) {
    if (scheduler_ != nullptr) {
        scheduler_->SetObserveCallbackManager(observe_callback_manager);
    } else {
        LOG_ERROR("[RealTimeChannel] SetObserveCallbackManager, scheduler_ is nullptr");
    }
}
