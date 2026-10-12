// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once
#include "base/async/executor.h"
#include "real_time_channel.h"
#include "live_stream_channel.h"

namespace message_engine {

class ChannelFactory {
public:
#ifdef BUILD_WITH_DRTC_USER
    static std::shared_ptr<RealTimeChannel> CreateDrtcUserRealTimeChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<drc_sdk::Executor> &working_executor, std::string log_tag);
    static std::shared_ptr<LiveStreamChannel> CreateDrtcUserLiveStreamChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<drc_sdk::Executor> &working_executor);
#endif
    static std::shared_ptr<LiveStreamChannel> CreateWebrtcLiveStreamChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<drc_sdk::Executor> &working_executor);
};

} // namespace message_engine
