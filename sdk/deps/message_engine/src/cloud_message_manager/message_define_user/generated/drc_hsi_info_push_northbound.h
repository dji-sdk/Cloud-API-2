// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcHsiInfoPushNorthboundMsg {
    std::vector<int> around_distances;
    bool back_enable = false;
    bool back_work = false;
    int down_distance = 0;
    bool down_enable = false;
    bool down_work = false;
    bool front_enable = false;
    bool front_work = false;
    bool horizontal_enable = false;
    bool horizontal_work = false;
    bool left_enable = false;
    bool left_work = false;
    bool right_enable = false;
    bool right_work = false;
    int up_distance = 0;
    bool up_enable = false;
    bool up_work = false;
    bool vertical_enable = false;
    bool vertical_work = false;

    AIGC_JSON_HELPER(around_distances, back_enable, back_work, down_distance, down_enable, down_work, front_enable, front_work, horizontal_enable, horizontal_work, left_enable, left_work, right_enable, right_work, up_distance, up_enable, up_work, vertical_enable, vertical_work)
};
///------ 用户自定义数据结构 end ------

class DrcHsiInfoPushNorthbound final : public MessageV2 {
public:
    DrcHsiInfoPushNorthbound();
    explicit DrcHsiInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcHsiInfoPushNorthboundMsg msg;
};

}