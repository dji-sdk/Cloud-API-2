// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesSdrLinkSetConfigSouthboundMsg {
    std::string sn;
    int sdr_method = 0;
    int band_mode = 0;
    int sub2g_auz = 0;

    AIGC_JSON_HELPER(sn, sdr_method, band_mode, sub2g_auz)
};
///------ 用户自定义数据结构 end ------

class ServicesSdrLinkSetConfigSouthbound final : public MessageV1 {
public:
    ServicesSdrLinkSetConfigSouthbound();
    explicit ServicesSdrLinkSetConfigSouthbound(const ServicesSdrLinkSetConfigSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesSdrLinkSetConfigSouthboundMsg msg;
};

}