// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drtc_user_real_time_channel.h"
#include "drtc_user_channel_define.h"

#include <assert.h>
#include <cstring>
#include <memory>
#include <random>

#include <message_decoder.h>
#include <json11.hpp>
#include <base/common/logger.h>
#include <base/io/byte_buffer_pool.h>
#include <common/util/async_capture_protect.h>

using namespace message_engine;

namespace {

constexpr uint16_t kMiddleStickValue = 1024;
constexpr uint16_t kStickControlCommandType = 1;
constexpr uint8_t kDrcJsonPrefix = 0x00;

std::string GenerateRandomStreamSuffix() {
    static const char kChars[] = "abcdefghijklmnopqrstuvwxyz0123456789";
    std::string result;
    result.reserve(14);
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(0, static_cast<int>(sizeof(kChars) - 2));
    for (int i = 0; i < 14; ++i) {
        result += kChars[dis(gen)];
    }
    return result;
}

void WriteUint16LE(uint8_t *p, uint16_t v) {
    p[0] = static_cast<uint8_t>(v & 0xff);
    p[1] = static_cast<uint8_t>((v >> 8) & 0xff);
}

std::shared_ptr<drc_sdk::ByteBuffer> BuildStickControlBinary(uint16_t seq, uint16_t roll, uint16_t pitch,
                                                             uint16_t throttle, uint16_t yaw,
                                                             uint16_t gimbal_pitch) {
    auto buffer = drc_sdk::ByteBufferPool::Instance()->AcquireShared();
    buffer->Resize(15);
    uint8_t *p = reinterpret_cast<uint8_t *>(buffer->data());
    *p++ = 1;
    WriteUint16LE(p, kStickControlCommandType);
    p += 2;
    WriteUint16LE(p, seq);
    p += 2;
    WriteUint16LE(p, roll);
    p += 2;
    WriteUint16LE(p, pitch);
    p += 2;
    WriteUint16LE(p, throttle);
    p += 2;
    WriteUint16LE(p, yaw);
    p += 2;
    WriteUint16LE(p, gimbal_pitch);
    return buffer;
}

bool TryParseStickControlJson(const std::string &message, std::shared_ptr<drc_sdk::ByteBuffer> &out_binary) {
    std::string err;
    const auto json = json11::Json::parse(message, err);
    if (!err.empty() || !json.is_object()) {
        return false;
    }
    const auto &obj = json.object_items();
    if (obj.find("method") == obj.end() || obj.at("method").string_value() != "stick_control") {
        return false;
    }
    if (!obj.at("data").is_object()) {
        return false;
    }
    const auto &data = obj.at("data").object_items();
    auto get_u16 = [&](const char *key, uint16_t def) -> uint16_t {
        const auto it = data.find(key);
        if (it == data.end()) {
            return def;
        }
        return static_cast<uint16_t>(it->second.int_value() & 0xffff);
    };
    const uint16_t seq = static_cast<uint16_t>(
        obj.find("seq") != obj.end() ? obj.at("seq").int_value() & 0xffff : 0);
    out_binary = BuildStickControlBinary(seq, get_u16("roll", kMiddleStickValue), get_u16("pitch", kMiddleStickValue),
                                         get_u16("throttle", kMiddleStickValue), get_u16("yaw", kMiddleStickValue),
                                         get_u16("gimbal_pitch", kMiddleStickValue));
    return true;
}

std::shared_ptr<drc_sdk::ByteBuffer> WrapCommandJson(const std::string &json) {
    auto buffer = drc_sdk::ByteBufferPool::Instance()->AcquireShared();
    buffer->Resize(json.size() + 1);
    buffer->data()[0] = static_cast<char>(kDrcJsonPrefix);
    memcpy(buffer->data() + 1, json.data(), json.size());
    return buffer;
}

bool LooksLikeJsonPayload(const uint8_t *data, size_t size) {
    if (size == 0) {
        return false;
    }
    const char c = static_cast<char>(data[0]);
    return c == '{' || c == '[';
}

}  // namespace

