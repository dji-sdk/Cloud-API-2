// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include "rtc/live_stream_channel.h"
#include "rtc/drtc_user/drtc_user_engine_manager.h"

namespace message_engine {

class DrtcUserLiveStreamChannel :
        public LiveStreamChannel,
        public DrtcUserEngineLiveStreamReceiver,
        public DrtcUserEngineEventsListener,
        public std::enable_shared_from_this<DrtcUserLiveStreamChannel>,
        public drc_sdk::common::AsyncCaptureProtect {
public:
    DrtcUserLiveStreamChannel(const ChannelInfo &channel_info, const std::string &token,
                              const std::shared_ptr<drc_sdk::Executor> &working_executor);

    ~DrtcUserLiveStreamChannel() override;

    bool Join(const JoinChannelCallback &callback) override;
    bool Leave() override;
    bool RenewToken(const std::string &token) override;

private:
    // DrtcUserEngineLiveStreamReceiver
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
    // Join flow: AcquireEngine -> Init -> JoinRoom -> find UAV VIDEO stream -> Subscribe
    void SubscribeVideoStream(const JoinChannelCallback &callback);
    void DoSubscribeVideoStream(const DrtcClient &producer, const DrtcStream &stream);

    // Find UAV producer with VIDEO stream from published streams
    bool FindUavVideoProducer(DrtcClient &out_client, DrtcStream &out_stream) const;

    // Strip DRTC 7-byte trailer (end of buffer), prepend Annex-B start code (align stream-hub DecodeData)
    std::shared_ptr<drc_sdk::ByteBuffer> ConvertToAnnexB(const std::shared_ptr<drc_sdk::ByteBuffer> &data);

private:
    int live_stream_listen_id_ = -1;
    int event_listen_id_ = -1;
    std::shared_ptr<DrtcUserEngineImpl> drtc_user_engine_;

    // Current subscribed VIDEO stream info
    DrtcClient video_producer_client_;
    DrtcStream video_producer_stream_;
    bool video_subscribed_ = false;
    std::string current_stream_id_;
};

}  // namespace message_engine
