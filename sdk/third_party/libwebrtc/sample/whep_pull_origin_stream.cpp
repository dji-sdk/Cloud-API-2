// 平台宏定义 - 必须在任何WebRTC头文件之前定义
#define WEBRTC_POSIX 1
// #define WEBRTC_LINUX 1
// #define WEBRTC_ARCH_X86_64 1
// #define WEBRTC_HAS_SEQUENCE_CHECKER 1
// #define WEBRTC_HAVE_SCTP 1
// #define WEBRTC_LIBRARY_IMPL 1
// #define WEBRTC_NON_STATIC_TRACE_EVENT_HANDLERS 0
// #define WEBRTC_ENABLE_PROTOBUF 1

#include <iostream>
#include <memory>
#include <thread>
#include <chrono>
#include <fstream>
#include <mutex>
#include <atomic>
#include <condition_variable>
#include <curl/curl.h>
#include <string>
#include <vector>

// WebRTC includes
#include "api/peer_connection_interface.h"
#include "api/create_peerconnection_factory.h"
#include "api/audio_codecs/builtin_audio_decoder_factory.h"
#include "api/audio_codecs/builtin_audio_encoder_factory.h"
#include "api/video_codecs/builtin_video_decoder_factory.h"
#include "api/video_codecs/builtin_video_encoder_factory.h"
#include "rtc_base/thread.h"
#include "rtc_base/logging.h"
#include "rtc_base/ssl_adapter.h"
#include "modules/audio_device/include/fake_audio_device.h"
#include "api/stats/rtc_stats_report.h"
#include "api/stats/rtcstats_objects.h"

// 添加StatsObserver来获取RTP统计
class StatsObserver : public webrtc::RTCStatsCollectorCallback {
public:
    void OnStatsDelivered(const rtc::scoped_refptr<const webrtc::RTCStatsReport>& report) override {
        std::cout << "=== RTP统计报告 ===" << std::endl;
        
        // 检查入站RTP流统计
        bool found_video_receiver = false;
        for (const auto& stats : *report) {
            if (stats.type() == webrtc::RTCInboundRtpStreamStats::kType) {
                const auto& inbound_stats = static_cast<const webrtc::RTCInboundRtpStreamStats&>(stats);
                if (inbound_stats.kind.has_value() && *inbound_stats.kind == "video") {
                    found_video_receiver = true;
                    std::cout << "找到视频接收器统计:" << std::endl;
                    if (inbound_stats.packets_received.has_value()) {
                        std::cout << "  接收包数: " << *inbound_stats.packets_received << std::endl;
                    }
                    if (inbound_stats.bytes_received.has_value()) {
                        std::cout << "  接收字节数: " << *inbound_stats.bytes_received << std::endl;
                    }
                    if (inbound_stats.frames_decoded.has_value()) {
                        std::cout << "  解码帧数: " << *inbound_stats.frames_decoded << std::endl;
                    }
                    if (inbound_stats.frames_dropped.has_value()) {
                        std::cout << "  丢弃帧数: " << *inbound_stats.frames_dropped << std::endl;
                    }
                    if (inbound_stats.last_packet_received_timestamp.has_value()) {
                        std::cout << "  最后包接收时间: " << *inbound_stats.last_packet_received_timestamp << std::endl;
                    }
                    if (inbound_stats.packets_lost.has_value()) {
                        std::cout << "  丢包数: " << *inbound_stats.packets_lost << std::endl;
                    }
                }
            }
        }
        
        if (!found_video_receiver) {
            std::cout << "未找到视频接收器统计信息" << std::endl;
        }
        
        std::cout << "=== 统计报告结束 ===" << std::endl;
    }
};

// 原始码流保存器 - 用于保存H.264/H.265原始码流
class RawStreamSaver {
public:
    RawStreamSaver(const std::string& filename) 
        : filename_(filename), frame_count_(0), total_bytes_(0) {
        output_file_.open(filename_, std::ios::binary);
        if (!output_file_.is_open()) {
            std::cerr << "无法打开输出文件: " << filename_ << std::endl;
        } else {
            std::cout << "原始码流将保存到: " << filename_ << std::endl;
        }
    }
    
