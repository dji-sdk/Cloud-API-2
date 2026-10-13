// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include <string>
#include <functional>
#include <chrono>
#include <base/io/byte_buffer.h>

namespace message_engine {

// 基础枚举定义
enum MeErrorCode : int64_t {
    // 大于0，就是无错误，为ListenID
    ME_NO_ERROR = 0,

    // 用户参数校验相关错误
    ERROR_PARAM_OBSERVE_WITHOUT_CALLBACK = -101,
    ERROR_PARAM_SEND_WITHOUT_INPUT_MESSAGE = -102,
    ERROR_PARAM_SEND_WITHOUT_ORIGINAL_MESSAGE = -103,

    // Transport相关错误
    ERROR_TRANSPORT_NOT_FOUND = -201,
    ERROR_TRANSPORT_ERROR = -202,

    // Scheduler相关错误
    ERROR_SCHEDULER_WAIT_RESPONSE_TIME_OUT = -301, // 发出去等待回复的消息迟迟没有收到回复

    // connect相关错误
    ERROR_CONNECT_ERROR = -401,
};

// 基础类型定义
using ListenIdCode = int64_t;
using TransportIdType = std::string;
using MethodType = std::string;

// 前向声明
class MessageWrapper;
class ChannelInfo;
class TopicInfo;
class MqttMultiPathParams;
class IChannel;
class IRealTimeChannel;
class ILiveStreamChannel;
class IAudioDataChannel;

// 回调函数类型定义
using AddChannelCompletion = std::function<void(const bool &is_succeed, const int &elapsed_time_ms, const int &error_code, const std::string &message)>;
using RealTimeChannelUpdateCallback = std::function<void(std::weak_ptr<IRealTimeChannel> real_time_channel)>;
using RealTimeChannelAvailableCallback = std::function<void(std::vector<std::weak_ptr<IRealTimeChannel>> real_time_channel)>;
using LiveStreamChannelAvailableCallback = std::function<void(std::vector<std::weak_ptr<ILiveStreamChannel>> live_stream_channel)>;
using AudioDataChannelAvailableCallback = std::function<void(std::vector<std::weak_ptr<IAudioDataChannel>> audio_data_channel)>;

using SendRealTimeMessageCallback = std::function<void(const bool &is_successed, const int &error_code, const std::string &extencontent)>;

using ObserveResultsCallback = std::function<void(const std::shared_ptr<MessageWrapper>&)>;
using MessageCallback = std::function<void(const std::string&, const std::string&)>;


/*
* RtcChannel相关定义 Start
*/
enum class ChannelType {
    UNKNOWN,
    RealTime,
    LiveStream,
    AudioData,
};

enum class ChannelProviderType {
    UNKNOWN,
    VolcEngine,
    DRTC,
    Agora,
    MRTC,
    WebRTC,
    Rtmp
};

// ***  audio ****//
enum class AudioDataType {
    UNKNOWN,
    OPUS,
    G722,
    AACLC,
    PCMA
};

typedef struct {
    AudioDataType audio_data_type;
} AudioFrameInfo;


// ***  video ****//
enum class VideoDataType {
    UNKNOWN,
    H_264,
    H_265,
    BYTEVC1
};

enum class VideoStreamType {
    HIGH,
    LOW,
};

enum class VideoFrameType {
    AUTO_DETECT,
    I_FRAME,
    P_FRAME
};

typedef struct {
    VideoDataType data_type;
    VideoStreamType stream_type;
    VideoFrameType frame_type;
    int frame_rate;
} VideoFrameInfo;

using JoinChannelCallback = std::function<void(const bool &is_succeed, const int &elapsed_time_ms, const bool &is_rejoin, const int &error_code, const std::string &message)>;

enum class ChannelStatus {
    OK,
    OFFLINE,
    NEED_RENEW_TOKEN_OFFLINE,
};

struct ChannelLifeCycleInfo {
    std::chrono::steady_clock::time_point create_time_;
    std::chrono::steady_clock::time_point joined_time_;
};

struct drc_binary_header {
    uint8_t is_binary : 1;  // 是否是二进制数据（否则就是json数据）
    uint8_t reserve : 7;    // 保留位
};

/// mrtc protocol name
constexpr auto WEBRTC_PROTOCOL_NAME = "dji,webrtc";
// constexpr auto VOLC_PROTOCOL_NAME = "dji,volc";
// constexpr auto AGORA_PROTOCOL_NAME = "dji,agora";
constexpr auto RTMP_PROTOCOL_NAME = "dji,rtmp";
// constexpr auto RTSP_PROTOCOL_NAME = "dji,rtsp";
constexpr auto GB28181_PROTOCOL_NAME = "dji,gbt28181";

/*
* RtcChannel相关定义 End
*/

} // namespace message_engine 