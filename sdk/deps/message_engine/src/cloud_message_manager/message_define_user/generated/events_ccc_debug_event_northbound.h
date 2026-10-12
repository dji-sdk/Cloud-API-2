// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct EventsCccDebugCommandUpdateCccParam {
    int progress = 0;
    std::string md5;

    AIGC_JSON_HELPER(progress, md5)
};

struct EventsCccDebugEventNorthboundMsg {
    int type = 0;
    EventsCccDebugCommandUpdateCccParam update_ccc;

    AIGC_JSON_HELPER(type, update_ccc)
};
///------ 用户自定义数据结构 end ------

class EventsCccDebugEventNorthbound final : public MessageV1 {
public:
    EventsCccDebugEventNorthbound();
    explicit EventsCccDebugEventNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsCccDebugEventNorthboundMsg msg;
};

}