    ~RawStreamSaver() {
        if (output_file_.is_open()) {
            output_file_.close();
            std::cout << "码流保存完成: " << filename_ 
                      << ", 总帧数: " << frame_count_ 
                      << ", 总字节数: " << total_bytes_ << std::endl;
        }
    }
    
    void SaveFrame(const uint8_t* data, size_t size, bool is_keyframe) {
        std::lock_guard<std::mutex> lock(mutex_);
        
        if (!output_file_.is_open()) {
            return;
        }
        
        frame_count_++;
        total_bytes_ += size;
        
        std::cout << "保存原始帧 #" << frame_count_ 
                  << " 大小: " << size << " 字节"
                  << " 类型: " << (is_keyframe ? "关键帧" : "普通帧") << std::endl;
        
        // 写入原始数据
        output_file_.write(reinterpret_cast<const char*>(data), size);
        output_file_.flush();
        
        // 每10帧报告一次进度
        if (frame_count_ % 10 == 0) {
            std::cout << "已保存 " << frame_count_ << " 帧，总共 " << total_bytes_ << " 字节" << std::endl;
        }
    }
    
private:
    std::string filename_;
    std::ofstream output_file_;
    std::mutex mutex_;
    int frame_count_;
    size_t total_bytes_;
};

// 自定义解码器 - 不进行实际解码，而是保存原始码流
class RawStreamVideoDecoder : public webrtc::VideoDecoder {
public:
    RawStreamVideoDecoder(const std::string& codec_name) 
        : codec_name_(codec_name), configured_(false) {
        std::string filename = "raw_stream_" + codec_name + ".h264";
        if (codec_name == "H265" || codec_name == "HEVC") {
            filename = "raw_stream_" + codec_name + ".h265";
        }
        stream_saver_ = std::make_unique<RawStreamSaver>(filename);
        std::cout << "RawStreamVideoDecoder创建: " << codec_name_ << std::endl;
    }
    
    ~RawStreamVideoDecoder() {
        std::cout << "RawStreamVideoDecoder析构: " << codec_name_ << std::endl;
    }
    
    bool Configure(const Settings& settings) override {
        std::cout << "RawStreamVideoDecoder::Configure调用" << std::endl;
        std::cout << "  编解码器: " << codec_name_ << std::endl;
        std::cout << "  最大渲染分辨率: " << settings.max_render_resolution().Width() 
                  << "x" << settings.max_render_resolution().Height() << std::endl;
        configured_ = true;
        return true;
    }
    
    int32_t Decode(const webrtc::EncodedImage& input_image, 
                   bool missing_frames,
                   int64_t render_time_ms) override {
        if (!configured_) {
            std::cerr << "解码器未配置" << std::endl;
            return WEBRTC_VIDEO_CODEC_UNINITIALIZED;
        }
        
        if (!input_image.data() || input_image.size() == 0) {
            std::cerr << "输入图像数据为空" << std::endl;
            return WEBRTC_VIDEO_CODEC_ERR_PARAMETER;
        }
        
        // 检查帧类型
        bool is_keyframe = (input_image._frameType == webrtc::VideoFrameType::kVideoFrameKey);
        
        std::cout << "接收到编码帧: 大小=" << input_image.size() 
                  << ", RTP时间戳=" << input_image.RtpTimestamp()
                  << ", 类型=" << (is_keyframe ? "I帧" : "P/B帧")
                  << ", 丢帧=" << (missing_frames ? "是" : "否") << std::endl;
        
        // 保存原始编码数据
        if (stream_saver_) {
            stream_saver_->SaveFrame(input_image.data(), input_image.size(), is_keyframe);
        }
        
        // 不进行实际解码，直接返回成功
        // 在实际应用中，这里应该调用解码回调来传递解码后的帧
        // 但我们只关心原始码流，所以跳过解码步骤
        
        return WEBRTC_VIDEO_CODEC_OK;
    }
    
    int32_t RegisterDecodeCompleteCallback(webrtc::DecodedImageCallback* callback) override {
        std::cout << "RawStreamVideoDecoder::RegisterDecodeCompleteCallback调用" << std::endl;
        decode_callback_ = callback;
        return WEBRTC_VIDEO_CODEC_OK;
    }
    
