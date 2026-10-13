// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_algorithm_dehaze_level_set_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcAlgorithmDehazeLevelSetSouthbound::DrcDrcAlgorithmDehazeLevelSetSouthbound() {
    this->method_ = "drc_algorithm_dehaze_level_set";
}

DrcDrcAlgorithmDehazeLevelSetSouthbound::DrcDrcAlgorithmDehazeLevelSetSouthbound(const DrcDrcAlgorithmDehazeLevelSetSouthboundMsg& msg) : DrcDrcAlgorithmDehazeLevelSetSouthbound() {
    this->msg = msg;
}

std::string DrcDrcAlgorithmDehazeLevelSetSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcAlgorithmDehazeLevelSetSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcAlgorithmDehazeLevelSetSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcAlgorithmDehazeLevelSetSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}