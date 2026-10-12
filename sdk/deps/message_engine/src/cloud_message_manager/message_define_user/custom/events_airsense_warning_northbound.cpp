// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_airsense_warning_northbound.h"

#include <base/common/logger.h>

#include <json11.hpp>

using namespace message_engine;

EventsAirsenseWarningNorthbound::EventsAirsenseWarningNorthbound() {
    this->topic_ = "thing/product/" + std::string(TopicUserType) + "/events/airsense_warning";
    this->method_ = "airsense_warning";
}

EventsAirsenseWarningNorthbound::EventsAirsenseWarningNorthbound(const EventsAirsenseWarningNorthboundMsg& msg) : EventsAirsenseWarningNorthbound() {
    this->msg = msg;
}

std::string EventsAirsenseWarningNorthbound::ToJsonString() {
    json11::Json::array flight_info;
    for (const auto& iter : msg.data) {
        flight_info.emplace_back(json11::Json::object{{"icao", iter.icao},
                                                      {"warning_level", (int)iter.warning_level},
                                                      {"latitude", iter.latitude},
                                                      {"longitude", iter.longitude},
                                                      {"altitude", iter.altitude},
                                                      {"altitude_type", (int)iter.altitude_type},
                                                      {"heading", iter.heading},
                                                      {"relative_altitude", iter.relative_altitude},
                                                      {"vert_trend", (int)iter.vert_trend},
                                                      {"distance", iter.distance}});
    }
    auto json_string = json11::Json(flight_info).dump();
    return json_string;
}

drc_sdk::ByteBuffer EventsAirsenseWarningNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsAirsenseWarningNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}