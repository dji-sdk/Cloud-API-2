// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct AiBoxAppListInfoOutputProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct AiBoxAppInfoEventList {
    std::string id;
    std::string name;
    std::string version;
    std::string description;
    std::string publisher;
    double app_size = 0.0;
    int type = 0;
    double time = 0.0;
    double data_size = 0.0;

    AIGC_JSON_HELPER(id, name, version, description, publisher, app_size, type, time, data_size)
};

struct AiBoxAppListInfoOutputExt {
    std::string sn;
    std::vector<AiBoxAppInfoEventList> ai_box_app_info;

    AIGC_JSON_HELPER(sn, ai_box_app_info)
};

struct AiBoxAppListInfoOutput {
    AiBoxAppListInfoOutputProgress progress;
    std::string status;
    AiBoxAppListInfoOutputExt ext;

    AIGC_JSON_HELPER(progress, status, ext)
};

struct EventsReplyAiBoxAppListInfosNorthboundMsg {
    AiBoxAppListInfoOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsReplyAiBoxAppListInfosNorthbound final : public MessageV1 {
public:
    EventsReplyAiBoxAppListInfosNorthbound();
    explicit EventsReplyAiBoxAppListInfosNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyAiBoxAppListInfosNorthboundMsg msg;
};

}