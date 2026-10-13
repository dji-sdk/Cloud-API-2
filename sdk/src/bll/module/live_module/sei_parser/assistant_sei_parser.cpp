// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "assistant_sei_parser.h"
#include "base/common/logger.h"

namespace flight_hub_sdk {

AssistantSeiParser::AssistantSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer)
    : sei_buffer_(sei_buffer) {}

AssistantSeiParser::~AssistantSeiParser() {}

bool AssistantSeiParser::parse() {
    if (!sei_buffer_) {
        LOG_ERROR("sei_buffer_ is null");
        return false;
    }

    if (sei_buffer_->size() < 20) {
        LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
        return false;
    }

    uint32_t offset = 0;

    version_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
    offset += 1;

    is_auto_direction_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset) & 0x1F;
    offset += 1;

    stream_count_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
    offset += 1;

    direction_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
    offset += 1;

    time_stamp_ms_ = *reinterpret_cast<uint32_t*>(sei_buffer_->data() + offset);
    offset += 4;

    frame_id_ = *reinterpret_cast<uint32_t*>(sei_buffer_->data() + offset);
    offset += 4;

    width_ = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;

    height_ = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;

    x_ = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;

    y_ = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);

    // LOG_DEBUG("parse assistant sei success, version_: %d, enable_: %d, is_auto_direction_: %d, stream_count_: %d, direction_: %d, time_stamp_ms_: %d, frame_id_: %d, width_: %d, height_: %d, x_: %d, y_: %d",
    //     version_, enable_, is_auto_direction_, stream_count_, direction_, time_stamp_ms_, frame_id_, width_, height_, x_, y_);

    return true;
}

AssistantSeiInfo AssistantSeiParser::get_assistant_sei_info() const {
    AssistantSeiInfo assistant_sei_info;

    assistant_sei_info.is_auto_direction = is_auto_direction_;
    assistant_sei_info.direction = direction_;

    return assistant_sei_info;
}

}