// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "requests_reply_request_landing_permission_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

RequestsReplyRequestLandingPermissionSouthbound::RequestsReplyRequestLandingPermissionSouthbound() {
    this->topic_ = "thing/product/gnd/requests_reply/request_landing_permission";
    this->method_ = "request_landing_permission";
}

RequestsReplyRequestLandingPermissionSouthbound::RequestsReplyRequestLandingPermissionSouthbound(const RequestsReplyRequestLandingPermissionSouthboundMsg& msg) : RequestsReplyRequestLandingPermissionSouthbound() {
    this->msg = msg;
}

std::string RequestsReplyRequestLandingPermissionSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("RequestsReplyRequestLandingPermissionSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer RequestsReplyRequestLandingPermissionSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("RequestsReplyRequestLandingPermissionSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}