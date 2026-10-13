// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_reply_esim_operator_switch_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesReplyEsimOperatorSwitchNorthbound::ServicesReplyEsimOperatorSwitchNorthbound() {
    this->topic_ = "thing/product/uav/services_reply/esim_operator_switch";
    this->method_ = "esim_operator_switch";
}

ServicesReplyEsimOperatorSwitchNorthbound::ServicesReplyEsimOperatorSwitchNorthbound(const std::shared_ptr<MessageV1>& message) : ServicesReplyEsimOperatorSwitchNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyEsimOperatorSwitchNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string ServicesReplyEsimOperatorSwitchNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReplyEsimOperatorSwitchNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesReplyEsimOperatorSwitchNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesReplyEsimOperatorSwitchNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}