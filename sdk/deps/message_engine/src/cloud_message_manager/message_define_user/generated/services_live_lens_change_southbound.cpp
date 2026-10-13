// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_live_lens_change_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesLiveLensChangeSouthbound::ServicesLiveLensChangeSouthbound() {
    this->topic_ = "thing/product/uav/services/live_lens_change";
    this->method_ = "live_lens_change";
}

ServicesLiveLensChangeSouthbound::ServicesLiveLensChangeSouthbound(const ServicesLiveLensChangeSouthboundMsg& msg) : ServicesLiveLensChangeSouthbound() {
    this->msg = msg;
}

std::string ServicesLiveLensChangeSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesLiveLensChangeSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesLiveLensChangeSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesLiveLensChangeSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}