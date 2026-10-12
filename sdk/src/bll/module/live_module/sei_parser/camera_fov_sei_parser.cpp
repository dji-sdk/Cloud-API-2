// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "camera_fov_sei_parser.h"
#include "base/common/logger.h"

namespace flight_hub_sdk {

CameraFovSeiParser::CameraFovSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer)
    : sei_buffer_(sei_buffer) {}

CameraFovSeiParser::~CameraFovSeiParser() {}

bool CameraFovSeiParser::parse() {
    if (!sei_buffer_) {
        LOG_ERROR("sei_buffer_ is null");
        return false;
    }

    if (sei_buffer_->size() < 13) {
        LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
        return false;
    }

    uint32_t offset = 0;

    version_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
    offset += 1;

    fov_horizontal_ = *reinterpret_cast<uint32_t*>(sei_buffer_->data() + offset);
    offset += 4;
    fov_vertical_ = *reinterpret_cast<uint32_t*>(sei_buffer_->data() + offset);
    offset += 4;

    cur_actual_focal_length_ = *reinterpret_cast<uint32_t*>(sei_buffer_->data() + offset);
    offset += 4;

    // LOG_DEBUG("parse camera fov success, version_: %d, fov_horizontal_: %d, fov_vertical_: %d, cur_actual_focal_length_: %d",
    //     version_, fov_horizontal_, fov_vertical_, cur_actual_focal_length_);

    return true;
}

FovSeiInfo CameraFovSeiParser::get_camera_fov_sei_info() const {
    FovSeiInfo fov_sei_info;
    fov_sei_info.fov_horizontal = fov_horizontal_;
    fov_sei_info.fov_vertical = fov_vertical_;
    fov_sei_info.cur_actual_focal_length = cur_actual_focal_length_;

    return fov_sei_info;
}

}