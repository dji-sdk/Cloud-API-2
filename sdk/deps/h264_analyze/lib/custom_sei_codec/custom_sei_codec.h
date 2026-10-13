// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <vector>
#include <memory>
#include "custom_sei_data_define.h"

// 自定义 SEI 编解码类
// 只提供静态方法就够了
class CustomSeiCodec {
public:

    // 给一帧 H264 数据和全量自定义 SEI 数据，合成一帧 H264 数据
    static bool encode(const std::vector<uint8_t>& frame_data, const CustomSEIHeader& custom_sei_header, const std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& custom_sei_data, std::vector<uint8_t>& output_frame_data);

    // 给一帧含有自定义 SEI 信息的 H264 数据, 从中解码出来自定义 SEI 数据, 注意 frame 里面的 SEI 数据不会被删除
    static bool decode(const std::vector<uint8_t>& frame_data, CustomSEIHeader& output_header, std::vector<std::shared_ptr<CustomSeiTLVDataBase>>& output_sei_data);

};
