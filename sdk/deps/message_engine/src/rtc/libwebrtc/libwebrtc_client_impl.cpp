// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "libwebrtc_client_impl.h"
#include "libwebrtc_thread_pool.h"
#include <thread>
#include <chrono>
#include <iostream>
#include <sys/stat.h>
#include <sys/types.h>
#include <iomanip>
#include <sstream>
#include <ctime>
#include <mutex>
#include <condition_variable>
#include "base/common/logger.h"
#include "common/log_safe.h"
#include "api/audio_codecs/builtin_audio_decoder_factory.h"
#include "api/audio_codecs/builtin_audio_encoder_factory.h"
#include "common/util/async_capture_protect.h"
#include "rtc_base/thread.h"
#include "rtc_base/logging.h"
#include "rtc_base/log_sinks.h"
#include "rtc_base/ssl_adapter.h"
#include "modules/audio_device/include/fake_audio_device.h"
#include "libwebrtc_tools.h"
#include "common/network/network.h"

namespace message_engine {

// 辅助函数：格式化日志前缀，包含stream_tag
static std::string FormatLogPrefix(const std::string& stream_tag) {
    if (stream_tag.empty()) {
        return "[LibWebRtc][LibwebrtcClientImpl]";
    }
    return "[LibWebRtc][LibwebrtcClientImpl][" + stream_tag + "]";
}

// 自定义LogSink，用于将WebRTC日志的时间戳改为系统时间
class SystemTimeLogSink : public rtc::LogSink {
public:
    explicit SystemTimeLogSink(rtc::FileRotatingLogSink* file_sink) 
        : file_sink_(file_sink) {}
    
    void OnLogMessage(const std::string& message, rtc::LoggingSeverity severity) override {
        OnLogMessage(message);
    }
    
