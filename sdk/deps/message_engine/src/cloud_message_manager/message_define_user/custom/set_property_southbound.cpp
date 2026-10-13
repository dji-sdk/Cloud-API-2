// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "set_property_southbound.h"

#include <base/common/logger.h>
#include <json11.hpp>

using namespace message_engine;

PropertyType SetPropertySouthboundMsg::GetValidProperty() const {
    if (rth_altitude.has_value()) {
        return PropertyType::RTH_ALTITUDE;
    }
    if (commander_flight_height.has_value()) {
        return PropertyType::COMMANDER_FLIGHT_HEIGHT;
    }
    if (rc_lost_action.has_value()) {
        return PropertyType::RC_LOST_ACTION;
    }
    if (commander_mode_lost_action.has_value()) {
        return PropertyType::COMMANDER_MODE_LOST_ACTION;
    }
    if (commander_flight_mode.has_value()) {
        return PropertyType::COMMANDER_FLIGHT_MODE;
    }
    if (camera_watermark_settings.has_value()) {
        return PropertyType::CAMERA_WATERMARK_SETTING;
    }
    if (rth_mode.has_value()) {
        return PropertyType::RTH_MODE;
    }
    return PropertyType::UNKNOWN;
}

SetPropertySouthbound::SetPropertySouthbound() {
    this->topic_ = "thing/product/" + std::string(TopicUserType) + "/property/set";
    this->method_ = "property";
}

SetPropertySouthbound::SetPropertySouthbound(const std::shared_ptr<MessageV1>& message) : SetPropertySouthbound() {
    LOG_INFO("[PropertySet]SetPropertySouthbound json, json: %s", message->GetData().c_str());

    this->tid_ = message->GetTid();
    this->bid_ = message->GetBid();
    std::string err;
    auto json = json11::Json::parse(message->GetData(), err);
    if (!err.empty()) {
        LOG_ERROR("[PropertySet]SetPropertySouthbound json parse error: %s, json: %s", err.c_str(), message->GetData().c_str());
        return;
    }
    if (!json["rth_altitude"].is_null() && json["rth_altitude"].is_number()) {
        auto value = json["rth_altitude"].int_value();
        msg.rth_altitude = value;
        LOG_INFO("[PropertySet]SetPropertySouthbound rth_altitude: %d", msg.rth_altitude.value());
        return;
    }
    if (!json["commander_flight_height"].is_null() && json["commander_flight_height"].is_number()) {
        auto value = json["commander_flight_height"].number_value();
        msg.commander_flight_height = value;
        LOG_INFO("[PropertySet]SetPropertySouthbound commander_flight_height: %.1f", msg.commander_flight_height.value());
        return;
    }
    if (!json["rc_lost_action"].is_null() && json["rc_lost_action"].is_number()) {
        auto value = json["rc_lost_action"].int_value();
        msg.rc_lost_action = value;
        LOG_INFO("[PropertySet]SetPropertySouthbound rc_lost_action: %d", msg.rc_lost_action.value());
        return;
    }
    if (!json["commander_mode_lost_action"].is_null() && json["commander_mode_lost_action"].is_number()) {
        auto value = json["commander_mode_lost_action"].int_value();
        msg.commander_mode_lost_action = value;
        LOG_INFO("[PropertySet]SetPropertySouthbound commander_mode_lost_action: %d", msg.commander_mode_lost_action.value());
        return;
    }
    if (!json["commander_flight_mode"].is_null() && json["commander_flight_mode"].is_number()) {
        auto value = json["commander_flight_mode"].int_value();
        msg.commander_flight_mode = value;
        LOG_INFO("[PropertySet]SetPropertySouthbound commander_flight_mode: %d", msg.commander_flight_mode.value());
        return;
    }
    if (!json["rth_mode"].is_null() && json["rth_mode"].is_number()) {
        auto value = json["rth_mode"].int_value();
        msg.rth_mode = value;
        LOG_INFO("[PropertySet]SetPropertySouthbound rth_mode: %d", msg.rth_mode.value());
        return;
    }
    if (!json["camera_watermark_settings"].is_null() && json["camera_watermark_settings"].is_object()) {
        const auto settings = json["camera_watermark_settings"];
        auto settings_obj = SetPropertyCameraWatermarkMsg();
        std::string error;
        aigc::JsonHelper::JsonToObject(settings_obj, settings.dump(), {}, &error);
        msg.camera_watermark_settings = settings_obj;
        if (!error.empty()) {
            LOG_ERROR("DrcDownDrcPhotoStorageSetSouthbound ctor error, err: %s, json: %s", err.c_str(), message->GetData().c_str());
        }
        LOG_INFO("[PropertySet]SetPropertySouthbound camera_watermark_settings received");
        return;
    }
}

std::string SetPropertySouthbound::ToJsonString() {
    json11::Json::object json_obj;
    auto property = msg.GetValidProperty();
    switch (property) {
        case PropertyType::RTH_ALTITUDE: {
            json_obj["rth_altitude"] = msg.rth_altitude.value();
            break;
        }
        case PropertyType::COMMANDER_FLIGHT_HEIGHT: {
            json_obj["commander_flight_height"] = msg.commander_flight_height.value();
            break;
        }
        case PropertyType::RC_LOST_ACTION: {
            json_obj["rc_lost_action"] = msg.rc_lost_action.value();
            break;
        }
        case PropertyType::COMMANDER_MODE_LOST_ACTION: {
            json_obj["commander_mode_lost_action"] = msg.commander_mode_lost_action.value();
            break;
        }
        case PropertyType::COMMANDER_FLIGHT_MODE: {
            json_obj["commander_flight_mode"] = msg.commander_flight_mode.value();
            break;
        }
        case PropertyType::RTH_MODE: {
            json_obj["rth_mode"] = msg.rth_mode.value();
            break;
        }
        case PropertyType::CAMERA_WATERMARK_SETTING: {
            if (!msg.camera_watermark_settings.has_value()) {
                break;
            }
            std::string err;
            std::string json_string;
            aigc::JsonHelper::ObjectToJson(msg.camera_watermark_settings.value(), json_string, &err);
            if (!err.empty()) {
                LOG_ERROR("DrcUpDrcLiveVideoInfoPushNorthbound ToJsonString error, err: %s", err.c_str());
            }
            json_obj["camera_watermark_settings"] = json_string;
            break;
        }
        default:
            break;
    }
    json11::Json json = json_obj;
    std::string json_string = json.dump();
    return json_string;
}

drc_sdk::ByteBuffer SetPropertySouthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("SetPropertySouthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}