// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "set_reply_property_northbound.h"

#include <base/common/logger.h>
#include <json11.hpp>

using namespace message_engine;

SetReplyPropertyNorthbound::SetReplyPropertyNorthbound() {
    this->topic_ = "thing/product/" + std::string(TopicUserType) + "/property/set_reply";
    this->method_ = "property";
}

SetReplyPropertyNorthbound::SetReplyPropertyNorthbound(const SetReplyPropertyNorthboundMsg& msg) : SetReplyPropertyNorthbound() {
    this->msg = msg;
}

std::string SetReplyPropertyNorthbound::ToJsonString() {
    json11::Json::object json_obj;
    switch (msg.type) {
        case PropertyType::RTH_ALTITUDE: {
            json_obj["rth_altitude"] = json11::Json::object{{"result", msg.result}};
            break;
        }
        case PropertyType::COMMANDER_FLIGHT_HEIGHT: {
            json_obj["commander_flight_height"] = json11::Json::object{{"result", msg.result}};
            break;
        }
        case PropertyType::RC_LOST_ACTION: {
            json_obj["rc_lost_action"] = json11::Json::object{{"result", msg.result}};
            break;
        }
        case PropertyType::COMMANDER_MODE_LOST_ACTION: {
            json_obj["commander_mode_lost_action"] = json11::Json::object{{"result", msg.result}};
            break;
        }
        case PropertyType::COMMANDER_FLIGHT_MODE: {
            json_obj["commander_flight_mode"] = json11::Json::object{{"result", msg.result}};
            break;
        }
        case PropertyType::CAMERA_WATERMARK_SETTING: {
            json_obj["camera_watermark_settings"] = json11::Json::object{{"result", msg.result}};
            break;
        }
        case PropertyType::RTH_MODE: {
            json_obj["rth_mode"] = json11::Json::object{{"result", msg.result}};
            break;
        }
        default:
            break;
    }
    json11::Json json = json_obj;
    std::string json_string = json.dump();
    return json_string;
}

drc_sdk::ByteBuffer SetReplyPropertyNorthbound::ToBinary() {
    const std::string json_str = ToJsonString();
    if (json_str.empty()) {
        LOG_ERROR("SetReplyPropertyNorthbound ToBinary error, json_str is empty");
        return {};
    }
    drc_sdk::ByteBuffer buffer;
    buffer.Resize(json_str.size());
    memcpy(buffer.data(), json_str.c_str(), json_str.size());
    return buffer;
}