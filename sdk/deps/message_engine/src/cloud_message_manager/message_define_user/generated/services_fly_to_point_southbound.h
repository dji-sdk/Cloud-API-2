// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesFlyToPointSouthboundMsgTargetPoint {
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;

    AIGC_JSON_HELPER(latitude, longitude, height)
};

struct ServicesFlyToPointSouthboundMsg {
    std::string fly_to_id;
    int max_speed = 0;
    std::vector<ServicesFlyToPointSouthboundMsgTargetPoint> points;

    AIGC_JSON_HELPER(fly_to_id, max_speed, points)
};
///------ 用户自定义数据结构 end ------

class ServicesFlyToPointSouthbound final : public MessageV1 {
public:
    ServicesFlyToPointSouthbound();
    explicit ServicesFlyToPointSouthbound(const ServicesFlyToPointSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesFlyToPointSouthboundMsg msg;
};

}