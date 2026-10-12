// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "rtc_channel_manager.h"

#include <transport_manager.h>

#include "base/common/logger.h"
#include "channel_factory.h"

using namespace message_engine;

RtcChannelManager::RtcChannelManager(const std::shared_ptr<drc_sdk::Executor>& executor, const std::shared_ptr<TransportManager>& transport_manager) : channel_working_executor_(executor), transport_manager_(transport_manager) {
    LOG_INFO("[MessageEngine][RtcChannelManager] ctor");
}

RtcChannelManager::~RtcChannelManager() {
    RemoveAllChannels();
}

bool RtcChannelManager::AddChannel(const ChannelInfo &channel_info, const std::string &token, AddChannelCompletion&& completion, const std::string& transport_id) {
    LOG_INFO("[MessageEngine][RtcChannelManager] AddChannel type %d, provider %d, app_id %s, channel_name %s, user_id %s, token_len: %zu",
        channel_info.type_, channel_info.provider_type_, channel_info.app_id_.c_str(), channel_info.channel_name_.c_str(), channel_info.user_id_.c_str(), token.size());
    std::shared_ptr<Channel> channel = nullptr;
    if (channel_info.type_ == ChannelType::RealTime) {
        if (channel_info.provider_type_ == ChannelProviderType::DRTC) {
#ifdef BUILD_WITH_DRTC_USER
            channel = ChannelFactory::CreateDrtcUserRealTimeChannel(channel_info, token, channel_working_executor_, transport_id);
#endif
        }
    } else if (channel_info.type_ == ChannelType::LiveStream) {
        if (channel_info.provider_type_ == ChannelProviderType::DRTC) {
#ifdef BUILD_WITH_DRTC_USER
            channel = ChannelFactory::CreateDrtcUserLiveStreamChannel(channel_info, token, channel_working_executor_);
#endif
        } else if (channel_info.provider_type_ == ChannelProviderType::WebRTC) {
            channel = ChannelFactory::CreateWebrtcLiveStreamChannel(channel_info, token, channel_working_executor_);
        }
    }
    return InternalChannelJoining(channel, completion, transport_id);
}

bool RtcChannelManager::InternalChannelJoining(const std::shared_ptr<IChannel> &channel, const AddChannelCompletion &completion, const std::string& transport_id) {
    if (channel == nullptr) {
        LOG_ERROR("[MessageEngine][RtcChannelManager] channel is null");
        return false;
    }
    if (channel->GetChannelInfo().type_ == ChannelType::RealTime) {
        std::dynamic_pointer_cast<RealTimeChannel>(channel)->SetObserveCallbackManager(transport_manager_->GetObserveCallbackManager());
    }

    // 在 Join 之前清理同名旧 channel，避免旧 channel 析构时 leaveRoom 踢掉新 channel
    {
        std::lock_guard<std::mutex> lock(mutex_);
        auto it = channels_.find(channel->GetChannelInfo().channel_name_);
        if (it != channels_.end()) {
            LOG_WARN("[MessageEngine][RtcChannelManager] Channel %s already exists, leaving old channel before join new.",
                channel->GetChannelInfo().channel_name_.c_str());
            auto old_channel = it->second;
            old_channel->Leave();
            channels_.erase(it);
        }
    }

    WeakDummy(weak_ptr);
    auto result = channel->Join([this, weak_ptr,channel, completion, transport_id](const bool &is_successed,
                                                                     const int &elapsed_time_ms,
                                                                     const bool &is_rejoin, const int &error_code,
                                                                     const std::string &message) {
        WeakDummyReturn(weak_ptr);
        if (completion) {
            if (is_successed) {
                {
                    std::lock_guard<std::mutex> lock(mutex_);
                    // 检查是否已存在同名channel，如果存在则记录警告
                    if (channels_.find(channel->GetChannelInfo().channel_name_) != channels_.end()) {
                        LOG_WARN("[MessageEngine][RtcChannelManager] Channel %s already exists, will be replaced. This may cause data loss for previous LiveModuleImpl instances.",
                            channel->GetChannelInfo().channel_name_.c_str());
                    }
                    channels_[channel->GetChannelInfo().channel_name_] = channel;
                }
                if(channel->GetChannelInfo().type_ == ChannelType::RealTime) {
                    // 添加到 TransportManager 中
                    channel_name_to_transport_id_[channel->GetChannelInfo().channel_name_] = transport_id;
                    transport_manager_->AddTransport(transport_id, std::dynamic_pointer_cast<RealTimeChannel>(channel));
                    OnRealTimeChannelAdded(std::dynamic_pointer_cast<IRealTimeChannel>(channel), transport_id);
                } else if (channel->GetChannelInfo().type_ == ChannelType::LiveStream) {
                    OnLiveStreamChannelAdded(std::dynamic_pointer_cast<ILiveStreamChannel>(channel));
                } else if(channel->GetChannelInfo().type_ == ChannelType::AudioData) {
                    OnAudioDataChannelAdded(std::dynamic_pointer_cast<IAudioDataChannel>(channel));
                }
            }
            completion(is_successed, elapsed_time_ms, error_code, message);
        }
    });
    return result;
}

