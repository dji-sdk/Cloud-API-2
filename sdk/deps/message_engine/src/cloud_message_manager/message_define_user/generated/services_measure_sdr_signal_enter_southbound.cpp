// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_measure_sdr_signal_enter_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesMeasureSdrSignalEnterSouthbound::ServicesMeasureSdrSignalEnterSouthbound() {
    this->topic_ = "thing/product/gnd/services/measure_sdr_signal_enter";
    this->method_ = "measure_sdr_signal_enter";
}

ServicesMeasureSdrSignalEnterSouthbound::ServicesMeasureSdrSignalEnterSouthbound(const ServicesMeasureSdrSignalEnterSouthboundMsg& msg) : ServicesMeasureSdrSignalEnterSouthbound() {
    this->msg = msg;
}

std::string ServicesMeasureSdrSignalEnterSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesMeasureSdrSignalEnterSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesMeasureSdrSignalEnterSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesMeasureSdrSignalEnterSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}