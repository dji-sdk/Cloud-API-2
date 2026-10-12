// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "live_stream_channel_wrapper.h"
#include "base/io/byte_buffer_pool.h"
#include "base/common/logger.h"

using namespace message_engine;

LiveStreamChannelWrapper::LiveStreamChannelWrapper(const std::shared_ptr<ILiveStreamChannelBridge>& live_stream_channel_bridge, const ChannelInfo& channel_info, const std::string& token, const std::shared_ptr<::drc_sdk::Executor> &working_executor)
    : live_stream_channel_bridge_(live_stream_channel_bridge),
      Channel(channel_info, token, working_executor),
      executor_(working_executor) {}

void LiveStreamChannelWrapper::SetReceiveVideoDataCallback(std::function<void(const std::shared_ptr<drc_sdk::ByteBuffer> &data, bool is_key_frame)> callback) {
    std::string stream_tag = channel_info_.stream_tag_.empty() ? channel_info_.channel_name_ : channel_info_.stream_tag_;
    if (!stream_tag.empty()) {
        LOG_INFO("LiveStreamChannelWrapper[%s]: SetReceiveVideoDataCallback - 设置receive_video_data_callback_", stream_tag.c_str());
    } else {
        LOG_INFO("LiveStreamChannelWrapper: SetReceiveVideoDataCallback - 设置receive_video_data_callback_");
    }
    receive_video_data_callback_ = std::move(callback);
    if (!stream_tag.empty()) {
        LOG_INFO("LiveStreamChannelWrapper[%s]: SetReceiveVideoDataCallback - 设置完成, callback是否为空: %s", 
            stream_tag.c_str(), (receive_video_data_callback_ ? "否" : "是"));
    } else {
        LOG_INFO("LiveStreamChannelWrapper: SetReceiveVideoDataCallback - 设置完成, callback是否为空: %s", 
            (receive_video_data_callback_ ? "否" : "是"));
    }
}

void LiveStreamChannelWrapper::SetNeedSendKeyFrameRequestCallback(std::function<void(const std::string &user_id, bool send_key_frame)> callback) {
    need_send_key_frame_callback_ = std::move(callback);
}

void LiveStreamChannelWrapper::SetNeedChangeBitrateRequestCallback(std::function<void(const uint32_t &bit_rate)> callback) {
    need_change_bit_rate_callback_ = std::move(callback);
}

void LiveStreamChannelWrapper::SetUserMuteVideoCallback(std::function<void(const std::string &user_id, const int &muted)> callback) {
    user_mute_video_callback_ = std::move(callback);
}

void LiveStreamChannelWrapper::SetUserMuteAudioCallback(std::function<void(const std::string &user_id, const int &muted)> callback) {
    user_mute_audio_callback_ = std::move(callback);
}

bool LiveStreamChannelWrapper::Join(const JoinChannelCallback &callback) {
    live_stream_channel_bridge_->SetChannelStatusCallback(shared_from_this());
    live_stream_channel_bridge_->SetReceiveVideoDataCallback(shared_from_this());
    std::string stream_tag = channel_info_.stream_tag_.empty() ? channel_info_.channel_name_ : channel_info_.stream_tag_;
    if (!stream_tag.empty()) {
        LOG_INFO("LiveStreamChannelWrapper[%s]: Join", stream_tag.c_str());
    } else {
        LOG_INFO("LiveStreamChannelWrapper: Join");
    }
    ChannelInfoBridge channel_info_bridge;
    channel_info_bridge.url = channel_info_.url_;
    WeakDummy(p);
    return live_stream_channel_bridge_->Join(channel_info_bridge, [callback, p, this, stream_tag]
        (bool is_succeed) {
        WeakDummyReturn(p);
        if (!stream_tag.empty()) {
            LOG_INFO("LiveStreamChannelWrapper[%s]: Join callback", stream_tag.c_str());
        } else {
            LOG_INFO("LiveStreamChannelWrapper: Join callback");
        }
        callback(is_succeed, 0, false, 0, "");
    });
}

bool LiveStreamChannelWrapper::Leave() {
    return live_stream_channel_bridge_->Leave();
}

bool LiveStreamChannelWrapper::RenewToken(const std::string &token) {
    return live_stream_channel_bridge_->RenewToken(token);
}

