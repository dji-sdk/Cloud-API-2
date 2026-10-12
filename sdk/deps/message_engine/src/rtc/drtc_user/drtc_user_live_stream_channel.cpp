// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drtc_user_live_stream_channel.h"
#include "drtc_user_channel_define.h"

#include <cstring>
#include <memory>

#include <base/common/logger.h>
#include <base/io/byte_buffer_pool.h>
#include <common/util/async_capture_protect.h>

using namespace message_engine;

// Annex-B start code: 00 00 00 01
static const uint8_t ANNEX_B_START_CODE[] = {0x00, 0x00, 0x00, 0x01};
static constexpr size_t ANNEX_B_START_CODE_SIZE = 4;
// DRTC VIDEO callback: last 7 bytes are SDK trailer (stream-hub strips tail, not head).
static constexpr size_t DRTC_VIDEO_TRAILER_SIZE = 7;

static std::string GetDrtcTargetStreamId(const ChannelInfo &channel_info) {
    return channel_info.drtc_stream_id_.empty() ? channel_info.channel_name_ : channel_info.drtc_stream_id_;
}

DrtcUserLiveStreamChannel::DrtcUserLiveStreamChannel(const ChannelInfo &channel_info, const std::string &token,
                                                       const std::shared_ptr<drc_sdk::Executor> &working_executor)
    : LiveStreamChannel(channel_info, token, working_executor) {

    memset(&video_producer_client_, 0, sizeof(DrtcClient));
    memset(&video_producer_stream_, 0, sizeof(DrtcStream));

    LOG_INFO("[DrtcUserLiveStreamChannel] construct, channel: %s", channel_info.channel_name_.c_str());
}

DrtcUserLiveStreamChannel::~DrtcUserLiveStreamChannel() {
    LOG_INFO("[DrtcUserLiveStreamChannel] destruct, channel: %s", channel_info_.channel_name_.c_str());

    if (drtc_user_engine_) {
        drtc_user_engine_->RemoveLiveStreamReceiver(live_stream_listen_id_);
        drtc_user_engine_->RemoveEventsListener(event_listen_id_);
    }

    // Release engine reference (per-stream owner_id = channel_name_ / stream_id)
    if (!channel_info_.channel_name_.empty()) {
        DrtcUserEngineManager::GetInstance().ReleaseEngine(
            channel_info_.room_id_, channel_info_.channel_name_);
    }
}

bool DrtcUserLiveStreamChannel::Join(const JoinChannelCallback &callback) {
    LOG_INFO("[DrtcUserLiveStreamChannel] Join channel: %s, room: %s",
             channel_info_.channel_name_.c_str(), channel_info_.room_id_.c_str());

    if (channel_info_.channel_name_.empty()) {
        LOG_ERROR("[DrtcUserLiveStreamChannel] channel_name_ is empty, cannot join");
        if (callback) {
            callback(false, 0, false, -1, "channel_name empty");
        }
        return false;
    }

    // Acquire engine from manager (one owner per stream_id for multi-stream live)
    drtc_user_engine_ = DrtcUserEngineManager::GetInstance().AcquireEngine(
        channel_info_.room_id_, channel_info_.channel_name_,
        channel_info_.config_path_, working_executor_);

    if (!drtc_user_engine_) {
        LOG_ERROR("[DrtcUserLiveStreamChannel] Failed to acquire engine");
        return false;
    }

    drtc_user_engine_->SetProviderUrl(channel_info_.url_);
    // Init engine
    if (!drtc_user_engine_->GetIsInited()) {
        if (!drtc_user_engine_->Init(channel_info_.client_id_)) {
            LOG_ERROR("[DrtcUserLiveStreamChannel] Engine init failed");
            return false;
        }
    }

    // Register receivers
    live_stream_listen_id_ = drtc_user_engine_->AddLiveStreamReceiver(shared_from_this());
    event_listen_id_ = drtc_user_engine_->AddEventsListener(shared_from_this());

    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr, callback]() {
        WeakDummyReturn(weak_ptr);

        // If engine already joined, subscribe directly
        if (drtc_user_engine_->GetJoinState() == DrtcUserJoinRoomState::JOINED) {
            LOG_INFO("[DrtcUserLiveStreamChannel] Engine already joined, subscribing directly");
            SubscribeVideoStream(callback);
            return;
        }

        // JoinRoom
        auto ret = drtc_user_engine_->JoinRoom(
            channel_info_.app_id_, channel_info_.room_id_, token_, channel_info_.url_,
            [this, weak_ptr, callback](const std::string &room_id, bool is_succeed,
                                        int error_code, const DrtcJoinRoomResp &resp) {
                WeakDummyReturn(weak_ptr);
                working_executor_->Dispatch([this, weak_ptr, callback, is_succeed, error_code]() {
                    WeakDummyReturn(weak_ptr);
                    if (is_succeed) {
                        SubscribeVideoStream(callback);
                    } else {
                        LOG_ERROR("[DrtcUserLiveStreamChannel] JoinRoom failed, code: %d", error_code);
                        if (callback) {
                            callback(false, 0, false, error_code, "JoinRoom failed");
                        }
                    }
                });
            });

        if (ret != 0) {
            LOG_ERROR("[DrtcUserLiveStreamChannel] JoinRoom call failed, ret: %d", ret);
            if (callback) {
                callback(false, 0, false, ret, "JoinRoom call failed");
            }
        }
    });
    return true;
}

