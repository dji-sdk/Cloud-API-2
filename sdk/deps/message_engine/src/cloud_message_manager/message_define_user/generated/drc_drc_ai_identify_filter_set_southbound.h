// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcAiIdentifyFilterSetSouthboundMsg {
    int index = 0;
    int light_type = 0;
    std::vector<int> filters;

    AIGC_JSON_HELPER(index, light_type, filters)
};
///------ 用户自定义数据结构 end ------

class DrcDrcAiIdentifyFilterSetSouthbound final : public MessageV2 {
public:
    DrcDrcAiIdentifyFilterSetSouthbound();
    explicit DrcDrcAiIdentifyFilterSetSouthbound(const DrcDrcAiIdentifyFilterSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcAiIdentifyFilterSetSouthboundMsg msg;
};

}