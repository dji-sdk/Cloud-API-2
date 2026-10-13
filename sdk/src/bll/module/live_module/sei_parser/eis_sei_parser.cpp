// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "eis_sei_parser.h"
#include "base/common/logger.h"

namespace flight_hub_sdk {

EisSeiParser::EisSeiParser(std::shared_ptr<drc_sdk::ByteBuffer> sei_buffer)
    : sei_buffer_(sei_buffer) {}

EisSeiParser::~EisSeiParser() {}

bool EisSeiParser::parse() {
    if (!sei_buffer_) {
        LOG_ERROR("sei_buffer_ is null");
        return false;
    }

    // EIS SEI 数据结构: version(1) + 4个CamCropPos(每个2+2=4字节) = 1 + 16 = 17字节
    if (sei_buffer_->size() < 17) {
        LOG_ERROR("sei_buffer_ size is too small to parse, size: %d", sei_buffer_->size());
        return false;
    }

    uint32_t offset = 0;

    // 解析版本号
    version_ = *reinterpret_cast<uint8_t*>(sei_buffer_->data() + offset);
    offset += 1;

    // 解析左上角坐标
    left_top_.x = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;
    left_top_.y = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;

    // 解析右上角坐标
    right_top_.x = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;
    right_top_.y = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;

    // 解析右下角坐标
    right_bottom_.x = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;
    right_bottom_.y = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;

    // 解析左下角坐标
    left_bottom_.x = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);
    offset += 2;
    left_bottom_.y = *reinterpret_cast<uint16_t*>(sei_buffer_->data() + offset);

    // LOG_DEBUG("parse eis sei success, version_: %d, left_top: (%d, %d), right_top: (%d, %d), right_bottom: (%d, %d), left_bottom: (%d, %d)",
    //     version_, left_top_.x, left_top_.y, right_top_.x, right_top_.y, right_bottom_.x, right_bottom_.y, left_bottom_.x, left_bottom_.y);

    return true;
}

EisSeiInfo EisSeiParser::get_eis_sei_info() const {
    EisSeiInfo eis_sei_info;
    eis_sei_info.version = version_;
    eis_sei_info.left_top = left_top_;
    eis_sei_info.right_top = right_top_;
    eis_sei_info.right_bottom = right_bottom_;
    eis_sei_info.left_bottom = left_bottom_;

    return eis_sei_info;
}

}
