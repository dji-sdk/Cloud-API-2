// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesFlighttaskUndoSouthboundMsg {
    std::vector<std::string> flight_ids;

    AIGC_JSON_HELPER(flight_ids)
};
///------ 用户自定义数据结构 end ------

class ServicesFlighttaskUndoSouthbound final : public MessageV1 {
public:
    ServicesFlighttaskUndoSouthbound();
    explicit ServicesFlighttaskUndoSouthbound(const ServicesFlighttaskUndoSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesFlighttaskUndoSouthboundMsg msg;
};

}