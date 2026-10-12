// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_relay_scan_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesRelayScanSouthbound::ServicesRelayScanSouthbound() {
    this->topic_ = "thing/product/gnd/services/relay_scan";
    this->method_ = "relay_scan";
}

ServicesRelayScanSouthbound::ServicesRelayScanSouthbound(const ServicesRelayScanSouthboundMsg& msg) : ServicesRelayScanSouthbound() {
    this->msg = msg;
}

std::string ServicesRelayScanSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesRelayScanSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesRelayScanSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesRelayScanSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}