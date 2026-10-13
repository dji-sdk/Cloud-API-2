// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "api/peer_connection_interface.h"
#include "modules/video_coding/include/video_error_codes.h"
#include "api/video_codecs/video_decoder.h"
#include "api/video_codecs/video_decoder_factory.h"
#include "api/video_codecs/sdp_video_format.h"
#include "api/stats/rtc_stats_report.h"
#include "api/stats/rtcstats_objects.h"
#include <string>
#include <functional>
#include <utility>
#include "base/common/logger.h"

namespace message_engine {

// PeerConnectionObserver类
class PeerConnectionObserver : public webrtc::PeerConnectionObserver {
public:
    PeerConnectionObserver(rtc::Thread* signaling_thread, const std::string& stream_tag = "") 
        : signaling_thread_(signaling_thread), stream_tag_(stream_tag) {}
    
    void SetErrorCallback(std::function<void(int error_code, const std::string& error_message)> callback) {
        error_callback_ = std::move(callback);
    }
    
    void OnSignalingChange(webrtc::PeerConnectionInterface::SignalingState new_state) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 信令状态变更: %d", stream_tag_.c_str(), static_cast<int>(new_state));
        } else {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver] 信令状态变更: %d", static_cast<int>(new_state));
        }
    }
    
    void OnAddTrack(rtc::scoped_refptr<webrtc::RtpReceiverInterface> receiver,
                    const std::vector<rtc::scoped_refptr<webrtc::MediaStreamInterface>>& streams) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 收到新的轨道", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver] 收到新的轨道");
        }
        auto track = receiver->track();
        if (track && track->kind() == webrtc::MediaStreamTrackInterface::kVideoKind) {
            if (!stream_tag_.empty()) {
                LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 收到视频轨道 - 原始码流模式,不设置VideoSink", stream_tag_.c_str());
                LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 视频轨道状态: %s", stream_tag_.c_str(), (track->state() == webrtc::MediaStreamTrackInterface::kLive ? "Live" : "Ended"));
                LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 视频轨道启用状态: %s", stream_tag_.c_str(), (track->enabled() ? "Enabled" : "Disabled"));
            } else {
                LOG_INFO("[LibWebRtc][PeerConnectionObserver] 收到视频轨道 - 原始码流模式,不设置VideoSink");
                LOG_INFO("[LibWebRtc][PeerConnectionObserver] 视频轨道状态: %s", (track->state() == webrtc::MediaStreamTrackInterface::kLive ? "Live" : "Ended"));
                LOG_INFO("[LibWebRtc][PeerConnectionObserver] 视频轨道启用状态: %s", (track->enabled() ? "Enabled" : "Disabled"));
            }
            
            // 在原始码流模式下,我们不需要设置VideoSink
            // 因为原始数据会在自定义解码器中被拦截
            
        } else if (track) {
            if (!stream_tag_.empty()) {
                LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 收到非视频轨道: %s", stream_tag_.c_str(), track->kind().c_str());
            } else {
                LOG_INFO("[LibWebRtc][PeerConnectionObserver] 收到非视频轨道: %s", track->kind().c_str());
            }
        } else {
            if (!stream_tag_.empty()) {
                LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 收到轨道但track为空", stream_tag_.c_str());
            } else {
                LOG_INFO("[LibWebRtc][PeerConnectionObserver] 收到轨道但track为空");
            }
        }
    }
    
    void OnRemoveTrack(rtc::scoped_refptr<webrtc::RtpReceiverInterface> receiver) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 移除轨道", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver] 移除轨道");
        }
    }
    
    void OnDataChannel(rtc::scoped_refptr<webrtc::DataChannelInterface> channel) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 收到数据通道", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver] 收到数据通道");
        }
    }
    
    void OnRenegotiationNeeded() override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 需要重新协商", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver] 需要重新协商");
        }
    }
    
    void OnIceConnectionChange(webrtc::PeerConnectionInterface::IceConnectionState new_state) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] ICE连接状态变更: %d", stream_tag_.c_str(), static_cast<int>(new_state));
        } else {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver] ICE连接状态变更: %d", static_cast<int>(new_state));
        }
        
        switch (new_state) {
            case webrtc::PeerConnectionInterface::kIceConnectionNew:
                if (!stream_tag_.empty()) {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s]  ICE状态: New (新建)", stream_tag_.c_str());
                } else {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver]  ICE状态: New (新建)");
                }
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionChecking:
                if (!stream_tag_.empty()) {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s]  ICE状态: Checking (检查中)", stream_tag_.c_str());
                } else {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver]  ICE状态: Checking (检查中)");
                }
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionConnected:
                if (!stream_tag_.empty()) {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s]  ICE状态: Connected (已连接)", stream_tag_.c_str());
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] ICE连接已建立,开始接收原始码流...", stream_tag_.c_str());
                } else {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver]  ICE状态: Connected (已连接)");
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver] ICE连接已建立,开始接收原始码流...");
                }
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionCompleted:
                if (!stream_tag_.empty()) {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s]  ICE状态: Completed (已完成)", stream_tag_.c_str());
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] ICE连接完成,原始码流接收中...", stream_tag_.c_str());
                } else {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver]  ICE状态: Completed (已完成)");
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver] ICE连接完成,原始码流接收中...");
                }
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionFailed:
                if (!stream_tag_.empty()) {
                    LOG_ERROR("[LibWebRtc][PeerConnectionObserver][%s]  ICE状态: Failed (失败)", stream_tag_.c_str());
                } else {
                    LOG_ERROR("[LibWebRtc][PeerConnectionObserver]  ICE状态: Failed (失败)");
                }
                if (error_callback_) {
                    error_callback_(2001, "ICE连接失败");
                }
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionDisconnected:
                if (!stream_tag_.empty()) {
                    LOG_WARN("[LibWebRtc][PeerConnectionObserver][%s]  ICE状态: Disconnected (断开)", stream_tag_.c_str());
                } else {
                    LOG_WARN("[LibWebRtc][PeerConnectionObserver]  ICE状态: Disconnected (断开)");
                }
                if (error_callback_) {
                    error_callback_(2002, "ICE连接断开");
                }
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionClosed:
                if (!stream_tag_.empty()) {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s]  ICE状态: Closed (关闭)", stream_tag_.c_str());
                } else {
                    LOG_INFO("[LibWebRtc][PeerConnectionObserver]  ICE状态: Closed (关闭)");
                }
                break;
            default:
                if (!stream_tag_.empty()) {
                    LOG_WARN("[LibWebRtc][PeerConnectionObserver][%s]  ICE状态: %d", stream_tag_.c_str(), static_cast<int>(new_state));
                } else {
                    LOG_WARN("[LibWebRtc][PeerConnectionObserver]  ICE状态: %d", static_cast<int>(new_state));
                }
                break;
        }
    }
    
    void OnIceGatheringChange(webrtc::PeerConnectionInterface::IceGatheringState new_state) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] ICE收集状态变更: %d", stream_tag_.c_str(), static_cast<int>(new_state));
        } else {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver] ICE收集状态变更: %d", static_cast<int>(new_state));
        }
    }
    
    void OnIceCandidate(const webrtc::IceCandidateInterface* candidate) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s] 收到ICE候选", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver] 收到ICE候选");
        }
        
        // 打印ICE候选详细信息
        std::string candidate_str;
        candidate->ToString(&candidate_str);
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver][%s]  ICE候选: %s", stream_tag_.c_str(), candidate_str.c_str());
        } else {
            LOG_INFO("[LibWebRtc][PeerConnectionObserver]  ICE候选: %s", candidate_str.c_str());
        }
    }
    
