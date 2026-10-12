// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RelayNonPairAttachResultEventExtResult {
    int relay_non_pair_connect_result = 0;
    int backoff_time = 0;
    int relay_bin_switch_status = 0;

    AIGC_JSON_HELPER(relay_non_pair_connect_result, backoff_time, relay_bin_switch_status)
};

struct RelayNonPairAttachResultEventExt {
    RelayNonPairAttachResultEventExtResult on_pair_attach_result;

    AIGC_JSON_HELPER(on_pair_attach_result)
};

struct RelayNonPairAttachResultEventProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct RelayNonPairAttachResultEventOutput {
    RelayNonPairAttachResultEventExt ext;
    RelayNonPairAttachResultEventProgress progress;
    std::string status;

    AIGC_JSON_HELPER(ext, progress, status)
};

struct EventsReplyRelayNonPairAttachResultNorthboundMsg {
    RelayNonPairAttachResultEventOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsReplyRelayNonPairAttachResultNorthbound final : public MessageV1 {
public:
    EventsReplyRelayNonPairAttachResultNorthbound();
    explicit EventsReplyRelayNonPairAttachResultNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyRelayNonPairAttachResultNorthboundMsg msg;
};

}