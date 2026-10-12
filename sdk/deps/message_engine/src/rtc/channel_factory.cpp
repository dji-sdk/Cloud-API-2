// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "channel_factory.h"

#ifdef BUILD_WITH_DRTC_USER
#include "drtc_user/drtc_user_real_time_channel.h"
#include "drtc_user/drtc_user_live_stream_channel.h"
#endif

#ifdef BUILD_WITH_LIBWEBRTC
#include "libwebrtc/libwebrtc_live_stream_channel.h"
#endif

namespace message_engine {

std::shared_ptr<LiveStreamChannel> ChannelFactory::CreateWebrtcLiveStreamChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<drc_sdk::Executor> &working_executor) {
    (void)token;
#ifdef BUILD_WITH_LIBWEBRTC
    return std::make_shared<LibwebrtcLiveStreamChannel>(channel_info, working_executor);
#else
    (void)channel_info;
    (void)working_executor;
    return nullptr;
#endif
}

#ifdef BUILD_WITH_DRTC_USER
std::shared_ptr<RealTimeChannel> ChannelFactory::CreateDrtcUserRealTimeChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<drc_sdk::Executor> &working_executor, std::string log_tag) {
    return std::make_shared<DrtcUserRealTimeChannel>(channel_info, token, working_executor, log_tag);
}

std::shared_ptr<LiveStreamChannel> ChannelFactory::CreateDrtcUserLiveStreamChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<drc_sdk::Executor> &working_executor) {
    return std::make_shared<DrtcUserLiveStreamChannel>(channel_info, token, working_executor);
}
#endif

} // namespace message_engine
