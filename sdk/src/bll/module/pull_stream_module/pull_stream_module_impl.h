// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/pull_stream.h"
#include "i_rtc_channel_manager.h"
#include "i_live_stream_channel.h"
#include <atomic>
#include <memory>

#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class PullStreamModuleImpl : public IPullStreamModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit PullStreamModuleImpl(std::shared_ptr<message_engine::IRtcChannelManager> rtc_channel_manager);
    ~PullStreamModuleImpl() override;

    // IPullStreamModule接口实现
    void start(const std::string& stream_url, 
               const std::function<void(FlightHubSdkErrorCode result)>& callback) override;
    void stop() override;
    int subscribeStream(const std::function<void(const std::vector<uint8_t>& data, 
                                               const FrameInfo& frame_info)>& callback) override;
    void unSubscribeStream(int listen_id) override;
    void setErrorCallback(const std::function<void(int error_code, 
                                                 const std::string& error_message)>& callback) override;
    bool isStreaming() const override;
    std::string getStreamInfo() const override;

private:
    void processStreamData(const std::shared_ptr<drc_sdk::ByteBuffer>& data, bool is_key_frame);
    void handleWebRTCError(const std::string& error, const std::string& message);
    void subscribeStreamInternal();
    message_engine::ChannelProviderType getChannelProviderType(const std::string& url);

    std::string stream_url_;
    std::atomic<bool> is_streaming_;
    std::shared_ptr<message_engine::IRtcChannelManager> rtc_channel_manager_;
    message_engine::ChannelInfo channel_info_;
    
    // 回调容器
    CallbackContainerTwoParams<std::vector<uint8_t>, FrameInfo, false> stream_callback_container_;
    std::function<void(int error_code, const std::string& error_message)> error_callback_;
};

} // namespace flight_hub_sdk
