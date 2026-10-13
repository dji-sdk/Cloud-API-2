// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "channel.h"
#include <utility>

using namespace message_engine;

Channel::Channel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<::drc_sdk::Executor> &channel_working_executor) :
                channel_info_(channel_info), token_(token),working_executor_(channel_working_executor) {
    life_cycle_info_.create_time_ = std::chrono::steady_clock::now();
}

ChannelInfo Channel::GetChannelInfo() const {
    return channel_info_;
}

std::string Channel::GetToken() const {
    return token_;
}

ChannelStatus Channel::GetChannelStatus() const {
    return status_;
}

std::vector<std::string> Channel::GetUsersList() const {
    return users_list_;
}

ChannelLifeCycleInfo Channel::GetChannelLifeCycleInfo() const {
    return life_cycle_info_;
}

void Channel::SetUsersListUpdateCallback(std::function<void(std::vector<std::string> users_list)>&& callback) {
    callback(users_list_);
    users_list_update_callbacks_.push_back(std::move(callback));
}

void Channel::SetTokenPrivilegeWillExpireCallback(std::function<void()>&& callback)  {
    token_privilege_will_expire_callback_ = std::move(callback);
}

void Channel::SetLicenseWillExpireWarningCallback(std::function<void(const int &left_days)>&& callback)  {
    license_will_expire_warning_callback_ = std::move(callback);
}

void Channel::SetGlobalErrorCallback(std::function<void(const std::string& error, const std::string& message)>&& callback)  {
    global_error_callback_ = std::move(callback);
}

ChannelInfo::ChannelInfo(){}

// For Volc
ChannelInfo::ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& app_id, const std::string& channel_name, const std::string& user_id, const std::string& log_path) :
    type_(type), provider_type_(provider_type), app_id_(app_id), channel_name_(channel_name), room_id_(channel_name), user_id_(user_id), log_path_(log_path) {}

// For Drtc
ChannelInfo::ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& app_id, const std::string& channel_name, const std::string& room_id, const std::string& client_id, const std::string& config_path) :
    type_(type), provider_type_(provider_type), channel_name_(channel_name), app_id_(app_id), room_id_(room_id), client_id_(client_id), config_path_(config_path) {}

// For Agora
ChannelInfo::ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& channel_name, const std::string& user_id, const std::string& log_path) :
    type_(type), provider_type_(provider_type), channel_name_(channel_name), room_id_(channel_name), user_id_(user_id), log_path_(log_path) {}

// For Mrtc
ChannelInfo::ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& channel_name, const std::string& protocol_name, const std::string& url, std::string serverIP, std::string serverPort, std::string serverID, std::string agentID, std::string agentPassword, std::string localPort, std::string channel) :
    type_(type), provider_type_(provider_type), channel_name_(channel_name), protocol_name_(protocol_name), url_(url), serverIP(serverIP), serverPort(serverPort), serverID(serverID), agentID(agentID), agentPassword(agentPassword), localPort(localPort), channel(channel)  {}

// For WebRtc
ChannelInfo::ChannelInfo(const ChannelType& type, const ChannelProviderType& provider_type, const std::string& channel_name, const std::string& url) :
    type_(type), provider_type_(provider_type), channel_name_(channel_name), url_(url) {}