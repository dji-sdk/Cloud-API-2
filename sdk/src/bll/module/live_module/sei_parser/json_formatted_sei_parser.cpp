// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "json_formatted_sei_parser.h"
#include "base/common/logger.h"
#include "ar_sei_parser.h"

namespace flight_hub_sdk {

JsonFormattedSeiParser::JsonFormattedSeiParser(const std::string& sei_buffer)
    : sei_buffer_(sei_buffer) {}

JsonFormattedSeiParser::~JsonFormattedSeiParser() {}

bool JsonFormattedSeiParser::parse() {
    std::string err;
    aigc::JsonHelper::JsonToObject(json_formatted_sei_info_, sei_buffer_.c_str(), {}, &err);
    if (!err.empty()) {
        LOG_ERROR("JsonFormattedSeiParser: JsonToObject error: %s", err.c_str());
        return false;
    }
    // std::string body;
    // aigc::JsonHelper::ObjectToJson(json_formatted_sei_info_, body, &err);
    // if (!err.empty()) {
    //     LOG_ERROR("JsonFormattedSeiParser: ObjectToJson error: %s", err.c_str());
    //     return false;
    // }
    // LOG_DEBUG("JsonFormattedSeiParser: parse success %s", body.c_str());
    return true;
}

bool JsonFormattedSeiParser::is_fov_sei_info_valid() const {
    if (json_formatted_sei_info_.cam_fov.image_width == 0 &&
        json_formatted_sei_info_.cam_fov.image_height == 0 && 
        json_formatted_sei_info_.cam_fov.lens_focal_length == 0) {
        return false;
    }
    return true;
}

FovSeiInfo JsonFormattedSeiParser::get_camera_fov_sei_info() const {
    FovSeiInfo fov_sei_info;
    fov_sei_info.fov_horizontal = json_formatted_sei_info_.cam_fov.image_width;
    fov_sei_info.fov_vertical = json_formatted_sei_info_.cam_fov.image_height;
    fov_sei_info.cur_actual_focal_length = json_formatted_sei_info_.cam_fov.lens_focal_length;
    return fov_sei_info;
}

bool JsonFormattedSeiParser::is_ar_sei_info_valid() const {
    if (json_formatted_sei_info_.camera_pos_high.size() > 0 && json_formatted_sei_info_.camera_pos_low.size() > 0) {
        return true;
    }
    return false;
}

ArSeiInfo JsonFormattedSeiParser::get_ar_sei_info() const {
    ArSeiInfo ar_sei_info;
    if (json_formatted_sei_info_.camera_pos_high.size() == 3 && json_formatted_sei_info_.camera_pos_low.size() == 3) {
        ar_sei_info.camera_pos_x = static_cast<int32_t>((static_cast<uint32_t>(json_formatted_sei_info_.camera_pos_high.at(0)) << 16) | static_cast<uint32_t>(json_formatted_sei_info_.camera_pos_low.at(0)));
        ar_sei_info.camera_pos_y = static_cast<int32_t>((static_cast<uint32_t>(json_formatted_sei_info_.camera_pos_high.at(1)) << 16) | static_cast<uint32_t>(json_formatted_sei_info_.camera_pos_low.at(1)));
        ar_sei_info.camera_pos_z = static_cast<int32_t>((static_cast<uint32_t>(json_formatted_sei_info_.camera_pos_high.at(2)) << 16) | static_cast<uint32_t>(json_formatted_sei_info_.camera_pos_low.at(2)));
    } else {
        LOG_ERROR("JsonFormattedSeiParser: get_ar_sei_info camera_pos_high or camera_pos_low size is not 3 %s", sei_buffer_.c_str());
        return ar_sei_info;
    }
    ar_sei_info.camera_yaw = static_cast<double>(json_formatted_sei_info_.cam_yaw) / AR_SEI_ANGLE_SCALE;
    ar_sei_info.camera_pitch = static_cast<double>(json_formatted_sei_info_.cam_pitch) / AR_SEI_ANGLE_SCALE;
    ar_sei_info.camera_roll = static_cast<double>(json_formatted_sei_info_.cam_roll) / AR_SEI_ANGLE_SCALE;
    ar_sei_info.latitude = json_formatted_sei_info_.gnd_lat;
    ar_sei_info.longitude = json_formatted_sei_info_.gnd_lng;
    ar_sei_info.altitude = json_formatted_sei_info_.gnd_alt;
    return ar_sei_info;
}
}