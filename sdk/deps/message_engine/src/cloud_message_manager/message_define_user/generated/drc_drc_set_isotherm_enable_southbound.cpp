// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_set_isotherm_enable_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcSetIsothermEnableSouthbound::DrcDrcSetIsothermEnableSouthbound() {
    this->method_ = "drc_set_isotherm_enable";
}

DrcDrcSetIsothermEnableSouthbound::DrcDrcSetIsothermEnableSouthbound(const DrcDrcSetIsothermEnableSouthboundMsg& msg) : DrcDrcSetIsothermEnableSouthbound() {
    this->msg = msg;
}

std::string DrcDrcSetIsothermEnableSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcSetIsothermEnableSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcSetIsothermEnableSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcSetIsothermEnableSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}