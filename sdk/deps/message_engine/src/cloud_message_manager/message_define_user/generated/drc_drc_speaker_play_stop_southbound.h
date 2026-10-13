// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcSpeakerPlayStopSouthboundMsg {
    int psdk_index = 0;

    AIGC_JSON_HELPER(psdk_index)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSpeakerPlayStopSouthbound final : public MessageV2 {
public:
    DrcDrcSpeakerPlayStopSouthbound();
    explicit DrcDrcSpeakerPlayStopSouthbound(const DrcDrcSpeakerPlayStopSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSpeakerPlayStopSouthboundMsg msg;
};

}