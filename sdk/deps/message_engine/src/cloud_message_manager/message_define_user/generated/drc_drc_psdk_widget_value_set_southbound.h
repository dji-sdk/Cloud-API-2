// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcPsdkWidgetValueSetSouthboundMsg {
    int psdk_index = 0;
    int index = 0;
    int value = 0;

    AIGC_JSON_HELPER(psdk_index, index, value)
};
///------ 用户自定义数据结构 end ------

class DrcDrcPsdkWidgetValueSetSouthbound final : public MessageV2 {
public:
    DrcDrcPsdkWidgetValueSetSouthbound();
    explicit DrcDrcPsdkWidgetValueSetSouthbound(const DrcDrcPsdkWidgetValueSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcPsdkWidgetValueSetSouthboundMsg msg;
};

}