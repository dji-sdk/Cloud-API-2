// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcAiAlarmStatePushTarget {
    int target_type = 0;
    double target_value = 0.0;
    bool use_min_threshold = false;
    bool use_max_threshold = false;
    double target_min_threshold = 0.0;
    double target_max_threshold = 0.0;

    AIGC_JSON_HELPER(target_type, target_value, use_min_threshold, use_max_threshold, target_min_threshold, target_max_threshold)
};

struct DrcAiAlarmStatePushStreamInputConf {
    int stream_source = 0;
    int model_idx = 0;
    int algorithm_type = 0;
    int alarm_interval = 0;
    std::vector<DrcAiAlarmStatePushTarget> alarm_targets;

    AIGC_JSON_HELPER(stream_source, model_idx, algorithm_type, alarm_interval, alarm_targets)
};

struct DrcDrcAiAlarmStatePushNorthboundMsg {
    bool alarm_on = false;
    std::vector<DrcAiAlarmStatePushStreamInputConf> stream_input_confs;

    AIGC_JSON_HELPER(alarm_on, stream_input_confs)
};
///------ 用户自定义数据结构 end ------

class DrcDrcAiAlarmStatePushNorthbound final : public MessageV2 {
public:
    DrcDrcAiAlarmStatePushNorthbound();
    explicit DrcDrcAiAlarmStatePushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcAiAlarmStatePushNorthboundMsg msg;
};

}