private:
    rtc::Thread* signaling_thread_;
    std::function<void(int error_code, const std::string& error_message)> error_callback_;
    std::string stream_tag_;  // 流标识标签，用于日志过滤和定位
};
    
// SDP创建观察者
class CreateSDPObserver : public webrtc::CreateSessionDescriptionObserver {
public:
    CreateSDPObserver(std::function<void(const std::string&)> on_success, std::function<void(const std::string&)> on_failure, const std::string& stream_tag = "")
        : on_success_(on_success), on_failure_(on_failure), stream_tag_(stream_tag) {}
    
    void OnSuccess(webrtc::SessionDescriptionInterface* desc) override {
        std::string sdp;
        desc->ToString(&sdp);
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][CreateSDPObserver][%s] SDP Offer创建成功", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][CreateSDPObserver] SDP Offer创建成功");
        }
        on_success_(sdp);
    }
    
    void OnFailure(webrtc::RTCError error) override {
        if (!stream_tag_.empty()) {
            LOG_ERROR("[LibWebRtc][CreateSDPObserver][%s] SDP Offer创建失败: %s", stream_tag_.c_str(), error.message());
        } else {
            LOG_ERROR("[LibWebRtc][CreateSDPObserver] SDP Offer创建失败: %s", error.message());
        }
        if (on_failure_) {
            on_failure_(error.message());
        }
    }
    
