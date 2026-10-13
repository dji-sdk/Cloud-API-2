// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/io/byte_buffer.h"
#include "api/message.h"

#define AR_SEI_ANGLE_SCALE 10000000

namespace flight_hub_sdk {

class ArSeiParser {
public:
    ArSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer);
    ~ArSeiParser();

    bool parse();

    ArSeiInfo get_ar_sei_info() const;

private:
    std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer_;
    uint8_t version_;
    uint8_t incremental_;
    uint8_t reserved_;
    uint8_t incremental_seq_;
    uint16_t delta_timestamp_;
    int32_t camera_euler_angle_[3] = {0, 0, 0};
    uint16_t camera_pos_low_[3] = {0, 0, 0};
    uint16_t camera_pos_high_[3] = {0, 0, 0};
    int32_t gnd_origin_lla_[3] = {0, 0, 0};

};

}