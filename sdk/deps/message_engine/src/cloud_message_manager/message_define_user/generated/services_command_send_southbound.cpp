// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_command_send_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesCommandSendSouthbound::ServicesCommandSendSouthbound() {
    this->topic_ = "thing/product/gnd/services/command_send";
    this->method_ = "command_send";
}

ServicesCommandSendSouthbound::ServicesCommandSendSouthbound(const ServicesCommandSendSouthboundMsg& msg) : ServicesCommandSendSouthbound() {
    this->msg = msg;
}

std::string ServicesCommandSendSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesCommandSendSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesCommandSendSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesCommandSendSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}