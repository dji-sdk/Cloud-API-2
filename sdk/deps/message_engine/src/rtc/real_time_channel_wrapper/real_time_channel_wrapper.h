// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/i_real_time_channel.h"
#include "api/bridge/i_real_time_channel_bridge.h"
#include "channel.h"
#include "common/util/async_capture_protect.h"
#include <memory>

namespace message_engine {

class RealTimeChannelWrapper : public IRealTimeChannel, 
                               public Channel, 
                               public IChannelDataCallbackBridge, 
                               public IChannelStatusCallbackBridge,
                               public ::drc_sdk::common::AsyncCaptureProtect, 
                               public std::enable_shared_from_this<RealTimeChannelWrapper> {
public:
    RealTimeChannelWrapper(const std::shared_ptr<IRealTimeChannelBridge>& real_time_channel_bridge, 
                           const ChannelInfo& channel_info, 
                           const std::string& token, 
                           const std::shared_ptr<::drc_sdk::Executor> &working_executor);
    ~RealTimeChannelWrapper() override = default;

public:
    // IRealTimeChannel
    void SendBinaryMessage(const std::string &target_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message, bool is_reliable, const SendRealTimeMessageCallback &callback) override;
    void SendStringMessage(const std::string &target_user_id, const std::string &message, bool is_reliable, const SendRealTimeMessageCallback &callback) override;
    void SendBinaryBroadcastMessage(const std::shared_ptr<drc_sdk::ByteBuffer> &message, bool is_reliable, const SendRealTimeMessageCallback &callback) override;
    void SendStringBroadcastMessage(const std::string &message, bool is_reliable, const SendRealTimeMessageCallback &callback) override;

    void SetReceiveBinaryMessageCallback(std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)>&& callback) override;
    void SetReceiveStringMessageCallback(std::function<void(const std::string &source_user_id, const std::string &message)>&& callback) override;

    bool Join(const JoinChannelCallback& callback) override;
    bool Leave() override;
    bool RenewToken(const std::string &token) override;

    // IChannelDataCallbackBridge
    void OnReceiveBinaryMessage(const std::string& target_user_id, const std::vector<uint8_t>& message) override;
    void OnReceiveStringMessage(const std::string& target_user_id, const std::string& message) override;
    void OnReceiveVideoData(const std::vector<uint8_t>& video_data, bool is_key_frame) override;

    // IChannelStatusCallbackBridge
    void OnUsersListUpdate(const std::vector<std::string>& users_list) override;
    void OnTokenPrivilegeWillExpire() override;
    void OnLicenseWillExpireWarning(int left_days) override;
    void OnGlobalError(const std::string& error, const std::string& message) override;

    // IChannel
    // using Channel::SetUsersListUpdateCallback;
    // using Channel::SetTokenPrivilegeWillExpireCallback;
    // using Channel::SetLicenseWillExpireWarningCallback;
    // using Channel::SetGlobalErrorCallback; 
    // using Channel::GetChannelInfo;
    // using Channel::GetToken;
    // using Channel::GetChannelStatus;
    // using Channel::GetUsersList;
    // using Channel::GetChannelLifeCycleInfo;
    void SetUsersListUpdateCallback(std::function<void(std::vector<std::string>users_list)>&& callback) override {
        Channel::SetUsersListUpdateCallback(std::move(callback));
    }

    void SetTokenPrivilegeWillExpireCallback(std::function<void()>&& callback) override {
        Channel::SetTokenPrivilegeWillExpireCallback(std::move(callback));
    }

    void SetLicenseWillExpireWarningCallback(std::function<void(const int &left_days)>&& callback) override {
        Channel::SetLicenseWillExpireWarningCallback(std::move(callback));
    }

    void SetGlobalErrorCallback(std::function<void(const std::string& error, const std::string& message)>&& callback) override {
        Channel::SetGlobalErrorCallback(std::move(callback));
    }

    ChannelInfo GetChannelInfo() const override {
        return Channel::GetChannelInfo();
    }

    std::string GetToken() const override {
        return Channel::GetToken();
    }

    ChannelStatus GetChannelStatus() const override {
        return Channel::GetChannelStatus();
    }

    std::vector<std::string> GetUsersList() const override {
        return Channel::GetUsersList();
    }
    
    ChannelLifeCycleInfo GetChannelLifeCycleInfo() const override {
        return Channel::GetChannelLifeCycleInfo();
    }

protected:
    std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)> receive_binary_message_callback_;
    std::function<void(const std::string &source_user_id, const std::string &message)> receive_string_message_callback_;

private:
    std::shared_ptr<IRealTimeChannelBridge> real_time_channel_bridge_;
};

} // namespace message_engine