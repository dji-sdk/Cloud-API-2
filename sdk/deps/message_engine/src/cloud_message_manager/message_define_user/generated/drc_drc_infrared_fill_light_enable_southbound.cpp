// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_infrared_fill_light_enable_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcInfraredFillLightEnableSouthbound::DrcDrcInfraredFillLightEnableSouthbound() {
    this->method_ = "drc_infrared_fill_light_enable";
}

DrcDrcInfraredFillLightEnableSouthbound::DrcDrcInfraredFillLightEnableSouthbound(const DrcDrcInfraredFillLightEnableSouthboundMsg& msg) : DrcDrcInfraredFillLightEnableSouthbound() {
    this->msg = msg;
}

std::string DrcDrcInfraredFillLightEnableSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcInfraredFillLightEnableSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcInfraredFillLightEnableSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcInfraredFillLightEnableSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}