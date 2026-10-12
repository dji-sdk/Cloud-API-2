// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_offline_map_update_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesOfflineMapUpdateSouthbound::ServicesOfflineMapUpdateSouthbound() {
    this->topic_ = "thing/product/gnd/services/offline_map_update";
    this->method_ = "offline_map_update";
}

ServicesOfflineMapUpdateSouthbound::ServicesOfflineMapUpdateSouthbound(const ServicesOfflineMapUpdateSouthboundMsg& msg) : ServicesOfflineMapUpdateSouthbound() {
    this->msg = msg;
}

std::string ServicesOfflineMapUpdateSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesOfflineMapUpdateSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesOfflineMapUpdateSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesOfflineMapUpdateSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}