// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "state_northbound.h"

#include <base/common/logger.h>

#include <json11.hpp>

using namespace message_engine;

StateNorthbound::StateNorthbound() {
    this->topic_ = "thing/product/uav/state";
    this->method_ = "state";
}

StateNorthbound::StateNorthbound(const StateNorthboundMsg& msg) : StateNorthbound() {
    this->msg = msg;
}

std::string StateNorthbound::ToJsonString() {
    json11::Json::object json_obj;
    if (msg.capability_set.has_value()) {
        std::string err;
        std::string json_string;
        aigc::JsonHelper::ObjectToJson(msg.capability_set.value(), json_string, &err);
        if (!err.empty()) {
            LOG_ERROR("StateNorthbound ToJsonString error, err: %s", err.c_str());
        }
        json11::Json capability_set = json11::Json::parse(json_string, err);
        if (!err.empty()) {
            LOG_ERROR("StateNorthbound ToJsonObject error, err: %s", err.c_str());
        }
        json_obj["capability_set"] = capability_set;
    }
    if (msg.camera_watermark_settings.has_value()) {
        std::string err;
        std::string json_string;
        aigc::JsonHelper::ObjectToJson(msg.camera_watermark_settings.value(), json_string, &err);
        if (!err.empty()) {
            LOG_ERROR("StateNorthbound ToJsonString error, err: %s", err.c_str());
        }
        json11::Json camera_watermark_settings = json11::Json::parse(json_string, err);
        if (!err.empty()) {
            LOG_ERROR("StateNorthbound ToJsonObject error, err: %s", err.c_str());
        }
        json_obj["camera_watermark_settings"] = camera_watermark_settings;
    }
    if (msg.drone_connection_status.has_value()) {
        std::string err;
        std::string json_string;
        aigc::JsonHelper::ObjectToJson(msg.drone_connection_status.value(), json_string, &err);
        if (!err.empty()) {
            LOG_ERROR("StateNorthbound ToJsonString error, err: %s", err.c_str());
        }
        json11::Json drone_connection_status = json11::Json::parse(json_string, err);
        if (!err.empty()) {
            LOG_ERROR("StateNorthbound ToJsonObject error, err: %s", err.c_str());
        }
        json_obj["drone_connection_status"] = drone_connection_status;
    }
    json11::Json json = json_obj;
    std::string json_string = json.dump();
    return json_string;
}

drc_sdk::ByteBuffer StateNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("StateNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}