private:
    std::function<void(const std::string&)> on_success_;
    std::function<void(const std::string&)> on_failure_;
    std::string stream_tag_;  // 流标识标签，用于日志过滤和定位
};

// SDP设置观察者
class SetSDPObserver : public webrtc::SetSessionDescriptionObserver {
public:
    SetSDPObserver(std::function<void()> on_success, std::function<void(const std::string&)> on_failure, const std::string& stream_tag = "") 
        : on_success_(on_success), on_failure_(on_failure), stream_tag_(stream_tag) {}
    
    void OnSuccess() override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][SetSDPObserver][%s] SDP设置成功", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][SetSDPObserver] SDP设置成功");
        }
        if (on_success_) {
            on_success_();
        }
    }
    
    void OnFailure(webrtc::RTCError error) override {
        if (!stream_tag_.empty()) {
            LOG_ERROR("[LibWebRtc][SetSDPObserver][%s] SDP设置失败: %s", stream_tag_.c_str(), error.message());
        } else {
            LOG_ERROR("[LibWebRtc][SetSDPObserver] SDP设置失败: %s", error.message());
        }
        if (on_failure_) {
            on_failure_(error.message());
        }
    }
    
private:
    std::function<void()> on_success_;
    std::function<void(const std::string&)> on_failure_;
    std::string stream_tag_;  // 流标识标签，用于日志过滤和定位
};


// 自定义解码器 - 不进行实际解码,而是保存原始码流
class RawStreamVideoReceiver : public webrtc::VideoDecoder {
public:
    RawStreamVideoReceiver(const std::string& codec_name, std::function<void(const webrtc::EncodedImage&)> on_frame, const std::string& stream_tag = "") 
        : codec_name_(codec_name), configured_(false), on_frame_(on_frame), stream_tag_(stream_tag) {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][RawStreamVideoDecoder][%s] 创建: %s", stream_tag_.c_str(), codec_name_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][RawStreamVideoDecoder] 创建: %s", codec_name_.c_str());
        }
    }
    
    ~RawStreamVideoReceiver() {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver][%s] 析构: %s", stream_tag_.c_str(), codec_name_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver] 析构: %s", codec_name_.c_str());
        }
    }
    
    bool Configure(const Settings& settings) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver][%s] Configure调用", stream_tag_.c_str());
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver][%s]  编解码器: %s", stream_tag_.c_str(), codec_name_.c_str());
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver][%s]  最大渲染分辨率: %dx%d", 
                        stream_tag_.c_str(), settings.max_render_resolution().Width(), 
                        settings.max_render_resolution().Height());
        } else {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver] Configure调用");
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver]  编解码器: %s", codec_name_.c_str());
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver]  最大渲染分辨率: %dx%d", 
                        settings.max_render_resolution().Width(), 
                        settings.max_render_resolution().Height());
        }
        configured_ = true;
        return true;
    }
    
    int32_t Decode(const webrtc::EncodedImage& input_image, 
                    bool missing_frames,
                    int64_t render_time_ms) override {
        if (!configured_) {
            LOG_ERROR("[LibWebRtc][RawStreamVideoReceiver] 解码器未配置");
            return WEBRTC_VIDEO_CODEC_UNINITIALIZED;
        }
        
        if (!input_image.data() || input_image.size() == 0) {
            LOG_ERROR("[LibWebRtc][RawStreamVideoReceiver] 输入图像数据为空");
            return WEBRTC_VIDEO_CODEC_ERR_PARAMETER;
        }
        
        // 检查帧类型
        bool is_keyframe = (input_image._frameType == webrtc::VideoFrameType::kVideoFrameKey);
        
        // 启用日志用于诊断：只在关键帧时打印，减少日志量
        if (is_keyframe) {
            if (!stream_tag_.empty()) {
                LOG_INFO("[LibWebRtc][RawStreamVideoReceiver][%s] 接收到关键帧(I帧): 大小=%ld, RTP时间戳=%ld, 丢帧=%s", 
                            stream_tag_.c_str(), input_image.size(), 
                            input_image.RtpTimestamp(),
                            (missing_frames ? "是" : "否"));
            } else {
                LOG_INFO("[LibWebRtc][RawStreamVideoReceiver] 接收到关键帧(I帧): 大小=%ld, RTP时间戳=%ld, 丢帧=%s", 
                            input_image.size(), 
                            input_image.RtpTimestamp(),
                            (missing_frames ? "是" : "否"));
            }
        }
        on_frame_(input_image);
        return WEBRTC_VIDEO_CODEC_OK;
    }
    
    int32_t RegisterDecodeCompleteCallback(webrtc::DecodedImageCallback* callback) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver][%s] RegisterDecodeCompleteCallback调用", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver] RegisterDecodeCompleteCallback调用");
        }
        decode_callback_ = callback;
        return WEBRTC_VIDEO_CODEC_OK;
    }
    
    int32_t Release() override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver][%s] Release调用", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiver] Release调用");
        }
        configured_ = false;
        decode_callback_ = nullptr;
        return WEBRTC_VIDEO_CODEC_OK;
    }
    
    const char* ImplementationName() const override {
        return "RawStreamVideoReceiver";
    }
    
