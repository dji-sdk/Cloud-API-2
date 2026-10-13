// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_return_home_cancel_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesReturnHomeCancelSouthbound::ServicesReturnHomeCancelSouthbound() {
    this->topic_ = "thing/product/uav/services/return_home_cancel";
    this->method_ = "return_home_cancel";
}

ServicesReturnHomeCancelSouthbound::ServicesReturnHomeCancelSouthbound(const ServicesReturnHomeCancelSouthboundMsg& msg) : ServicesReturnHomeCancelSouthbound() {
    this->msg = msg;
}

std::string ServicesReturnHomeCancelSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesReturnHomeCancelSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesReturnHomeCancelSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesReturnHomeCancelSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}