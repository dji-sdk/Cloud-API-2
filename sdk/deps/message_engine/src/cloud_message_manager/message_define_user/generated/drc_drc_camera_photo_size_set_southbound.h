// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcCameraPhotoSizeSetSouthboundMsg {
    std::string payload_index;
    int photo_size = 0;
    std::string camera_type;

    AIGC_JSON_HELPER(payload_index, photo_size, camera_type)
};
///------ 用户自定义数据结构 end ------

class DrcDrcCameraPhotoSizeSetSouthbound final : public MessageV2 {
public:
    DrcDrcCameraPhotoSizeSetSouthbound();
    explicit DrcDrcCameraPhotoSizeSetSouthbound(const DrcDrcCameraPhotoSizeSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcCameraPhotoSizeSetSouthboundMsg msg;
};

}