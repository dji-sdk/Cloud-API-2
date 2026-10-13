// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesFlyToPointUpdateSouthboundMsgPoint {
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    AIGC_JSON_HELPER(latitude, longitude, height)
};

struct ServicesFlyToPointUpdateSouthboundMsg {
    int max_speed = 0;
    std::vector<ServicesFlyToPointUpdateSouthboundMsgPoint> points;

    AIGC_JSON_HELPER(max_speed, points)
};
///------ 用户自定义数据结构 end ------

class ServicesFlyToPointUpdateSouthbound final : public MessageV1 {
public:
    ServicesFlyToPointUpdateSouthbound();
    explicit ServicesFlyToPointUpdateSouthbound(const ServicesFlyToPointUpdateSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesFlyToPointUpdateSouthboundMsg msg;
};

}