// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_clear_sdr_topo_info_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesClearSdrTopoInfoSouthbound::ServicesClearSdrTopoInfoSouthbound() {
    this->topic_ = "thing/product/gnd/services/clear_sdr_topo_info";
    this->method_ = "clear_sdr_topo_info";
}

ServicesClearSdrTopoInfoSouthbound::ServicesClearSdrTopoInfoSouthbound(const ServicesClearSdrTopoInfoSouthboundMsg& msg) : ServicesClearSdrTopoInfoSouthbound() {
    this->msg = msg;
}

std::string ServicesClearSdrTopoInfoSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesClearSdrTopoInfoSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesClearSdrTopoInfoSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesClearSdrTopoInfoSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}