DrtcUserRealTimeChannel::DrtcUserRealTimeChannel(const ChannelInfo &channel_info, const std::string &token,
                                                   const std::shared_ptr<drc_sdk::Executor> &working_executor,
                                                   const std::string &log_tag)
    : RealTimeChannel(channel_info, token, working_executor) {
    log_tag_ = log_tag;
    scheduler_ = std::make_shared<MessageScheduler>(working_executor_, log_tag_);
    scheduler_->Start();

    memset(&control_stream_, 0, sizeof(DrtcStream));
    memset(&local_command_stream_, 0, sizeof(DrtcStream));
    memset(&command_producer_client_, 0, sizeof(DrtcClient));
    memset(&command_producer_stream_, 0, sizeof(DrtcStream));

    LOG_INFO("[DrtcUserRealTimeChannel] construct, channel: %s", channel_info.channel_name_.c_str());
}

DrtcUserRealTimeChannel::~DrtcUserRealTimeChannel() {
    LOG_INFO("[DrtcUserRealTimeChannel] destruct, channel: %s", channel_info_.channel_name_.c_str());
    scheduler_->Stop();

    if (drtc_user_engine_) {
        drtc_user_engine_->RemoveMessageReceiver(message_listen_id_);
        drtc_user_engine_->RemoveLiveStreamReceiver(live_stream_listen_id_);
        drtc_user_engine_->RemoveEventsListener(event_listen_id_);
    }

    DrtcUserEngineManager::GetInstance().ReleaseEngine(channel_info_.room_id_, DRTC_USER_OWNER_REALTIME);
}

bool DrtcUserRealTimeChannel::Join(const JoinChannelCallback &callback) {
    LOG_INFO("[DrtcUserRealTimeChannel] Join channel: %s, room: %s",
             channel_info_.channel_name_.c_str(), channel_info_.room_id_.c_str());

    drtc_user_engine_ = DrtcUserEngineManager::GetInstance().AcquireEngine(
        channel_info_.room_id_, DRTC_USER_OWNER_REALTIME,
        channel_info_.config_path_, working_executor_);

    if (!drtc_user_engine_) {
        LOG_ERROR("[DrtcUserRealTimeChannel] Failed to acquire engine");
        return false;
    }

    drtc_user_engine_->SetProviderUrl(channel_info_.url_);
    if (!drtc_user_engine_->GetIsInited()) {
        if (!drtc_user_engine_->Init(channel_info_.client_id_)) {
            LOG_ERROR("[DrtcUserRealTimeChannel] Engine init failed");
            return false;
        }
    }

    message_listen_id_ = drtc_user_engine_->AddMessageReceiver(shared_from_this());
    live_stream_listen_id_ = drtc_user_engine_->AddLiveStreamReceiver(shared_from_this());
    event_listen_id_ = drtc_user_engine_->AddEventsListener(shared_from_this());

    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr, callback]() {
        WeakDummyReturn(weak_ptr);

        if (drtc_user_engine_->GetJoinState() == DrtcUserJoinRoomState::JOINED) {
            LOG_INFO("[DrtcUserRealTimeChannel] Engine already joined, subscribing directly");
            SubscribeCommandStream(callback);
            return;
        }

        auto ret = drtc_user_engine_->JoinRoom(
            channel_info_.app_id_, channel_info_.room_id_, token_, channel_info_.url_,
            [this, weak_ptr, callback](const std::string &room_id, bool is_succeed,
                                        int error_code, const DrtcJoinRoomResp &resp) {
                WeakDummyReturn(weak_ptr);
                working_executor_->Dispatch([this, weak_ptr, callback, is_succeed, error_code]() {
                    WeakDummyReturn(weak_ptr);
                    if (is_succeed) {
                        SubscribeCommandStream(callback);
                    } else {
                        LOG_ERROR("[DrtcUserRealTimeChannel] JoinRoom failed, code: %d", error_code);
                        if (callback) {
                            callback(false, 0, false, error_code, "JoinRoom failed");
                        }
                    }
                });
            });

        if (ret != 0) {
            LOG_ERROR("[DrtcUserRealTimeChannel] JoinRoom call failed, ret: %d", ret);
            if (callback) {
                callback(false, 0, false, ret, "JoinRoom call failed");
            }
        }
    });
    return true;
}

