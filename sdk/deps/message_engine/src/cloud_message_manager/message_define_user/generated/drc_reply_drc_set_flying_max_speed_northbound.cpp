// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_reply_drc_set_flying_max_speed_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcReplyDrcSetFlyingMaxSpeedNorthbound::DrcReplyDrcSetFlyingMaxSpeedNorthbound() {
    this->method_ = "drc_set_flying_max_speed";
}

DrcReplyDrcSetFlyingMaxSpeedNorthbound::DrcReplyDrcSetFlyingMaxSpeedNorthbound(const std::shared_ptr<MessageV2>& message) : DrcReplyDrcSetFlyingMaxSpeedNorthbound() {
    this->seq_ = message->GetSeq();
    this->peer_id_ = message->GetPeerId();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcReplyDrcSetFlyingMaxSpeedNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string DrcReplyDrcSetFlyingMaxSpeedNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcReplyDrcSetFlyingMaxSpeedNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcReplyDrcSetFlyingMaxSpeedNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcReplyDrcSetFlyingMaxSpeedNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}