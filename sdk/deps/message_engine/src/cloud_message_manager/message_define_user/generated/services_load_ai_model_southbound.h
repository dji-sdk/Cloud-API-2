// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesLoadAiModelSouthboundMsg {
    std::string signed_id;
    std::string url;

    AIGC_JSON_HELPER(signed_id, url)
};
///------ 用户自定义数据结构 end ------

class ServicesLoadAiModelSouthbound final : public MessageV1 {
public:
    ServicesLoadAiModelSouthbound();
    explicit ServicesLoadAiModelSouthbound(const ServicesLoadAiModelSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesLoadAiModelSouthboundMsg msg;
};

}