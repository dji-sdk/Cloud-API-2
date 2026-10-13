// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/async/executor.h"
#include "common/util/async_capture_protect.h"
#include "api/i_rtc_channel_manager.h"
#include "channel.h"
#include "real_time_channel.h"
#include "live_stream_channel.h"
#include "transport/transport_manager.h"
#include <unordered_map>
#include <list>

/*
 * 只感知Channel、RealTimeChannel、LiveStreamChannel几个抽象类，不感知volc与drtc
 * channel_working_executor是channel实例的工作线程，所有的调用和回调都会在这个线程执行
 */
namespace message_engine {

class RtcChannelManager : public ::drc_sdk::common::AsyncCaptureProtect, public IRtcChannelManager {
public:
    RtcChannelManager(const std::shared_ptr<drc_sdk::Executor>& executor, const std::shared_ptr<TransportManager>& transport_manager);
    ~RtcChannelManager() override;
public:

    bool AddChannel(const ChannelInfo &channel_info, const std::string &token, AddChannelCompletion &&completion, const std::string &transport_id) override;
    bool RemoveChannel(const std::string &channel_name) override;
    bool RemoveChannel(const ChannelType& channel_type) override;
    void RemoveAllChannels() override;

    bool UpdateChannelToken(const ChannelInfo &channel_info, const std::string &token) override;
    bool QueryChannelExists(const std::string &channel_name) override;

    std::weak_ptr<IChannel> GetChannel(const std::string &channel_name) override;

    std::vector<std::weak_ptr<IRealTimeChannel> > GetRealTimeChannels() override;
    std::vector<std::weak_ptr<ILiveStreamChannel> > GetLiveStreamChannels() override;
    std::weak_ptr<IAudioDataChannel> GetAudioDataChannel() override;

    void RegisterRealTimeChannelAvailableCallback(RealTimeChannelAvailableCallback callback) override;
    void RegisterLiveStreamChannelAvailableCallback(LiveStreamChannelAvailableCallback callback) override;
    void RegisterAudioDataChannelAvailableCallback(AudioDataChannelAvailableCallback callback) override;

private:
    bool InternalChannelJoining(const std::shared_ptr<IChannel>& channel, const AddChannelCompletion &completion, const std::string& transport_id);

    void OnRealTimeChannelAdded(const std::shared_ptr<IRealTimeChannel>& real_time_channel, const std::string& transport_id);
    void OnRealTimeChannelRemoved(const std::shared_ptr<IRealTimeChannel>& real_time_channel);

    void OnLiveStreamChannelAdded(const std::shared_ptr<ILiveStreamChannel>& real_time_channel);
    void OnLiveStreamChannelRemoved(const std::shared_ptr<ILiveStreamChannel>& real_time_channel);

    void OnAudioDataChannelAdded(const std::shared_ptr<IAudioDataChannel>& audio_data_channel);
    void OnAudioDataChannelRemoved(const std::shared_ptr<IAudioDataChannel>& audio_data_channel);

    void NotifyAvailableRealTimeChannel();
    void NotifyAvailableLiveStreamChannel();
    void NotifyAvailableAudioDataChannel();
private:
    std::mutex mutex_;
    std::mutex mutex_live_stream_channel_available_callbacks_;
    std::mutex mutex_real_time_channel_available_callbacks_;
    std::mutex mutex_audio_data_channel_available_callbacks_;
    std::unordered_map<std::string, std::shared_ptr<IChannel>> channels_;
    std::unordered_map<std::string, std::string> channel_name_to_transport_id_;
    std::shared_ptr<::drc_sdk::Executor> channel_working_executor_; // channel实例的工作线程，channel_mgr本身对于channels的增删改查，由调用线程决定


    std::list<RealTimeChannelAvailableCallback> real_time_channel_available_callbacks_;
    std::list<LiveStreamChannelAvailableCallback> live_stream_channel_available_callbacks_;
    std::list<AudioDataChannelAvailableCallback> audio_data_channel_available_callbacks_;

    std::shared_ptr<TransportManager> transport_manager_;
};
} // namespace message_engine