private:
    std::string codec_name_;
    bool configured_;
    webrtc::DecodedImageCallback* decode_callback_;
    std::function<void(const webrtc::EncodedImage&)> on_frame_;
    std::string stream_tag_;  // 流标识标签，用于日志过滤和定位
};

// 支持H264/H265的原始码流解码器工厂类
class RawStreamVideoReceiverFactory : public webrtc::VideoDecoderFactory {
public:
    RawStreamVideoReceiverFactory(std::function<void(const webrtc::EncodedImage&)> on_frame, const std::string& stream_tag = "") 
        : on_frame_(on_frame), stream_tag_(stream_tag) {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory][%s] 创建", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory] 创建");
        }
    }

    ~RawStreamVideoReceiverFactory() {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory][%s] 析构", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory] 析构");
        }
    }

    std::vector<webrtc::SdpVideoFormat> GetSupportedFormats() const override {
        std::vector<webrtc::SdpVideoFormat> formats;
        
        // 支持H.264
        webrtc::SdpVideoFormat h264_mode1(
            "H264",
            {{"level-asymmetry-allowed", "1"},
                {"packetization-mode", "1"},
                {"profile-level-id", "42e01f"}}
        );
        formats.push_back(h264_mode1);
        
        webrtc::SdpVideoFormat h264_mode0(
            "H264",
            {{"level-asymmetry-allowed", "1"},
                {"packetization-mode", "0"},
                {"profile-level-id", "42e01f"}}
        );
        formats.push_back(h264_mode0);
        
        // 支持H.265 (如果需要)
        webrtc::SdpVideoFormat h265(
            "H265",
            {{"profile-id", "1"}}
        );
        formats.push_back(h265);
        
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory][%s] 支持H264和H265格式", stream_tag_.c_str());
        } else {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory] 支持H264和H265格式");
        }
        
        return formats;
    }
    
    std::unique_ptr<webrtc::VideoDecoder> Create(
        const webrtc::Environment& env,
        const webrtc::SdpVideoFormat& format) override {
        if (!stream_tag_.empty()) {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory][%s] Create被调用,格式: %s", stream_tag_.c_str(), format.name.c_str());
        } else {
            LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory] Create被调用,格式: %s", format.name.c_str());
        }
        
        if (format.name == "H264") {
            if (!stream_tag_.empty()) {
                LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory][%s] 创建H264原始码流解码器,参数: ", stream_tag_.c_str());
                for (const auto& param : format.parameters) {
                    LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory][%s]  参数: %s=%s", stream_tag_.c_str(), param.first.c_str(), param.second.c_str());
                }
            } else {
                LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory] 创建H264原始码流解码器,参数: ");
                for (const auto& param : format.parameters) {
                    LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory]  参数: %s=%s", param.first.c_str(), param.second.c_str());
                }
            }
            
            return std::make_unique<RawStreamVideoReceiver>("H264", on_frame_, stream_tag_);
        } else if (format.name == "H265" || format.name == "HEVC") {
            if (!stream_tag_.empty()) {
                LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory][%s] 创建H265原始码流解码器", stream_tag_.c_str());
            } else {
                LOG_INFO("[LibWebRtc][RawStreamVideoReceiverFactory] 创建H265原始码流解码器");
            }
            return std::make_unique<RawStreamVideoReceiver>("H265", on_frame_, stream_tag_);
        } else {
            if (!stream_tag_.empty()) {
                LOG_ERROR("[LibWebRtc][RawStreamVideoReceiverFactory][%s] 不支持的视频格式: %s", stream_tag_.c_str(), format.name.c_str());
            } else {
                LOG_ERROR("[LibWebRtc][RawStreamVideoReceiverFactory] 不支持的视频格式: %s", format.name.c_str());
            }
        }
        return nullptr;
    }

