// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_reply_relay_disconnect_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesReplyRelayDisconnectNorthbound::ServicesReplyRelayDisconnectNorthbound() {
    this->topic_ = "thing/product/gnd/services_reply/relay_disconnect";
    this->method_ = "relay_disconnect";
}

ServicesReplyRelayDisconnectNorthbound::ServicesReplyRelayDisconnectNorthbound(const std::shared_ptr<MessageV1>& message) : ServicesReplyRelayDisconnectNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyRelayDisconnectNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string ServicesReplyRelayDisconnectNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyRelayDisconnectNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesReplyRelayDisconnectNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesReplyRelayDisconnectNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}