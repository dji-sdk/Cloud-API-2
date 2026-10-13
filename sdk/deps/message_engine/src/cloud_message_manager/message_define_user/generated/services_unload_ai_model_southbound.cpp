// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_unload_ai_model_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesUnloadAiModelSouthbound::ServicesUnloadAiModelSouthbound() {
    this->topic_ = "thing/product/gnd/services/unload_ai_model";
    this->method_ = "unload_ai_model";
}

ServicesUnloadAiModelSouthbound::ServicesUnloadAiModelSouthbound(const ServicesUnloadAiModelSouthboundMsg& msg) : ServicesUnloadAiModelSouthbound() {
    this->msg = msg;
}

std::string ServicesUnloadAiModelSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesUnloadAiModelSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesUnloadAiModelSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesUnloadAiModelSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}