bool RtcChannelManager::UpdateChannelToken(const ChannelInfo &channel_info, const std::string &token) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (channels_.find(channel_info.channel_name_) == channels_.end()) {
        return false;
    }
    auto& channel =  channels_[channel_info.channel_name_];
    return channel->RenewToken(token);
}

bool RtcChannelManager::QueryChannelExists(const std::string &channel_name) {
    std::lock_guard<std::mutex> lock(mutex_);
    return channels_.contains(channel_name);
}

bool RtcChannelManager::RemoveChannel(const std::string &channel_name) {
    LOG_INFO("[RtcChannelManager] RemoveChannel channel_name %s", channel_name.c_str());
    std::shared_ptr<IChannel> channel = nullptr;
    {
        std::lock_guard<std::mutex> lock(mutex_);
        if (!channels_.contains(channel_name)) {
            LOG_ERROR("[RtcChannelManager] Channel %s is not exist", channel_name.c_str());
            return false;
        }
        channel = channels_[channel_name];
        channels_.erase(channel_name);
    }

    if(channel->GetChannelInfo().type_ == ChannelType::RealTime) {
        OnRealTimeChannelRemoved(std::dynamic_pointer_cast<RealTimeChannel>(channel));
    } else if (channel->GetChannelInfo().type_ == ChannelType::LiveStream) {
        OnLiveStreamChannelRemoved(std::dynamic_pointer_cast<LiveStreamChannel>(channel));
    }
    channel->Leave();
    LOG_INFO("[RtcChannelManager] RemoveChannel channel_name %s success", channel_name.c_str());
    return true;
}

bool RtcChannelManager::RemoveChannel(const ChannelType& channel_type) {
    LOG_INFO("[RtcChannelManager] RemoveChannel ChannelType %d", channel_type);
    std::vector<std::shared_ptr<IChannel>> channels;
    {
        std::lock_guard<std::mutex> lock(mutex_);
        for (auto ch = channels_.begin(); ch != channels_.end(); ) {
            if (ch->second && ch->second->GetChannelInfo().type_ == channel_type) {
                channels.push_back(ch->second);
                ch = channels_.erase(ch);
            } else {
                ++ch;
            }
        }
    }
    LOG_INFO("[RtcChannelManager] RemoveChannel ChannelType %d, size %d", channel_type, channels.size());
    if (channels.empty()) {
        return true;
    }
    for (const auto &channel : channels) {
        if(channel_type == ChannelType::RealTime) {
            OnRealTimeChannelRemoved(std::dynamic_pointer_cast<IRealTimeChannel>(channel));
        } else if (channel_type == ChannelType::LiveStream) {
            OnLiveStreamChannelRemoved(std::dynamic_pointer_cast<ILiveStreamChannel>(channel));
        }
        channel->Leave();
    }
    return true;
}

std::weak_ptr<IChannel> RtcChannelManager::GetChannel(const std::string &channel_name) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (channels_.find(channel_name) == channels_.end()) {
        return {};
    }
    std::shared_ptr<IChannel> channel = channels_.find(channel_name)->second;
    return channel;
}

