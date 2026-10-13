// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcSetIsothermRenderingPolygonPoint {
    int x = 0;
    int y = 0;

    AIGC_JSON_HELPER(x, y)
};

struct DrcDrcSetIsothermRenderingPolygonSouthboundMsg {
    std::string payload_index;
    std::vector<DrcSetIsothermRenderingPolygonPoint> polygon;

    AIGC_JSON_HELPER(payload_index, polygon)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSetIsothermRenderingPolygonSouthbound final : public MessageV2 {
public:
    DrcDrcSetIsothermRenderingPolygonSouthbound();
    explicit DrcDrcSetIsothermRenderingPolygonSouthbound(const DrcDrcSetIsothermRenderingPolygonSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSetIsothermRenderingPolygonSouthboundMsg msg;
};

}