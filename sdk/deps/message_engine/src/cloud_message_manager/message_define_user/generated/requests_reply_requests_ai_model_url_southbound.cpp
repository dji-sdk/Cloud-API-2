// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "requests_reply_requests_ai_model_url_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

RequestsReplyRequestsAiModelUrlSouthbound::RequestsReplyRequestsAiModelUrlSouthbound() {
    this->topic_ = "thing/product/gnd/requests_reply/requests_ai_model_url";
    this->method_ = "requests_ai_model_url";
}

RequestsReplyRequestsAiModelUrlSouthbound::RequestsReplyRequestsAiModelUrlSouthbound(const RequestsReplyRequestsAiModelUrlSouthboundMsg& msg) : RequestsReplyRequestsAiModelUrlSouthbound() {
    this->msg = msg;
}

std::string RequestsReplyRequestsAiModelUrlSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsReplyRequestsAiModelUrlSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer RequestsReplyRequestsAiModelUrlSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("RequestsReplyRequestsAiModelUrlSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}