    int32_t Release() override {
        std::cout << "RawStreamVideoDecoder::Release调用" << std::endl;
        configured_ = false;
        decode_callback_ = nullptr;
        return WEBRTC_VIDEO_CODEC_OK;
    }
    
    const char* ImplementationName() const override {
        return "RawStreamVideoDecoder";
    }
    
private:
    std::string codec_name_;
    bool configured_;
    webrtc::DecodedImageCallback* decode_callback_;
    std::unique_ptr<RawStreamSaver> stream_saver_;
};

// 支持H264/H265的原始码流解码器工厂类
class RawStreamVideoDecoderFactory : public webrtc::VideoDecoderFactory {
public:
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
        
        std::cout << "RawStreamVideoDecoderFactory: 支持H264和H265格式" << std::endl;
        
        return formats;
    }
    
    std::unique_ptr<webrtc::VideoDecoder> Create(
        const webrtc::Environment& env,
        const webrtc::SdpVideoFormat& format) override {
        std::cout << "RawStreamVideoDecoderFactory::Create被调用，格式: " << format.name << std::endl;
        
        if (format.name == "H264") {
            std::cout << "创建H264原始码流解码器，参数: ";
            for (const auto& param : format.parameters) {
                std::cout << param.first << "=" << param.second << " ";
            }
            std::cout << std::endl;
            
            return std::make_unique<RawStreamVideoDecoder>("H264");
        } else if (format.name == "H265" || format.name == "HEVC") {
            std::cout << "创建H265原始码流解码器" << std::endl;
            return std::make_unique<RawStreamVideoDecoder>("H265");
        } else {
            std::cout << "不支持的视频格式: " << format.name << std::endl;
        }
        return nullptr;
    }
};

// 简单的HTTP响应结构
struct CurlResponse {
    std::string data;
    std::string location;
    
    static size_t WriteCallback(void* contents, size_t size, size_t nmemb, CurlResponse* response) {
        size_t totalSize = size * nmemb;
        response->data.append((char*)contents, totalSize);
        return totalSize;
    }
    
    static size_t HeaderCallback(void* contents, size_t size, size_t nmemb, CurlResponse* response) {
        size_t totalSize = size * nmemb;
        std::string header((char*)contents, totalSize);
        if (header.substr(0, 9) == "Location:") {
            response->location = header.substr(10);
            // 移除末尾的\r\n
            response->location.erase(response->location.find_last_not_of(" \n\r\t") + 1);
        }
        return totalSize;
    }
};

// PeerConnectionObserver类
class PeerConnectionObserver : public webrtc::PeerConnectionObserver {
public:
    PeerConnectionObserver(rtc::Thread* signaling_thread) : signaling_thread_(signaling_thread) {}
    
    void OnSignalingChange(webrtc::PeerConnectionInterface::SignalingState new_state) override {
        std::cout << "信令状态变更: " << static_cast<int>(new_state) << std::endl;
    }
    
    void OnAddTrack(rtc::scoped_refptr<webrtc::RtpReceiverInterface> receiver,
                    const std::vector<rtc::scoped_refptr<webrtc::MediaStreamInterface>>& streams) override {
        std::cout << "收到新的轨道" << std::endl;
        auto track = receiver->track();
        if (track && track->kind() == webrtc::MediaStreamTrackInterface::kVideoKind) {
            std::cout << "收到视频轨道 - 原始码流模式，不设置VideoSink" << std::endl;
            std::cout << "视频轨道状态: " << (track->state() == webrtc::MediaStreamTrackInterface::kLive ? "Live" : "Ended") << std::endl;
            std::cout << "视频轨道启用状态: " << (track->enabled() ? "Enabled" : "Disabled") << std::endl;
            
            // 在原始码流模式下，我们不需要设置VideoSink
            // 因为原始数据会在自定义解码器中被拦截
            
        } else if (track) {
            std::cout << "收到非视频轨道: " << track->kind() << std::endl;
        } else {
            std::cout << "收到轨道但track为空" << std::endl;
        }
    }
    
    void OnRemoveTrack(rtc::scoped_refptr<webrtc::RtpReceiverInterface> receiver) override {
        std::cout << "移除轨道" << std::endl;
    }
    