void DrtcUserLiveStreamChannel::SubscribeVideoStream(const JoinChannelCallback &callback) {
    LOG_INFO("[DrtcUserLiveStreamChannel] SubscribeVideoStream");

    DrtcClient producer_client;
    DrtcStream producer_stream;

    if (!FindUavVideoProducer(producer_client, producer_stream)) {
        LOG_WARN("[DrtcUserLiveStreamChannel] No UAV VIDEO producer found yet, waiting for PublishStreamNotify");
        // Still mark as joined - will auto-subscribe on late join via DrtcOnPublishStreamNotify
        status_ = ChannelStatus::OK;
        life_cycle_info_.joined_time_ = std::chrono::steady_clock::now();
        if (callback) {
            callback(true, 0, false, 0, "");
        }
        return;
    }

    video_producer_client_ = producer_client;
    video_producer_stream_ = producer_stream;

    WeakDummy(p);
    auto ret = drtc_user_engine_->Subscribe(producer_client, producer_stream,
        [this, p, callback](bool is_succeed, const DrtcSubscribeResp &resp) {
            WeakDummyReturn(p);
            working_executor_->Dispatch([this, p, callback, is_succeed]() {
                WeakDummyReturn(p);
                if (is_succeed) {
                    video_subscribed_ = true;
                    LOG_INFO("[DrtcUserLiveStreamChannel] Subscribe VIDEO success");
                } else {
                    LOG_ERROR("[DrtcUserLiveStreamChannel] Subscribe VIDEO failed");
                }
                status_ = ChannelStatus::OK;
                life_cycle_info_.joined_time_ = std::chrono::steady_clock::now();
                if (callback) {
                    callback(is_succeed, 0, false, is_succeed ? 0 : -1,
                             is_succeed ? "" : "Subscribe VIDEO failed");
                }
            });
        });

    if (ret != 0) {
        LOG_ERROR("[DrtcUserLiveStreamChannel] Subscribe call failed, ret: %d", ret);
        if (callback) {
            callback(false, 0, false, ret, "Subscribe call failed");
        }
    }
}

void DrtcUserLiveStreamChannel::DoSubscribeVideoStream(const DrtcClient &producer,
                                                         const DrtcStream &stream) {
    LOG_INFO("[DrtcUserLiveStreamChannel] DoSubscribeVideoStream (late join) producer: %s, stream: %s",
             producer.client_id, stream.stream_id);

    video_producer_client_ = producer;
    video_producer_stream_ = stream;

    WeakDummy(p);
    auto ret = drtc_user_engine_->Subscribe(producer, stream,
        [this, p](bool is_succeed, const DrtcSubscribeResp &resp) {
            WeakDummyReturn(p);
            working_executor_->Dispatch([this, p, is_succeed]() {
                WeakDummyReturn(p);
                if (is_succeed) {
                    video_subscribed_ = true;
                    LOG_INFO("[DrtcUserLiveStreamChannel] Late subscribe VIDEO success");
                } else {
                    LOG_ERROR("[DrtcUserLiveStreamChannel] Late subscribe VIDEO failed");
                }
            });
        });

    if (ret != 0) {
        LOG_ERROR("[DrtcUserLiveStreamChannel] Late subscribe call failed, ret: %d", ret);
    }
}

