// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_custom_data_transmission_to_psdk_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesCustomDataTransmissionToPsdkSouthbound::ServicesCustomDataTransmissionToPsdkSouthbound() {
    this->topic_ = "thing/product/{gateway_sn}/services/custom_data_transmission_to_psdk";
    this->method_ = "custom_data_transmission_to_psdk";
}

ServicesCustomDataTransmissionToPsdkSouthbound::ServicesCustomDataTransmissionToPsdkSouthbound(const ServicesCustomDataTransmissionToPsdkSouthboundMsg& msg) : ServicesCustomDataTransmissionToPsdkSouthbound() {
    this->msg = msg;
}

std::string ServicesCustomDataTransmissionToPsdkSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesCustomDataTransmissionToPsdkSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesCustomDataTransmissionToPsdkSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesCustomDataTransmissionToPsdkSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}
