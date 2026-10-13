// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct AiBoxAppFileExportEventOutputProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct AiBoxAppInfoList {
    std::string object_key;
    std::string sn;
    std::string app_id;

    AIGC_JSON_HELPER(object_key, sn, app_id)
};

struct AiBoxAppFileExportEventOutputExt {
    std::string sn;
    std::vector<AiBoxAppInfoList> ai_box_app_info;

    AIGC_JSON_HELPER(sn, ai_box_app_info)
};

struct AiBoxAppFileExportEventOutput {
    AiBoxAppFileExportEventOutputProgress progress;
    std::string status;
    AiBoxAppFileExportEventOutputExt ext;

    AIGC_JSON_HELPER(progress, status, ext)
};

struct EventsReplyAiBoxAppFileExportProgressNorthboundMsg {
    AiBoxAppFileExportEventOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsReplyAiBoxAppFileExportProgressNorthbound final : public MessageV1 {
public:
    EventsReplyAiBoxAppFileExportProgressNorthbound();
    explicit EventsReplyAiBoxAppFileExportProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyAiBoxAppFileExportProgressNorthboundMsg msg;
};

}