// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "i_live_stream_channel.h"
#include "channel.h"

// Livestream Channel的抽象类，抹平自研 RTC SDK 和 三方SDK（例如火山）的接口区别
// 可以比较方便的通过工厂方法来创建对应的实例

namespace message_engine {
        class LiveStreamChannel : public ILiveStreamChannel, public Channel {
        public:
            explicit LiveStreamChannel(const ChannelInfo& channel_info, const std::string &token, const std::shared_ptr<::drc_sdk::Executor> &working_executor);

            LiveStreamChannel() = default;
            ~LiveStreamChannel() override = default;

        public:
            void SetReceiveVideoDataCallback(std::function<void(const std::shared_ptr<drc_sdk::ByteBuffer> &data, bool is_key_frame)> callback) override;
            void SendVideoData(const std::shared_ptr<drc_sdk::ByteBuffer> &data, const VideoFrameInfo &info) override;
            void SendAudioData(const std::shared_ptr<drc_sdk::ByteBuffer> &data, const AudioFrameInfo &info) override;
            void SetNeedSendKeyFrameRequestCallback(std::function<void(const std::string &user_id, bool send_key_frame)> callback) override;
            void SetNeedChangeBitrateRequestCallback(std::function<void(const uint32_t &bit_rate)> callback) override;
            void SetUserMuteVideoCallback(std::function<void(const std::string &user_id, const int &muted)> callback) override;
            void SetUserMuteAudioCallback(std::function<void(const std::string &user_id, const int &muted)> callback) override;

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
            bool Join(const JoinChannelCallback &callback) override {return false;};
            bool Leave() override {return false;};
            bool RenewToken(const std::string &token) override {return false;};

        protected:
            std::function<void(const std::string &user_id, bool send_key_frame)> need_send_key_frame_callback_;
            std::function<void(const uint32_t &bit_rate)> need_change_bit_rate_callback_;

            std::function<void(const std::string &user_id, const int &muted)> user_mute_video_callback_;
            std::function<void(const std::string &user_id, const int &muted)> user_mute_audio_callback_;

            std::function<void(const std::shared_ptr<drc_sdk::ByteBuffer> &data, bool is_key_frame)> receive_video_data_callback_;
        };
} // namespace message_engine
