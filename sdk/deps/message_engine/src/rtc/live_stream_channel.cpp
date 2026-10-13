// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "live_stream_channel.h"

using namespace message_engine;


LiveStreamChannel::LiveStreamChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<::drc_sdk::Executor> &working_executor) :
    Channel(channel_info, token, working_executor) {
}

void LiveStreamChannel::SetNeedSendKeyFrameRequestCallback(std::function<void(const std::string& user_id, bool send_key_frame)> callback) {
    need_send_key_frame_callback_ = callback;
}

void LiveStreamChannel::SetNeedChangeBitrateRequestCallback(std::function<void(const uint32_t& bit_rate)> callback) {
    need_change_bit_rate_callback_ = callback;
}

void LiveStreamChannel::SetUserMuteVideoCallback(std::function<void(const std::string &user_id, const int &muted)> callback) {
    user_mute_video_callback_ = callback;
}

void LiveStreamChannel::SetUserMuteAudioCallback(std::function<void(const std::string &user_id, const int &muted)> callback) {
    user_mute_audio_callback_ = callback;
}

void LiveStreamChannel::SetReceiveVideoDataCallback(std::function<void(const std::shared_ptr<drc_sdk::ByteBuffer> &data, bool is_key_frame)> callback) {
    receive_video_data_callback_ = callback;
}

void LiveStreamChannel::SendVideoData(const std::shared_ptr<drc_sdk::ByteBuffer> &data, const VideoFrameInfo &info) {
    /// pass
}

void LiveStreamChannel::SendAudioData(const std::shared_ptr<drc_sdk::ByteBuffer> &data, const AudioFrameInfo &info) {
    /// pass
}
