// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "events_candidate_aircrafts_calculate_results_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

EventsCandidateAircraftsCalculateResultsNorthbound::EventsCandidateAircraftsCalculateResultsNorthbound() {
    this->topic_ = "thing/product/uav/events/candidate_aircrafts_calculate_results";
    this->method_ = "candidate_aircrafts_calculate_results";
}

EventsCandidateAircraftsCalculateResultsNorthbound::EventsCandidateAircraftsCalculateResultsNorthbound(const std::shared_ptr<MessageV1>& message) : EventsCandidateAircraftsCalculateResultsNorthbound() {
    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsCandidateAircraftsCalculateResultsNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string EventsCandidateAircraftsCalculateResultsNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("EventsCandidateAircraftsCalculateResultsNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer EventsCandidateAircraftsCalculateResultsNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("EventsCandidateAircraftsCalculateResultsNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}