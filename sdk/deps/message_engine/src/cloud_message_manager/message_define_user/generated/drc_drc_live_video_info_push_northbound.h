// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcLiveVideoInfoPushNorthboundMsg {
    std::string payload_index;
    std::string video_index;
    std::string video_type;
    std::vector<std::string> switchable_video_types;

    AIGC_JSON_HELPER(payload_index, video_index, video_type, switchable_video_types)
};
///------ 用户自定义数据结构 end ------

class DrcDrcLiveVideoInfoPushNorthbound final : public MessageV2 {
public:
    DrcDrcLiveVideoInfoPushNorthbound();
    explicit DrcDrcLiveVideoInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcLiveVideoInfoPushNorthboundMsg msg;
};

}