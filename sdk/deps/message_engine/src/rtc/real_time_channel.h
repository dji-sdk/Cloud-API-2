// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "channel.h"
#include "i_real_time_channel.h"
#include "transport/i_transport.h"

#include <message_scheduler.h>

// RealTime Channel的抽象类，抹平自研 RTC SDK 和 三方SDK（例如火山）的接口区别
// 可以比较方便的通过工厂方法来创建对应的实例
namespace message_engine {

using SendRealTimeMessageCallback = std::function<void(const bool &is_successed, const int &error_code, const std::string &extencontent)>;

class RealTimeChannel : public IRealTimeChannel, public ITransport , public Channel {
public:
    explicit RealTimeChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<::drc_sdk::Executor> &working_executor);
    RealTimeChannel() = default;
    ~RealTimeChannel() override {};

public:
    virtual void SendBinaryMessage(const std::string &target_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message, bool is_reliable, const SendRealTimeMessageCallback &callback) override = 0;
    virtual void SendStringMessage(const std::string &target_user_id, const std::string &message, bool is_reliable, const SendRealTimeMessageCallback &callback) override = 0;
    virtual void SendBinaryBroadcastMessage(const std::shared_ptr<drc_sdk::ByteBuffer> &message, bool is_reliable, const SendRealTimeMessageCallback &callback) override = 0;
    virtual void SendStringBroadcastMessage(const std::string &message, bool is_reliable, const SendRealTimeMessageCallback &callback) override = 0;

    void SetReceiveBinaryMessageCallback(std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)>&& callback) override;
    void SetReceiveStringMessageCallback(std::function<void(const std::string &source_user_id, const std::string &message)>&& callback) override;

    // MessageScheduler SetObserveCallbackManager
    void SetObserveCallbackManager(const std::shared_ptr<ObserveCallbackManager>& observe_callback_manager);

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
private:
    bool Join(const JoinChannelCallback& callback) override {return false;};
    bool Leave() override {return false;};
    bool RenewToken(const std::string &token) override{return false;};

protected:
    std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)> receive_binary_message_callback_;
    std::function<void(const std::string &source_user_id, const std::string &message)> receive_string_message_callback_;
    /// scheduler
    std::shared_ptr<MessageScheduler> scheduler_ = nullptr;
};
} // namespace message_engine