    void OnDataChannel(rtc::scoped_refptr<webrtc::DataChannelInterface> channel) override {
        std::cout << "收到数据通道" << std::endl;
    }
    
    void OnRenegotiationNeeded() override {
        std::cout << "需要重新协商" << std::endl;
    }
    
    void OnIceConnectionChange(webrtc::PeerConnectionInterface::IceConnectionState new_state) override {
        std::cout << "ICE连接状态变更: " << static_cast<int>(new_state) << std::endl;
        
        switch (new_state) {
            case webrtc::PeerConnectionInterface::kIceConnectionNew:
                std::cout << "  ICE状态: New (新建)" << std::endl;
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionChecking:
                std::cout << "  ICE状态: Checking (检查中)" << std::endl;
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionConnected:
                std::cout << "  ICE状态: Connected (已连接)" << std::endl;
                std::cout << "ICE连接已建立，开始接收原始码流..." << std::endl;
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionCompleted:
                std::cout << "  ICE状态: Completed (已完成)" << std::endl;
                std::cout << "ICE连接完成，原始码流接收中..." << std::endl;
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionFailed:
                std::cout << "  ICE状态: Failed (失败)" << std::endl;
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionDisconnected:
                std::cout << "  ICE状态: Disconnected (断开)" << std::endl;
                break;
            case webrtc::PeerConnectionInterface::kIceConnectionClosed:
                std::cout << "  ICE状态: Closed (关闭)" << std::endl;
                break;
        }
    }
    
    void OnIceGatheringChange(webrtc::PeerConnectionInterface::IceGatheringState new_state) override {
        std::cout << "ICE收集状态变更: " << static_cast<int>(new_state) << std::endl;
    }
    
    void OnIceCandidate(const webrtc::IceCandidateInterface* candidate) override {
        std::cout << "收到ICE候选" << std::endl;
        
        // 打印ICE候选详细信息
        std::string candidate_str;
        candidate->ToString(&candidate_str);
        std::cout << "  ICE候选: " << candidate_str << std::endl;
    }
    
private:
    rtc::Thread* signaling_thread_;
};

// SDP创建观察者
class CreateSDPObserver : public webrtc::CreateSessionDescriptionObserver {
public:
    CreateSDPObserver(std::function<void(const std::string&)> on_success)
        : on_success_(on_success) {}
    
    void OnSuccess(webrtc::SessionDescriptionInterface* desc) override {
        std::string sdp;
        desc->ToString(&sdp);
        std::cout << "SDP Offer创建成功" << std::endl;
        on_success_(sdp);
    }
    
    void OnFailure(webrtc::RTCError error) override {
        std::cerr << "SDP Offer创建失败: " << error.message() << std::endl;
    }
    
private:
    std::function<void(const std::string&)> on_success_;
};

// SDP设置观察者
class SetSDPObserver : public webrtc::SetSessionDescriptionObserver {
public:
    SetSDPObserver(std::function<void()> on_success = nullptr) 
        : on_success_(on_success) {}
    
    void OnSuccess() override {
        std::cout << "SDP设置成功" << std::endl;
        if (on_success_) {
            on_success_();
        }
    }
    
    void OnFailure(webrtc::RTCError error) override {
        std::cerr << "SDP设置失败: " << error.message() << std::endl;
    }
    
private:
    std::function<void()> on_success_;
};

// WHEP客户端主类
class WHEPClient {
public:
    WHEPClient(const std::string& whep_url) 
        : whep_url_(whep_url), should_stop_(false), is_initialized_(false) {}
    
