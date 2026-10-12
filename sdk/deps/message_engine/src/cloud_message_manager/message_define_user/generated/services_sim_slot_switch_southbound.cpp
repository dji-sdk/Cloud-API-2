// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_sim_slot_switch_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesSimSlotSwitchSouthbound::ServicesSimSlotSwitchSouthbound() {
    this->topic_ = "thing/product/uav/services/sim_slot_switch";
    this->method_ = "sim_slot_switch";
}

ServicesSimSlotSwitchSouthbound::ServicesSimSlotSwitchSouthbound(const ServicesSimSlotSwitchSouthboundMsg& msg) : ServicesSimSlotSwitchSouthbound() {
    this->msg = msg;
}

std::string ServicesSimSlotSwitchSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesSimSlotSwitchSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesSimSlotSwitchSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesSimSlotSwitchSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}