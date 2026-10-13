// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct InFlightTaskRequestFlyToTask {
    std::string id;
    std::string status;
    int task_seq = 0;

    AIGC_JSON_HELPER(id, status, task_seq)
};

struct InFlightTaskRequestOutput {
    std::string flight_id;
    InFlightTaskRequestFlyToTask fly_to_task;

    AIGC_JSON_HELPER(flight_id, fly_to_task)
};

struct RequestsReplyInFlightTaskRequestSouthboundMsg {
    InFlightTaskRequestOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class RequestsReplyInFlightTaskRequestSouthbound final : public MessageV1 {
public:
    RequestsReplyInFlightTaskRequestSouthbound();
    explicit RequestsReplyInFlightTaskRequestSouthbound(const RequestsReplyInFlightTaskRequestSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    RequestsReplyInFlightTaskRequestSouthboundMsg msg;
};

}