// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_ai_box_app_operate_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesAiBoxAppOperateSouthbound::ServicesAiBoxAppOperateSouthbound() {
    this->topic_ = "thing/product/gnd/services/ai_box_app_operate";
    this->method_ = "ai_box_app_operate";
}

ServicesAiBoxAppOperateSouthbound::ServicesAiBoxAppOperateSouthbound(const ServicesAiBoxAppOperateSouthboundMsg& msg) : ServicesAiBoxAppOperateSouthbound() {
    this->msg = msg;
}

std::string ServicesAiBoxAppOperateSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesAiBoxAppOperateSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesAiBoxAppOperateSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesAiBoxAppOperateSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}