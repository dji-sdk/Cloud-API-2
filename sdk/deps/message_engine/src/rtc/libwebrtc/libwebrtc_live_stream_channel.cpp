// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "libwebrtc_live_stream_channel.h"
#include "base/common/logger.h"

namespace message_engine {

LibwebrtcLiveStreamChannel::LibwebrtcLiveStreamChannel(const ChannelInfo& channel_info, const std::shared_ptr<::drc_sdk::Executor> &working_executor) :
    LiveStreamChannel(channel_info, "", working_executor) {
    std::string stream_tag = channel_info.stream_tag_.empty() ? channel_info.channel_name_ : channel_info.stream_tag_;
    if (!stream_tag.empty()) {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] ctor", stream_tag.c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel] ctor");
    }
}

LibwebrtcLiveStreamChannel::~LibwebrtcLiveStreamChannel() {
    std::string stream_tag = channel_info_.stream_tag_.empty() ? channel_info_.channel_name_ : channel_info_.stream_tag_;
    if (!stream_tag.empty()) {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] dtor", stream_tag.c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel] dtor");
    }
    if (client_) {
        client_->Stop();
        client_ = nullptr;
    }
}

bool LibwebrtcLiveStreamChannel::Join(const JoinChannelCallback &callback) {
    // 传递stream_tag用于日志标识
    std::string stream_tag = channel_info_.stream_tag_.empty() ? channel_info_.channel_name_ : channel_info_.stream_tag_;
    if (!stream_tag.empty()) {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] Join", stream_tag.c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel] Join");
    }
    client_ = std::make_shared<LibwebrtcClientImpl>(channel_info_.url_, working_executor_, stream_tag);
    WeakDummy(p);
    if (!stream_tag.empty()) {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] Join - 设置SetVideoFrameCallback", stream_tag.c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel] Join - 设置SetVideoFrameCallback");
    }
    client_->SetVideoFrameCallback([this, p, stream_tag](std::shared_ptr<drc_sdk::ByteBuffer> frame, bool is_key_frame) {
        WeakDummyReturn(p);
        // 增加诊断日志：只在关键帧时记录，减少日志量
        if (is_key_frame) {
            if (!stream_tag.empty()) {
                LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] SetVideoFrameCallback回调 - 收到视频帧, 大小: %zu, 关键帧: 是, receive_video_data_callback_是否设置: %s", 
                    stream_tag.c_str(), frame->Size(), (receive_video_data_callback_ ? "是" : "否"));
            } else {
                LOG_INFO("[message_engine][libwebrtc_live_stream_channel] SetVideoFrameCallback回调 - 收到视频帧, 大小: %zu, 关键帧: 是, receive_video_data_callback_是否设置: %s", 
                    frame->Size(), (receive_video_data_callback_ ? "是" : "否"));
            }
        }
        if (receive_video_data_callback_) {
            // 增加诊断日志：只在关键帧时记录，减少日志量
            if (is_key_frame) {
                if (!stream_tag.empty()) {
                    LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] SetVideoFrameCallback回调 - 调用receive_video_data_callback_, 大小: %zu, 关键帧: 是", 
                        stream_tag.c_str(), frame->Size());
                } else {
                    LOG_INFO("[message_engine][libwebrtc_live_stream_channel] SetVideoFrameCallback回调 - 调用receive_video_data_callback_, 大小: %zu, 关键帧: 是", 
                        frame->Size());
                }
            }
            receive_video_data_callback_(frame, is_key_frame);
        } else {
            // 回调未设置，记录警告（只在关键帧时记录，减少日志量）
            if (is_key_frame) {
                if (!stream_tag.empty()) {
                    LOG_WARN("[message_engine][libwebrtc_live_stream_channel][%s] SetVideoFrameCallback回调 - receive_video_data_callback_为空，数据未传递给LiveStreamChannelWrapper, 大小: %zu", 
                        stream_tag.c_str(), frame->Size());
                } else {
                    LOG_WARN("[message_engine][libwebrtc_live_stream_channel] SetVideoFrameCallback回调 - receive_video_data_callback_为空，数据未传递给LiveStreamChannelWrapper, 大小: %zu", 
                        frame->Size());
                }
            }
        }
    });
    if (!stream_tag.empty()) {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] Join - SetVideoFrameCallback设置完成", stream_tag.c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel] Join - SetVideoFrameCallback设置完成");
    }
    
    client_->SetErrorCallback([this, p](int error_code, const std::string& error_message) {
        WeakDummyReturn(p);
        if (global_error_callback_) {
            // 将错误码转换为字符串，格式为 "WebRTC_ErrorCode_XXXX"
            std::string error_code_str = "WebRTC_ErrorCode_" + std::to_string(error_code);
            global_error_callback_(error_code_str, error_message);
        }
    });
    if (!client_->Initialize()) {
        if (!stream_tag.empty()) {
            LOG_ERROR("[message_engine][libwebrtc_live_stream_channel][%s] Initialize failed", stream_tag.c_str());
        } else {
            LOG_ERROR("[message_engine][libwebrtc_live_stream_channel] Initialize failed");
        }
        callback(false, 0, false, 0, "Initialize failed");
        return false;
    }
    auto ret = client_->Start([this, p, callback](bool success, const int err_code, const std::string& error_message) {
        WeakDummyReturn(p);
        if (success) {
            callback(true, 0, false, 0, "Join success");
        } else {
            callback(false, err_code, false, 0, error_message);
        }
    });
    return ret;
}

bool LibwebrtcLiveStreamChannel::Leave() {
    std::string stream_tag = channel_info_.stream_tag_.empty() ? channel_info_.channel_name_ : channel_info_.stream_tag_;
    if (!stream_tag.empty()) {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] Leave", stream_tag.c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel] Leave");
    }
    client_->Stop();
    client_ = nullptr;
    if (!stream_tag.empty()) {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] Leave success", stream_tag.c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel] Leave success");
    }
    return true;
}

bool LibwebrtcLiveStreamChannel::RenewToken(const std::string &token) {
    std::string stream_tag = channel_info_.stream_tag_.empty() ? channel_info_.channel_name_ : channel_info_.stream_tag_;
    if (!stream_tag.empty()) {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel][%s] RenewToken", stream_tag.c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_live_stream_channel] RenewToken");
    }
    return true;
}

} // namespace message_engine