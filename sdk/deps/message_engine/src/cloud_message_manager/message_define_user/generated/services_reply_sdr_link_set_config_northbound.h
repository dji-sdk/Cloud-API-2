// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct SdrLinkSetConfigOutput {
    std::string sn;
    int sdr_method = 0;
    int band_mode = 0;
    int sub2g_auz = 0;

    AIGC_JSON_HELPER(sn, sdr_method, band_mode, sub2g_auz)
};

struct ServicesReplySdrLinkSetConfigNorthboundMsg {
    int result = 0;
    SdrLinkSetConfigOutput output;

    AIGC_JSON_HELPER(result, output)
};
///------ 用户自定义数据结构 end ------

class ServicesReplySdrLinkSetConfigNorthbound final : public MessageV1 {
public:
    ServicesReplySdrLinkSetConfigNorthbound();
    explicit ServicesReplySdrLinkSetConfigNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesReplySdrLinkSetConfigNorthboundMsg msg;
};

}