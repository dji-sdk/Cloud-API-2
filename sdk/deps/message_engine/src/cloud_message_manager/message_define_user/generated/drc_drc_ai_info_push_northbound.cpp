// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_ai_info_push_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcAiInfoPushNorthbound::DrcDrcAiInfoPushNorthbound() {
    this->method_ = "drc_ai_info_push";
}

DrcDrcAiInfoPushNorthbound::DrcDrcAiInfoPushNorthbound(const std::shared_ptr<MessageV2>& message) : DrcDrcAiInfoPushNorthbound() {
    this->seq_ = message->GetSeq();
    this->peer_id_ = message->GetPeerId();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcAiInfoPushNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string DrcDrcAiInfoPushNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcAiInfoPushNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcAiInfoPushNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcAiInfoPushNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}