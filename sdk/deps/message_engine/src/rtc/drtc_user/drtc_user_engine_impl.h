// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <atomic>
#include <mutex>
#include <set>
#include <unordered_map>
#include <vector>
#include <functional>

#include <base/io/byte_buffer.h>
#include <common/util/async_capture_protect.h>
#include <base/async/executor.h>

#include "drtc_device_sdk.h"
#include "drtc_device_sdk_define.h"
#include "drtc_device_sdk_factory.h"

// Workaround: GCC can't resolve these types in nested class override context
// Force visibility by declaring dummy variable (never used)
static inline void _drtc_type_force_visibility_() {
    (void)sizeof(DrtcExchangeTokenResp);
    (void)sizeof(DrtcNetworkQualityNotify);
}

namespace message_engine {

// Join room state for DRTC user engine
enum class DrtcUserJoinRoomState {
    IDLE = 0,
    JOINING = 1,
    JOINED = 2,
    EXCHANGING = 3,
};

// Message receiver interface for command data
class DrtcUserEngineMessageReceiver {
public:
    virtual ~DrtcUserEngineMessageReceiver() = default;
    virtual void OnDrtcUserMessageReceived(const std::string &peer_client_id,
                                           const DrtcStream &stream,
                                           const std::shared_ptr<drc_sdk::ByteBuffer> &message) = 0;
};

// Live stream receiver interface for video data
class DrtcUserEngineLiveStreamReceiver {
public:
    virtual ~DrtcUserEngineLiveStreamReceiver() = default;
    virtual void OnDrtcUserVideoDataReceived(const std::string &peer_client_id,
                                             const DrtcStream &stream,
                                             const std::shared_ptr<drc_sdk::ByteBuffer> &data) = 0;
    virtual void OnDrtcUserPublishStreamNotify(const DrtcClient &client,
                                               const DrtcStream &stream,
                                               const std::string &appendix) = 0;
    virtual void OnDrtcUserUnpublishStreamNotify(const DrtcClient &client,
                                                  const DrtcStream &stream) = 0;
};

// Events listener for room/token/error events
class DrtcUserEngineEventsListener {
public:
    virtual ~DrtcUserEngineEventsListener() = default;
    virtual void OnDrtcUserJoinRoomNotify(const DrtcClient &client) = 0;
    virtual void OnDrtcUserLeaveRoomNotify(const DrtcClient &client, drtc_reason_code_t reason) = 0;
    virtual void OnDrtcUserTokenWillExpire(int expires_in_seconds) = 0;
    virtual void OnDrtcUserTokenDidExpire() = 0;
    virtual void OnDrtcUserGlobalError(drtc_devicesdk_error_code_t code, const std::string &msg) = 0;
};

// Callback types
using DrtcUserJoinRoomCallback = std::function<void(const std::string &room_id, bool is_succeed,
                                                     int error_code, const DrtcJoinRoomResp &resp)>;
using DrtcUserPublishCallback = std::function<void(bool is_succeed, const DrtcStream &stream, int error_code)>;
using DrtcUserSubscribeCallback = std::function<void(bool is_succeed, const DrtcSubscribeResp &resp)>;

// Published stream info from JoinRoom response
struct DrtcUserPublishedStreamInfo {
    DrtcClient client;
    std::vector<PublishedStream> streams;
};

class DrtcUserEngineImpl final : public drtc::DrtcDeviceSdk::Listener,
                                  public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit DrtcUserEngineImpl(const std::string &config_path,
                                const std::shared_ptr<drc_sdk::Executor> &executor);
    ~DrtcUserEngineImpl() override;

    // Lifecycle
    void SetProviderUrl(const std::string &provider_url);
    bool Init(const std::string &client_id);
    bool DeInit();
    bool GetIsInited() const;

    // Room operations
    int JoinRoom(const std::string &app_id, const std::string &room_id,
                 const std::string &token, const std::string &provider_url,
                 const DrtcUserJoinRoomCallback &callback);
    int LeaveRoom();

    // Stream operations
    int Publish(const DrtcStream &stream, const DrtcUserPublishCallback &callback);
    int Unpublish(const DrtcStream &stream);
    int Subscribe(const DrtcClient &producer_client, const DrtcStream &producer_stream,
                  const DrtcUserSubscribeCallback &callback);
    int Unsubscribe(const DrtcClient &producer_client, const DrtcStream &producer_stream);

    // Data sending
    int SendData(const DrtcStream &stream, const std::shared_ptr<drc_sdk::ByteBuffer> &data);

    // Token update
    int UpdateToken(const std::string &token);

    // Key frame request
    int RequestVideoKeyFrame(const DrtcClient &producer_client, const DrtcStream &producer_stream);

    // Listener management
    int AddMessageReceiver(const std::shared_ptr<DrtcUserEngineMessageReceiver> &receiver);
    int AddLiveStreamReceiver(const std::shared_ptr<DrtcUserEngineLiveStreamReceiver> &receiver);
    int AddEventsListener(const std::shared_ptr<DrtcUserEngineEventsListener> &listener);
    bool RemoveMessageReceiver(int listen_id);
    bool RemoveLiveStreamReceiver(int listen_id);
    bool RemoveEventsListener(int listen_id);

