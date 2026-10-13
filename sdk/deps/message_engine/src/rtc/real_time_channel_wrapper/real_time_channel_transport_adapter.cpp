// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "real_time_channel_transport_adapter.h"
#include "cloud_message_manager/message_decoder/message_decoder.h"
#include "cloud_message_manager/message_wrapper/message_v1.h"
#include "cloud_message_manager/message_wrapper/message_v2.h"
#include "cloud_message_manager/message_wrapper/message_v3.h"
#include "base/common/logger.h"
#include <json11.hpp>
#include <cstring>

namespace message_engine {

RealTimeChannelTransportAdapter::RealTimeChannelTransportAdapter(
    const std::shared_ptr<RealTimeChannelWrapper>& wrapper,
    const std::shared_ptr<TransportManager>& transport_manager,
    const std::shared_ptr<drc_sdk::Executor>& executor,
    const std::string& log_tag)
    : wrapper_(wrapper),
      transport_manager_(transport_manager),
      log_tag_(log_tag) {
    LOG_INFO("[TransportAdapter-%s] Constructor: creating MessageScheduler", log_tag_.c_str());
    scheduler_ = std::make_shared<MessageScheduler>(executor, log_tag_);
    LOG_INFO("[TransportAdapter-%s] Constructor: calling scheduler_->Start()", log_tag_.c_str());
    scheduler_->Start();
    LOG_INFO("[TransportAdapter-%s] Constructor: Start done, calling SetObserveCallbackManager", log_tag_.c_str());
    scheduler_->SetObserveCallbackManager(transport_manager_->GetObserveCallbackManager());
    LOG_INFO("[TransportAdapter-%s] Constructor: setting receive callbacks", log_tag_.c_str());
    wrapper_->SetReceiveBinaryMessageCallback(
        [this](const std::string& source_user_id, const std::shared_ptr<drc_sdk::ByteBuffer>& message) {
            OnReceivedBinaryMessage(source_user_id, message);
        });
    wrapper_->SetReceiveStringMessageCallback(
        [this](const std::string& source_user_id, const std::string& message) {
            OnReceivedStringMessage(source_user_id, message);
        });
    LOG_INFO("[TransportAdapter-%s] Constructor done", log_tag_.c_str());
}

RealTimeChannelTransportAdapter::~RealTimeChannelTransportAdapter() {
    if (scheduler_) {
        scheduler_->Stop();
    }
}

void RealTimeChannelTransportAdapter::Init() {
    LOG_INFO("[TransportAdapter-%s] Init: AddListener(shared_from_this)", log_tag_.c_str());
    scheduler_->AddListener(shared_from_this());
    LOG_INFO("[TransportAdapter-%s] Init done", log_tag_.c_str());
}

MeErrorCode RealTimeChannelTransportAdapter::Send(const std::shared_ptr<MessageWrapper>& message) {
    return scheduler_->ProcessSendMessage(message);
}

MeErrorCode RealTimeChannelTransportAdapter::Send(const std::shared_ptr<MessageWrapper>& message,
                                                  const MeResponse& response,
                                                  const std::shared_ptr<drc_sdk::Executor>& user_define_executor) {
    return scheduler_->ProcessSendMessage(message, response, user_define_executor);
}

ListenIdCode RealTimeChannelTransportAdapter::Observe(const std::shared_ptr<ObserveCallback>& callback) {
    return scheduler_->AddObserve(callback);
}

void RealTimeChannelTransportAdapter::CancelObserve(const ListenIdCode& listen_id) {
    scheduler_->CancelObserve(listen_id);
}

void RealTimeChannelTransportAdapter::OnMessageNeedToBeSent(const std::shared_ptr<MessageWrapper>& message) {
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

void RealTimeChannelTransportAdapter::OnSentV1Message(const std::shared_ptr<MessageV1>& /*message*/) {
    LOG_ERROR("[Transport-%s] OnSentV1Message, not support", log_tag_.c_str());
}

void RealTimeChannelTransportAdapter::OnSentV2Message(const std::shared_ptr<MessageV2>& message) {
    if (!message) return;
    std::string error;
    json11::Json::object content;
    content["method"] = message->GetMethod();
    content["seq"] = static_cast<double>(message->GetSeq());
    // GetData() only has value when constructed from received data.
    // For outgoing generated messages (e.g. DrcHeartBeatSouthbound),
    // data is in the msg struct and serialized via ToJsonString().
    std::string data_json = message->GetData();
    if (data_json.empty()) {
        data_json = message->ToJsonString();
    }
    content["data"] = json11::Json::parse(data_json, error);
    if (!error.empty()) {
        LOG_ERROR("[Transport-%s] OnSentV2Message parse error: %s, data_json: %s", log_tag_.c_str(), error.c_str(), data_json.c_str());
        return;
    }
    const auto message_string = json11::Json(content).dump();
    if (message->GetPeerId().empty()) {
        wrapper_->SendStringBroadcastMessage(message_string, true, [](const bool&, const int&, const std::string&) {});
    } else {
        wrapper_->SendStringMessage(message->GetPeerId(), message_string, true,
                                    [](const bool&, const int&, const std::string&) {});
    }
}

void RealTimeChannelTransportAdapter::OnSentV3Message(const std::shared_ptr<MessageV3>& message) {
    if (!message) return;
    auto data_buffer = std::make_shared<drc_sdk::ByteBuffer>();
    data_buffer->Resize(sizeof(MessageV3Header) + message->ToBinary().size());
    auto* header_ptr = reinterpret_cast<MessageV3Header*>(data_buffer->data());
    header_ptr->command_id = message->GetCommandId();
    header_ptr->seq = message->GetSeq();
    memcpy(data_buffer->data() + sizeof(MessageV3Header), message->ToBinary().data(),
           message->ToBinary().size());
    if (message->GetPeerId().empty()) {
        wrapper_->SendBinaryBroadcastMessage(data_buffer, true, [](const bool&, const int&, const std::string&) {});
    } else {
        wrapper_->SendBinaryMessage(message->GetPeerId(), data_buffer, true,
                                    [](const bool&, const int&, const std::string&) {});
    }
}

void RealTimeChannelTransportAdapter::OnReceivedBinaryMessage(const std::string& source_user_id,
                                                              const std::shared_ptr<drc_sdk::ByteBuffer>& message) {
    auto decoded = MessageDecoder::Decode(message);
    if (!decoded) {
        LOG_ERROR("[Transport-%s] OnReceivedBinaryMessage decode failed", log_tag_.c_str());
        return;
    }
    decoded->SetPeerId(source_user_id);
    scheduler_->ProcessReceivedMessage(decoded);
}

void RealTimeChannelTransportAdapter::OnReceivedStringMessage(const std::string& source_user_id,
                                                             const std::string& message) {
    auto decoded = MessageDecoder::Decode(message);
    if (!decoded) {
        LOG_ERROR("[Transport-%s] OnReceivedStringMessage decode failed", log_tag_.c_str());
        return;
    }
    decoded->SetPeerId(source_user_id);
    scheduler_->ProcessReceivedMessage(decoded);
}

}  // namespace message_engine