void RtcChannelManager::RemoveAllChannels() {
    std::lock_guard<std::mutex> lock(mutex_);
    for (const auto& channel : channels_) {
        channel.second->Leave();
    }
    channels_.clear();
}

std::vector<std::weak_ptr<IRealTimeChannel>> RtcChannelManager::GetRealTimeChannels() {
    std::lock_guard<std::mutex> lock(mutex_);
    std::vector<std::weak_ptr<IRealTimeChannel>> tmp;
    for (const auto& channel : channels_) {
        if (channel.second->GetChannelInfo().type_ == ChannelType::RealTime) {
            auto item = std::dynamic_pointer_cast<RealTimeChannel>(channel.second);
            tmp.push_back(item);
        }
    }
    return tmp;
}

std::vector<std::weak_ptr<ILiveStreamChannel> > RtcChannelManager::GetLiveStreamChannels() {
    std::lock_guard<std::mutex> lock(mutex_);
    std::vector<std::weak_ptr<ILiveStreamChannel>> tmp;
    for (const auto& channel : channels_) {
        if (channel.second->GetChannelInfo().type_ == ChannelType::LiveStream) {
            auto item = std::dynamic_pointer_cast<LiveStreamChannel>(channel.second);
            tmp.push_back(item);
        }
    }
    return tmp;
}

std::weak_ptr<IAudioDataChannel> RtcChannelManager::GetAudioDataChannel() {
    LOG_INFO("[MessageEngine][RtcChannelManager] GetAudioDataChannel");
    std::lock_guard<std::mutex> lock(mutex_);
    std::weak_ptr<IAudioDataChannel> tmp;
    for (const auto& channel : channels_) {
        if (channel.second->GetChannelInfo().type_ == ChannelType::AudioData) {
            auto item = std::dynamic_pointer_cast<IAudioDataChannel>(channel.second);
            tmp = item;
            break;
        }
    }
    return tmp;
}

void RtcChannelManager::OnRealTimeChannelAdded(const std::shared_ptr<IRealTimeChannel>& real_time_channel, const std::string& transport_id) {
    LOG_INFO("[MessageEngine][RtcChannelManager] OnRealTimeChannelAdded");
    NotifyAvailableRealTimeChannel();
}


void RtcChannelManager::OnRealTimeChannelRemoved(const std::shared_ptr<IRealTimeChannel>& real_time_channel) {
    LOG_INFO("[MessageEngine][RtcChannelManager] OnRealTimeChannelRemoved");
    // 从 TransportManager 中删除
    auto transport_id = channel_name_to_transport_id_[real_time_channel->GetChannelInfo().channel_name_];
    transport_manager_->RemoveTransport(transport_id);

    NotifyAvailableRealTimeChannel();
}

void RtcChannelManager::OnLiveStreamChannelAdded(const std::shared_ptr<ILiveStreamChannel>& live_stream_channel) {
    LOG_INFO("[MessageEngine][RtcChannelManager] OnLiveStreamChannelAdded");
    NotifyAvailableLiveStreamChannel();
}

void RtcChannelManager::OnLiveStreamChannelRemoved(const std::shared_ptr<ILiveStreamChannel>& live_stream_channel) {
    LOG_INFO("[MessageEngine][RtcChannelManager] OnLiveStreamChannelRemoved");
    NotifyAvailableLiveStreamChannel();
}

void RtcChannelManager::OnAudioDataChannelAdded(const std::shared_ptr<IAudioDataChannel>& audio_data_channel) {
    LOG_INFO("[MessageEngine][RtcChannelManager] OnAudioDataChannelAdded");
    NotifyAvailableAudioDataChannel();
}

void RtcChannelManager::OnAudioDataChannelRemoved(const std::shared_ptr<IAudioDataChannel>& audio_data_channel) {
    LOG_INFO("[MessageEngine][RtcChannelManager] OnAudioDataChannelRemoved");
    NotifyAvailableAudioDataChannel();
}

