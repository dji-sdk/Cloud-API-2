// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/live_stream.h"
#include "common/util/async_capture_protect.h"
#include "i_rtc_channel_manager.h"
#include "http_interface/http_interface.h"
#include "message.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class LiveModuleImpl : public ILiveModule, public drc_sdk::common::AsyncCaptureProtect {

enum class SeiType {
    ASSISTANT_SEI = 1,
    AR_SEI = 4,
    AI_SEI = 7,
    EIS_SEI = 9,
    FOV_SEI = 10,
    TIME_STAMP_SEI = 100,
    END_TO_END_DELAY_SEI = 155,
};

public: 
    explicit LiveModuleImpl(const LiveInfo& params, std::shared_ptr<message_engine::IRtcChannelManager> rtc_channel_manager, const std::string& gateway_sn = "");
    ~LiveModuleImpl() override;

    void start(const std::function<void(FlightHubError result)>& callback) override;

    void startWithURL(const std::function<void(FlightHubError err, const LiveStreamStartResult& result)>& callback) override;

    void stop() override;

    void stopWithURL(const std::function<void(FlightHubError err)>& callback) override;

    int subscribeLiveStream(const std::function<void(const std::vector<uint8_t>& data, const FrameInfo& frame_info)>& callback) override;

    void unSubscribeLiveStream(int listen_id) override;

    /// SEI
    int subscribeAiSeiInfo(const std::function<void(const AiSeiInfo& ai_sei_info)>& callback) override;

    void unSubscribeAiSeiInfo(int listen_id) override;

    int subscribeArSeiInfo(const std::function<void(const ArSeiInfo& ar_sei_info)>& callback) override;

    void unSubscribeArSeiInfo(int listen_id) override;

    int subscribeFovSeiInfo(const std::function<void(const FovSeiInfo& fov_sei_info)>& callback) override;

    void unSubscribeFovSeiInfo(int listen_id) override;

    int subscribeAssistantSeiInfo(const std::function<void(const AssistantSeiInfo& assistant_sei_info)>& callback) override;

    void unSubscribeAssistantSeiInfo(int listen_id) override;

    int subscribeEisSeiInfo(const std::function<void(const EisSeiInfo& eis_sei_info)>& callback) override;

    void unSubscribeEisSeiInfo(int listen_id) override;

    void setLiveQuality(const LiveQualitySettingParams& params, const std::function<void(FlightHubError err)>& callback) override;

private:
    message_engine::ChannelProviderType getChannelProviderType(const LiveStreamStartResponse& response);


    void parseVolcUrlParams(const std::string& url, std::string& app_id, std::string& user_id, std::string& token);
    void parseAgoraUrlParams(const std::string& url, std::string& app_id, std::string& channel_name, std::string& user_id, std::string& token);
    void parseDrtcUrlParams(const std::string& url, std::string& app_id, std::string& room_id, std::string& channel_name, std::string& client_id, std::string& token, std::string& provider_url);

    void subscribeLiveStreamInternal();

    void processLiveStreamData(const std::shared_ptr<drc_sdk::ByteBuffer>& data, bool is_i_frame);

    void processAISei(const std::shared_ptr<drc_sdk::ByteBuffer>& data);

    void processARSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data);

    void processFovSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data);

    void processAssistantSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data);

    void processEisSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data);

    void processEndToEndDelaySei(const std::shared_ptr<drc_sdk::ByteBuffer>& data);

    void processJsonFormattedSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data);

    void processSei(const std::shared_ptr<drc_sdk::ByteBuffer>& data);

    bool isJsonFormattedSei() const;

    bool isNeedProcessSei();
    
    void handleWebRTCError(const std::string& error, const std::string& message);
    
    void handleTokenExpiration();
    
    LiveInfo live_info_;
    std::string gateway_sn_;   // 网关设备SN（drone场景为机场SN，dock场景为机场SN）
    message_engine::ChannelInfo channel_info_;
    std::shared_ptr<message_engine::IRtcChannelManager> rtc_channel_manager_;
    std::atomic<bool> is_stopped_{false}; // 防止重复停止的标志

    CallbackContainerTwoParams<std::vector<uint8_t>, FrameInfo, false> live_stream_callback_container_;

    CallbackContainer<ArSeiInfo> ar_sei_info_callback_container_;

    CallbackContainer<AiSeiInfo> ai_sei_info_callback_container_;

    CallbackContainer<FovSeiInfo> fov_sei_info_callback_container_;

    CallbackContainer<AssistantSeiInfo> assistant_sei_info_callback_container_;

    CallbackContainer<EisSeiInfo> eis_sei_info_callback_container_;

};

} // namespace flight_hub_sdk