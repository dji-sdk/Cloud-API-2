// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDownDrcSpeakerTtsPlayStartSouthboundTtsMsg {
    std::string name;
    std::string text;
    std::string md5;

    AIGC_JSON_HELPER(name, text, md5)
};

struct DrcDrcSpeakerTtsPlayStartSouthboundMsg {
    int psdk_index = 0;
    DrcDownDrcSpeakerTtsPlayStartSouthboundTtsMsg tts;

    AIGC_JSON_HELPER(psdk_index, tts)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSpeakerTtsPlayStartSouthbound final : public MessageV2 {
public:
    DrcDrcSpeakerTtsPlayStartSouthbound();
    explicit DrcDrcSpeakerTtsPlayStartSouthbound(const DrcDrcSpeakerTtsPlayStartSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSpeakerTtsPlayStartSouthboundMsg msg;
};

}