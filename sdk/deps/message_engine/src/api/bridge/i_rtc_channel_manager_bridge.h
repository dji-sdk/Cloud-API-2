// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include <string>
#include "i_real_time_channel_bridge.h"
#include "i_live_stream_channel_bridge.h"
#include "channel_info_bridge.h"

namespace message_engine {

class IRealTimeChannelBridge;
class ILiveStreamChannelBridge;

class IRtcChannelManagerBridge {
public:
    virtual ~IRtcChannelManagerBridge() = default;
    virtual std::shared_ptr<IRealTimeChannelBridge> GetRealTimeChannel(const ChannelInfoBridge& channel_info, const std::string& token) = 0;
    virtual std::shared_ptr<ILiveStreamChannelBridge> GetLiveStreamChannel(const ChannelInfoBridge& channel_info, const std::string& token) = 0;
    virtual bool DestroyRealTimeChannel(const std::string& channel_name) = 0;
    virtual bool DestroyLiveStreamChannel(const std::string& channel_name) = 0;
    virtual void DestroyAllChannels() = 0;
    virtual bool QueryChannelExists(const std::string& channel_name) = 0;
};

} // namespace message_engine 