// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"
#include "i_audio_data_channel.h"
#include "i_channel.h"
#include <memory>
#include <string>

namespace message_engine {

constexpr auto RTC_BASE_TRANSPORT_ID = "rt_base";

class IRtcChannelManager {
public:
    virtual ~IRtcChannelManager() = default;

    virtual bool AddChannel(const ChannelInfo &channel_info, const std::string &token, AddChannelCompletion &&completion, const std::string &transport_id) = 0;

    virtual bool RemoveChannel(const std::string& channel_name) = 0;
    virtual bool RemoveChannel(const ChannelType& channel_type) = 0;
    virtual void RemoveAllChannels() = 0;
    virtual std::weak_ptr<IChannel> GetChannel(const std::string& channel_name) = 0;
    virtual bool QueryChannelExists(const std::string& channel_name) = 0;
    virtual bool UpdateChannelToken(const ChannelInfo& channel_info, const std::string& token) = 0;

    virtual std::vector<std::weak_ptr<IRealTimeChannel>> GetRealTimeChannels() = 0;
    virtual std::vector<std::weak_ptr<ILiveStreamChannel>> GetLiveStreamChannels() = 0;
    virtual std::weak_ptr<IAudioDataChannel> GetAudioDataChannel() = 0;

    virtual void RegisterRealTimeChannelAvailableCallback(RealTimeChannelAvailableCallback callback) = 0;
    virtual void RegisterLiveStreamChannelAvailableCallback(LiveStreamChannelAvailableCallback callback) = 0;
    virtual void RegisterAudioDataChannelAvailableCallback(AudioDataChannelAvailableCallback callback) = 0;
};

} // namespace message_engine 