bool DrtcUserLiveStreamChannel::FindUavVideoProducer(DrtcClient &out_client, DrtcStream &out_stream) const {
    const std::string target_stream_id = GetDrtcTargetStreamId(channel_info_);
    auto published_streams = drtc_user_engine_->GetPublishedStreams();
    for (const auto &info : published_streams) {
        if (info.client.device_type != DRTC_DEVICE_UAV &&
            info.client.device_type != DRTC_DEVICE_DOCK) {
            continue;
        }
        for (const auto &ps : info.streams) {
            if (ps.stream.stream_type != DRTC_STREAM_VIDEO) {
                continue;
            }
            if (!target_stream_id.empty() &&
                std::string(ps.stream.stream_id) != target_stream_id) {
                continue;
            }
            out_client = info.client;
            out_stream = ps.stream;
            return true;
        }
    }
    return false;
}

bool DrtcUserLiveStreamChannel::Leave() {
    LOG_INFO("[DrtcUserLiveStreamChannel] Leave channel: %s", channel_info_.channel_name_.c_str());

    if (!drtc_user_engine_) {
        return false;
    }

    // Unsubscribe VIDEO stream
    if (video_subscribed_) {
        drtc_user_engine_->Unsubscribe(video_producer_client_, video_producer_stream_);
        video_subscribed_ = false;
    }

    status_ = ChannelStatus::OFFLINE;
    return true;
}

bool DrtcUserLiveStreamChannel::RenewToken(const std::string &token) {
    LOG_INFO("[DrtcUserLiveStreamChannel] RenewToken");
    if (!drtc_user_engine_) {
        return false;
    }
    token_ = token;
    auto ret = drtc_user_engine_->UpdateToken(token);
    return ret == 0;
}

// ======================== DrtcUserEngineLiveStreamReceiver ========================

void DrtcUserLiveStreamChannel::OnDrtcUserVideoDataReceived(const std::string &peer_client_id,
                                                             const DrtcStream &stream,
                                                             const std::shared_ptr<drc_sdk::ByteBuffer> &data) {
    if (stream.stream_type != DRTC_STREAM_VIDEO) {
        return;
    }

    if (!channel_info_.channel_name_.empty() &&
        std::string(stream.stream_id) != GetDrtcTargetStreamId(channel_info_)) {
        return;
    }

    // Store current stream id
    if (!peer_client_id.empty()) {
        current_stream_id_ = peer_client_id;
    }

    // Convert to Annex-B format and forward to callback
    auto annex_b_data = ConvertToAnnexB(data);
    if (!annex_b_data || annex_b_data->size() == 0) {
        return;
    }

    // H.265 NAL type: (nal_header >> 1) & 0x3F; IDR=19, VPS=32, SPS=33, PPS=34
    bool is_key_frame = false;
    if (annex_b_data->size() > ANNEX_B_START_CODE_SIZE) {
        const uint8_t nal_type = (annex_b_data->data()[ANNEX_B_START_CODE_SIZE] >> 1) & 0x3F;
        is_key_frame = (nal_type == 19 || nal_type == 32 || nal_type == 33 || nal_type == 34);
    }

    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr, annex_b_data, is_key_frame]() {
        WeakDummyReturn(weak_ptr);
        if (receive_video_data_callback_) {
            receive_video_data_callback_(annex_b_data, is_key_frame);
        }
    });
}

void DrtcUserLiveStreamChannel::OnDrtcUserPublishStreamNotify(const DrtcClient &client,
                                                               const DrtcStream &stream,
                                                               const std::string &appendix) {
    // Late join: auto-subscribe when UAV publishes matching VIDEO stream
    if (stream.stream_type != DRTC_STREAM_VIDEO || video_subscribed_) {
        return;
    }
    if (client.device_type != DRTC_DEVICE_UAV && client.device_type != DRTC_DEVICE_DOCK) {
        return;
    }
    const std::string target_stream_id = GetDrtcTargetStreamId(channel_info_);
    if (!target_stream_id.empty() &&
        std::string(stream.stream_id) != target_stream_id) {
        return;
    }
    LOG_INFO("[DrtcUserLiveStreamChannel] Late join: UAV published VIDEO stream %s, auto-subscribing",
             stream.stream_id);
    WeakDummy(p);
    working_executor_->Dispatch([this, p, client, stream]() {
        WeakDummyReturn(p);
        DoSubscribeVideoStream(client, stream);
    });
}

