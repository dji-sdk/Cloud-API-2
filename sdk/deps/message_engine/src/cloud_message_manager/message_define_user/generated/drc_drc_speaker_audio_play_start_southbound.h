// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDownDrcSpeakerAudioPlayStartSouthboundFileMsg {
    std::string name;
    std::string url;
    std::string md5;
    std::string format;

    AIGC_JSON_HELPER(name, url, md5, format)
};

struct DrcDrcSpeakerAudioPlayStartSouthboundMsg {
    int psdk_index = 0;
    DrcDownDrcSpeakerAudioPlayStartSouthboundFileMsg file;

    AIGC_JSON_HELPER(psdk_index, file)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSpeakerAudioPlayStartSouthbound final : public MessageV2 {
public:
    DrcDrcSpeakerAudioPlayStartSouthbound();
    explicit DrcDrcSpeakerAudioPlayStartSouthbound(const DrcDrcSpeakerAudioPlayStartSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSpeakerAudioPlayStartSouthboundMsg msg;
};

}