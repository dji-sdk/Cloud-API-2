// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCameraWatermarkSetSouthboundMsg {
    std::string payload_index;
    int global_enable = 0;
    int drone_type_enable = 0;
    int manufacturer_enable = 0;
    int drone_sn_enable = 0;
    int datetime_enable = 0;
    int gps_enable = 0;
    int user_custom_string_enable = 0;
    std::string user_custom_string;
    int layout = 0;

    AIGC_JSON_HELPER(payload_index, global_enable, drone_type_enable, manufacturer_enable, drone_sn_enable, datetime_enable, gps_enable, user_custom_string_enable, user_custom_string, layout)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraWatermarkSetSouthbound final : public MessageV2 {
public:
    DrcDrcCameraWatermarkSetSouthbound();
    explicit DrcDrcCameraWatermarkSetSouthbound(const DrcDrcCameraWatermarkSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraWatermarkSetSouthboundMsg msg;
};

}