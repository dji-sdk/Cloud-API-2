// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_upload_flighttask_media_prioritize_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesUploadFlighttaskMediaPrioritizeSouthbound::ServicesUploadFlighttaskMediaPrioritizeSouthbound() {
    this->topic_ = "thing/product/gnd/services/upload_flighttask_media_prioritize";
    this->method_ = "upload_flighttask_media_prioritize";
}

ServicesUploadFlighttaskMediaPrioritizeSouthbound::ServicesUploadFlighttaskMediaPrioritizeSouthbound(const ServicesUploadFlighttaskMediaPrioritizeSouthboundMsg& msg) : ServicesUploadFlighttaskMediaPrioritizeSouthbound() {
    this->msg = msg;
}

std::string ServicesUploadFlighttaskMediaPrioritizeSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesUploadFlighttaskMediaPrioritizeSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesUploadFlighttaskMediaPrioritizeSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesUploadFlighttaskMediaPrioritizeSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}