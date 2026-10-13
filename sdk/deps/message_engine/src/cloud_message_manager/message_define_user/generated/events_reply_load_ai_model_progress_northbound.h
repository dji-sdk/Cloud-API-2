// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct LoadAiModelEventExt {
    std::string signed_id;

    AIGC_JSON_HELPER(signed_id)
};

struct LoadAiModelEventProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct LoadAiModelEventOutput {
    LoadAiModelEventExt ext;
    LoadAiModelEventProgress progress;
    std::string status;

    AIGC_JSON_HELPER(ext, progress, status)
};

struct EventsReplyLoadAiModelProgressNorthboundMsg {
    LoadAiModelEventOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsReplyLoadAiModelProgressNorthbound final : public MessageV1 {
public:
    EventsReplyLoadAiModelProgressNorthbound();
    explicit EventsReplyLoadAiModelProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyLoadAiModelProgressNorthboundMsg msg;
};

}