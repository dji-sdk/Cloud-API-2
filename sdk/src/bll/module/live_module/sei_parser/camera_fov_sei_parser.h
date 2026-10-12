// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/io/byte_buffer.h"
#include "api/message.h"

namespace flight_hub_sdk {

class CameraFovSeiParser {
public:
    CameraFovSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer);
    ~CameraFovSeiParser();

    bool parse();

    FovSeiInfo get_camera_fov_sei_info() const;

private:
    std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer_;
    uint8_t version_;
    uint32_t fov_horizontal_;
    uint32_t fov_vertical_;
    uint32_t cur_actual_focal_length_;
};

}

