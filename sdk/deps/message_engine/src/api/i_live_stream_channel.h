// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "i_channel.h"

namespace message_engine {
class ILiveStreamChannel : public IChannel {
public:
    virtual ~ILiveStreamChannel() = default;

    /// For Pull Live Stream
    virtual void SetReceiveVideoDataCallback(std::function<void(const std::shared_ptr<drc_sdk::ByteBuffer> &data, bool is_key_frame)> callback) = 0;

    /// For Push Live Stream
    virtual void SendVideoData(const std::shared_ptr<drc_sdk::ByteBuffer> &data, const VideoFrameInfo &info) = 0;
    virtual void SendAudioData(const std::shared_ptr<drc_sdk::ByteBuffer> &data, const AudioFrameInfo &info) = 0;

    virtual void SetNeedSendKeyFrameRequestCallback(std::function<void(const std::string &user_id, bool send_key_frame)> callback) = 0;
    virtual void SetNeedChangeBitrateRequestCallback(std::function<void(const uint32_t &bit_rate)> callback) = 0;
    virtual void SetUserMuteVideoCallback(std::function<void(const std::string &user_id, const int &muted)> callback) = 0;
    virtual void SetUserMuteAudioCallback(std::function<void(const std::string &user_id, const int &muted)> callback) = 0;
};
} // namespace message_engine
