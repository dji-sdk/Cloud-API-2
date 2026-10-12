// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcAiSpotlightZoomSelectSouthboundMsg {
    double center_x = 0.0;
    double center_y = 0.0;
    double height = 0.0;
    double width = 0.0;

    AIGC_JSON_HELPER(center_x, center_y, height, width)
};
///------ 用户自定义数据结构 end ------

class DrcDrcAiSpotlightZoomSelectSouthbound final : public MessageV2 {
public:
    DrcDrcAiSpotlightZoomSelectSouthbound();
    explicit DrcDrcAiSpotlightZoomSelectSouthbound(const DrcDrcAiSpotlightZoomSelectSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcAiSpotlightZoomSelectSouthboundMsg msg;
};

}