// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct FlightAreaFileProgressData {
    std::string name;
    std::string checksum;

    AIGC_JSON_HELPER(name, checksum)
};

struct EventsReplyFlightAreasSyncProgressNorthboundMsg {
    int reason = 0;
    std::string status;
    FlightAreaFileProgressData file;

    AIGC_JSON_HELPER(reason, status, file)
};
///------ 用户自定义数据结构 end ------

class EventsReplyFlightAreasSyncProgressNorthbound final : public MessageV1 {
public:
    EventsReplyFlightAreasSyncProgressNorthbound();
    explicit EventsReplyFlightAreasSyncProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyFlightAreasSyncProgressNorthboundMsg msg;
};

}