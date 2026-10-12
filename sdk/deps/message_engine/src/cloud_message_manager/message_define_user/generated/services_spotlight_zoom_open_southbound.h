// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct TargetPosition {
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    AIGC_JSON_HELPER(latitude, longitude, height)
};

struct ServicesSpotlightZoomOpenSouthboundMsg {
    TargetPosition observe_target_position;

    AIGC_JSON_HELPER(observe_target_position)
};
///------ 用户自定义数据结构 end ------

class ServicesSpotlightZoomOpenSouthbound final : public MessageV1 {
public:
    ServicesSpotlightZoomOpenSouthbound();
    explicit ServicesSpotlightZoomOpenSouthbound(const ServicesSpotlightZoomOpenSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesSpotlightZoomOpenSouthboundMsg msg;
};

}