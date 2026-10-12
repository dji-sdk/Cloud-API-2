// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "ar_sei_parser.h"
#include "base/common/logger.h"

namespace flight_hub_sdk {

ArSeiParser::ArSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer)
    : sei_buffer_(sei_buffer) {}

ArSeiParser::~ArSeiParser() {}

bool ArSeiParser::parse() {
    if (!sei_buffer_) {
        LOG_ERROR("sei_buffer_ is null");
        return false;
    }

    if (sei_buffer_->size() < 20) {
        LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
        return false;
    }

    uint32_t offset = 0;

    incremental_seq_ = *sei_buffer_->data() & 0x03;
    reserved_ = (*sei_buffer_->data() >> 2) & 0x03;
    incremental_ = (*sei_buffer_->data() >> 4) & 0x01;
    version_ = (*sei_buffer_->data() >> 5) & 0x07;
    offset += 1;

    delta_timestamp_ = *reinterpret_cast<int16_t*>(sei_buffer_->data() + offset);
    offset += 2;

    camera_euler_angle_[0] = *reinterpret_cast<int32_t*>(sei_buffer_->data() + offset);
    offset += 4;
    camera_euler_angle_[1] = *reinterpret_cast<int32_t*>(sei_buffer_->data() + offset);
    offset += 4;
    camera_euler_angle_[2] = *reinterpret_cast<int32_t*>(sei_buffer_->data() + offset);
    offset += 4;

    camera_pos_low_[0] = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;
    camera_pos_low_[1] = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;
    camera_pos_low_[2] = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;

    if (incremental_ == 0) {
        camera_pos_high_[0] = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
        offset += 2;
        camera_pos_high_[1] = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
        offset += 2;
        camera_pos_high_[2] = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
        offset += 2;
    
        gnd_origin_lla_[0] = *reinterpret_cast<int32_t*>(sei_buffer_->data() + offset);
        offset += 4;
        gnd_origin_lla_[1] = *reinterpret_cast<int32_t*>(sei_buffer_->data() + offset);
        offset += 4;
        gnd_origin_lla_[2] = *reinterpret_cast<int32_t*>(sei_buffer_->data() + offset);
    }

    // LOG_DEBUG("parse ar sei success, incremental_seq_: %d, reserved_: %d, incremental_: %d, version_: %d, delta_timestamp_: %d, camera_euler_angle_: %d, %d, %d, camera_pos_low_: %d, %d, %d, camera_pos_high_: %d, %d, %d, gnd_origin_lla_: %d, %d, %d",
    //     incremental_seq_, reserved_, incremental_, version_, delta_timestamp_,
    //     camera_euler_angle_[0], camera_euler_angle_[1], camera_euler_angle_[2],
    //     camera_pos_low_[0], camera_pos_low_[1], camera_pos_low_[2],
    //     camera_pos_high_[0], camera_pos_high_[1], camera_pos_high_[2],
    //     gnd_origin_lla_[0], gnd_origin_lla_[1], gnd_origin_lla_[2]);

    return true;
}

ArSeiInfo ArSeiParser::get_ar_sei_info() const {
    ArSeiInfo ar_sei_info;

    ar_sei_info.camera_yaw = static_cast<double>(camera_euler_angle_[0]) / AR_SEI_ANGLE_SCALE;
    ar_sei_info.camera_pitch = static_cast<double>(camera_euler_angle_[1]) / AR_SEI_ANGLE_SCALE;
    ar_sei_info.camera_roll = static_cast<double>(camera_euler_angle_[2]) / AR_SEI_ANGLE_SCALE;
    ar_sei_info.camera_pos_x = static_cast<int32_t>((static_cast<uint32_t>(camera_pos_high_[0]) << 16) | static_cast<uint32_t>(camera_pos_low_[0]));
    ar_sei_info.camera_pos_y = static_cast<int32_t>((static_cast<uint32_t>(camera_pos_high_[1]) << 16) | static_cast<uint32_t>(camera_pos_low_[1]));
    ar_sei_info.camera_pos_z = static_cast<int32_t>((static_cast<uint32_t>(camera_pos_high_[2]) << 16) | static_cast<uint32_t>(camera_pos_low_[2]));
    ar_sei_info.latitude = gnd_origin_lla_[0];
    ar_sei_info.longitude = gnd_origin_lla_[1];
    ar_sei_info.gnd_origin_z = gnd_origin_lla_[2];
    ar_sei_info.altitude = gnd_origin_lla_[2] - ar_sei_info.camera_pos_z;

    return ar_sei_info;
}

}