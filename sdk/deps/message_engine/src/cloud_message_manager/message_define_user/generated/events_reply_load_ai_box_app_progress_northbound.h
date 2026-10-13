// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct LoadAiBoxAppEventOutputProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct LoadAiBoxAppEventOutputExt {
    std::string sn;

    AIGC_JSON_HELPER(sn)
};

struct LoadAiBoxAppEventOutput {
    LoadAiBoxAppEventOutputProgress progress;
    std::string status;
    LoadAiBoxAppEventOutputExt ext;

    AIGC_JSON_HELPER(progress, status, ext)
};

struct EventsReplyLoadAiBoxAppProgressNorthboundMsg {
    LoadAiBoxAppEventOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsReplyLoadAiBoxAppProgressNorthbound final : public MessageV1 {
public:
    EventsReplyLoadAiBoxAppProgressNorthbound();
    explicit EventsReplyLoadAiBoxAppProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyLoadAiBoxAppProgressNorthboundMsg msg;
};

}