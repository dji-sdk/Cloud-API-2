// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/io/byte_buffer.h"
#include "api/message.h"

namespace flight_hub_sdk {

// YOLOE target_type 35–73 明细见 ai_sei_parser.cpp 文件头。

class AiSeiParser {

public:
    AiSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer);
    ~AiSeiParser();

    bool parse();

    AiSeiInfo get_ai_sei_info() const;

private:
    // AI inside 的 target_type 需要根据 DRC 推送确定
    struct Target {
        uint16_t target_id;
        uint8_t target_type;
        uint8_t target_state;
        uint16_t target_cx;
        uint16_t target_cy;
        uint16_t target_w;
        uint16_t target_h;
        uint32_t target_distance;
    };

    struct TargetCount {
        uint8_t type;
        uint16_t count;
    };

    std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer_;
    uint8_t version_;
    uint32_t time_stamp_;
    uint8_t frame_type_;
    uint16_t track_id_;
    uint8_t reserved_;
    uint8_t obj_group_count_;
    std::vector<Target> targets_;
    std::vector<TargetCount> target_counts_;

};

}