void DrtcUserRealTimeChannel::SubscribeCommandStream(const JoinChannelCallback &callback) {
    LOG_INFO("[DrtcUserRealTimeChannel] SubscribeCommandStream");

    if (!FindUavProducer(command_producer_client_)) {
        LOG_WARN("[DrtcUserRealTimeChannel] No UAV producer found, proceeding to publish local streams");
        PublishLocalStreams(callback);
        return;
    }

    auto published_streams = drtc_user_engine_->GetPublishedStreams();
    bool found_command = false;
    for (const auto &info : published_streams) {
        if (std::string(info.client.client_id) == std::string(command_producer_client_.client_id)) {
            for (const auto &ps : info.streams) {
                if (ps.stream.stream_type == DRTC_STREAM_COMMAND) {
                    command_producer_stream_ = ps.stream;
                    found_command = true;
                    break;
                }
            }
            break;
        }
    }

    if (!found_command) {
        LOG_WARN("[DrtcUserRealTimeChannel] No UAV COMMAND stream found, proceeding to publish local streams");
        PublishLocalStreams(callback);
        return;
    }

    DoSubscribeCommandStream(command_producer_client_, command_producer_stream_, callback);
}

void DrtcUserRealTimeChannel::DoSubscribeCommandStream(const DrtcClient &producer, const DrtcStream &stream,
                                                        const JoinChannelCallback &callback) {
    command_producer_client_ = producer;
    command_producer_stream_ = stream;

    if (command_subscribed_ &&
        std::string(producer.client_id) == std::string(command_producer_client_.client_id) &&
        std::string(stream.stream_id) == std::string(command_producer_stream_.stream_id)) {
        LOG_INFO("[DrtcUserRealTimeChannel] UAV COMMAND already subscribed, stream: %s", stream.stream_id);
        if (callback) {
            PublishLocalStreams(callback);
        }
        return;
    }

    WeakDummy(p);
    auto ret = drtc_user_engine_->Subscribe(producer, stream,
        [this, p, callback](bool is_succeed, const DrtcSubscribeResp &resp) {
            WeakDummyReturn(p);
            working_executor_->Dispatch([this, p, callback, is_succeed]() {
                WeakDummyReturn(p);
                if (is_succeed) {
                    command_subscribed_ = true;
                    LOG_INFO("[DrtcUserRealTimeChannel] Subscribe UAV COMMAND success");
                } else {
                    LOG_WARN("[DrtcUserRealTimeChannel] Subscribe UAV COMMAND failed, proceeding anyway");
                }
                if (callback) {
                    PublishLocalStreams(callback);
                }
            });
        });

    if (ret != 0) {
        LOG_WARN("[DrtcUserRealTimeChannel] Subscribe call failed, ret: %d, proceeding to publish", ret);
        if (callback) {
            PublishLocalStreams(callback);
        }
    }
}

