// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include <string>
#include "rtc/real_time_channel.h"
#include "rtc/drtc_user/drtc_user_engine_manager.h"

#include <message_scheduler.h>
#include <message_v1.h>
#include <message_v2.h>
#include <message_v3.h>

namespace message_engine {

class DrtcUserRealTimeChannel :
        public RealTimeChannel,
        public DrtcUserEngineMessageReceiver,
        public DrtcUserEngineLiveStreamReceiver,
        public DrtcUserEngineEventsListener,
        public MessageSchedulerListener,
        public std::enable_shared_from_this<DrtcUserRealTimeChannel>,
        public drc_sdk::common::AsyncCaptureProtect {
public:
    DrtcUserRealTimeChannel(const ChannelInfo &channel_info, const std::string &token,
                            const std::shared_ptr<drc_sdk::Executor> &working_executor,
                            const std::string &log_tag);

    ~DrtcUserRealTimeChannel() override;

    bool Join(const JoinChannelCallback &callback) override;
    bool Leave() override;
    bool RenewToken(const std::string &token) override;

public:
    void SendBinaryMessage(const std::string &target_user_id,
                           const std::shared_ptr<drc_sdk::ByteBuffer> &message, bool is_reliable,
                           const SendRealTimeMessageCallback &callback) override;

    void SendStringMessage(const std::string &target_user_id, const std::string &message,
                           bool is_reliable,
                           const SendRealTimeMessageCallback &callback) override;

    void SendBinaryBroadcastMessage(const std::shared_ptr<drc_sdk::ByteBuffer> &message,
                                    bool is_reliable,
                                    const SendRealTimeMessageCallback &callback) override;

    void SendStringBroadcastMessage(const std::string &message, bool is_reliable,
                                    const SendRealTimeMessageCallback &callback) override;

private:
    // DrtcUserEngineMessageReceiver
    void OnDrtcUserMessageReceived(const std::string &peer_client_id,
                                   const DrtcStream &stream,
                                   const std::shared_ptr<drc_sdk::ByteBuffer> &message) override;

    // DrtcUserEngineLiveStreamReceiver (used for late-join COMMAND subscribe)
    void OnDrtcUserVideoDataReceived(const std::string &peer_client_id,
                                     const DrtcStream &stream,
                                     const std::shared_ptr<drc_sdk::ByteBuffer> &data) override;
    void OnDrtcUserPublishStreamNotify(const DrtcClient &client,
                                       const DrtcStream &stream,
                                       const std::string &appendix) override;
    void OnDrtcUserUnpublishStreamNotify(const DrtcClient &client,
                                          const DrtcStream &stream) override;

    // DrtcUserEngineEventsListener
    void OnDrtcUserJoinRoomNotify(const DrtcClient &client) override;
    void OnDrtcUserLeaveRoomNotify(const DrtcClient &client, drtc_reason_code_t reason) override;
    void OnDrtcUserTokenWillExpire(int expires_in_seconds) override;
    void OnDrtcUserTokenDidExpire() override;
    void OnDrtcUserGlobalError(drtc_devicesdk_error_code_t code, const std::string &msg) override;

private:
    // Join flow: AcquireEngine -> Init -> JoinRoom -> Subscribe UAV COMMAND -> Publish CONTROL + COMMAND
    void SubscribeCommandStream(const JoinChannelCallback &callback);
    void DoSubscribeCommandStream(const DrtcClient &producer, const DrtcStream &stream,
                                  const JoinChannelCallback &callback = nullptr);
    void PublishLocalStreams(const JoinChannelCallback &callback);
    void SendStringPayload(const std::string &message, const SendRealTimeMessageCallback &callback);

    bool FindUavProducer(DrtcClient &out_client) const;

private:
    int message_listen_id_ = -1;
    int live_stream_listen_id_ = -1;
    int event_listen_id_ = -1;
    std::shared_ptr<DrtcUserEngineImpl> drtc_user_engine_;

    // Local published streams (align Web: rt_channel_stick_* + rt_channel_cmd_*)
    std::string pub_stick_stream_id_;
    std::string pub_command_stream_id_;
    DrtcStream control_stream_;
    DrtcStream local_command_stream_;
    bool control_published_ = false;
    bool command_published_ = false;

    // Subscribed UAV COMMAND stream
    DrtcClient command_producer_client_;
    DrtcStream command_producer_stream_;
    bool command_subscribed_ = false;

private:
    std::string log_tag_;

    /// ITransport
    MeErrorCode Send(const std::shared_ptr<MessageWrapper> &message) override;
    MeErrorCode Send(const std::shared_ptr<MessageWrapper> &message, const MeResponse &response,
                     const std::shared_ptr<drc_sdk::Executor> &user_define_executor) override;
    ListenIdCode Observe(const std::shared_ptr<ObserveCallback> &callback) override;
    void CancelObserve(const ListenIdCode &listen_id) override;

    /// MessageSchedulerListener
    void OnMessageNeedToBeSent(const std::shared_ptr<MessageWrapper> &message) override;
    void OnSentV1Message(const std::shared_ptr<MessageV1> &message);
    void OnSentV2Message(const std::shared_ptr<MessageV2> &message);
    void OnSentV3Message(const std::shared_ptr<MessageV3> &message);

    void OnReceivedBinaryMessage(const std::string &source_user_id,
                                 const std::shared_ptr<drc_sdk::ByteBuffer> &byte_buffer);
    void OnReceivedStringMessage(const std::string &source_user_id, const std::string &string_message);
    void OnReceivedMessage(const std::string &source_user_id,
                           const std::shared_ptr<MessageWrapper> &message);
};

}  // namespace message_engine
