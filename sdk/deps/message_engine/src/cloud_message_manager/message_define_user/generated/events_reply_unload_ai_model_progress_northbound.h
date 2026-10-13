// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct UnLoadAiModelEventExt {
    std::string signed_id;

    AIGC_JSON_HELPER(signed_id)
};

struct UnLoadAiModelEventProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct UnLoadAiModelEventOutput {
    UnLoadAiModelEventExt ext;
    UnLoadAiModelEventProgress progress;
    std::string status;

    AIGC_JSON_HELPER(ext, progress, status)
};

struct EventsReplyUnloadAiModelProgressNorthboundMsg {
    UnLoadAiModelEventOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsReplyUnloadAiModelProgressNorthbound final : public MessageV1 {
public:
    EventsReplyUnloadAiModelProgressNorthbound();
    explicit EventsReplyUnloadAiModelProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyUnloadAiModelProgressNorthboundMsg msg;
};

}