void DrtcUserRealTimeChannel::PublishLocalStreams(const JoinChannelCallback &callback) {
    LOG_INFO("[DrtcUserRealTimeChannel] PublishLocalStreams");

    if (pub_stick_stream_id_.empty()) {
        const auto suffix = GenerateRandomStreamSuffix();
        pub_stick_stream_id_ = "rt_channel_stick_" + suffix;
        pub_command_stream_id_ = "rt_channel_cmd_" + suffix;
    }

    DrtcStream stick_stream{};
    stick_stream.stream_type = DRTC_STREAM_CONTROL;
    strncpy(stick_stream.stream_id, pub_stick_stream_id_.c_str(), STREAM_ID_LENGTH - 1);

    WeakDummy(p);
    auto ret = drtc_user_engine_->Publish(stick_stream,
        [this, p, callback](bool is_succeed, const DrtcStream &published_stream, int error_code) {
            WeakDummyReturn(p);
            working_executor_->Dispatch([this, p, callback, is_succeed, published_stream, error_code]() {
                WeakDummyReturn(p);
                if (!is_succeed) {
                    LOG_ERROR("[DrtcUserRealTimeChannel] Publish CONTROL failed, code: %d", error_code);
                    if (callback) {
                        callback(false, 0, false, error_code, "Publish CONTROL failed");
                    }
                    return;
                }

                control_stream_ = published_stream;
                control_published_ = true;
                LOG_INFO("[DrtcUserRealTimeChannel] Publish CONTROL success, stream: %s",
                         control_stream_.stream_id);

                DrtcStream cmd_stream{};
                cmd_stream.stream_type = DRTC_STREAM_COMMAND;
                strncpy(cmd_stream.stream_id, pub_command_stream_id_.c_str(), STREAM_ID_LENGTH - 1);

                auto cmd_ret = drtc_user_engine_->Publish(cmd_stream,
                    [this, p, callback](bool cmd_ok, const DrtcStream &published_cmd, int cmd_err) {
                        WeakDummyReturn(p);
                        working_executor_->Dispatch([this, p, callback, cmd_ok, published_cmd, cmd_err]() {
                            WeakDummyReturn(p);
                            if (cmd_ok) {
                                local_command_stream_ = published_cmd;
                                command_published_ = true;
                                status_ = ChannelStatus::OK;
                                life_cycle_info_.joined_time_ = std::chrono::steady_clock::now();
                                scheduler_->AddListener(shared_from_this());
                                LOG_INFO("[DrtcUserRealTimeChannel] Publish COMMAND success, stream: %s",
                                         local_command_stream_.stream_id);
                            } else {
                                LOG_ERROR("[DrtcUserRealTimeChannel] Publish COMMAND failed, code: %d", cmd_err);
                            }
                            if (callback) {
                                callback(cmd_ok, 0, false, cmd_err,
                                         cmd_ok ? "" : "Publish COMMAND failed");
                            }
                        });
                    });

                if (cmd_ret != 0) {
                    LOG_ERROR("[DrtcUserRealTimeChannel] Publish COMMAND call failed, ret: %d", cmd_ret);
                    if (callback) {
                        callback(false, 0, false, cmd_ret, "Publish COMMAND call failed");
                    }
                }
            });
        });

    if (ret != 0) {
        LOG_ERROR("[DrtcUserRealTimeChannel] Publish CONTROL call failed, ret: %d", ret);
        if (callback) {
            callback(false, 0, false, ret, "Publish CONTROL call failed");
        }
    }
}

bool DrtcUserRealTimeChannel::FindUavProducer(DrtcClient &out_client) const {
    auto published_streams = drtc_user_engine_->GetPublishedStreams();
    for (const auto &info : published_streams) {
        if (info.client.device_type == DRTC_DEVICE_UAV ||
            info.client.device_type == DRTC_DEVICE_DOCK) {
            out_client = info.client;
            return true;
        }
    }
    return false;
}

bool DrtcUserRealTimeChannel::Leave() {
    LOG_INFO("[DrtcUserRealTimeChannel] Leave channel: %s", channel_info_.channel_name_.c_str());

    if (!drtc_user_engine_) {
        return false;
    }

    if (control_published_) {
        drtc_user_engine_->Unpublish(control_stream_);
        control_published_ = false;
    }

    if (command_published_) {
        drtc_user_engine_->Unpublish(local_command_stream_);
        command_published_ = false;
    }

    if (command_subscribed_) {
        drtc_user_engine_->Unsubscribe(command_producer_client_, command_producer_stream_);
        command_subscribed_ = false;
    }

    pub_stick_stream_id_.clear();
    pub_command_stream_id_.clear();
    status_ = ChannelStatus::OFFLINE;
    return true;
}

bool DrtcUserRealTimeChannel::RenewToken(const std::string &token) {
    LOG_INFO("[DrtcUserRealTimeChannel] RenewToken");
    if (!drtc_user_engine_) {
        return false;
    }
    token_ = token;
    auto ret = drtc_user_engine_->UpdateToken(token);
    return ret == 0;
}

void DrtcUserRealTimeChannel::SendStringPayload(const std::string &message,
                                                 const SendRealTimeMessageCallback &callback) {
    std::shared_ptr<drc_sdk::ByteBuffer> stick_buffer;
    if (TryParseStickControlJson(message, stick_buffer)) {
        if (!control_published_) {
            if (callback) {
                callback(false, -1, "CONTROL stream not ready");
            }
            return;
        }
        const auto ret = drtc_user_engine_->SendData(control_stream_, stick_buffer);
        if (callback) {
            callback(ret == 0, ret, "");
        }
        return;
    }

    if (!command_published_) {
        if (callback) {
            callback(false, -1, "COMMAND stream not ready");
        }
        return;
    }
    const auto buffer = WrapCommandJson(message);
    const auto ret = drtc_user_engine_->SendData(local_command_stream_, buffer);
    if (callback) {
        callback(ret == 0, ret, "");
    }
}

