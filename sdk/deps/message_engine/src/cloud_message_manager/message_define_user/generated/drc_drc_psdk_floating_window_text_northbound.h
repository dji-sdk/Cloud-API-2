// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcDrcPsdkFloatingWindowTextNorthboundMsg {
    int psdk_index = 0;
    std::string floating_window_text;

    AIGC_JSON_HELPER(psdk_index, floating_window_text)
};
///------ 用户自定义数据结构 end ------

class DrcDrcPsdkFloatingWindowTextNorthbound final : public MessageV2 {
public:
    DrcDrcPsdkFloatingWindowTextNorthbound();
    explicit DrcDrcPsdkFloatingWindowTextNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcPsdkFloatingWindowTextNorthboundMsg msg;
};

}