    void OnLogMessage(const std::string& message) override {
        if (!file_sink_) {
            return;
        }
        
        // 获取当前系统时间
        auto now = std::chrono::system_clock::now();
        auto time_t = std::chrono::system_clock::to_time_t(now);
        auto ms = std::chrono::duration_cast<std::chrono::milliseconds>(
            now.time_since_epoch()) % 1000;
        
        // 格式化为: YYYY-MM-DD HH:MM:SS.mmm (与SDK日志格式一致)
        // SDK日志格式: 2025-11-22 11:06:05.364
        std::tm* tm_info = std::localtime(&time_t);
        std::ostringstream oss;
        oss << std::put_time(tm_info, "%Y-%m-%d %H:%M:%S");
        oss << "." << std::setfill('0') << std::setw(3) << ms.count();
        
        std::string system_timestamp = oss.str();  // 格式: 2025-11-22 11:06:05.364
        
        // WebRTC日志格式: [mm:ss:mmm][thread_id] (file:line): message
        // 例如: [000:000][78233] (input_volume_stats_reporter.cc:98): ...
        // 我们保留WebRTC的相对时间戳，在开头添加系统时间戳
        // 最终格式: 2025-11-22 11:06:05.364 [000:000][78233] (file:line): message
        std::string formatted_message;
        
        // 检查消息是否已经包含系统时间戳（避免重复添加）
        if (message.find(system_timestamp) == std::string::npos) {
            // 在开头添加系统时间戳，保留原有的WebRTC时间戳
            formatted_message = system_timestamp + " " + message;
        } else {
            formatted_message = message;
        }
        
        // 写入到FileRotatingLogSink
        file_sink_->OnLogMessage(formatted_message);
    }
    
private:
    rtc::FileRotatingLogSink* file_sink_;
};

LibwebrtcClientImpl::LibwebrtcClientImpl(const std::string& whep_url, const std::shared_ptr<::drc_sdk::Executor> &executor, const std::string& stream_tag)
    : whep_url_(whep_url), should_stop_(false), is_initialized_(false), executor_(executor), 
      last_frame_time_(0), has_received_frame_(false), stream_tag_(stream_tag),
      network_thread_(nullptr), worker_thread_(nullptr), signaling_thread_(nullptr),
      close_completed_(false) {}

LibwebrtcClientImpl::~LibwebrtcClientImpl() {
    if (!stream_tag_.empty()) {
        LOG_INFO("%s ~LibwebrtcClientImpl", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_client_impl] ~LibwebrtcClientImpl");
    }
    
    // 首先设置停止标志，防止新的回调被触发
    should_stop_.store(true);
    
    // 首先清理回调，防止在析构过程中还有新的回调被触发
    video_frame_callback_ = nullptr;
    error_callback_ = nullptr;
    
    // 清理WebRTC日志
    if (log_sink_) {
        rtc::LogMessage::RemoveLogToStream(log_sink_.get());
        log_sink_.reset();
    }
    if (file_log_sink_) {
        file_log_sink_.reset();
    }
    
    // 保存 signaling_thread_ 的指针，用于后续的 PostTask
    rtc::Thread* sig_thread = signaling_thread_;
    
    // 调用 Stop 来关闭 PeerConnection 和清理资源
    Stop();

    // 如果还有未关闭的 peer_connection_，尝试同步关闭
    // 这种情况不应该发生（Stop() 应该已经处理了），但为了安全起见还是处理一下
    // 注意：必须在 Close() 完成后再释放 pc_factory_ 和 pc_observer_，因为它们可能被 Close() 使用
    if (peer_connection_ && sig_thread) {
        LOG_WARN("%s peer_connection_ 仍未关闭，尝试同步关闭", FormatLogPrefix(stream_tag_).c_str());
        if (sig_thread->IsCurrent()) {
            // 如果已经在 signaling_thread 上，直接同步关闭
            peer_connection_->Close();
            peer_connection_ = nullptr;
        } else if (sig_thread && !sig_thread->IsQuitting()) {
            // 使用成员变量的条件变量等待 Close() 完成（修复：避免栈变量被销毁导致 Use-After-Free）
            // 重置 close_completed_ 标志
            {
                std::lock_guard<std::mutex> lock(close_mutex_);
                close_completed_ = false;
            }
            
            rtc::scoped_refptr<webrtc::PeerConnectionInterface> pc = peer_connection_;
            peer_connection_ = nullptr;
            
            // 使用 PostTask 异步关闭
            // 使用 AsyncCaptureProtect 保护机制，防止对象析构后访问成员变量
            // WeakDummyReturn(p) 会检查对象是否还存在，如果不存在会提前返回
            // 捕获成员变量的指针，确保即使对象已销毁也能通知条件变量
            // 注意：close_mutex_ 和 close_cv_ 在析构函数中还在使用（析构函数在等待）
            std::mutex* mutex_ptr = &close_mutex_;
            std::condition_variable* cv_ptr = &close_cv_;
            bool* completed_ptr = &close_completed_;
            WeakDummy(p);
            sig_thread->PostTask([pc, p, mutex_ptr, cv_ptr, completed_ptr]() {
                // 先执行 Close()，即使对象已销毁也要执行（pc 是独立的智能指针）
                if (pc) {
                    pc->Close();
                }
                // 检查对象是否还存在，如果不存在则提前返回（避免访问已销毁的成员变量）
                WeakDummyReturn(p);
                // 如果执行到这里，说明对象还存在，可以安全访问成员变量
                // 通知等待线程 Close() 已完成（使用指针，不依赖 this）
                {
                    std::lock_guard<std::mutex> lock(*mutex_ptr);
                    *completed_ptr = true;
                }
                cv_ptr->notify_one();
            });
            
            // 等待 Close() 完成，使用成员变量的条件变量避免忙等待
            // 线程池的线程不会退出，PostTask 应该总是能执行
            // 如果对象还存在，lambda 会执行并通知；如果对象已销毁，析构函数已经完成，不会等待
            std::unique_lock<std::mutex> lock(close_mutex_);
            // 无条件等待，直到 close_completed_ 为 true
            // 参考其他库的做法（libdatachannel、srtc），不使用硬编码的超时时间
            close_cv_.wait(lock, [this]() {
                return this->close_completed_;
            });
            
            if (!stream_tag_.empty()) {
                LOG_INFO("%s 析构函数: Close() 操作已完成", FormatLogPrefix(stream_tag_).c_str());
            }
        } else {
            // signaling_thread_ 为 nullptr 或已停止，无法安全调用 Close()
            if (!sig_thread) {
                LOG_WARN("%s signaling_thread_ 为 nullptr，直接释放 peer_connection_", 
                         FormatLogPrefix(stream_tag_).c_str());
            } else {
                LOG_WARN("%s signaling_thread_ 已停止，直接释放 peer_connection_", 
                         FormatLogPrefix(stream_tag_).c_str());
            }
            peer_connection_ = nullptr;
        }
    }
    
    // 在停止线程之前，确保所有WebRTC对象已释放
    // 这些对象内部可能持有线程的引用，需要在线程活跃时释放
    // 关键：pc_factory_ 和 pc_observer_ 必须在 Close() 完成后再释放
    if (!stream_tag_.empty()) {
        LOG_INFO("%s Ensuring WebRTC objects are released...", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_client_impl] Ensuring WebRTC objects are released...");
    }
    
    // 释放 PeerConnection 相关对象
    // 注意：这些对象必须在 Close() 完成后再释放
    if (pc_observer_) {
        pc_observer_.reset();
    }
    if (peer_connection_) {
        peer_connection_ = nullptr;
    }
    if (pc_factory_) {
        pc_factory_ = nullptr;
    }
    // 注意：video_decoder_factory_ 的所有权已经转移给 pc_factory_
    // 当 pc_factory_ 被释放时，video_decoder_factory_ 也会被自动释放
    // 所以这里不需要手动释放 video_decoder_factory_（它已经是 nullptr）
    // video_decoder_factory_.reset();  // 不需要，所有权已转移
    
    if (!stream_tag_.empty()) {
        LOG_INFO("%s WebRTC objects released", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_client_impl] WebRTC objects released");
    }
    
    // 释放共享线程池的引用（不停止线程，因为其他客户端可能还在使用）
    // 线程池会在所有客户端都释放后自动清理
    auto& thread_pool = LibwebrtcThreadPool::GetInstance();
    thread_pool.ReleaseThreads();
    
    if (!stream_tag_.empty()) {
        LOG_INFO("%s 已释放共享线程池引用 (当前引用计数: %d)", FormatLogPrefix(stream_tag_).c_str(), thread_pool.GetRefCount());
    } else {
        LOG_INFO("已释放共享线程池引用 (当前引用计数: %d)", thread_pool.GetRefCount());
    }
    
    // 清理线程指针（这些是共享线程的指针，不需要delete）
    // 注意：必须在所有 PostTask 操作完成后才能设置为 nullptr
    network_thread_ = nullptr;
    worker_thread_ = nullptr;
    signaling_thread_ = nullptr;
    
    // 注意：不在这里调用CleanupSSL，因为其他客户端可能还在使用
    // SSL的清理应该在SDK关闭时统一处理
    if (!stream_tag_.empty()) {
        LOG_INFO("%s ~LibwebrtcClientImpl success", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("[message_engine][libwebrtc_client_impl] ~LibwebrtcClientImpl success");
    }
}

bool LibwebrtcClientImpl::Initialize() {
    if (!stream_tag_.empty()) {
        LOG_INFO("%s 初始化WHEP客户端（原始码流模式）...", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("初始化WHEP客户端（原始码流模式）...");
    }
    
    // 初始化WebRTC日志文件输出
    const std::string log_dir = "./webrtc_logs";
    const std::string log_prefix = "webrtc_fhsdk";
    const size_t max_log_size = 10 * 1024 * 1024;  // 10MB per file
    const size_t max_log_files = 5;                // 保留5个日志文件
    
    // 确保日志目录存在
    struct stat info;
    if (stat(log_dir.c_str(), &info) != 0) {
        // 目录不存在，创建它
        if (mkdir(log_dir.c_str(), 0755) != 0) {
            LOG_ERROR("警告: 无法创建日志目录: %s", log_dir.c_str());
        } else {
            LOG_INFO("已创建日志目录: %s", log_dir.c_str());
        }
    }
    
    // 创建FileRotatingLogSink（底层文件日志）
    file_log_sink_ = std::make_unique<rtc::FileRotatingLogSink>(
        log_dir, log_prefix, max_log_size, max_log_files);
    
    // 初始化日志文件
    if (file_log_sink_->Init()) {
        // 创建自定义LogSink，将时间戳改为系统时间
        log_sink_ = std::make_unique<SystemTimeLogSink>(file_log_sink_.get());
        
        // 启用日志文件输出，级别为LS_VERBOSE（最大详细度）
        rtc::LogMessage::AddLogToStream(log_sink_.get(), rtc::LS_VERBOSE);
        // 禁用缓冲，确保日志及时写入文件
        file_log_sink_->DisableBuffering();
        LOG_INFO("WebRTC日志文件已初始化: %s/%s*.log (使用系统时间戳)", log_dir.c_str(), log_prefix.c_str());
    } else {
        LOG_ERROR("警告: 无法初始化WebRTC日志文件");
    }
    
    // 只输出到文件，不输出到控制台
    // 禁用控制台输出（LogToDebug会输出到stderr/stdout）
    rtc::LogMessage::LogToDebug(rtc::LS_NONE);  // 设置为LS_NONE禁用控制台输出
    // 仍然启用时间戳和线程信息（这些会写入文件）
    rtc::LogMessage::LogTimestamps();
    rtc::LogMessage::LogThreads();
    
    // 初始化WebRTC SSL（只初始化一次，WebRTC内部会检查是否已初始化）
    // 注意：虽然可以安全地多次调用，但最好在首次初始化时调用
    static std::once_flag ssl_init_flag;
    std::call_once(ssl_init_flag, []() {
        rtc::InitializeSSL();
        LOG_INFO("[LibWebRtc] SSL已初始化（全局，仅一次）");
    });
    
    // 使用共享线程池，而不是每个客户端创建独立线程
    // 优势：14路流只需要3个线程，而不是42个线程，彻底解决线程泄漏问题
    auto& thread_pool = LibwebrtcThreadPool::GetInstance();
    network_thread_ = thread_pool.GetNetworkThread();
    
    // 如果获取network_thread失败，直接返回
    if (!network_thread_) {
        LOG_ERROR("从共享线程池获取network_thread失败");
        return false;
    }
    
    // 获取worker_thread和signaling_thread（引用计数已在GetNetworkThread中增加）
    worker_thread_ = thread_pool.GetWorkerThread();
    signaling_thread_ = thread_pool.GetSignalingThread();
    
    // 如果获取失败，需要回滚引用计数
    if (!worker_thread_ || !signaling_thread_) {
        LOG_ERROR("从共享线程池获取worker_thread或signaling_thread失败");
        // 回滚引用计数
        thread_pool.ReleaseThreads();
        network_thread_ = nullptr;
        worker_thread_ = nullptr;
        signaling_thread_ = nullptr;
        return false;
    }
    
    if (!stream_tag_.empty()) {
        LOG_INFO("%s 使用共享线程池 (引用计数: %d)", FormatLogPrefix(stream_tag_).c_str(), thread_pool.GetRefCount());
    } else {
        LOG_INFO("使用共享线程池 (引用计数: %d)", thread_pool.GetRefCount());
    }
    
    // 每个客户端创建自己的 VideoDecoderFactory 和 PeerConnectionFactory
    // 这样可以确保回调路由正确，支持多客户端场景
    // 虽然无法全局共享 PeerConnectionFactory，但可以共享线程池（3个线程）
    WeakDummy(p);
    video_decoder_factory_ = std::make_unique<RawStreamVideoReceiverFactory>(
        [this, p](const webrtc::EncodedImage& frame) {
            WeakDummyReturn(p);
            this->OnVideoFrame(frame);
        },
        stream_tag_
    );
    
    // 创建 PeerConnectionFactory（使用共享的线程池）
    // 注意：CreatePeerConnectionFactory 会获取 video_decoder_factory 的所有权
    // 所以使用 std::move 转移所有权，之后 video_decoder_factory_ 会变成 nullptr
    // 但 pc_factory_ 会管理 video_decoder_factory 的生命周期，所以不需要在析构函数中释放
    pc_factory_ = webrtc::CreatePeerConnectionFactory(
        network_thread_,
        worker_thread_,
        signaling_thread_,
        rtc::scoped_refptr<webrtc::AudioDeviceModule>(new webrtc::FakeAudioDeviceModule()),
        webrtc::CreateBuiltinAudioEncoderFactory(),
        webrtc::CreateBuiltinAudioDecoderFactory(),
        nullptr, // video_encoder_factory
        std::move(video_decoder_factory_),  // 转移所有权给 PeerConnectionFactory
        nullptr, // audio_mixer
        nullptr  // audio_processing
    );
    
    if (!pc_factory_) {
        LOG_ERROR("%s 创建PeerConnectionFactory失败", FormatLogPrefix(stream_tag_).c_str());
        NotifyError(1001, "创建PeerConnectionFactory失败");
        video_decoder_factory_.reset();
        return false;
    }
    
    if (!stream_tag_.empty()) {
        LOG_INFO("%s PeerConnectionFactory创建成功（原始码流模式）", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("PeerConnectionFactory创建成功（原始码流模式）");
    }
    
    is_initialized_ = true;
    return true;
}

void LibwebrtcClientImpl::OnVideoFrame(const webrtc::EncodedImage& frame) {
    // 检查是否已经停止，避免在停止过程中处理帧
    if (should_stop_.load()) {
        return;
    }
    
    // 更新最后接收帧时间
    last_frame_time_.store(rtc::TimeMillis());
    has_received_frame_.store(true);
    
    auto frame_buffer = std::make_shared<drc_sdk::ByteBuffer>(frame.data(), frame.size());
    auto is_key_frame = frame.FrameType() == webrtc::VideoFrameType::kVideoFrameKey;
    
    // 启用日志用于诊断：只在关键帧时打印，减少日志量
    if (is_key_frame) {
        LOG_INFO("%s 收到关键帧(I帧), 大小: %d, RTP时间戳: %u", 
                 FormatLogPrefix(stream_tag_).c_str(), frame.size(), frame.RtpTimestamp());
    }
    // 使用 executor 切换到工作线程执行回调
    if (!executor_) {
        LOG_WARN("%s executor_ 为空，无法执行回调", FormatLogPrefix(stream_tag_).c_str());
        return;
    }
    
    // 捕获回调的副本，确保即使 Stop() 清空了回调，已提交的任务仍能执行
    auto callback_copy = video_frame_callback_;
    if (!callback_copy) {
        // 回调未设置，直接返回
        return;
    }

    WeakDummy(p);
    executor_->Post([this, p, frame_buffer, is_key_frame, callback_copy]() {
        WeakDummyReturn(p);
        // 再次检查是否已经停止，避免在停止后执行回调
        if (this->should_stop_.load()) {
            return;
        }
        // 使用捕获的回调副本，而不是成员变量，确保回调能执行
        if (callback_copy) {
            // 只在关键帧时记录，减少日志量
            if (is_key_frame) {
                LOG_INFO("%s 调用video_frame_callback_, 大小: %zu, 关键帧: 是", 
                         FormatLogPrefix(this->stream_tag_).c_str(), frame_buffer->Size());
            }
            callback_copy(frame_buffer, is_key_frame);
        }
    });
}

bool LibwebrtcClientImpl::Start(StartLibwebrtcClientCallback start_callback) {
    if (!is_initialized_) {
        LOG_ERROR("客户端未初始化");
        start_callback(false, 0, "客户端未初始化");
        return false;
    }
    
    // 修复：重置 should_stop_ 标志，允许重新启动
    // 注意：必须在 PostTask 之前重置，确保 lambda 中的检查能正确执行
    // 原因：上一轮 Stop() 会将 should_stop_ 设置为 true，如果不重置，下一轮 Start() 会被误判为"已停止"
    should_stop_.store(false);
    
    if (!stream_tag_.empty()) {
        LOG_INFO("%s 启动WHEP连接（原始码流模式）...", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("启动WHEP连接（原始码流模式）...");
    }
    
    // 关键修复：将 PeerConnection 的创建和初始化全部移到 SignalingThread 上执行
    // 这样可以避免多线程并发创建 PC 导致的竞态条件和崩溃
    WeakDummy(p);
    signaling_thread_->PostTask([this, p, start_callback]() {
        WeakDummyReturn(p);
        
        // 再次检查是否已经停止
        if (should_stop_.load()) {
            LOG_WARN("%s 启动过程中已停止，取消创建PeerConnection", FormatLogPrefix(stream_tag_).c_str());
            start_callback(false, 0, "启动过程中已停止");
            return;
        }

        
        // 创建PeerConnection配置
        webrtc::PeerConnectionInterface::RTCConfiguration config;
        config.sdp_semantics = webrtc::SdpSemantics::kUnifiedPlan;
        
        // 创建PeerConnection
        // 注意：pc_observer_ 必须在 signaling_thread 上访问，以避免竞态
        pc_observer_ = std::make_unique<PeerConnectionObserver>(signaling_thread_, stream_tag_);
        // 类型转换为PeerConnectionObserver以调用SetErrorCallback
        // 使用 AsyncCaptureProtect 保护机制，防止对象析构后访问成员变量
        // 注意：p 已经在外部 lambda 的捕获列表中，这里直接使用
        dynamic_cast<PeerConnectionObserver*>(pc_observer_.get())->SetErrorCallback([this, p](int error_code, const std::string& error_message) {
            WeakDummyReturn(p);
            this->NotifyError(error_code, error_message);
        });
        
        // 创建 PeerConnection（在 SignalingThread 上执行，符合 WebRTC 要求）
        webrtc::PeerConnectionDependencies pc_dependencies(pc_observer_.get());
        auto result = pc_factory_->CreatePeerConnectionOrError(config, std::move(pc_dependencies));
        
        if (!result.ok()) {
            std::cerr << "创建PeerConnection失败: " << result.error().message() << std::endl;
            NotifyError(1002, std::string("创建PeerConnection失败: ") + result.error().message());
            start_callback(false, 0, "创建PeerConnection失败");
            return;
        }
        
        peer_connection_ = result.value();
        if (!stream_tag_.empty()) {
            LOG_INFO("%s PeerConnection创建成功", FormatLogPrefix(stream_tag_).c_str());
        } else {
            LOG_INFO("PeerConnection创建成功");
        }
        
        // 添加视频接收器，设置为recvonly
        webrtc::RtpTransceiverInit transceiver_init;
        transceiver_init.direction = webrtc::RtpTransceiverDirection::kRecvOnly;
        auto video_receiver = peer_connection_->AddTransceiver(cricket::MediaType::MEDIA_TYPE_VIDEO, transceiver_init);
        if (!video_receiver.ok()) {
            LOG_ERROR("添加视频接收器失败");
            NotifyError(1003, "添加视频接收器失败");
            start_callback(false, 0, "添加视频接收器失败");
            return;
        }
        
        if (!stream_tag_.empty()) {
            LOG_INFO("%s 添加视频接收器成功（原始码流模式）", FormatLogPrefix(stream_tag_).c_str());
        } else {
            LOG_INFO("添加视频接收器成功（原始码流模式）");
        }
        
        // 创建Offer
        // 使用 AsyncCaptureProtect 保护机制，防止对象析构后访问成员变量
        // 注意：p 已经在外部 lambda 的捕获列表中，这里直接使用
        auto create_offer_observer = rtc::make_ref_counted<CreateSDPObserver>(
            [this, p, start_callback](const std::string& offer_sdp) {
                WeakDummyReturn(p);
                this->OnOfferCreated(offer_sdp, std::move(start_callback));
            },
            [this, p, start_callback](const std::string& error_message) {
                WeakDummyReturn(p);
                if (!this->stream_tag_.empty()) {
                    LOG_ERROR("%s 创建Offer失败: %s", FormatLogPrefix(this->stream_tag_).c_str(), error_message.c_str());
                } else {
                    LOG_ERROR("创建Offer失败: %s", error_message.c_str());
                }
                start_callback(false, 0, "创建Offer失败");
            },
            stream_tag_);
        
        peer_connection_->CreateOffer(create_offer_observer.get(), webrtc::PeerConnectionInterface::RTCOfferAnswerOptions());
    });
    
    return true;
}

void LibwebrtcClientImpl::OnOfferCreated(const std::string& offer_sdp, StartLibwebrtcClientCallback start_callback) {
    if (!stream_tag_.empty()) {
        LOG_INFO("%s SDP Offer创建成功", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("SDP Offer创建成功");
    }
    
    // 设置本地描述
    webrtc::SdpParseError error;
    auto local_desc = webrtc::CreateSessionDescription(webrtc::SdpType::kOffer, offer_sdp, &error);
    
    if (!local_desc) {
        LOG_ERROR("解析本地SDP失败: %s", error.description.c_str());
        start_callback(false, 0, "解析本地SDP失败");
        return;
    }
    
    WeakDummy(p);
    auto set_local_observer = rtc::make_ref_counted<SetSDPObserver>(
        [this, offer_sdp, p, start_callback]() {
            WeakDummyReturn(p);
            if (!this->stream_tag_.empty()) {
                LOG_INFO("%s 本地SDP设置成功", FormatLogPrefix(this->stream_tag_).c_str());
            } else {
                LOG_INFO("本地SDP设置成功");
            }
            signaling_thread_->PostTask([this, p, offer_sdp, start_callback]() {
                WeakDummyReturn(p);
                this->SendWHEPRequest(offer_sdp, std::move(start_callback));
            });
        }, [this, p, start_callback](const std::string& error_message) {
            WeakDummyReturn(p);
            LOG_ERROR("%s 设置本地SDP失败: %s", FormatLogPrefix(this->stream_tag_).c_str(), error_message.c_str());
            start_callback(false, 0, "设置本地SDP失败");
        },
        stream_tag_);
    
    peer_connection_->SetLocalDescription(set_local_observer.get(), local_desc.release());
}

void LibwebrtcClientImpl::SendWHEPRequest(const std::string& offer_sdp, StartLibwebrtcClientCallback start_callback) {
    if (!stream_tag_.empty()) {
        LOG_INFO("%s 发送WHEP请求到: %s", FormatLogPrefix(stream_tag_).c_str(), whep_url_.c_str());
    } else {
        LOG_INFO("发送WHEP请求到: %s", whep_url_.c_str());
    }
    LOG_INFO("=== 发送的SDP Offer ===");
    LOG_INFO("%s", offer_sdp.c_str());
    LOG_INFO("=== SDP结束 ===");
    
    drc_sdk::common::HttpHeaders headers;
    headers["Content-Type"] = "application/sdp";
    headers["Accept"] = "application/sdp";
    WeakDummy(p);
    drc_sdk::common::Network::Instance().Post(whep_url_, headers, drc_sdk::common::HttpPostType::JSON_DATA, offer_sdp, 
        [this, p, start_callback](const drc_sdk::common::NetworkError err_code, const std::string& response, const std::vector<std::string>& certs, const drc_sdk::common::HttpHeaders& resp_headers) {
        WeakDummyReturn(p);
        if (err_code != drc_sdk::common::NetworkError::SUCCESS) {
            LOG_ERROR("%s WHEP请求失败: %d", FormatLogPrefix(this->stream_tag_).c_str(), err_code);
            NotifyError(1004, "WHEP请求失败: " + std::to_string(static_cast<int>(err_code)));
            start_callback(false, static_cast<int>(err_code), "WHEP请求失败");
            return;
        }
        if (!this->stream_tag_.empty()) {
            LOG_INFO("%s WHEP请求成功，收到SDP答案", FormatLogPrefix(this->stream_tag_).c_str());
            LOG_INFO("%s === 服务器返回内容 ===", FormatLogPrefix(this->stream_tag_).c_str());
            LOG_DEBUG("%s %s", FormatLogPrefix(this->stream_tag_).c_str(), drc_sdk::common::log_safe(response).c_str());
            LOG_INFO("%s === 内容结束 ===", FormatLogPrefix(this->stream_tag_).c_str());
        } else {
            LOG_INFO("[LibWebRtc] WHEP请求成功，收到SDP答案");
            LOG_INFO("[LibWebRtc] === 服务器返回内容 ===");
            LOG_DEBUG("[LibWebRtc] %s", drc_sdk::common::log_safe(response).c_str());
            LOG_INFO("[LibWebRtc] === 内容结束 ===");
        }
        // 设置远程描述（必须在 SignalingThread 上执行）
        // 关键修复：SetRemoteDescription 必须在 SignalingThread 上执行，符合 WebRTC 要求
        webrtc::SdpParseError error;
        auto remote_desc = webrtc::CreateSessionDescription(webrtc::SdpType::kAnswer, response, &error);
        
        if (!remote_desc) {
            LOG_ERROR("解析远程SDP失败: %s", error.description.c_str());
            start_callback(false, 0, "解析远程SDP失败");
            return;
        }
        
        // PostTask 到 SignalingThread 执行 SetRemoteDescription
        WeakDummy(p);
        signaling_thread_->PostTask([this, p, remote_desc = std::move(remote_desc), start_callback]() mutable {
            WeakDummyReturn(p);
            
            auto set_remote_observer = rtc::make_ref_counted<SetSDPObserver>(
                [this, p, start_callback]() {
                    WeakDummyReturn(p);
                    if (!this->stream_tag_.empty()) {
                        LOG_INFO("%s 开始接收原始码流...", FormatLogPrefix(this->stream_tag_).c_str());
                    } else {
                        LOG_INFO("开始接收原始码流...");
                    }
                    this->CheckRTPStats();
                    start_callback(true, 0, "开始接收原始码流");
                },
                [this, p, start_callback](const std::string& error_message) {
                    WeakDummyReturn(p);
                    LOG_ERROR("%s 设置远程SDP失败: %s", FormatLogPrefix(this->stream_tag_).c_str(), error_message.c_str());
                    start_callback(false, 0, "设置远程SDP失败");
                },
                this->stream_tag_);
            
            peer_connection_->SetRemoteDescription(set_remote_observer.get(), remote_desc.release());
        });
    });

}

void LibwebrtcClientImpl::CheckRTPStats() {
    // 不再进行定期RTP统计检查，避免在析构时崩溃
    // RTP统计信息可以通过解析webrtc日志文件获取
    // webrtc日志中会输出以下信息：
    // 1. RTP包接收信息: "Packet received on SSRC: ..."
    // 2. 统计信息: "VideoReceiveStreamInterface stats: ..."
    // 这些信息已经足够用于监控和调试
    
    // 仍然保留数据流超时检查，但使用更简单的方式
    // 在OnVideoFrame中已经更新了last_frame_time_，可以在需要时检查
    if (!stream_tag_.empty()) {
        LOG_INFO("%s RTP统计信息可通过webrtc日志文件获取，不再进行定期检查", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("RTP统计信息可通过webrtc日志文件获取，不再进行定期检查");
    }
}

void LibwebrtcClientImpl::Stop() {
    if (should_stop_.exchange(true)) {
        return; // 已经在停止过程中
    }
    
    if (!stream_tag_.empty()) {
        LOG_INFO("%s 停止WHEP客户端（原始码流模式）...", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("停止WHEP客户端（原始码流模式）...");
    }
    
    // 等待所有已提交到 executor 的回调任务执行完毕
    // 这样可以确保所有回调都能正确执行，统计信息能正确更新
    if (executor_) {
        executor_->Flush();
    }
    
    // 清理回调，防止后续还有回调被触发
    video_frame_callback_ = nullptr;
    error_callback_ = nullptr;
    
    // 关闭PeerConnection - 使用 PostTask 异步关闭，并等待完成
    // 关键修复：捕获 peer_connection_ 的智能指针副本，而不是 this
    // 这样即使 this (LibwebrtcClientImpl) 被销毁，pc 对象依然存活直到 Close 执行完毕
    // 重要：等待 Close() 完成后再返回，确保 pc_factory_ 在 Close() 完成前不被释放
    // 这样可以避免对象生命周期管理问题导致的 Use-After-Free 崩溃
    if (peer_connection_) {
        // 保存 signaling_thread_ 的指针，避免在检查和使用之间被修改
        rtc::Thread* sig_thread = signaling_thread_;
        
        if (sig_thread && sig_thread->IsCurrent()) {
            // 如果已经在 signaling_thread 上，直接关闭
            peer_connection_->Close();
            peer_connection_ = nullptr;
        } else if (sig_thread && !sig_thread->IsQuitting()) {
            // 关键修复：检查线程是否还在运行，如果线程已停止，PostTask 可能不会执行
            // 使用 PostTask 异步关闭，并等待完成
            // 关键修复：等待 Close() 完成后再返回，确保 pc_factory_ 在 Close() 完成前不被释放
            // 这样可以避免对象生命周期管理问题导致的 Use-After-Free 崩溃
            rtc::scoped_refptr<webrtc::PeerConnectionInterface> pc = peer_connection_;
            // 立即释放主线程持有的引用，让 Close 在 signaling_thread 上异步执行
            peer_connection_ = nullptr;
            
            // 使用成员变量的条件变量等待 Close() 完成（修复：避免栈变量被销毁导致 Use-After-Free）
            // 重置 close_completed_ 标志
            {
                std::lock_guard<std::mutex> lock(close_mutex_);
                close_completed_ = false;
            }
            
            // 使用 PostTask 异步关闭
            // 使用 AsyncCaptureProtect 保护机制，防止对象析构后访问成员变量
            // WeakDummyReturn(p) 会检查对象是否还存在，如果不存在会提前返回
            // 捕获成员变量的指针，确保即使对象已销毁也能通知条件变量
            // 注意：close_mutex_ 和 close_cv_ 在析构函数中还在使用（析构函数在等待）
            std::mutex* mutex_ptr = &close_mutex_;
            std::condition_variable* cv_ptr = &close_cv_;
            bool* completed_ptr = &close_completed_;
            WeakDummy(p);
            sig_thread->PostTask([pc, p, mutex_ptr, cv_ptr, completed_ptr]() {
                // 先执行 Close()，即使对象已销毁也要执行（pc 是独立的智能指针）
                if (pc) {
                    pc->Close();
                }
                // 检查对象是否还存在，如果不存在则提前返回（避免访问已销毁的成员变量）
                WeakDummyReturn(p);
                // 如果执行到这里，说明对象还存在，可以安全访问成员变量
                // 通知等待线程 Close() 已完成（使用指针，不依赖 this）
                {
                    std::lock_guard<std::mutex> lock(*mutex_ptr);
                    *completed_ptr = true;
                }
                cv_ptr->notify_one();
            });
            
            // 等待 Close() 完成，使用成员变量的条件变量避免忙等待
            // 线程池的线程不会退出，PostTask 应该总是能执行
            // 如果对象还存在，lambda 会执行并通知；如果对象已销毁，析构函数已经完成，不会等待
            std::unique_lock<std::mutex> lock(close_mutex_);
            // 无条件等待，直到 close_completed_ 为 true
            // 参考其他库的做法（libdatachannel、srtc），不使用硬编码的超时时间
            close_cv_.wait(lock, [this]() {
                return this->close_completed_;
            });
            
            if (!stream_tag_.empty()) {
                LOG_DEBUG("%s Stop(): Close() 操作已完成", FormatLogPrefix(stream_tag_).c_str());
            }
        } else {
            // signaling_thread_ 为 nullptr 或已停止，无法安全调用 Close()
            if (!sig_thread) {
                LOG_WARN("%s signaling_thread_ 为 nullptr，直接释放 peer_connection_", 
                         FormatLogPrefix(stream_tag_).c_str());
            } else {
                LOG_WARN("%s signaling_thread_ 已停止，直接释放 peer_connection_", 
                         FormatLogPrefix(stream_tag_).c_str());
            }
            peer_connection_ = nullptr;
        }
    }
    
    // 注意：不在这里清理 pc_observer_ 和 pc_factory_
    // 因为它们可能还在被 PostTask 中的 Close() 使用
    // 这些资源会在析构函数中，确保 Close() 完成后再释放
    
    if (!stream_tag_.empty()) {
        LOG_INFO("%s WHEP客户端已停止（资源将在析构函数中释放）", FormatLogPrefix(stream_tag_).c_str());
    } else {
        LOG_INFO("WHEP客户端已停止（资源将在析构函数中释放）");
    }
}

void LibwebrtcClientImpl::SetVideoFrameCallback(std::function<void(std::shared_ptr<drc_sdk::ByteBuffer> frame, bool is_key_frame)> callback) {
    video_frame_callback_ = callback;
}

void LibwebrtcClientImpl::SetErrorCallback(std::function<void(int error_code, const std::string& error_message)> callback) {
    error_callback_ = std::move(callback);
}

void LibwebrtcClientImpl::NotifyError(int error_code, const std::string& error_message) {
    // 检查是否已经停止，避免在停止过程中触发错误回调
    if (should_stop_.load()) {
        return;
    }
    
    if (error_callback_) {
        WeakDummy(p);
        executor_->Post([this, p, error_code, error_message]() {
            WeakDummyReturn(p);
            // 再次检查是否已经停止，避免在停止后执行回调
            if (this->should_stop_.load()) {
                return;
            }
            if (error_callback_) {
                error_callback_(error_code, error_message);
            }
        });
    }
}

} // namespace message_engine