void LiveStreamChannelWrapper::OnReceiveVideoData(const std::vector<uint8_t>& video_data, bool is_key_frame) {
    std::string stream_tag = channel_info_.stream_tag_.empty() ? channel_info_.channel_name_ : channel_info_.stream_tag_;
    // 增加诊断日志：只在关键帧时记录，减少日志量
    if (is_key_frame) {
        if (!stream_tag.empty()) {
            LOG_INFO("LiveStreamChannelWrapper[%s]: OnReceiveVideoData - 收到视频数据, 大小: %zu, 关键帧: 是, callback是否设置: %s", 
                stream_tag.c_str(), video_data.size(), (receive_video_data_callback_ ? "是" : "否"));
        } else {
            LOG_INFO("LiveStreamChannelWrapper: OnReceiveVideoData - 收到视频数据, 大小: %zu, 关键帧: 是, callback是否设置: %s", 
                video_data.size(), (receive_video_data_callback_ ? "是" : "否"));
        }
    }
    if (receive_video_data_callback_) {
        auto buffer = drc_sdk::ByteBufferPool::Instance()->AcquireShared();
        buffer->Assign(video_data.data(), video_data.size());
        // 增加诊断日志：只在关键帧时记录，减少日志量
        if (is_key_frame) {
            if (!stream_tag.empty()) {
                LOG_INFO("LiveStreamChannelWrapper[%s]: OnReceiveVideoData - 调用receive_video_data_callback_, 大小: %zu, 关键帧: 是", 
                    stream_tag.c_str(), buffer->Size());
            } else {
                LOG_INFO("LiveStreamChannelWrapper: OnReceiveVideoData - 调用receive_video_data_callback_, 大小: %zu, 关键帧: 是", 
                    buffer->Size());
            }
        }
        receive_video_data_callback_(buffer, is_key_frame);
    } else {
        // 回调未设置，记录警告（只在关键帧时记录，减少日志量）
        if (is_key_frame) {
            if (!stream_tag.empty()) {
                LOG_WARN("LiveStreamChannelWrapper[%s]: OnReceiveVideoData - receive_video_data_callback_为空，数据未传递给LiveModuleImpl, 大小: %zu", 
                    stream_tag.c_str(), video_data.size());
            } else {
                LOG_WARN("LiveStreamChannelWrapper: OnReceiveVideoData - receive_video_data_callback_为空，数据未传递给LiveModuleImpl, 大小: %zu", 
                    video_data.size());
            }
        }
    }
}

void LiveStreamChannelWrapper::OnReceiveBinaryMessage(const std::string& target_user_id, const std::vector<uint8_t>& message) {}

void LiveStreamChannelWrapper::OnReceiveStringMessage(const std::string& target_user_id, const std::string& message) {}

void LiveStreamChannelWrapper::OnUsersListUpdate(const std::vector<std::string>& users_list) {
    decltype(users_list_update_callbacks_) users_list_update_callbacks_tmp;
    {
        std::lock_guard<std::mutex> lock(users_list_update_callbacks_mutex_);
        users_list_update_callbacks_tmp = users_list_update_callbacks_;
    }
    for (auto& callback : users_list_update_callbacks_tmp) {
        callback(users_list);
    }
}

void LiveStreamChannelWrapper::OnTokenPrivilegeWillExpire() {
    if (token_privilege_will_expire_callback_) {
        token_privilege_will_expire_callback_();
    }
}

void LiveStreamChannelWrapper::OnLicenseWillExpireWarning(int left_days) {
    if (license_will_expire_warning_callback_) {
        license_will_expire_warning_callback_(left_days);
    }
}

void LiveStreamChannelWrapper::OnGlobalError(const std::string& error, const std::string& message) {
    if (global_error_callback_) {
        global_error_callback_(error, message);
    }
}

void LiveStreamChannelWrapper::SendVideoData(const std::shared_ptr<drc_sdk::ByteBuffer> &data, const VideoFrameInfo &info) {
    // nothing to do
}

void LiveStreamChannelWrapper::SendAudioData(const std::shared_ptr<drc_sdk::ByteBuffer> &data, const AudioFrameInfo &info) {
    // nothing to do
}