private:
    std::function<void(const webrtc::EncodedImage&)> on_frame_;
    std::string stream_tag_;  // 流标识标签，用于日志过滤和定位
};


// 添加StatsObserver来获取RTP统计
class StatsObserver : public webrtc::RTCStatsCollectorCallback {
public:
    void OnStatsDelivered(const rtc::scoped_refptr<const webrtc::RTCStatsReport>& report) override {
        LOG_INFO("[LibWebRtc][StatsObserver] RTP统计报告");
        
        // 检查入站RTP流统计
        bool found_video_receiver = false;
        for (const auto& stats : *report) {
            if (stats.type() == webrtc::RTCInboundRtpStreamStats::kType) {
                const auto& inbound_stats = static_cast<const webrtc::RTCInboundRtpStreamStats&>(stats);
                if (inbound_stats.kind.has_value() && *inbound_stats.kind == "video") {
                    found_video_receiver = true;
                    LOG_INFO("[LibWebRtc][StatsObserver] 找到视频接收器统计");
                    if (inbound_stats.packets_received.has_value()) {
                        LOG_INFO("[LibWebRtc][StatsObserver]  接收包数: %d", *inbound_stats.packets_received);
                    }
                    if (inbound_stats.bytes_received.has_value()) {
                        LOG_INFO("[LibWebRtc][StatsObserver]  接收字节数: %d", *inbound_stats.bytes_received);
                    }
                    if (inbound_stats.frames_decoded.has_value()) {
                        LOG_INFO("[LibWebRtc][StatsObserver]  解码帧数: %d", *inbound_stats.frames_decoded);
                    }
                    if (inbound_stats.frames_dropped.has_value()) {
                        LOG_INFO("[LibWebRtc][StatsObserver]  丢弃帧数: %d", *inbound_stats.frames_dropped);
                    }
                    if (inbound_stats.last_packet_received_timestamp.has_value()) {
                        LOG_INFO("[LibWebRtc][StatsObserver]  最后包接收时间: %d", *inbound_stats.last_packet_received_timestamp);
                    }
                    if (inbound_stats.packets_lost.has_value()) {
                        LOG_INFO("[LibWebRtc][StatsObserver]  丢包数: %d", *inbound_stats.packets_lost);
                    }
                }
            }
        }
        
        if (!found_video_receiver) {
            LOG_INFO("[LibWebRtc][StatsObserver] 未找到视频接收器统计信息");
        }
        
        LOG_INFO("[LibWebRtc][StatsObserver] === 统计报告结束 ===");
    }
};

} // namespace message_engine
