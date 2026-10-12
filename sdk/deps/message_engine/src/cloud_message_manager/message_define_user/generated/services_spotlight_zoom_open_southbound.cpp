// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "services_spotlight_zoom_open_southbound.h"

#include <base/common/logger.h>

using namespace message_engine;

ServicesSpotlightZoomOpenSouthbound::ServicesSpotlightZoomOpenSouthbound() {
    this->topic_ = "thing/product/uav/services/spotlight_zoom_open";
    this->method_ = "spotlight_zoom_open";
}

ServicesSpotlightZoomOpenSouthbound::ServicesSpotlightZoomOpenSouthbound(const ServicesSpotlightZoomOpenSouthboundMsg& msg) : ServicesSpotlightZoomOpenSouthbound() {
    this->msg = msg;
}

std::string ServicesSpotlightZoomOpenSouthbound::ToJsonString() {
    std::string err;
    std::string json_string;
    aigc::JsonHelper::ObjectToJson(msg, json_string, &err);
    if (!err.empty()) {
        LOG_ERROR("ServicesSpotlightZoomOpenSouthbound ToJsonString error, err: %s", err.c_str());
    }
    return json_string;
}

drc_sdk::ByteBuffer ServicesSpotlightZoomOpenSouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("ServicesSpotlightZoomOpenSouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}