void RtcChannelManager::NotifyAvailableRealTimeChannel() {
    LOG_INFO("[MessageEngine][RtcChannelManager] NotifyAvailableRealTimeChannel");
    std::vector<std::weak_ptr<IRealTimeChannel>> tmp;
    decltype(real_time_channel_available_callbacks_) callbacks_tmp;
    {
        std::lock_guard<std::mutex> lock(mutex_real_time_channel_available_callbacks_);
        for (const auto& channel : channels_) {
            if (channel.second->GetChannelInfo().type_ == ChannelType::RealTime) {
                auto item = std::dynamic_pointer_cast<RealTimeChannel>(channel.second);
                tmp.push_back(item);
            }
        }
        callbacks_tmp = real_time_channel_available_callbacks_;
    }
    for(const auto& callback : callbacks_tmp) {
        callback(tmp);
    }
}

void RtcChannelManager::NotifyAvailableLiveStreamChannel() {
    LOG_INFO("[MessageEngine][RtcChannelManager] NotifyAvailableLiveStreamChannel");
    std::vector<std::weak_ptr<ILiveStreamChannel>> tmp;
    decltype(live_stream_channel_available_callbacks_) callbacks_tmp;
    {
        std::lock_guard<std::mutex> lock(mutex_live_stream_channel_available_callbacks_);
        for (const auto& channel : channels_) {
            if (channel.second->GetChannelInfo().type_ == ChannelType::LiveStream) {
                auto item = std::dynamic_pointer_cast<LiveStreamChannel>(channel.second);
                tmp.push_back(item);
            }
        }
        callbacks_tmp = live_stream_channel_available_callbacks_;
    }
    for(const auto& callback : callbacks_tmp) {
        callback(tmp);
    }
}

void RtcChannelManager::NotifyAvailableAudioDataChannel() {
    LOG_INFO("[MessageEngine][RtcChannelManager] NotifyAvailableAudioDataChannel");
    std::vector<std::weak_ptr<IAudioDataChannel>> tmp;
    decltype(audio_data_channel_available_callbacks_) callbacks_tmp;
    {
        std::lock_guard<std::mutex> lock(mutex_audio_data_channel_available_callbacks_);
        for (const auto& channel : channels_) {
            if (channel.second->GetChannelInfo().type_ == ChannelType::AudioData) {
                auto item = std::dynamic_pointer_cast<IAudioDataChannel>(channel.second);
                tmp.push_back(item);
            }
        }
        callbacks_tmp = audio_data_channel_available_callbacks_;
    }
    for(const auto& callback : callbacks_tmp) {
        callback(tmp);
    }
}

void RtcChannelManager::RegisterRealTimeChannelAvailableCallback(RealTimeChannelAvailableCallback callback) {
    LOG_INFO("[MessageEngine][RtcChannelManager] RegisterRealTimeChannelAvailableCallback");
    std::lock_guard lock(mutex_real_time_channel_available_callbacks_);
    if(callback == nullptr) {
        LOG_INFO("[MessageEngine][RtcChannelManager] RegisterRealTimeChannelAvailableCallback callback is nullptr");
        return;
    }
    real_time_channel_available_callbacks_.emplace_back(std::move(callback));
}

void RtcChannelManager::RegisterLiveStreamChannelAvailableCallback(LiveStreamChannelAvailableCallback callback) {
    LOG_INFO("[MessageEngine][RtcChannelManager] RegisterLiveStreamChannelAvailableCallback");
    std::lock_guard lock(mutex_live_stream_channel_available_callbacks_);
    if(callback == nullptr) {
        LOG_INFO("[MessageEngine][RtcChannelManager] RegisterLiveStreamChannelAvailableCallback callback is nullptr");
        return;
    }
    live_stream_channel_available_callbacks_.emplace_back(std::move(callback));
}

void RtcChannelManager::RegisterAudioDataChannelAvailableCallback(AudioDataChannelAvailableCallback callback) {
    LOG_INFO("[MessageEngine][RtcChannelManager] RegisterAudioDataChannelAvailableCallback");
    std::lock_guard lock(mutex_audio_data_channel_available_callbacks_);
    if(callback == nullptr) {
        LOG_INFO("[MessageEngine][RtcChannelManager] RegisterAudioDataChannelAvailableCallback callback is nullptr");
        return;
    }
    audio_data_channel_available_callbacks_.emplace_back(std::move(callback));
}