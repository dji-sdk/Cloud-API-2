// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_load_ai_box_app_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesLoadAiBoxAppSouthbound::ServicesLoadAiBoxAppSouthbound() {
    this->topic_ = "thing/product/gnd/services/load_ai_box_app";
    this->method_ = "load_ai_box_app";
}

ServicesLoadAiBoxAppSouthbound::ServicesLoadAiBoxAppSouthbound(const ServicesLoadAiBoxAppSouthboundMsg& msg) : ServicesLoadAiBoxAppSouthbound() {
    this->msg = msg;
}

std::string ServicesLoadAiBoxAppSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesLoadAiBoxAppSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesLoadAiBoxAppSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesLoadAiBoxAppSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}