// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "requests_reply_flighttask_resource_get_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

RequestsReplyFlighttaskResourceGetSouthbound::RequestsReplyFlighttaskResourceGetSouthbound() {
    this->topic_ = "thing/product/uav/requests_reply/flighttask_resource_get";
    this->method_ = "flighttask_resource_get";
}

RequestsReplyFlighttaskResourceGetSouthbound::RequestsReplyFlighttaskResourceGetSouthbound(const RequestsReplyFlighttaskResourceGetSouthboundMsg& msg) : RequestsReplyFlighttaskResourceGetSouthbound() {
    this->msg = msg;
}

std::string RequestsReplyFlighttaskResourceGetSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsReplyFlighttaskResourceGetSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer RequestsReplyFlighttaskResourceGetSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("RequestsReplyFlighttaskResourceGetSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}