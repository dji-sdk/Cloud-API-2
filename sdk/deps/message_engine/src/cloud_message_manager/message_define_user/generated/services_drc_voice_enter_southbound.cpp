// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_drc_voice_enter_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesDrcVoiceEnterSouthbound::ServicesDrcVoiceEnterSouthbound() {
    this->topic_ = "thing/product/uav/services/drc_voice_enter";
    this->method_ = "drc_voice_enter";
}

ServicesDrcVoiceEnterSouthbound::ServicesDrcVoiceEnterSouthbound(const ServicesDrcVoiceEnterSouthboundMsg& msg) : ServicesDrcVoiceEnterSouthbound() {
    this->msg = msg;
}

std::string ServicesDrcVoiceEnterSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesDrcVoiceEnterSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesDrcVoiceEnterSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesDrcVoiceEnterSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}