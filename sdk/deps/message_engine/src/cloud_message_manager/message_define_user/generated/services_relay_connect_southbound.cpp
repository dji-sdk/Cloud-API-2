// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_relay_connect_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesRelayConnectSouthbound::ServicesRelayConnectSouthbound() {
    this->topic_ = "thing/product/gnd/services/relay_connect";
    this->method_ = "relay_connect";
}

ServicesRelayConnectSouthbound::ServicesRelayConnectSouthbound(const ServicesRelayConnectSouthboundMsg& msg) : ServicesRelayConnectSouthbound() {
    this->msg = msg;
}

std::string ServicesRelayConnectSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesRelayConnectSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesRelayConnectSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesRelayConnectSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}