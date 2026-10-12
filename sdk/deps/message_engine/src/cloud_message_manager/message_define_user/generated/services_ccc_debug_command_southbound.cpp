// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_ccc_debug_command_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesCccDebugCommandSouthbound::ServicesCccDebugCommandSouthbound() {
    this->topic_ = "thing/product/uav/services/ccc_debug_command";
    this->method_ = "ccc_debug_command";
}

ServicesCccDebugCommandSouthbound::ServicesCccDebugCommandSouthbound(const ServicesCccDebugCommandSouthboundMsg& msg) : ServicesCccDebugCommandSouthbound() {
    this->msg = msg;
}

std::string ServicesCccDebugCommandSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesCccDebugCommandSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesCccDebugCommandSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesCccDebugCommandSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}