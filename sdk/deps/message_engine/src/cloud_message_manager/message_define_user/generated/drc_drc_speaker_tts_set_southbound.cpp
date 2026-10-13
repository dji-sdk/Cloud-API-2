// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_speaker_tts_set_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcSpeakerTtsSetSouthbound::DrcDrcSpeakerTtsSetSouthbound() {
    this->method_ = "drc_speaker_tts_set";
}

DrcDrcSpeakerTtsSetSouthbound::DrcDrcSpeakerTtsSetSouthbound(const DrcDrcSpeakerTtsSetSouthboundMsg& msg) : DrcDrcSpeakerTtsSetSouthbound() {
    this->msg = msg;
}

std::string DrcDrcSpeakerTtsSetSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcSpeakerTtsSetSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcSpeakerTtsSetSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcSpeakerTtsSetSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}