void DrtcUserRealTimeChannel::SendBinaryMessage(const std::string &target_user_id,
                                                  const std::shared_ptr<drc_sdk::ByteBuffer> &message,
                                                  bool is_reliable,
                                                  const SendRealTimeMessageCallback &callback) {
    (void)target_user_id;
    (void)is_reliable;
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr, message, callback]() {
        WeakDummyReturn(weak_ptr);
        if (!drtc_user_engine_ || !control_published_) {
            if (callback) {
                callback(false, -1, "Not ready");
            }
            return;
        }
        // Align with device DrtcRealTimeChannel::SendBinaryMessage:
        // DRTC has no native is_binary flag; prepend 1-byte drc_binary_header on wire.
        // OnSentV3 packs pure MessageV3; framing belongs here, not in V3 encoding.
        if (!message) {
            if (callback) {
                callback(false, -1, "null message");
            }
            return;
        }
        auto buffer = drc_sdk::ByteBufferPool::Instance()->AcquireShared();
        drc_binary_header header{};
        header.is_binary = true;
        buffer->Assign(&header, sizeof(drc_binary_header));
        buffer->Write(message->data(), message->size());
        const auto ret = drtc_user_engine_->SendData(control_stream_, buffer);
        if (callback) {
            callback(ret == 0, ret, "");
        }
    });
}

void DrtcUserRealTimeChannel::SendStringMessage(const std::string &target_user_id,
                                                  const std::string &message, bool is_reliable,
                                                  const SendRealTimeMessageCallback &callback) {
    (void)target_user_id;
    (void)is_reliable;
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr, message, callback]() {
        WeakDummyReturn(weak_ptr);
        if (!drtc_user_engine_) {
            if (callback) {
                callback(false, -1, "Not ready");
            }
            return;
        }
        SendStringPayload(message, callback);
    });
}

void DrtcUserRealTimeChannel::SendBinaryBroadcastMessage(const std::shared_ptr<drc_sdk::ByteBuffer> &message,
                                                          bool is_reliable,
                                                          const SendRealTimeMessageCallback &callback) {
    SendBinaryMessage("", message, is_reliable, callback);
}

void DrtcUserRealTimeChannel::SendStringBroadcastMessage(const std::string &message, bool is_reliable,
                                                          const SendRealTimeMessageCallback &callback) {
    SendStringMessage("", message, is_reliable, callback);
}

void DrtcUserRealTimeChannel::OnDrtcUserMessageReceived(const std::string &peer_client_id,
                                                         const DrtcStream &stream,
                                                         const std::shared_ptr<drc_sdk::ByteBuffer> &message) {
    if (stream.stream_type != DRTC_STREAM_COMMAND) {
        return;
    }

    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr, peer_client_id, message]() {
        WeakDummyReturn(weak_ptr);
        if (!message || message->size() == 0) {
            return;
        }

        const uint8_t *data = reinterpret_cast<const uint8_t *>(message->data());
        size_t size = message->size();

        if (data[0] == kDrcJsonPrefix && size > 1 && LooksLikeJsonPayload(data + 1, size - 1)) {
            data += 1;
            size -= 1;
        }

        if (LooksLikeJsonPayload(data, size)) {
            const std::string msg_str(reinterpret_cast<const char *>(data), size);
            OnReceivedStringMessage(peer_client_id, msg_str);
            if (receive_string_message_callback_) {
                receive_string_message_callback_(peer_client_id, msg_str);
            }
            return;
        }

        if (size >= 1 && data[0] == 1) {
            auto buffer = drc_sdk::ByteBufferPool::Instance()->AcquireShared();
            buffer->Assign(data + 1, size - 1);
            OnReceivedBinaryMessage(peer_client_id, buffer);
            if (receive_binary_message_callback_) {
                receive_binary_message_callback_(peer_client_id, buffer);
            }
            return;
        }

        const std::string msg_str(reinterpret_cast<const char *>(data), size);
        OnReceivedStringMessage(peer_client_id, msg_str);
        if (receive_string_message_callback_) {
            receive_string_message_callback_(peer_client_id, msg_str);
        }
    });
}

