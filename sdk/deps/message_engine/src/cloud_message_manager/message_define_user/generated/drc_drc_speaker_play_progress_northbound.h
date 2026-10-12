// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcUpDrcSpeakerPlayProgressNorthboundProgressMsg {
    std::string step_key;
    int percent = 0;

    AIGC_JSON_HELPER(step_key, percent)
};

struct DrcDrcSpeakerPlayProgressNorthboundMsg {
    int psdk_index = 0;
    int result = 0;
    std::string status;
    DrcUpDrcSpeakerPlayProgressNorthboundProgressMsg progress;
    std::string md5;

    AIGC_JSON_HELPER(psdk_index, result, status, progress, md5)
};
///------ 用户自定义数据结构 end ------

class DrcDrcSpeakerPlayProgressNorthbound final : public MessageV2 {
public:
    DrcDrcSpeakerPlayProgressNorthbound();
    explicit DrcDrcSpeakerPlayProgressNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcSpeakerPlayProgressNorthboundMsg msg;
};

}