    // Get published streams from last JoinRoom response
    std::vector<DrtcUserPublishedStreamInfo> GetPublishedStreams() const;

    // Room info
    std::string GetRoomId() const;
    DrtcUserJoinRoomState GetJoinState() const;

private:
    // DrtcDeviceSdk::Listener callbacks
    void DrtcOnJoinRoomNotify(const DrtcJoinRoomNotify &notify) override;
    void DrtcOnLeaveRoomNotify(const DrtcLeaveRoomNotify &notify) override;
    void DrtcOnPublishStreamNotify(const DrtcPublishStreamNotify &notify) override;
    void DrtcOnUnpublishStreamNotify(const DrtcUnpublishStreamNotify &notify) override;
    void DrtcOnTokenWillExpireNotify(const DrtcTokenWillExpireNotify &notify) override;
    void DrtcOnTokenDidExpireNotify(const DrtcTokenDidExpireNotify &notify) override;
    void DrtcOnPeerStateChangeNotify(const DrtcClientStateChangeNotify &notify) override;
    void DrtcOnLocalStateChangeNotify(const drtc_client_state_event_t &notify) override;
    void DrtcOnExchangeTokenResponse(DRTC_OUT const DrtcExchangeTokenResp &out) override;
    void DrtcOnJoinRoomResponse(const DrtcJoinRoomResp &out) override;
    void DrtcOnLeaveRoomResponse(const DrtcLeaveRoomResp &out) override;
    void DrtcOnPublishResponse(const DrtcPublishResp &out, int request_id) override;
    void DrtcOnUnpublishResponse(const DrtcUnpublishResp &out, int request_id) override;
    void DrtcOnSubscribeResponse(const DrtcSubscribeResp &out, int request_id) override;
    void DrtcOnUnsubscribeResponse(const DrtcUnsubscribeResp &out, int request_id) override;
    void DrtcOnUpdateTokenResponse(const DrtcUpdateTokenResp &out, int request_id) override;
    void DrtcOnData(const DrtcClient &peer_client, const DrtcStream &stream, DeviceSdkData &data) override;
    void DrtcOnDataEvent(const DrtcClient &client, const DrtcStream &stream, DrtcDataEvent &event) override;
    void DrtcOnMessageReceived(const DrtcClient &peer_client, DeviceSdkMessage &message) override;
    void DrtcOnUserMessage(const DrtcClient &peer_client, const DeviceSdkMessage &message, uint8_t transmission_type) override;
    void DrtcOnDataBitrateChanged(uint32_t target_kbps) override;
    void DrtcOnGlobalError(drtc_devicesdk_error_code_t code, const char *msg) override;
    void DrtcOnStats(const char *msg, uint32_t len) override;
    void DrtcOnClientKickoutNotify(const DrtcClientKickoutNotify &notify) override;
    void DrtcOnNetworkQualityNotify(const DrtcNetworkQualityNotify &notify) override;
    void DrtcOnClientBandwidthNotify(const DrtcClient &peer_client, uint32_t bw_kbps) override;

private:
    int GenerateListenerId();
    static bool ParseProviderUrl(const std::string &url, std::string &host, int &port, std::string &path);
    static std::string ResolveConfigPath(const std::string &base_path, const std::string &provider_url);
    int StartDrtcJoinRoom();
    void NotifyJoinFailed(int error_code);

private:
    std::string config_path_;
    std::string provider_url_;
    std::shared_ptr<drc_sdk::Executor> executor_;
    drtc::DrtcDeviceSdk *drtc_sdk_ = nullptr;

    std::mutex init_mutex_;
    std::atomic<bool> is_inited_{false};

    DrtcClient self_client_;
    std::string client_id_;
    std::string reference_sn_;  // PaaS room_id = device SN, used as exchange reference_sn
    std::string room_id_;       // real DRTC room id after exchange
    std::string app_id_;
    std::string token_;

    std::atomic<DrtcUserJoinRoomState> join_state_{DrtcUserJoinRoomState::IDLE};

    // Join room callback
    std::mutex join_room_callback_mutex_;
    DrtcUserJoinRoomCallback join_room_callback_;

    // Published streams from JoinRoom response
    mutable std::mutex published_streams_mutex_;
    std::vector<DrtcUserPublishedStreamInfo> published_streams_;

    // Publish callbacks
    std::mutex publish_callbacks_mutex_;
    std::unordered_map<int, DrtcUserPublishCallback> publish_callbacks_;

    // Subscribe callbacks
    std::mutex subscribe_callbacks_mutex_;
    std::unordered_map<int, DrtcUserSubscribeCallback> subscribe_callbacks_;

    // Message receivers
    std::mutex message_receivers_mutex_;
    std::unordered_map<int, std::weak_ptr<DrtcUserEngineMessageReceiver>> message_receivers_;

    // Live stream receivers
    std::mutex live_stream_receivers_mutex_;
    std::unordered_map<int, std::weak_ptr<DrtcUserEngineLiveStreamReceiver>> live_stream_receivers_;

    // Events listeners
    std::mutex events_listeners_mutex_;
    std::unordered_map<int, std::weak_ptr<DrtcUserEngineEventsListener>> events_listeners_;
};

}  // namespace message_engine