    bool Initialize() {
        std::cout << "初始化WHEP客户端（原始码流模式）..." << std::endl;
        
        // 启用WebRTC日志记录
        rtc::LogMessage::LogToDebug(rtc::LS_INFO);
        rtc::LogMessage::LogTimestamps();
        rtc::LogMessage::LogThreads();
        
        // 初始化WebRTC
        rtc::InitializeSSL();
        
        // 创建线程
        network_thread_ = rtc::Thread::CreateWithSocketServer();
        worker_thread_ = rtc::Thread::Create();
        signaling_thread_ = rtc::Thread::Create();
        
        if (!network_thread_ || !worker_thread_ || !signaling_thread_) {
            std::cerr << "创建WebRTC线程失败" << std::endl;
            return false;
        }
        
        network_thread_->Start();
        worker_thread_->Start();
        signaling_thread_->Start();
        
        // 创建PeerConnectionFactory，使用自定义的原始码流解码器工厂
        pc_factory_ = webrtc::CreatePeerConnectionFactory(
            network_thread_.get(),
            worker_thread_.get(),
            signaling_thread_.get(),
            rtc::scoped_refptr<webrtc::AudioDeviceModule>(new webrtc::FakeAudioDeviceModule()),
            webrtc::CreateBuiltinAudioEncoderFactory(),
            webrtc::CreateBuiltinAudioDecoderFactory(),
            nullptr, // 不需要视频编码器
            std::make_unique<RawStreamVideoDecoderFactory>(), // 使用原始码流解码器工厂
            nullptr,  // audio_mixer
            nullptr   // audio_processing
        );
        
        if (!pc_factory_) {
            std::cerr << "创建PeerConnectionFactory失败" << std::endl;
            return false;
        }
        
        std::cout << "PeerConnectionFactory创建成功（原始码流模式）" << std::endl;
        
        is_initialized_ = true;
        return true;
    }
    
    bool Start() {
        if (!is_initialized_) {
            std::cerr << "客户端未初始化" << std::endl;
            return false;
        }
        
        std::cout << "启动WHEP连接（原始码流模式）..." << std::endl;
        
        // 创建PeerConnection配置
        webrtc::PeerConnectionInterface::RTCConfiguration config;
        config.sdp_semantics = webrtc::SdpSemantics::kUnifiedPlan;
        
        // 添加STUN服务器
        // webrtc::PeerConnectionInterface::IceServer stun_server1;
        // stun_server1.uri = "stun:stun.l.google.com:19302";
        // config.servers.push_back(stun_server1);
        
        // webrtc::PeerConnectionInterface::IceServer stun_server2;
        // stun_server2.uri = "stun:stun1.l.google.com:19302";
        // config.servers.push_back(stun_server2);
        
        std::cout << "配置了 " << config.servers.size() << " 个STUN服务器" << std::endl;
        
        // 创建PeerConnection
        pc_observer_ = std::make_unique<PeerConnectionObserver>(signaling_thread_.get());
        
        webrtc::PeerConnectionDependencies pc_dependencies(pc_observer_.get());
        auto result = pc_factory_->CreatePeerConnectionOrError(config, std::move(pc_dependencies));
        
        if (!result.ok()) {
            std::cerr << "创建PeerConnection失败: " << result.error().message() << std::endl;
            return false;
        }
        
        peer_connection_ = result.value();
        std::cout << "PeerConnection创建成功" << std::endl;
        
        // 添加视频接收器，设置为recvonly
        webrtc::RtpTransceiverInit transceiver_init;
        transceiver_init.direction = webrtc::RtpTransceiverDirection::kRecvOnly;
        auto video_receiver = peer_connection_->AddTransceiver(cricket::MediaType::MEDIA_TYPE_VIDEO, transceiver_init);
        if (!video_receiver.ok()) {
            std::cerr << "添加视频接收器失败" << std::endl;
            return false;
        }
        
        std::cout << "添加视频接收器成功（原始码流模式）" << std::endl;
        
        // 创建Offer
        auto create_offer_observer = rtc::make_ref_counted<CreateSDPObserver>(
            [this](const std::string& offer_sdp) {
                this->OnOfferCreated(offer_sdp);
            });
        
        peer_connection_->CreateOffer(create_offer_observer.get(), webrtc::PeerConnectionInterface::RTCOfferAnswerOptions());
        
        return true;
    }
    
    void OnOfferCreated(const std::string& offer_sdp) {
        std::cout << "SDP Offer创建成功" << std::endl;
        
        // 设置本地描述
        webrtc::SdpParseError error;
        auto local_desc = webrtc::CreateSessionDescription(webrtc::SdpType::kOffer, offer_sdp, &error);
        
        if (!local_desc) {
            std::cerr << "解析本地SDP失败: " << error.description << std::endl;
            return;
        }
        
        auto set_local_observer = rtc::make_ref_counted<SetSDPObserver>(
            [this, offer_sdp]() {
                std::cout << "本地SDP设置成功" << std::endl;
                this->SendWHEPRequest(offer_sdp);
            });
        
        peer_connection_->SetLocalDescription(set_local_observer.get(), local_desc.release());
    }
    
