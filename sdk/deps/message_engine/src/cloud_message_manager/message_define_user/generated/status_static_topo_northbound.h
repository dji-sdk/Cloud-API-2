// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct StatusStaticTopoNorthboundMsgSubDeviceInfo {
    std::string sn;
    std::string domain;
    int type = 0;
    int sub_type = 0;

    AIGC_JSON_HELPER(sn, domain, type, sub_type)
};

struct StatusStaticTopoNorthboundMsg {
    std::string domain;
    int type = 0;
    int sub_type = 0;
    std::vector<StatusStaticTopoNorthboundMsgSubDeviceInfo> sub_devices;

    AIGC_JSON_HELPER(domain, type, sub_type, sub_devices)
};
///------ 用户自定义数据结构 end ------

class StatusStaticTopoNorthbound final : public MessageV1 {
public:
    StatusStaticTopoNorthbound();
    explicit StatusStaticTopoNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    StatusStaticTopoNorthboundMsg msg;
};

}