void DrtcUserRealTimeChannel::OnDrtcUserVideoDataReceived(const std::string &peer_client_id,
                                                           const DrtcStream &stream,
                                                           const std::shared_ptr<drc_sdk::ByteBuffer> &data) {
    (void)peer_client_id;
    (void)stream;
    (void)data;
}

void DrtcUserRealTimeChannel::OnDrtcUserPublishStreamNotify(const DrtcClient &client,
                                                             const DrtcStream &stream,
                                                             const std::string &appendix) {
    (void)appendix;
    if (command_subscribed_ || stream.stream_type != DRTC_STREAM_COMMAND) {
        return;
    }
    if (client.device_type != DRTC_DEVICE_UAV && client.device_type != DRTC_DEVICE_DOCK) {
        return;
    }

    LOG_INFO("[DrtcUserRealTimeChannel] Late join: UAV published COMMAND stream %s, auto-subscribing",
             stream.stream_id);
    command_producer_client_ = client;
    command_producer_stream_ = stream;

    WeakDummy(p);
    working_executor_->Dispatch([this, p, client, stream]() {
        WeakDummyReturn(p);
        DoSubscribeCommandStream(client, stream, nullptr);
    });
}

void DrtcUserRealTimeChannel::OnDrtcUserUnpublishStreamNotify(const DrtcClient &client,
                                                                const DrtcStream &stream) {
    if (stream.stream_type != DRTC_STREAM_COMMAND || !command_subscribed_) {
        return;
    }
    if (std::string(client.client_id) == std::string(command_producer_client_.client_id) &&
        std::string(stream.stream_id) == std::string(command_producer_stream_.stream_id)) {
        LOG_WARN("[DrtcUserRealTimeChannel] UAV unpublished COMMAND stream, marking unsubscribed");
        command_subscribed_ = false;
    }
}

void DrtcUserRealTimeChannel::OnDrtcUserJoinRoomNotify(const DrtcClient &client) {
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

void DrtcUserRealTimeChannel::OnDrtcUserLeaveRoomNotify(const DrtcClient &client, drtc_reason_code_t reason) {
    (void)reason;
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

void DrtcUserRealTimeChannel::OnDrtcUserTokenWillExpire(int expires_in_seconds) {
    (void)expires_in_seconds;
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr]() {
        WeakDummyReturn(weak_ptr);
        if (token_privilege_will_expire_callback_) {
            token_privilege_will_expire_callback_();
        }
    });
}

void DrtcUserRealTimeChannel::OnDrtcUserTokenDidExpire() {
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr]() {
        WeakDummyReturn(weak_ptr);
        status_ = ChannelStatus::NEED_RENEW_TOKEN_OFFLINE;
    });
}

void DrtcUserRealTimeChannel::OnDrtcUserGlobalError(drtc_devicesdk_error_code_t code, const std::string &msg) {
    (void)code;
    WeakDummy(weak_ptr);
    working_executor_->Dispatch([this, weak_ptr, msg]() {
        WeakDummyReturn(weak_ptr);
        if (global_error_callback_) {
            global_error_callback_("", msg);
        }
        status_ = ChannelStatus::OFFLINE;
    });
}

ListenIdCode DrtcUserRealTimeChannel::Observe(const std::shared_ptr<ObserveCallback> &callback) {
    return scheduler_->AddObserve(callback);
}

void DrtcUserRealTimeChannel::CancelObserve(const ListenIdCode &listen_id) {
    scheduler_->CancelObserve(listen_id);
}

MeErrorCode DrtcUserRealTimeChannel::Send(const std::shared_ptr<MessageWrapper> &message) {
    return scheduler_->ProcessSendMessage(message);
}

MeErrorCode DrtcUserRealTimeChannel::Send(const std::shared_ptr<MessageWrapper> &message,
                                            const MeResponse &response,
                                            const std::shared_ptr<drc_sdk::Executor> &user_define_executor) {
    return scheduler_->ProcessSendMessage(message, response, user_define_executor);
}

