// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_fileupload_progress_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsFileuploadProgressSouthbound::EventsFileuploadProgressSouthbound() {
    this->topic_ = "thing/product/gnd/events/fileupload_progress";
    this->method_ = "fileupload_progress";
}

EventsFileuploadProgressSouthbound::EventsFileuploadProgressSouthbound(const EventsFileuploadProgressSouthboundMsg& msg) : EventsFileuploadProgressSouthbound() {
    this->msg = msg;
}

std::string EventsFileuploadProgressSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsFileuploadProgressSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsFileuploadProgressSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsFileuploadProgressSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}