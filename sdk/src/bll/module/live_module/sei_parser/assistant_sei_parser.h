// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/io/byte_buffer.h"
#include "api/message.h"

namespace flight_hub_sdk {

class AssistantSeiParser {
public:
    AssistantSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer);
    ~AssistantSeiParser();

    bool parse();

    AssistantSeiInfo get_assistant_sei_info() const;

private:
    std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer_;
    uint8_t version_;
    uint8_t is_auto_direction_;
    uint8_t stream_count_;
    uint8_t direction_;
    uint32_t time_stamp_ms_;
    uint32_t frame_id_;
    uint16_t width_;
    uint16_t height_;
    uint16_t x_;
    uint16_t y_;
};

}