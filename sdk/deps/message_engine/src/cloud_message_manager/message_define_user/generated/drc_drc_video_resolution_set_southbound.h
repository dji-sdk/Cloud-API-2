// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcVideoResolutionSetSouthboundMsg {
    std::string payload_index;
    int video_resolution = 0;
    std::string camera_type;

    AIGC_JSON_HELPER(payload_index, video_resolution, camera_type)
};
///------ 用户自定义数据结构 end ------

class DrcDrcVideoResolutionSetSouthbound final : public MessageV2 {
public:
    DrcDrcVideoResolutionSetSouthbound();
    explicit DrcDrcVideoResolutionSetSouthbound(const DrcDrcVideoResolutionSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcVideoResolutionSetSouthboundMsg msg;
};

}