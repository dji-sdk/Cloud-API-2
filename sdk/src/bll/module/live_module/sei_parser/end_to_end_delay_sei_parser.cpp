// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "end_to_end_delay_sei_parser.h"
#include "base/common/logger.h"

namespace flight_hub_sdk {

EndToEndDelaySeiParser::EndToEndDelaySeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer)
    : sei_buffer_(sei_buffer) {}

EndToEndDelaySeiParser::~EndToEndDelaySeiParser() {}

bool EndToEndDelaySeiParser::parse() {
    if (!sei_buffer_) {
        LOG_ERROR("sei_buffer_ is null");
        return false;
    }

    if (sei_buffer_->size() < 7) {
        LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
        return false;
    }

    uint32_t offset = 0;

    version_ = (*reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset) >> 4) & 0x0F;
    live_type_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset) & 0x0F;
    offset += 1;

    timestamp_ = *reinterpret_cast<uint32_t*>(sei_buffer_->data() + offset);
    offset += 4;
    
    delay_ = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);

    // LOG_DEBUG("parse end to end delay sei success, version_: %d, live_type_: %d, timestamp_: %d, delay_: %d",
    //     version_, live_type_, timestamp_, delay_);

    return true;
}

} // namespace flight_hub_sdk