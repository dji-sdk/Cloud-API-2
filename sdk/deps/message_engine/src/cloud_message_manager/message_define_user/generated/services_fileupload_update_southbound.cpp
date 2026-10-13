// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_fileupload_update_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesFileuploadUpdateSouthbound::ServicesFileuploadUpdateSouthbound() {
    this->topic_ = "thing/product/gnd/services/fileupload_update";
    this->method_ = "fileupload_update";
}

ServicesFileuploadUpdateSouthbound::ServicesFileuploadUpdateSouthbound(const ServicesFileuploadUpdateSouthboundMsg& msg) : ServicesFileuploadUpdateSouthbound() {
    this->msg = msg;
}

std::string ServicesFileuploadUpdateSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesFileuploadUpdateSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesFileuploadUpdateSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesFileuploadUpdateSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}