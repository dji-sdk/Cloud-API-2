// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct EventsHmsNorthboundMsgHmsDetailArgs {
    int component_index = 0;
    int sensor_index = 0;

    AIGC_JSON_HELPER(component_index, sensor_index)
};

struct EventsHmsNorthboundMsgHmsDetail {
    EventsHmsNorthboundMsgHmsDetailArgs args;
    std::string code;
    std::string device_type;
    int imminent = 0;
    int level = 0;
    int in_the_sky = 0;
    int module = 0;

    AIGC_JSON_HELPER(args, code, device_type, imminent, level, in_the_sky, module)
};

struct EventsHmsNorthboundMsg {
    std::vector<EventsHmsNorthboundMsgHmsDetail> list;

    AIGC_JSON_HELPER(list)
};
///------ 用户自定义数据结构 end ------

class EventsHmsNorthbound final : public MessageV1 {
public:
    EventsHmsNorthbound();
    explicit EventsHmsNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsHmsNorthboundMsg msg;
};

}