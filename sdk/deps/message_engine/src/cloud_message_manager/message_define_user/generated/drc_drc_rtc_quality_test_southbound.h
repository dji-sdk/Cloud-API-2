// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcRtcQualityTestSouthboundMsg {
    int index = 0;
    int length = 0;
    std::string data;
    int diff_time = 0;
    double timestamp = 0.0;
    bool is_need_ack = false;
    bool is_test_end = false;

    AIGC_JSON_HELPER(index, length, data, diff_time, timestamp, is_need_ack, is_test_end)
};
///------ 用户自定义数据结构 end ------

class DrcDrcRtcQualityTestSouthbound final : public MessageV2 {
public:
    DrcDrcRtcQualityTestSouthbound();
    explicit DrcDrcRtcQualityTestSouthbound(const DrcDrcRtcQualityTestSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcRtcQualityTestSouthboundMsg msg;
};

}