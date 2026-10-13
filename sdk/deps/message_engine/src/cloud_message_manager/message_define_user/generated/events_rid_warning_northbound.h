// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RidWarningItem {
    std::string sn;
    int distance = 0;
    double latitude = 0.0;
    double longitude = 0.0;
    double altitude = 0.0;

    AIGC_JSON_HELPER(sn, distance, latitude, longitude, altitude)
};

struct EventsRidWarningNorthboundMsg {
    std::vector<RidWarningItem> list;

    AIGC_JSON_HELPER(list)
};
///------ 用户自定义数据结构 end ------

class EventsRidWarningNorthbound final : public MessageV1 {
public:
    EventsRidWarningNorthbound();
    explicit EventsRidWarningNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsRidWarningNorthboundMsg msg;
};

}