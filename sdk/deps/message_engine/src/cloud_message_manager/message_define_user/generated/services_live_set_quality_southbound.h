// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesLiveSetQualitySouthboundMsg {
    std::string video_id;
    int video_quality = 0;

    AIGC_JSON_HELPER(video_id, video_quality)
};
///------ 用户自定义数据结构 end ------

class ServicesLiveSetQualitySouthbound final : public MessageV1 {
public:
    ServicesLiveSetQualitySouthbound();
    explicit ServicesLiveSetQualitySouthbound(const ServicesLiveSetQualitySouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesLiveSetQualitySouthboundMsg msg;
};

}