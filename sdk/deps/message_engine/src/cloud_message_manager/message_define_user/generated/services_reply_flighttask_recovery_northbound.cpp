// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_reply_flighttask_recovery_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesReplyFlighttaskRecoveryNorthbound::ServicesReplyFlighttaskRecoveryNorthbound() {
    this->topic_ = "thing/product/uav/services_reply/flighttask_recovery";
    this->method_ = "flighttask_recovery";
}

ServicesReplyFlighttaskRecoveryNorthbound::ServicesReplyFlighttaskRecoveryNorthbound(const std::shared_ptr<MessageV1>& message) : ServicesReplyFlighttaskRecoveryNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyFlighttaskRecoveryNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string ServicesReplyFlighttaskRecoveryNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyFlighttaskRecoveryNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesReplyFlighttaskRecoveryNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesReplyFlighttaskRecoveryNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}