// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct FlySafeUpgradeProgress {
    int percent = 0;
    int current_step = 0;

    AIGC_JSON_HELPER(percent, current_step)
};

struct FlySafeUpgradeExt {
    int placeholder = 0;

    AIGC_JSON_HELPER(placeholder)
};

struct FlySafeUpgradeProgressOutput {
    std::string status;
    FlySafeUpgradeProgress progress;
    FlySafeUpgradeExt ext;

    AIGC_JSON_HELPER(status, progress, ext)
};

struct EventsReplyFlysafeDatabaseUpgradeNorthboundMsg {
    int result = 0;
    FlySafeUpgradeProgressOutput output;

    AIGC_JSON_HELPER(result, output)
};
///------ 用户自定义数据结构 end ------

class EventsReplyFlysafeDatabaseUpgradeNorthbound final : public MessageV1 {
public:
    EventsReplyFlysafeDatabaseUpgradeNorthbound();
    explicit EventsReplyFlysafeDatabaseUpgradeNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyFlysafeDatabaseUpgradeNorthboundMsg msg;
};

}