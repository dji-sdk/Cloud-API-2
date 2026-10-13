// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "pull_stream_module_impl.h"
#include "common/util/async_capture_protect.h"
#include "i_rtc_channel_manager.h"
#include "i_live_stream_channel.h"
#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include <cstdlib>
#include <utility>

using namespace flight_hub_sdk;

PullStreamModuleImpl::PullStreamModuleImpl(std::shared_ptr<message_engine::IRtcChannelManager> rtc_channel_manager)
    : is_streaming_(false), rtc_channel_manager_(std::move(rtc_channel_manager)) {
    LOG_INFO("PullStreamModuleImpl: 构造函数");
}

PullStreamModuleImpl::~PullStreamModuleImpl() {
    if (is_streaming_) {
        PullStreamModuleImpl::stop();
    }
}

void PullStreamModuleImpl::start(const std::string& stream_url, 
                                const std::function<void(FlightHubSdkErrorCode result)>& callback) {
    if (!rtc_channel_manager_) {
        LOG_ERROR("PullStreamModuleImpl: RTC通道管理器为空");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 开始拉流(RTC 通道管理器为空, 未建通道); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PULL_STREAM_START, state_err, "[PullStreamModuleImpl]");

        callback(state_err.error_code);
        return;
    }

    if (is_streaming_) {
        LOG_WARN("PullStreamModuleImpl: 已经在拉流中");

        // 埋点: 开始拉流(已在拉流中, 直接返回成功); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PULL_STREAM_START, FlightHubError::success(), "[PullStreamModuleImpl]");

        callback(FlightHubSdkErrorCode::SUCCESS);
        return;
    }

    stream_url_ = stream_url;
    
    // 创建通道信息
    channel_info_.channel_name_ = "pull_stream_" + std::to_string(reinterpret_cast<uintptr_t>(this));
    channel_info_.type_ = message_engine::ChannelType::LiveStream;
    channel_info_.provider_type_ = getChannelProviderType(stream_url);
    channel_info_.url_ = stream_url;

    LOG_INFO("PullStreamModuleImpl: 开始拉流，URL: %s", stream_url.c_str());

    WeakDummy(p);
    auto ret = rtc_channel_manager_->AddChannel(channel_info_, "", 
        [callback, this, p](const bool &is_succeed, const int &elapsed_time_ms, const int &error_code, const std::string &message) {
            WeakDummyReturn(p);
            LOG_INFO("PullStreamModuleImpl: 通道添加结果: %d", is_succeed);
            if (is_succeed) {
                is_streaming_.store(true);
                subscribeStreamInternal();

                // 埋点: 开始拉流; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_PULL_STREAM_START, FlightHubError::success(), "[PullStreamModuleImpl]");

                callback(FlightHubSdkErrorCode::SUCCESS);
            } else {
                LOG_ERROR("PullStreamModuleImpl: 通道添加失败: %s", message.c_str());

                auto channel_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INIT_FAILED);

                // 埋点: 开始拉流(RTC 通道添加失败); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_PULL_STREAM_START, channel_err, "[PullStreamModuleImpl]");

                callback(channel_err.error_code);
            }
        }, "");

    if (!ret) {
        LOG_ERROR("PullStreamModuleImpl: 添加通道失败");

        auto add_channel_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 开始拉流(AddChannel 调用失败, 未发起建链); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_PULL_STREAM_START, add_channel_err, "[PullStreamModuleImpl]");

        callback(add_channel_err.error_code);
    }
}

void PullStreamModuleImpl::stop() {
    LOG_INFO("PullStreamModuleImpl: 停止拉流");
    if (!is_streaming_) {
        LOG_WARN("PullStreamModuleImpl: 当前没有在拉流");
        return;
    }

    if (rtc_channel_manager_) {
        rtc_channel_manager_->RemoveChannel(channel_info_.channel_name_);
    }
    
    is_streaming_.store(false);
    LOG_INFO("PullStreamModuleImpl: 拉流已停止");
}

int PullStreamModuleImpl::subscribeStream(const std::function<void(const std::vector<uint8_t>& data, 
                                                                 const FrameInfo& frame_info)>& callback) {
    return stream_callback_container_.addCallback(callback);
}

void PullStreamModuleImpl::unSubscribeStream(int listen_id) {
    stream_callback_container_.removeCallback(listen_id);
}

void PullStreamModuleImpl::setErrorCallback(const std::function<void(int error_code, 
                                                                   const std::string& error_message)>& callback) {
    error_callback_ = callback;
}

bool PullStreamModuleImpl::isStreaming() const {
    return is_streaming_.load();
}

std::string PullStreamModuleImpl::getStreamInfo() const {
    return "URL: " + stream_url_ + ", Status: " + (is_streaming_.load() ? "Streaming" : "Stopped");
}

void PullStreamModuleImpl::processStreamData(const std::shared_ptr<drc_sdk::ByteBuffer>& data, bool is_key_frame) {
    if (!data || data->Empty()) {
        LOG_WARN("PullStreamModuleImpl: 数据为空");
        return;
    }

    FrameInfo frame_info;
    frame_info.is_key_frame = is_key_frame;
    
    // 通知所有订阅者
    stream_callback_container_.notifyCallback(
        std::vector<uint8_t>(data->data(), data->data() + data->size()), 
        frame_info
    );
}

void PullStreamModuleImpl::handleWebRTCError(const std::string& error, const std::string& message) {
    LOG_ERROR("PullStreamModuleImpl: WebRTC错误: %s - %s", error.c_str(), message.c_str());
    
    // 解析错误码
    int error_code = 0;
    if (error.find("WebRTC_ErrorCode_") == 0) {
        const char* error_code_str = error.c_str() + 17; // 跳过 "WebRTC_ErrorCode_" 前缀
        char* end_ptr = nullptr;
        error_code = std::strtol(error_code_str, &end_ptr, 10);
        if (end_ptr == error_code_str || *end_ptr != '\0') {
            LOG_ERROR("PullStreamModuleImpl: 解析错误码失败，原始字符串: %s", error.c_str());
            error_code = 0;
        }
    }
    
    // 通知错误回调
    if (error_callback_) {
        error_callback_(error_code, message);
    }
}

void PullStreamModuleImpl::subscribeStreamInternal() {
    auto live_stream_channel = rtc_channel_manager_->GetChannel(channel_info_.channel_name_);
    auto live_stream_channel_ptr = live_stream_channel.lock();
    if (!live_stream_channel_ptr) {
        LOG_ERROR("PullStreamModuleImpl: 直播流通道为空");
        return;
    }

    WeakDummy(p);
    
    // 设置视频数据回调
    std::dynamic_pointer_cast<message_engine::ILiveStreamChannel>(live_stream_channel_ptr)->SetReceiveVideoDataCallback(
        [this, p](const std::shared_ptr<drc_sdk::ByteBuffer>& data, bool is_key_frame) {
            WeakDummyReturn(p);
            processStreamData(data, is_key_frame);
        }
    );
    
    // 设置全局错误回调
    live_stream_channel_ptr->SetGlobalErrorCallback([this, p](const std::string& error, const std::string& message) {
        WeakDummyReturn(p);
        handleWebRTCError(error, message);
    });
}

message_engine::ChannelProviderType PullStreamModuleImpl::getChannelProviderType(const std::string& url) {
    if (url.find("whep") != std::string::npos) {
        return message_engine::ChannelProviderType::WebRTC;
    }
    // 可以根据需要添加其他协议支持
    return message_engine::ChannelProviderType::UNKNOWN;
}
