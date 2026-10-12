// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct OfflineMapFileProgressData {
    std::string name;
    std::string checksum;

    AIGC_JSON_HELPER(name, checksum)
};

struct EventsReplyOfflineMapSyncProgressNorthboundMsg {
    int reason = 0;
    std::string status;
    OfflineMapFileProgressData file;
    int version = 0;

    AIGC_JSON_HELPER(reason, status, file, version)
};
///------ 用户自定义数据结构 end ------

class EventsReplyOfflineMapSyncProgressNorthbound final : public MessageV1 {
public:
    EventsReplyOfflineMapSyncProgressNorthbound();
    explicit EventsReplyOfflineMapSyncProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyOfflineMapSyncProgressNorthboundMsg msg;
};

}