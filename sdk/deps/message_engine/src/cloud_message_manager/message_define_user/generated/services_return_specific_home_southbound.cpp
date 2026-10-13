// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_return_specific_home_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesReturnSpecificHomeSouthbound::ServicesReturnSpecificHomeSouthbound() {
    this->topic_ = "thing/product/gnd/services/return_specific_home";
    this->method_ = "return_specific_home";
}

ServicesReturnSpecificHomeSouthbound::ServicesReturnSpecificHomeSouthbound(const ServicesReturnSpecificHomeSouthboundMsg& msg) : ServicesReturnSpecificHomeSouthbound() {
    this->msg = msg;
}

std::string ServicesReturnSpecificHomeSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReturnSpecificHomeSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesReturnSpecificHomeSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesReturnSpecificHomeSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}