// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct AckData {
    int index = 0;
    int length = 0;
    std::string data;

    AIGC_JSON_HELPER(index, length, data)
};

struct TimeoutIndex {
    int index = 0;
    int timout_time = 0;

    AIGC_JSON_HELPER(index, timout_time)
};

struct EndData {
    int total_size = 0;
    int total_time = 0;
    int max_index = 0;
    std::vector<int> lost_index;
    std::vector<int> out_of_order_index;
    std::vector<TimeoutIndex> timeout_index;

    AIGC_JSON_HELPER(total_size, total_time, max_index, lost_index, out_of_order_index, timeout_index)
};

struct DrcReplyDrcRtcQualityTestNorthboundMsg {
    int result = 0;
    AckData ack_data;
    EndData end_data;

    AIGC_JSON_HELPER(result, ack_data, end_data)
};
///------ 用户自定义数据结构 end ------

class DrcReplyDrcRtcQualityTestNorthbound final : public MessageV2 {
public:
    DrcReplyDrcRtcQualityTestNorthbound();
    explicit DrcReplyDrcRtcQualityTestNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcReplyDrcRtcQualityTestNorthboundMsg msg;
};

}