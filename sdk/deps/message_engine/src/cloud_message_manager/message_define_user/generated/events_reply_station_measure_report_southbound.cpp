// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_reply_station_measure_report_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsReplyStationMeasureReportSouthbound::EventsReplyStationMeasureReportSouthbound() {
    this->topic_ = "thing/product/gnd/events_reply/station_measure_report";
    this->method_ = "station_measure_report";
}

EventsReplyStationMeasureReportSouthbound::EventsReplyStationMeasureReportSouthbound(const EventsReplyStationMeasureReportSouthboundMsg& msg) : EventsReplyStationMeasureReportSouthbound() {
    this->msg = msg;
}

std::string EventsReplyStationMeasureReportSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsReplyStationMeasureReportSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsReplyStationMeasureReportSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsReplyStationMeasureReportSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}