// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct FlightTaskProgressInfo {
    int current_step = 0;

    AIGC_JSON_HELPER(current_step)
};

struct FlightTaskProgressGetOutput {
    std::string flight_id;
    std::string status;
    FlightTaskProgressInfo progress;

    AIGC_JSON_HELPER(flight_id, status, progress)
};

struct RequestsReplyFlighttaskProgressGetSouthboundMsg {
    FlightTaskProgressGetOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class RequestsReplyFlighttaskProgressGetSouthbound final : public MessageV1 {
public:
    RequestsReplyFlighttaskProgressGetSouthbound();
    explicit RequestsReplyFlighttaskProgressGetSouthbound(const RequestsReplyFlighttaskProgressGetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    RequestsReplyFlighttaskProgressGetSouthboundMsg msg;
};

}