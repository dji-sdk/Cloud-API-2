// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_landing_mission_state_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcLandingMissionStateNorthbound::DrcLandingMissionStateNorthbound() {
    this->method_ = "landing_mission_state";
}

DrcLandingMissionStateNorthbound::DrcLandingMissionStateNorthbound(const std::shared_ptr<MessageV2>& message) : DrcLandingMissionStateNorthbound() {
    this->seq_ = message->GetSeq();
    this->peer_id_ = message->GetPeerId();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcLandingMissionStateNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string DrcLandingMissionStateNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcLandingMissionStateNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcLandingMissionStateNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcLandingMissionStateNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}
