// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "status_reply_update_topo_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

StatusReplyUpdateTopoSouthbound::StatusReplyUpdateTopoSouthbound() {
    this->topic_ = "sys/product/uav/status_reply";
    this->method_ = "update_topo";
}

StatusReplyUpdateTopoSouthbound::StatusReplyUpdateTopoSouthbound(const StatusReplyUpdateTopoSouthboundMsg& msg) : StatusReplyUpdateTopoSouthbound() {
    this->msg = msg;
}

std::string StatusReplyUpdateTopoSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("StatusReplyUpdateTopoSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer StatusReplyUpdateTopoSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("StatusReplyUpdateTopoSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}