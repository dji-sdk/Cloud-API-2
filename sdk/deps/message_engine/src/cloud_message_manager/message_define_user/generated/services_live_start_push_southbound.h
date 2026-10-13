// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesLiveStartPushSouthboundMsg {
    std::string url;
    int url_type = 0;
    std::string video_id;
    int video_quality = 0;

    AIGC_JSON_HELPER(url, url_type, video_id, video_quality)
};
///------ 用户自定义数据结构 end ------

class ServicesLiveStartPushSouthbound final : public MessageV1 {
public:
    ServicesLiveStartPushSouthbound();
    explicit ServicesLiveStartPushSouthbound(const ServicesLiveStartPushSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesLiveStartPushSouthboundMsg msg;
};

}