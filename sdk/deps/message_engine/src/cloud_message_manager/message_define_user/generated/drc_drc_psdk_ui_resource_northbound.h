// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcPsdkUiResourceNorthboundMsg {
    int psdk_index = 0;
    int psdk_ready = 0;
    int psdk_type = 0;
    std::string object_key;

    AIGC_JSON_HELPER(psdk_index, psdk_ready, psdk_type, object_key)
};
///------ 用户自定义数据结构 end ------

class DrcDrcPsdkUiResourceNorthbound final : public MessageV2 {
public:
    DrcDrcPsdkUiResourceNorthbound();
    explicit DrcDrcPsdkUiResourceNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcPsdkUiResourceNorthboundMsg msg;
};

}