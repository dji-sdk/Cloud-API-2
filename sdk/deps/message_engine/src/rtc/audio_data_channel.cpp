// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "audio_data_channel.h"

#include <base/common/logger.h>
#include <utility>

using namespace message_engine;

AudioDataChannel::AudioDataChannel(const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<::drc_sdk::Executor> &working_executor) :
    Channel(channel_info, token, working_executor) {
}

void AudioDataChannel::SetAudioDataCallback(std::function<void(const std::string &source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer> &message)>&& callback)  {
    audio_data_callback_ = std::move(callback);
}