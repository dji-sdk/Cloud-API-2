// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_offline_map_sync_progress_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsOfflineMapSyncProgressSouthbound::EventsOfflineMapSyncProgressSouthbound() {
    this->topic_ = "thing/product/gnd/events/offline_map_sync_progress";
    this->method_ = "offline_map_sync_progress";
}

EventsOfflineMapSyncProgressSouthbound::EventsOfflineMapSyncProgressSouthbound(const EventsOfflineMapSyncProgressSouthboundMsg& msg) : EventsOfflineMapSyncProgressSouthbound() {
    this->msg = msg;
}

std::string EventsOfflineMapSyncProgressSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsOfflineMapSyncProgressSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsOfflineMapSyncProgressSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsOfflineMapSyncProgressSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}