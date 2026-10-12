// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_ai_box_app_file_export_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesAiBoxAppFileExportSouthbound::ServicesAiBoxAppFileExportSouthbound() {
    this->topic_ = "thing/product/gnd/services/ai_box_app_file_export";
    this->method_ = "ai_box_app_file_export";
}

ServicesAiBoxAppFileExportSouthbound::ServicesAiBoxAppFileExportSouthbound(const ServicesAiBoxAppFileExportSouthboundMsg& msg) : ServicesAiBoxAppFileExportSouthbound() {
    this->msg = msg;
}

std::string ServicesAiBoxAppFileExportSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesAiBoxAppFileExportSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesAiBoxAppFileExportSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesAiBoxAppFileExportSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}