// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/io/byte_buffer.h"
#include "api/message.h"

namespace flight_hub_sdk {

class EisSeiParser {
public:
    EisSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer);
    ~EisSeiParser();

    bool parse();

    EisSeiInfo get_eis_sei_info() const;

private:
    std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer_;
    uint8_t version_;
    CamCropPos left_top_;
    CamCropPos right_top_;
    CamCropPos right_bottom_;
    CamCropPos left_bottom_;
};

}
