// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/io/byte_buffer.h"

namespace flight_hub_sdk {

class EndToEndDelaySeiParser {
public:
    EndToEndDelaySeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer);
    ~EndToEndDelaySeiParser();
    bool parse();
    // uint32_t get_end_to_end_delay() const;

private:
    std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer_;
    uint8_t version_;
    uint8_t live_type_;
    uint32_t timestamp_;
    uint16_t delay_;
};

}
