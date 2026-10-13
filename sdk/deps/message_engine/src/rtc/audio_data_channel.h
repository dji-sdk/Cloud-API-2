// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "channel.h"
#include "i_audio_data_channel.h"

// Audio Data Channel的抽象类，抹平自研 RTC SDK 和 三方SDK（例如火山）的接口区别
// 可以比较方便的通过工厂方法来创建对应的实例
namespace message_engine {

class AudioDataChannel : public IAudioDataChannel, public Channel {
public:
    explicit AudioDataChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<::drc_sdk::Executor> &working_executor);
    AudioDataChannel() = default;
    ~AudioDataChannel() override {};

public:
    void SetAudioDataCallback(std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)>&& callback) override;

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
    std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)> audio_data_callback_;
};
} // namespace message_engine
