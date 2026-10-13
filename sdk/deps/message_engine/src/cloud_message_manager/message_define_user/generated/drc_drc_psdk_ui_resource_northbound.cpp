// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_psdk_ui_resource_northbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcPsdkUiResourceNorthbound::DrcDrcPsdkUiResourceNorthbound() {
    this->method_ = "drc_psdk_ui_resource";
}

DrcDrcPsdkUiResourceNorthbound::DrcDrcPsdkUiResourceNorthbound(const std::shared_ptr<MessageV2>& message) : DrcDrcPsdkUiResourceNorthbound() {
    this->seq_ = message->GetSeq();
    this->peer_id_ = message->GetPeerId();
    std::string err;
    aigc::JsonHelper::JsonToObject(msg, message->GetData(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcPsdkUiResourceNorthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
    }
}

std::string DrcDrcPsdkUiResourceNorthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcPsdkUiResourceNorthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcPsdkUiResourceNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcPsdkUiResourceNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}