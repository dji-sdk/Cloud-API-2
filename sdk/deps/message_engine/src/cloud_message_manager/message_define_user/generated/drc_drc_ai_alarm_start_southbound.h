// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcAiAlarmTarget {
    int target_type = 0;
    double target_value = 0.0;
    bool use_min_threshold = false;
    bool use_max_threshold = false;
    double target_min_threshold = 0.0;
    double target_max_threshold = 0.0;

    AIGC_JSON_HELPER(target_type, target_value, use_min_threshold, use_max_threshold, target_min_threshold, target_max_threshold)
};

struct DrcAiAlarmStreamInputConf {
    int stream_source = 0;
    int model_idx = 0;
    int algorithm_type = 0;
    int alarm_interval = 0;
    std::vector<DrcAiAlarmTarget> alarm_targets;

    AIGC_JSON_HELPER(stream_source, model_idx, algorithm_type, alarm_interval, alarm_targets)
};

struct DrcDrcAiAlarmStartSouthboundMsg {
    std::vector<DrcAiAlarmStreamInputConf> stream_input_confs;

    AIGC_JSON_HELPER(stream_input_confs)
};
///------ 用户自定义数据结构 end ------

class DrcDrcAiAlarmStartSouthbound final : public MessageV2 {
public:
    DrcDrcAiAlarmStartSouthbound();
    explicit DrcDrcAiAlarmStartSouthbound(const DrcDrcAiAlarmStartSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcAiAlarmStartSouthboundMsg msg;
};

}