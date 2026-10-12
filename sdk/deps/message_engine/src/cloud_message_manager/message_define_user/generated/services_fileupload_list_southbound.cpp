// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_fileupload_list_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesFileuploadListSouthbound::ServicesFileuploadListSouthbound() {
    this->topic_ = "thing/product/gnd/services/fileupload_list";
    this->method_ = "fileupload_list";
}

ServicesFileuploadListSouthbound::ServicesFileuploadListSouthbound(const ServicesFileuploadListSouthboundMsg& msg) : ServicesFileuploadListSouthbound() {
    this->msg = msg;
}

std::string ServicesFileuploadListSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesFileuploadListSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesFileuploadListSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesFileuploadListSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}