    void SendWHEPRequest(const std::string& offer_sdp) {
        std::cout << "发送WHEP请求到: " << whep_url_ << std::endl;
        std::cout << "=== 发送的SDP Offer ===" << std::endl;
        std::cout << offer_sdp << std::endl;
        std::cout << "=== SDP结束 ===" << std::endl;
        
        CURL* curl = curl_easy_init();
        if (!curl) {
            std::cerr << "CURL初始化失败" << std::endl;
            return;
        }
        
        CurlResponse response;
        
        // 设置CURL选项
        curl_easy_setopt(curl, CURLOPT_URL, whep_url_.c_str());
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, offer_sdp.c_str());
        curl_easy_setopt(curl, CURLOPT_POSTFIELDSIZE, offer_sdp.length());
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, CurlResponse::WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
        curl_easy_setopt(curl, CURLOPT_HEADERFUNCTION, CurlResponse::HeaderCallback);
        curl_easy_setopt(curl, CURLOPT_HEADERDATA, &response);
        curl_easy_setopt(curl, CURLOPT_TIMEOUT, 30L);
        
        // 设置HTTP头部
        struct curl_slist* headers = nullptr;
        headers = curl_slist_append(headers, "Content-Type: application/sdp");
        headers = curl_slist_append(headers, "Accept: application/sdp");
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        
        CURLcode res = curl_easy_perform(curl);
        
        long response_code;
        curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
        
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);
        
        if (res != CURLE_OK) {
            std::cerr << "WHEP请求失败: " << curl_easy_strerror(res) << std::endl;
            return;
        }
        
        if (response_code != 200 && response_code != 201) {
            std::cerr << "WHEP请求返回错误码: " << response_code << std::endl;
            if (!response.data.empty()) {
                std::cerr << "响应内容: " << response.data << std::endl;
            }
            return;
        }
        
        std::cout << "WHEP请求成功，收到SDP答案" << std::endl;
        std::cout << "=== 服务器返回内容 ===" << std::endl;
        std::cout << response.data << std::endl;
        std::cout << "=== 内容结束 ===" << std::endl;
        
        // 设置远程描述
        webrtc::SdpParseError error;
        auto remote_desc = webrtc::CreateSessionDescription(webrtc::SdpType::kAnswer, response.data, &error);
        
        if (!remote_desc) {
            std::cerr << "解析远程SDP失败: " << error.description << std::endl;
            return;
        }
        
        auto set_remote_observer = rtc::make_ref_counted<SetSDPObserver>(
            [this]() {
                std::cout << "开始接收原始码流..." << std::endl;
                this->CheckRTPStats();
            });
        
        peer_connection_->SetRemoteDescription(set_remote_observer.get(), remote_desc.release());
    }
    
    void CheckRTPStats() {
        std::cout << "检查RTP接收统计..." << std::endl;
        
        // 延迟检查RTP统计，确保连接稳定
        if (signaling_thread_) {
            signaling_thread_->PostDelayedTask([this]() {
                std::cout << "延迟检查RTP统计..." << std::endl;
                
                // 获取RTP接收统计
                if (peer_connection_) {
                    peer_connection_->GetStats(new rtc::RefCountedObject<StatsObserver>());
                }
                
                // 继续定期检查统计信息
                this->StartPeriodicStatsCheck();
            }, webrtc::TimeDelta::Millis(1000)); // 1秒后检查
        }
    }
    
    void StartPeriodicStatsCheck() {
        if (signaling_thread_ && !should_stop_) {
            signaling_thread_->PostDelayedTask([this]() {
                if (!should_stop_ && peer_connection_) {
                    std::cout << "定期检查RTP统计..." << std::endl;
                    peer_connection_->GetStats(new rtc::RefCountedObject<StatsObserver>());
                    this->StartPeriodicStatsCheck(); // 继续下一次检查
                }
            }, webrtc::TimeDelta::Millis(10000)); // 每10秒检查一次
        }
    }
    
    void Stop() {
        if (should_stop_.exchange(true)) {
            return; // 已经在停止过程中
        }
        
        std::cout << "停止WHEP客户端（原始码流模式）..." << std::endl;
        
        // 关闭PeerConnection
        if (peer_connection_) {
            if (signaling_thread_ && signaling_thread_->IsCurrent()) {
                peer_connection_->Close();
            } else if (signaling_thread_) {
                signaling_thread_->PostTask([this]() {
                    if (peer_connection_) {
                        peer_connection_->Close();
                    }
                });
                std::this_thread::sleep_for(std::chrono::milliseconds(100));
            }
            peer_connection_ = nullptr;
        }
        
        std::cout << "WHEP客户端已停止" << std::endl;
    }
    
    ~WHEPClient() {
        Stop();
        
        // 等待一段时间确保所有异步操作完成
        std::this_thread::sleep_for(std::chrono::milliseconds(200));
        
        // 安全地停止WebRTC线程
        if (signaling_thread_) {
            signaling_thread_->Stop();
        }
        if (worker_thread_) {
            worker_thread_->Stop();
        }
        if (network_thread_) {
            network_thread_->Stop();
        }
        
        // 清理SSL
        rtc::CleanupSSL();
    }
    
