// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "drc_drc_linkage_zoom_set_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

DrcDrcLinkageZoomSetSouthbound::DrcDrcLinkageZoomSetSouthbound() {
    this->method_ = "drc_linkage_zoom_set";
}

DrcDrcLinkageZoomSetSouthbound::DrcDrcLinkageZoomSetSouthbound(const DrcDrcLinkageZoomSetSouthboundMsg& msg) : DrcDrcLinkageZoomSetSouthbound() {
    this->msg = msg;
}

std::string DrcDrcLinkageZoomSetSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("DrcDrcLinkageZoomSetSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer DrcDrcLinkageZoomSetSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("DrcDrcLinkageZoomSetSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}