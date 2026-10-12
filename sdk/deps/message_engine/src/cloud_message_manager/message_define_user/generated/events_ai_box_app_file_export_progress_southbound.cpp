// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_ai_box_app_file_export_progress_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsAiBoxAppFileExportProgressSouthbound::EventsAiBoxAppFileExportProgressSouthbound() {
    this->topic_ = "thing/product/gnd/events/ai_box_app_file_export_progress";
    this->method_ = "ai_box_app_file_export_progress";
}

EventsAiBoxAppFileExportProgressSouthbound::EventsAiBoxAppFileExportProgressSouthbound(const EventsAiBoxAppFileExportProgressSouthboundMsg& msg) : EventsAiBoxAppFileExportProgressSouthbound() {
    this->msg = msg;
}

std::string EventsAiBoxAppFileExportProgressSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsAiBoxAppFileExportProgressSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsAiBoxAppFileExportProgressSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsAiBoxAppFileExportProgressSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}