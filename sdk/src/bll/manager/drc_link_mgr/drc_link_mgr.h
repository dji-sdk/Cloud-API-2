// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <string>
#include <functional>
#include "api/value.h"
#include "api/message.h"
#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "http_interface/response_message.h"
#include "base/async/timer.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

#define MAX_HEART_BEAT_INTERVAL_S 30
#define HEART_BEAT_INTERVAL_S 1
#define RECONNECT_DELAY_S 5

struct DrcReplyData {
    int count = 0;           // 心跳响应计数器（用于超时判断，阶段三使用）
    int64_t heart_beat = 0;  // 链路延迟（ms）：(当前时间 - 发送时间) / 2
};

class DrcLinkMgr : public drc_sdk::common::AsyncCaptureProtect {
public:
    DrcLinkMgr(std::string sn, std::string gateway_sn, std::shared_ptr<message_engine::IMessageEngine> message_engine, int drone_instance_id);
    ~DrcLinkMgr();

    void connectDrcLink(const std::function<void(FlightHubSdkErrorCode err_code)> callback);
    void disconnectDrcLink(const std::function<void(FlightHubSdkErrorCode err_code)> callback);
    int listenDrcLinkState(const std::function<void(const DroneLinkState& state)> callback);
    void cancelListenDrcLinkState(int listen_id);

    // ── Send methods ──
    void drcRtcQualityTest(const RtcQualityTestParams& params, const std::function<void(FlightHubError err)>& callback);

    // ── Observe methods (Northbound pushes) ──
    int listenDrcDelayInfo(const std::function<void(const DrcDelayInfo& info)>& callback);
    void cancelListenDrcDelayInfo(int listen_id);
    int listenDrcCountDownInfo(const std::function<void(const DrcCountDownInfo& info)>& callback);
    void cancelListenDrcCountDownInfo(int listen_id);

    // 供 DroneImpl 调用：标记为用户主动断链，抑制 scheduleReconnect 自动重连
    void onUserDisconnect() { user_disconnected_ = true; }
private:
    int instance_id_ = 0;
    std::string log_tag_;

    void initialize();

    void onRecvOpenDataChannelResponse(const OpenDataChannelResponse& open_data_channel_response, const std::function<void(FlightHubSdkErrorCode err_code)> callback);
    void onRecvDataChannelResponseWithMqtt(const OpenDataChannelResponse& open_data_channel_response, const std::function<void(FlightHubSdkErrorCode err_code)> callback);
    void onRecvDataChannelResponseWithRTC(const OpenDataChannelResponse& open_data_channel_response, const std::function<void(FlightHubSdkErrorCode err_code)> callback);
    void onRecvDataChannelResponseWithDRTC(const OpenDataChannelResponse& open_data_channel_response, const std::function<void(FlightHubSdkErrorCode err_code)> callback);
    void onDrcLinkStateChanged(message_engine::MQTT_CONNECT_STATUS state);
    void onDrcLinkStateChanged(bool is_rtc_connected);
    void updateDrcLinkState(DroneLinkState state);

    void startHeartBeatTimer();
    void stopHeartBeatTimer();

    void scheduleReconnect();

    void sendDrcStateInitializeMsg();

    // Observe init/cleanup
    void initDelayInfoListening();
    void initCountDownInfoListening();

    void onReceiveDelayInfo(const std::shared_ptr<message_engine::DrcDelayInfoPushNorthbound>& msg);
    void onReceiveCountDownInfo(const std::shared_ptr<message_engine::DrcDrcCountDownInfoPushNorthbound>& msg);

    std::string sn_;
    std::string gateway_sn_;
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;

    CallbackContainer<DroneLinkState> drc_link_state_callbacks_;
    CallbackContainer<DrcDelayInfo> delay_info_callbacks_;
    CallbackContainer<DrcCountDownInfo> count_down_info_callbacks_;

    /// cache
    DroneLinkState drc_link_state_ = DroneLinkState::DroneLinkState_IDLE;
    std::chrono::steady_clock::time_point last_heart_beat_timestamp_;
    DrcReplyData drc_reply_data_;
    bool reconnect_pending_ = false;
    bool user_disconnected_ = false;

    // listen ids
    int delay_info_listen_id_ = -1;
    int count_down_info_listen_id_ = -1;

    // timer
    drc_sdk::Timer heart_beat_timer_;

};

} // namespace flight_hub_sdk