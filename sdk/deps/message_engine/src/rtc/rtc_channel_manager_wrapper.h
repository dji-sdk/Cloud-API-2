// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/i_rtc_channel_manager.h"
#include "api/bridge/i_rtc_channel_manager_bridge.h"
#include "common/util/async_capture_protect.h"
#include "transport/transport_manager.h"
#include <list>
#include <map>
#include <unordered_map>
#include "base/async/executor.h"

namespace message_engine {

class RtcChannelManagerWrapper : public IRtcChannelManager, public ::drc_sdk::common::AsyncCaptureProtect {
public:
    RtcChannelManagerWrapper(const std::shared_ptr<IRtcChannelManagerBridge>& channel_manager_bridge,
                             const std::shared_ptr<TransportManager>& transport_manager,
                             const std::shared_ptr<::drc_sdk::Executor> &working_executor);
    ~RtcChannelManagerWrapper();

    bool AddChannel(const ChannelInfo& channel_info, const std::string& token, AddChannelCompletion&& completion, const std::string& transport_id) override;
    bool RemoveChannel(const std::string& channel_name) override;
    bool RemoveChannel(const ChannelType& channel_type) override;
    void RemoveAllChannels() override;
    std::weak_ptr<IChannel> GetChannel(const std::string& channel_name) override;
    bool QueryChannelExists(const std::string& channel_name) override;
    bool UpdateChannelToken(const ChannelInfo& channel_info, const std::string& token) override;

    std::vector<std::weak_ptr<IRealTimeChannel>> GetRealTimeChannels() override;
    std::vector<std::weak_ptr<ILiveStreamChannel> > GetLiveStreamChannels() override;
    std::weak_ptr<IAudioDataChannel> GetAudioDataChannel() override;

    void RegisterRealTimeChannelAvailableCallback(RealTimeChannelAvailableCallback callback) override;
    void RegisterLiveStreamChannelAvailableCallback(LiveStreamChannelAvailableCallback callback) override;
    void RegisterAudioDataChannelAvailableCallback(AudioDataChannelAvailableCallback callback) override;
private:
    void OnRealTimeChannelAdded(const std::shared_ptr<IRealTimeChannel>& channel, const std::string& transport_id);
    void OnRealTimeChannelRemoved(const std::shared_ptr<IRealTimeChannel>& channel);

    std::shared_ptr<IRtcChannelManagerBridge> channel_manager_bridge_;
    std::shared_ptr<TransportManager> transport_manager_;
    std::shared_ptr<::drc_sdk::Executor> working_executor_;
    std::unordered_map<std::string, std::string> channel_name_to_transport_id_;
    std::unordered_map<std::string, std::shared_ptr<ITransport>> transport_adapters_;

    std::list<RealTimeChannelAvailableCallback> real_time_channel_available_callbacks_;
    std::list<LiveStreamChannelAvailableCallback> live_stream_channel_available_callbacks_;

    std::map<std::string, std::shared_ptr<IRealTimeChannel>> real_time_channels_;
    std::map<std::string, std::shared_ptr<ILiveStreamChannel>> live_stream_channels_;
};

} // namespace message_engine