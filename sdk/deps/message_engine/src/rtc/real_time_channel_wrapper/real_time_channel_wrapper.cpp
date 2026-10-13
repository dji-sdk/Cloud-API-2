// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "real_time_channel_wrapper.h"

using namespace message_engine;

RealTimeChannelWrapper::RealTimeChannelWrapper(const std::shared_ptr<IRealTimeChannelBridge>& real_time_channel_bridge, const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<::drc_sdk::Executor> &working_executor)
    : real_time_channel_bridge_(real_time_channel_bridge),
      Channel(channel_info, token, working_executor) {
}

void RealTimeChannelWrapper::SendBinaryMessage(const std::string &target_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message, bool is_reliable, const SendRealTimeMessageCallback &callback) {
    auto ret = real_time_channel_bridge_->SendMessage(target_user_id, std::vector<uint8_t>(message->data(), message->data() + message->size()), is_reliable);
    if (ret) {
        callback(true, 0, "");
    } else {
        callback(false, -1, "send message failed");
    }
}

void RealTimeChannelWrapper::SendStringMessage(const std::string &target_user_id, const std::string &message, bool is_reliable, const SendRealTimeMessageCallback &callback) {
    auto ret = real_time_channel_bridge_->SendMessage(target_user_id, std::vector<uint8_t>(message.begin(), message.end()), is_reliable);
    if (ret) {
        callback(true, 0, "");
    } else {
        callback(false, -1, "send message failed");
    }
}

void RealTimeChannelWrapper::SendBinaryBroadcastMessage(const std::shared_ptr<drc_sdk::ByteBuffer> &message, bool is_reliable, const SendRealTimeMessageCallback &callback) {
    auto ret = real_time_channel_bridge_->SendMessage("", std::vector<uint8_t>(message->data(), message->data() + message->size()), is_reliable);
    if (ret) {
        callback(true, 0, "");
    } else {
        callback(false, -1, "send message failed");
    }
}

void RealTimeChannelWrapper::SendStringBroadcastMessage(const std::string &message, bool is_reliable, const SendRealTimeMessageCallback &callback) {
    auto ret = real_time_channel_bridge_->SendMessage("", std::vector<uint8_t>(message.begin(), message.end()), is_reliable);
    if (ret) {
        callback(true, 0, "");
    } else {
        callback(false, -1, "send message failed");
    }
}

void RealTimeChannelWrapper::SetReceiveBinaryMessageCallback(std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)>&& callback) {
    receive_binary_message_callback_ = std::move(callback);
}

void RealTimeChannelWrapper::SetReceiveStringMessageCallback(std::function<void(const std::string &source_user_id, const std::string &message)>&& callback) {
    receive_string_message_callback_ = std::move(callback);
}

bool RealTimeChannelWrapper::Join(const JoinChannelCallback& callback) {
    real_time_channel_bridge_->SetChannelStatusCallback(shared_from_this());
    real_time_channel_bridge_->SetReceiveMessageCallback(shared_from_this());
    WeakDummy(p);
    ChannelInfoBridge channel_info_bridge;
    channel_info_bridge.provider_type = channel_info_.provider_type_;
    channel_info_bridge.app_id = channel_info_.app_id_;
    channel_info_bridge.channel_name = channel_info_.channel_name_;
    channel_info_bridge.room_id = channel_info_.room_id_.empty() ? channel_info_.channel_name_ : channel_info_.room_id_;
    channel_info_bridge.user_id = channel_info_.user_id_;
    channel_info_bridge.client_id = channel_info_.client_id_;
    channel_info_bridge.log_path = channel_info_.log_path_;
    channel_info_bridge.payload_index = channel_info_.payload_index_;
    channel_info_bridge.country_code = channel_info_.country_code_;
    channel_info_bridge.url = channel_info_.url_;
    channel_info_bridge.token = token_;
    return real_time_channel_bridge_->Join(channel_info_bridge, [callback, p, this]
        (bool is_succeed) {
        WeakDummyReturn(p);
        callback(is_succeed, 0, false, 0, "");
    });
}

bool RealTimeChannelWrapper::Leave() {
    return real_time_channel_bridge_->Leave();
}

bool RealTimeChannelWrapper::RenewToken(const std::string &token) {
    return real_time_channel_bridge_->RenewToken(token);
}

void RealTimeChannelWrapper::OnReceiveBinaryMessage(const std::string& target_user_id, const std::vector<uint8_t>& message) {
    if (receive_binary_message_callback_) {
        receive_binary_message_callback_(target_user_id, std::make_shared<drc_sdk::ByteBuffer>(message.data(), message.size()));
    }
}

void RealTimeChannelWrapper::OnReceiveStringMessage(const std::string& target_user_id, const std::string& message) {
    if (receive_string_message_callback_) {
        receive_string_message_callback_(target_user_id, message);
    }
}

void RealTimeChannelWrapper::OnReceiveVideoData(const std::vector<uint8_t>& video_data, bool is_key_frame) {}

void RealTimeChannelWrapper::OnUsersListUpdate(const std::vector<std::string>& users_list) {
    decltype(users_list_update_callbacks_) users_list_update_callbacks_tmp;
    {
        std::lock_guard<std::mutex> lock(users_list_update_callbacks_mutex_);
        users_list_update_callbacks_tmp = users_list_update_callbacks_;
    }
    for (auto& callback : users_list_update_callbacks_tmp) {
        callback(users_list);
    }
}

void RealTimeChannelWrapper::OnTokenPrivilegeWillExpire() {
    if (token_privilege_will_expire_callback_) {
        token_privilege_will_expire_callback_();
    }
}

void RealTimeChannelWrapper::OnLicenseWillExpireWarning(int left_days) {
    if (license_will_expire_warning_callback_) {
        license_will_expire_warning_callback_(left_days);
    }
}

void RealTimeChannelWrapper::OnGlobalError(const std::string& error, const std::string& message) {
    if (global_error_callback_) {
        global_error_callback_(error, message);
    }
}