void DrtcUserRealTimeChannel::OnMessageNeedToBeSent(const std::shared_ptr<MessageWrapper> &message) {
    switch (message->GetMessageProtocolType()) {
        case MessageProtocolType::V1: {
            const auto v1_message = std::dynamic_pointer_cast<MessageV1>(message);
            OnSentV1Message(v1_message);
            break;
        }
        case MessageProtocolType::V2: {
            const auto v2_message = std::dynamic_pointer_cast<MessageV2>(message);
            OnSentV2Message(v2_message);
            break;
        }
        case MessageProtocolType::V3: {
            const auto v3_message = std::dynamic_pointer_cast<MessageV3>(message);
            OnSentV3Message(v3_message);
            break;
        }
        default:
            break;
    }
}

void DrtcUserRealTimeChannel::OnSentV1Message(const std::shared_ptr<MessageV1> &message) {
    (void)message;
    LOG_ERROR("[Transport-drtc-user] OnSentV1Message, not support");
}

void DrtcUserRealTimeChannel::OnSentV2Message(const std::shared_ptr<MessageV2> &message) {
    std::string error;
    json11::Json::object content;
    content["method"] = message->GetMethod();
    content["seq"] = static_cast<double>(message->GetSeq());
    content["data"] = json11::Json::parse(message->ToJsonString(), error);
    if (!error.empty()) {
        LOG_ERROR("[Transport-drtc-user] OnSentV2Message Failed to parse: %s, error: %s",
                  message->ToJsonString().c_str(), error.c_str());
        return;
    }
    const auto message_string = json11::Json(content).dump();

    if (message->GetPeerId().empty()) {
        SendStringBroadcastMessage(message_string, true,
            [](const bool &is_successed, const int &error_code, const std::string &extencontent) {
                (void)is_successed;
                (void)error_code;
                (void)extencontent;
            });
    } else {
        SendStringMessage(message->GetPeerId(), message_string, true,
            [](const bool &is_successed, const int &error_code, const std::string &extencontent) {
                (void)is_successed;
                (void)error_code;
                (void)extencontent;
            });
    }
}

void DrtcUserRealTimeChannel::OnSentV3Message(const std::shared_ptr<MessageV3> &message) {
    auto data_buffer = std::make_shared<drc_sdk::ByteBuffer>();
    data_buffer->Resize(sizeof(MessageV3Header) + message->ToBinary().size());

    const auto v3_header = reinterpret_cast<MessageV3Header *>(data_buffer->data());
    v3_header->command_id = message->GetCommandId();
    v3_header->seq = message->GetSeq();
    memcpy(data_buffer->data() + sizeof(MessageV3Header), message->ToBinary().data(), message->ToBinary().size());

    if (message->GetPeerId().empty()) {
        SendBinaryBroadcastMessage(data_buffer, true,
            [](const bool &is_successed, const int &error_code, const std::string &extencontent) {
                (void)is_successed;
                (void)error_code;
                (void)extencontent;
            });
    } else {
        SendBinaryMessage(message->GetPeerId(), data_buffer, true,
            [](const bool &is_successed, const int &error_code, const std::string &extencontent) {
                (void)is_successed;
                (void)error_code;
                (void)extencontent;
            });
    }
}

void DrtcUserRealTimeChannel::OnReceivedBinaryMessage(const std::string &source_user_id,
                                                       const std::shared_ptr<drc_sdk::ByteBuffer> &byte_buffer) {
    auto message = MessageDecoder::Decode(byte_buffer);
    if (message == nullptr) {
        LOG_ERROR("[Transport-%s] OnReceivedBinaryMessage failed, user: %s", log_tag_.c_str(), source_user_id.c_str());
        return;
    }
    OnReceivedMessage(source_user_id, message);
}

void DrtcUserRealTimeChannel::OnReceivedStringMessage(const std::string &source_user_id,
                                                       const std::string &string_message) {
    auto message = MessageDecoder::Decode(string_message);
    if (message == nullptr) {
        LOG_ERROR("[Transport-%s] OnReceivedStringMessage failed, user: %s", log_tag_.c_str(), source_user_id.c_str());
        return;
    }
    OnReceivedMessage(source_user_id, message);
}

void DrtcUserRealTimeChannel::OnReceivedMessage(const std::string &source_user_id,
                                                  const std::shared_ptr<MessageWrapper> &message) {
    if (message == nullptr) {
        return;
    }
    message->SetPeerId(source_user_id);
    if (message->GetMessageProtocolType() == MessageProtocolType::V1) {
        return;
    }
    scheduler_->ProcessReceivedMessage(message);
}
