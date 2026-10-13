// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_reply_relay_passwd_hash_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesReplyRelayPasswdHashNorthbound::ServicesReplyRelayPasswdHashNorthbound() {
    this->topic_ = "thing/product/gnd/services_reply/relay_passwd_hash";
    this->method_ = "relay_passwd_hash";
}

ServicesReplyRelayPasswdHashNorthbound::ServicesReplyRelayPasswdHashNorthbound(const std::shared_ptr<MessageV1>& message) : ServicesReplyRelayPasswdHashNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyRelayPasswdHashNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string ServicesReplyRelayPasswdHashNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyRelayPasswdHashNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesReplyRelayPasswdHashNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesReplyRelayPasswdHashNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}