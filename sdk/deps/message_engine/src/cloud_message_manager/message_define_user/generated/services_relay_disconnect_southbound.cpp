// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_relay_disconnect_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesRelayDisconnectSouthbound::ServicesRelayDisconnectSouthbound() {
    this->topic_ = "thing/product/gnd/services/relay_disconnect";
    this->method_ = "relay_disconnect";
}

ServicesRelayDisconnectSouthbound::ServicesRelayDisconnectSouthbound(const ServicesRelayDisconnectSouthboundMsg& msg) : ServicesRelayDisconnectSouthbound() {
    this->msg = msg;
}

std::string ServicesRelayDisconnectSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesRelayDisconnectSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesRelayDisconnectSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesRelayDisconnectSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}