void DrtcUserLiveStreamChannel::OnDrtcUserUnpublishStreamNotify(const DrtcClient &client,
                                                                  const DrtcStream &stream) {
    if (stream.stream_type == DRTC_STREAM_VIDEO && video_subscribed_) {
        if (std::string(client.client_id) == std::string(video_producer_client_.client_id) &&
            std::string(stream.stream_id) == std::string(video_producer_stream_.stream_id)) {
            LOG_WARN("[DrtcUserLiveStreamChannel] UAV unpublished VIDEO stream, marking unsubscribed");
            video_subscribed_ = false;
        }
    }
}

// ======================== DrtcUserEngineEventsListener ========================

void DrtcUserLiveStreamChannel::OnDrtcUserJoinRoomNotify(const DrtcClient &client) {
    std::string client_id(client.client_id);
    if (std::find(users_list_.begin(), users_list_.end(), client_id) == users_list_.end()) {
        users_list_.push_back(client_id);
    }
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr]() {
        WeakDummyReturn(weak_ptr);
        decltype(users_list_update_callbacks_) callbacks_tmp;
        {
            std::lock_guard<std::mutex> lock(users_list_update_callbacks_mutex_);
            callbacks_tmp = users_list_update_callbacks_;
        }
        for (auto &cb : callbacks_tmp) {
            cb(users_list_);
        }
    });
}

void DrtcUserLiveStreamChannel::OnDrtcUserLeaveRoomNotify(const DrtcClient &client, drtc_reason_code_t reason) {
    std::string client_id(client.client_id);
    auto it = std::find(users_list_.begin(), users_list_.end(), client_id);
    if (it != users_list_.end()) {
        users_list_.erase(it);
    }
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr]() {
        WeakDummyReturn(weak_ptr);
        decltype(users_list_update_callbacks_) callbacks_tmp;
        {
            std::lock_guard<std::mutex> lock(users_list_update_callbacks_mutex_);
            callbacks_tmp = users_list_update_callbacks_;
        }
        for (auto &cb : callbacks_tmp) {
            cb(users_list_);
        }
    });
}

void DrtcUserLiveStreamChannel::OnDrtcUserTokenWillExpire(int expires_in_seconds) {
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr]() {
        WeakDummyReturn(weak_ptr);
        if (token_privilege_will_expire_callback_) {
            token_privilege_will_expire_callback_();
        }
    });
}

void DrtcUserLiveStreamChannel::OnDrtcUserTokenDidExpire() {
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr]() {
        WeakDummyReturn(weak_ptr);
        status_ = ChannelStatus::NEED_RENEW_TOKEN_OFFLINE;
    });
}

void DrtcUserLiveStreamChannel::OnDrtcUserGlobalError(drtc_devicesdk_error_code_t code, const std::string &msg) {
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr, msg]() {
        WeakDummyReturn(weak_ptr);
        if (global_error_callback_) {
            global_error_callback_("", msg);
        }
        status_ = ChannelStatus::OFFLINE;
    });
}

// ======================== Video Data Conversion ========================

std::shared_ptr<drc_sdk::ByteBuffer> DrtcUserLiveStreamChannel::ConvertToAnnexB(
    const std::shared_ptr<drc_sdk::ByteBuffer> &data) {
    if (!data || data->size() <= DRTC_VIDEO_TRAILER_SIZE) {
        return nullptr;
    }

    // DRTC SDK appends a 7-byte trailer at the end of each VIDEO callback (see stream-hub DecodeData).
    // Keep the first (size - 7) bytes, drop the trailer, then prepend Annex-B start code.
    size_t payload_size = data->size() - DRTC_VIDEO_TRAILER_SIZE;
    auto output = drc_sdk::ByteBufferPool::Instance()->AcquireShared();
    output->Resize(ANNEX_B_START_CODE_SIZE + payload_size);

    memcpy(output->data(), ANNEX_B_START_CODE, ANNEX_B_START_CODE_SIZE);
    memcpy(output->data() + ANNEX_B_START_CODE_SIZE, data->data(), payload_size);

    return output;
}
