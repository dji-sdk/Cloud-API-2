// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_sdr_link_set_config_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesSdrLinkSetConfigSouthbound::ServicesSdrLinkSetConfigSouthbound() {
    this->topic_ = "thing/product/uav/services/sdr_link_set_config";
    this->method_ = "sdr_link_set_config";
}

ServicesSdrLinkSetConfigSouthbound::ServicesSdrLinkSetConfigSouthbound(const ServicesSdrLinkSetConfigSouthboundMsg& msg) : ServicesSdrLinkSetConfigSouthbound() {
    this->msg = msg;
}

std::string ServicesSdrLinkSetConfigSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesSdrLinkSetConfigSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesSdrLinkSetConfigSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesSdrLinkSetConfigSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}