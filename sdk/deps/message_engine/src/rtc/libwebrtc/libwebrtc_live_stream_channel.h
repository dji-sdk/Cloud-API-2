// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/i_live_stream_channel.h"
#include "live_stream_channel.h"
#include "common/util/async_capture_protect.h"
#include <memory>
#include "libwebrtc/libwebrtc_client_impl.h"

namespace message_engine {

class LibwebrtcLiveStreamChannel : public LiveStreamChannel, 
                                 public ::drc_sdk::common::AsyncCaptureProtect, 
                                 public std::enable_shared_from_this<LibwebrtcLiveStreamChannel> {
public:
    LibwebrtcLiveStreamChannel(const ChannelInfo& channel_info, const std::shared_ptr<::drc_sdk::Executor> &working_executor);
    ~LibwebrtcLiveStreamChannel() override;

public:
    bool Join(const JoinChannelCallback &callback) override;
    bool Leave() override;
    bool RenewToken(const std::string &token) override;

private:
    std::shared_ptr<::drc_sdk::Executor> executor_;
    std::shared_ptr<LibwebrtcClientImpl> client_;
};

} // namespace message_engine