// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesClearSdrTopoInfoSouthboundMsg {
    std::string flight_id;
    bool need_clear = false;

    AIGC_JSON_HELPER(flight_id, need_clear)
};
///------ 用户自定义数据结构 end ------

class ServicesClearSdrTopoInfoSouthbound final : public MessageV1 {
public:
    ServicesClearSdrTopoInfoSouthbound();
    explicit ServicesClearSdrTopoInfoSouthbound(const ServicesClearSdrTopoInfoSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesClearSdrTopoInfoSouthboundMsg msg;
};

}