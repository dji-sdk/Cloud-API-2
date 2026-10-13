// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesAiBoxAppFileExportSouthboundMsg {
    std::string app_id;
    std::string sn;
    int export_type = 0;
    int export_action = 0;

    AIGC_JSON_HELPER(app_id, sn, export_type, export_action)
};
///------ 用户自定义数据结构 end ------

class ServicesAiBoxAppFileExportSouthbound final : public MessageV1 {
public:
    ServicesAiBoxAppFileExportSouthbound();
    explicit ServicesAiBoxAppFileExportSouthbound(const ServicesAiBoxAppFileExportSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesAiBoxAppFileExportSouthboundMsg msg;
};

}