private:
    std::string whep_url_;
    std::string whep_session_url_;
    std::atomic<bool> should_stop_;
    std::atomic<bool> is_initialized_;
    
    std::unique_ptr<rtc::Thread> network_thread_;
    std::unique_ptr<rtc::Thread> worker_thread_;
    std::unique_ptr<rtc::Thread> signaling_thread_;
    
    rtc::scoped_refptr<webrtc::PeerConnectionFactoryInterface> pc_factory_;
    rtc::scoped_refptr<webrtc::PeerConnectionInterface> peer_connection_;
    std::unique_ptr<PeerConnectionObserver> pc_observer_;
};

int main(int argc, char* argv[]) {
    // 主线程WrapCurrent，保证WebRTC回调能正常派发
    if (!rtc::Thread::Current()) {
        rtc::ThreadManager::Instance()->WrapCurrentThread();
    }
    
    std::cout << "=== WHEP WebRTC 原始码流拉流客户端 ===" << std::endl;
    std::cout << "此 demo 将拉取 H.264/H.265 原始码流并保存到文件" << std::endl;

    if (argc < 2) {
        std::cerr << "用法: " << argv[0] << " <whep_url>" << std::endl;
        std::cerr << "示例: " << argv[0] << " http://127.0.0.1/rtc/v1/whep/?app=live&stream=livestream" << std::endl;
        return 1;
    }

    // 初始化CURL
    curl_global_init(CURL_GLOBAL_DEFAULT);

    const std::string whep_url = argv[1];

    // 创建WHEP客户端
    WHEPClient client(whep_url);
    
    // 初始化客户端
    if (!client.Initialize()) {
        std::cerr << "客户端初始化失败" << std::endl;
        curl_global_cleanup();
        return -1;
    }
    
    // 启动拉流
    if (!client.Start()) {
        std::cerr << "启动拉流失败" << std::endl;
        curl_global_cleanup();
        return -1;
    }
    
    std::cout << "原始码流拉流已启动，原始H.264数据将保存到 raw_stream_H264.h264 文件" << std::endl;
    std::cout << "按回车键停止..." << std::endl;
    
    try {
        // 等待用户输入
        std::cin.get();
    } catch (const std::exception& e) {
        std::cerr << "等待用户输入时发生异常: " << e.what() << std::endl;
    }
    
    // 停止客户端
    try {
        client.Stop();
    } catch (const std::exception& e) {
        std::cerr << "停止客户端时发生异常: " << e.what() << std::endl;
    }
    
    // 等待一段时间确保清理完成
    std::this_thread::sleep_for(std::chrono::milliseconds(500));
    
    // 清理CURL
    try {
        curl_global_cleanup();
    } catch (const std::exception& e) {
        std::cerr << "清理CURL时发生异常: " << e.what() << std::endl;
    }
    
    std::cout << "程序结束，原始码流已保存" << std::endl;
    return 0;
} 