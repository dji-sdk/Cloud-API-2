// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "rtc_channel_manager_wrapper.h"
#include "live_stream_channel_wrapper/live_stream_channel_wrapper.h"
#include "real_time_channel_wrapper/real_time_channel_wrapper.h"
#include "real_time_channel_wrapper/real_time_channel_transport_adapter.h"
#include "base/common/logger.h"

namespace message_engine {

RtcChannelManagerWrapper::RtcChannelManagerWrapper(
    const std::shared_ptr<IRtcChannelManagerBridge>& channel_manager_bridge,
    const std::shared_ptr<TransportManager>& transport_manager,
    const std::shared_ptr<::drc_sdk::Executor> &working_executor)
    : channel_manager_bridge_(channel_manager_bridge),
      transport_manager_(transport_manager),
      working_executor_(working_executor) {}

RtcChannelManagerWrapper::~RtcChannelManagerWrapper() {
    RemoveAllChannels();
    real_time_channel_available_callbacks_.clear();
    live_stream_channel_available_callbacks_.clear();
}

bool RtcChannelManagerWrapper::AddChannel(const ChannelInfo& channel_info, const std::string& token, AddChannelCompletion&& completion, const std::string& transport_id) {
    if (channel_info.type_ == ChannelType::RealTime) {
        ChannelInfoBridge channel_info_bridge;
        channel_info_bridge.provider_type = channel_info.provider_type_;
        channel_info_bridge.app_id = channel_info.app_id_;
        channel_info_bridge.channel_name = channel_info.channel_name_;
        channel_info_bridge.room_id = channel_info.room_id_.empty() ? channel_info.channel_name_ : channel_info.room_id_;
        channel_info_bridge.user_id = channel_info.user_id_;
        channel_info_bridge.client_id = channel_info.client_id_;
        channel_info_bridge.url = channel_info.url_;
        channel_info_bridge.token = token;
        auto real_time_channel_bridge = channel_manager_bridge_->GetRealTimeChannel(channel_info_bridge, token);
        auto real_time_channel = std::make_shared<RealTimeChannelWrapper>(real_time_channel_bridge, channel_info, token, working_executor_);
        WeakDummy(p);
        return real_time_channel->Join([completion, p, real_time_channel, this, channel_info, transport_id]
            (const bool &is_succeed, const int &elapsed_time_ms, const bool &is_rejoin, const int &error_code, const std::string &message) {
            WeakDummyReturn(p);
            if (is_succeed) {
                try {
                    LOG_INFO("[RtcChannelManagerWrapper] Join success, creating adapter for channel %s", channel_info.channel_name_.c_str());
                    real_time_channels_[channel_info.channel_name_] = real_time_channel;
                    channel_name_to_transport_id_[channel_info.channel_name_] = transport_id;
                    // 根据 provider_type 选择 log_tag：volc_rtc / drtc_rtc
                    const char* log_tag = (channel_info.provider_type_ == message_engine::ChannelProviderType::DRTC) ? "drtc_rtc" : "volc_rtc";
                    auto adapter = std::make_shared<RealTimeChannelTransportAdapter>(
                        real_time_channel, transport_manager_, working_executor_, log_tag);
                    LOG_INFO("[RtcChannelManagerWrapper] Adapter created, calling Init");
                    adapter->Init();
                    LOG_INFO("[RtcChannelManagerWrapper] Init done, calling AddTransport");
                    transport_adapters_[transport_id] = adapter;
                    transport_manager_->AddTransport(transport_id, adapter);
                    LOG_INFO("[RtcChannelManagerWrapper] AddTransport done, calling OnRealTimeChannelAdded");
                    OnRealTimeChannelAdded(real_time_channel, transport_id);
                    LOG_INFO("[RtcChannelManagerWrapper] OnRealTimeChannelAdded done");
                } catch (const std::exception& e) {
                    LOG_ERROR("[RtcChannelManagerWrapper] Exception in Join success path: %s", e.what());
                    throw;
                } catch (...) {
                    LOG_ERROR("[RtcChannelManagerWrapper] Unknown exception in Join success path");
                    throw;
                }
            }
            LOG_INFO("[RtcChannelManagerWrapper] Calling completion, is_succeed=%d", is_succeed);
            try {
                completion(is_succeed, elapsed_time_ms, error_code, message);
            } catch (const std::exception& e) {
                LOG_ERROR("[RtcChannelManagerWrapper] Exception in completion: %s", e.what());
                throw;
            } catch (...) {
                LOG_ERROR("[RtcChannelManagerWrapper] Unknown exception in completion");
                throw;
            }
        });
    }
    if (channel_info.type_ == ChannelType::LiveStream) {
        ChannelInfoBridge channel_info_bridge;
        channel_info_bridge.provider_type = channel_info.provider_type_;
        channel_info_bridge.app_id = channel_info.app_id_;
        channel_info_bridge.channel_name = channel_info.channel_name_;
        channel_info_bridge.room_id = channel_info.room_id_.empty() ? channel_info.channel_name_ : channel_info.room_id_;
        channel_info_bridge.user_id = channel_info.user_id_;
        channel_info_bridge.url = channel_info.url_;
        channel_info_bridge.token = token;
        auto live_stream_channel_bridge = channel_manager_bridge_->GetLiveStreamChannel(channel_info_bridge, token);
        auto live_stream_channel = std::make_shared<LiveStreamChannelWrapper>(live_stream_channel_bridge, channel_info, token, working_executor_);
        WeakDummy(p);
        return live_stream_channel->Join([completion, p, live_stream_channel, this, channel_info]
            (const bool &is_succeed, const int &elapsed_time_ms, const bool &is_rejoin, const int &error_code, const std::string &message) {
            WeakDummyReturn(p);
            if (is_succeed) {
                live_stream_channels_[channel_info.channel_name_] = live_stream_channel;
            }
            completion(is_succeed, elapsed_time_ms, error_code, message);
        });
    }
    return false;
}

bool RtcChannelManagerWrapper::RemoveChannel(const std::string& channel_name) {
    if (real_time_channels_.find(channel_name) != real_time_channels_.end()) {
        auto it = channel_name_to_transport_id_.find(channel_name);
        if (it != channel_name_to_transport_id_.end()) {
            OnRealTimeChannelRemoved(real_time_channels_[channel_name]);
            transport_manager_->RemoveTransport(it->second);
            transport_adapters_.erase(it->second);
            channel_name_to_transport_id_.erase(it);
        }
        real_time_channels_[channel_name]->Leave();
        real_time_channels_.erase(channel_name);
        channel_manager_bridge_->DestroyRealTimeChannel(channel_name);
    }
    if (live_stream_channels_.find(channel_name) != live_stream_channels_.end()) {
        live_stream_channels_[channel_name]->Leave();
        live_stream_channels_.erase(channel_name);
        channel_manager_bridge_->DestroyLiveStreamChannel(channel_name);
    }
    return false;
}

bool RtcChannelManagerWrapper::RemoveChannel(const ChannelType& channel_type) {
    if (channel_type == ChannelType::RealTime) {
        for (auto& channel : real_time_channels_) {
            auto it = channel_name_to_transport_id_.find(channel.first);
            if (it != channel_name_to_transport_id_.end()) {
                OnRealTimeChannelRemoved(channel.second);
                transport_manager_->RemoveTransport(it->second);
                transport_adapters_.erase(it->second);
                channel_name_to_transport_id_.erase(it);
            }
            channel.second->Leave();
            channel_manager_bridge_->DestroyRealTimeChannel(channel.first);
        }
        real_time_channels_.clear();
    } else if (channel_type == ChannelType::LiveStream) {
        for (auto& channel : live_stream_channels_) {
            channel.second->Leave();
            channel_manager_bridge_->DestroyLiveStreamChannel(channel.first);
        }
        live_stream_channels_.clear();
    }
    return false;
}

void RtcChannelManagerWrapper::RemoveAllChannels() {
    for (auto& channel : real_time_channels_) {
        auto it = channel_name_to_transport_id_.find(channel.first);
        if (it != channel_name_to_transport_id_.end()) {
            transport_manager_->RemoveTransport(it->second);
            transport_adapters_.erase(it->second);
            channel_name_to_transport_id_.erase(it);
        }
        channel.second->Leave();
        channel_manager_bridge_->DestroyRealTimeChannel(channel.first);
    }
    real_time_channels_.clear();
    transport_adapters_.clear();
    for (auto& channel : live_stream_channels_) {
        channel.second->Leave();
        channel_manager_bridge_->DestroyLiveStreamChannel(channel.first);
    }
    live_stream_channels_.clear();
}

std::weak_ptr<IChannel> RtcChannelManagerWrapper::GetChannel(const std::string& channel_name) {
    if (real_time_channels_.find(channel_name) != real_time_channels_.end()) {
        return real_time_channels_[channel_name];
    }
    if (live_stream_channels_.find(channel_name) != live_stream_channels_.end()) {
        return live_stream_channels_[channel_name];
    }
    return std::weak_ptr<IChannel>();
}

bool RtcChannelManagerWrapper::QueryChannelExists(const std::string& channel_name) {
    return real_time_channels_.find(channel_name) != real_time_channels_.end() || live_stream_channels_.find(channel_name) != live_stream_channels_.end();
}

bool RtcChannelManagerWrapper::UpdateChannelToken(const ChannelInfo& channel_info, const std::string& token) {
    if (real_time_channels_.find(channel_info.channel_name_) != real_time_channels_.end()) {
        return real_time_channels_[channel_info.channel_name_]->RenewToken(token);
    }
    if (live_stream_channels_.find(channel_info.channel_name_) != live_stream_channels_.end()) {
        return live_stream_channels_[channel_info.channel_name_]->RenewToken(token);
    }
    return false;
}

std::vector<std::weak_ptr<IRealTimeChannel> > RtcChannelManagerWrapper::GetRealTimeChannels() {
    std::vector<std::weak_ptr<IRealTimeChannel>> real_time_channels;
    for (auto& channel : real_time_channels_) {
        real_time_channels.push_back(channel.second);
    }
    return real_time_channels;
}

std::vector<std::weak_ptr<ILiveStreamChannel> > RtcChannelManagerWrapper::GetLiveStreamChannels() {
    std::vector<std::weak_ptr<ILiveStreamChannel>> live_stream_channels;
    for (auto& channel : live_stream_channels_) {
        live_stream_channels.push_back(channel.second);
    }
    return live_stream_channels;
}

std::weak_ptr<IAudioDataChannel> RtcChannelManagerWrapper::GetAudioDataChannel() {
    return std::weak_ptr<IAudioDataChannel>();
}

void RtcChannelManagerWrapper::RegisterRealTimeChannelAvailableCallback(RealTimeChannelAvailableCallback callback) {
    real_time_channel_available_callbacks_.push_back(callback);
}

void RtcChannelManagerWrapper::RegisterLiveStreamChannelAvailableCallback(LiveStreamChannelAvailableCallback callback) {
    live_stream_channel_available_callbacks_.push_back(callback);
}

void RtcChannelManagerWrapper::RegisterAudioDataChannelAvailableCallback(AudioDataChannelAvailableCallback callback) {}

void RtcChannelManagerWrapper::OnRealTimeChannelAdded(const std::shared_ptr<IRealTimeChannel>& channel, const std::string& transport_id) {
    LOG_INFO("[MessageEngine][RtcChannelManagerWrapper] OnRealTimeChannelAdded");
    for (const auto& cb : real_time_channel_available_callbacks_) {
        std::vector<std::weak_ptr<IRealTimeChannel>> channels;
        for (const auto& ch : real_time_channels_) {
            channels.push_back(ch.second);
        }
        cb(channels);
    }
}

void RtcChannelManagerWrapper::OnRealTimeChannelRemoved(const std::shared_ptr<IRealTimeChannel>& channel) {
    LOG_INFO("[MessageEngine][RtcChannelManagerWrapper] OnRealTimeChannelRemoved");
    for (const auto& cb : real_time_channel_available_callbacks_) {
        std::vector<std::weak_ptr<IRealTimeChannel>> channels;
        for (const auto& ch : real_time_channels_) {
            if (ch.second != channel) {
                channels.push_back(ch.second);
            }
        }
        cb(channels);
    }
}
} // namespace message_engine  