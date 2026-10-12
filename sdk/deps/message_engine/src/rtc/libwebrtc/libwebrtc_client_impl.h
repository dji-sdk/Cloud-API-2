// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

// #define WEBRTC_POSIX 1
#include "api/peer_connection_interface.h"
#include "api/create_peerconnection_factory.h"
#include "api/video_codecs/video_decoder_factory.h"
#include "common/util/async_capture_protect.h"
#include <string>
#include <memory>
#include <mutex>
#include <condition_variable>
#include "base/io/byte_buffer.h"
#include "base/async/executor.h"

// Forward declaration for WebRTC logging
namespace rtc {
    class FileRotatingLogSink;
    class LogSink;
    class Thread;
}

// Forward declaration for shared thread pool
namespace message_engine {
    class LibwebrtcThreadPool;
}

namespace message_engine {

using StartLibwebrtcClientCallback = std::function<void(bool success, const int err_code, const std::string& error_message)>;

class LibwebrtcClientImpl : public drc_sdk::common::AsyncCaptureProtect {
public:
    LibwebrtcClientImpl(const std::string& whep_url, const std::shared_ptr<::drc_sdk::Executor> &executor, const std::string& stream_tag = "");
    ~LibwebrtcClientImpl() override;
    bool Initialize();
    
    bool Start(StartLibwebrtcClientCallback start_callback);
    
    void SetVideoFrameCallback(std::function<void(std::shared_ptr<drc_sdk::ByteBuffer> frame, bool is_key_frame)> callback);

    void SetErrorCallback(std::function<void(int error_code, const std::string& error_message)> callback);

    void Stop();

private:
    void OnOfferCreated(const std::string& offer_sdp, StartLibwebrtcClientCallback on_offer_created);
    void SendWHEPRequest(const std::string& offer_sdp, StartLibwebrtcClientCallback on_offer_created);
    void CheckRTPStats();
    void OnVideoFrame(const webrtc::EncodedImage& frame);
    void NotifyError(int error_code, const std::string& error_message);
    
    std::string whep_url_;
    std::string whep_session_url_;
    std::atomic<bool> should_stop_;
    std::atomic<bool> is_initialized_;
    std::atomic<int64_t> last_frame_time_;
    std::atomic<bool> has_received_frame_;
    
    // 使用共享线程池，而不是每个客户端创建独立线程
    // 这样可以避免线程泄漏：14路流只需要3个线程，而不是42个线程
    rtc::Thread* network_thread_;  // 从共享线程池获取
    rtc::Thread* worker_thread_;   // 从共享线程池获取
    rtc::Thread* signaling_thread_; // 从共享线程池获取
    
    // 每个客户端创建自己的 VideoDecoderFactory 和 PeerConnectionFactory
    // 这样可以确保回调路由正确，支持多客户端场景
    std::unique_ptr<webrtc::VideoDecoderFactory> video_decoder_factory_;
    rtc::scoped_refptr<webrtc::PeerConnectionFactoryInterface> pc_factory_;
    rtc::scoped_refptr<webrtc::PeerConnectionInterface> peer_connection_;
    std::unique_ptr<webrtc::PeerConnectionObserver> pc_observer_;

    std::shared_ptr<drc_sdk::Executor> executor_;
    std::function<void(std::shared_ptr<drc_sdk::ByteBuffer> frame, bool is_key_frame)> video_frame_callback_;
    std::function<void(int error_code, const std::string& error_message)> error_callback_;
    
    std::string stream_tag_;  // 流标识标签，格式: device_sn_payload_index_video_id，用于日志过滤和定位
    
    // WebRTC日志文件输出
    std::unique_ptr<rtc::FileRotatingLogSink> file_log_sink_;  // 底层文件日志
    std::unique_ptr<rtc::LogSink> log_sink_;  // 包装后的日志（带系统时间戳）
    
    // 用于等待 Close() 完成的同步对象（成员变量，避免栈变量被销毁导致 Use-After-Free）
    // 参考 libdatachannel 和 srtc 的实现方式
    std::mutex close_mutex_;           // 保护 close_completed_ 的互斥锁
    std::condition_variable close_cv_; // 用于等待 Close() 完成的条件变量
    bool close_completed_;             // Close() 是否已完成
};

} // namespace message_engine