// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_ai_identify_score_mode_set_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcAiIdentifyScoreModeSetSouthbound::DrcDrcAiIdentifyScoreModeSetSouthbound() {
    this->method_ = "drc_ai_identify_score_mode_set";
}

DrcDrcAiIdentifyScoreModeSetSouthbound::DrcDrcAiIdentifyScoreModeSetSouthbound(const DrcDrcAiIdentifyScoreModeSetSouthboundMsg& msg) : DrcDrcAiIdentifyScoreModeSetSouthbound() {
    this->msg = msg;
}

std::string DrcDrcAiIdentifyScoreModeSetSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcAiIdentifyScoreModeSetSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcAiIdentifyScoreModeSetSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcAiIdentifyScoreModeSetSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}