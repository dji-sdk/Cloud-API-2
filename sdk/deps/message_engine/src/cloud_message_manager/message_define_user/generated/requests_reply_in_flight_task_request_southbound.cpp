// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "requests_reply_in_flight_task_request_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

RequestsReplyInFlightTaskRequestSouthbound::RequestsReplyInFlightTaskRequestSouthbound() {
    this->topic_ = "thing/product/uav/requests_reply/in_flight_task_request";
    this->method_ = "in_flight_task_request";
}

RequestsReplyInFlightTaskRequestSouthbound::RequestsReplyInFlightTaskRequestSouthbound(const RequestsReplyInFlightTaskRequestSouthboundMsg& msg) : RequestsReplyInFlightTaskRequestSouthbound() {
    this->msg = msg;
}

std::string RequestsReplyInFlightTaskRequestSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsReplyInFlightTaskRequestSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer RequestsReplyInFlightTaskRequestSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("RequestsReplyInFlightTaskRequestSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}