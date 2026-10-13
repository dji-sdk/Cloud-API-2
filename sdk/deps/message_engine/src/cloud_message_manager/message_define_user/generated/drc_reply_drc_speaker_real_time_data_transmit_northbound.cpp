// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_reply_drc_speaker_real_time_data_transmit_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound::DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound() {
    this->method_ = "drc_speaker_real_time_data_transmit";
}

DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound::DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound(const std::shared_ptr<MessageV2>& message) : DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound() {
    this->seq_ = message->GetSeq();
    this->peer_id_ = message->GetPeerId();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcReplyDrcSpeakerRealTimeDataTransmitNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}