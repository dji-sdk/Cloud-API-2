// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_rtc_quality_test_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcRtcQualityTestSouthbound::DrcDrcRtcQualityTestSouthbound() {
    this->method_ = "drc_rtc_quality_test";
}

DrcDrcRtcQualityTestSouthbound::DrcDrcRtcQualityTestSouthbound(const DrcDrcRtcQualityTestSouthboundMsg& msg) : DrcDrcRtcQualityTestSouthbound() {
    this->msg = msg;
}

std::string DrcDrcRtcQualityTestSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcRtcQualityTestSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcRtcQualityTestSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcRtcQualityTestSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}