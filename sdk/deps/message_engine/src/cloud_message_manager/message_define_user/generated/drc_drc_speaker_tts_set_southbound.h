// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcSpeakerTtsSetSouthboundMsg {
    int psdk_index = 0;
    int volume = 0;
    int type = 0;
    int language = 0;
    int speed = 0;

    AIGC_JSON_HELPER(psdk_index, volume, type, language, speed)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSpeakerTtsSetSouthbound final : public MessageV2 {
public:
    DrcDrcSpeakerTtsSetSouthbound();
    explicit DrcDrcSpeakerTtsSetSouthbound(const DrcDrcSpeakerTtsSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSpeakerTtsSetSouthboundMsg msg;
};

}