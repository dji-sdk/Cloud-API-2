// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DeviceTopoSubDeviceInfo {
    int relation = 0;
    std::string sn;
    std::string domain;
    int type = 0;
    int sub_type = 0;
    std::string device_secret;
    std::string nonce;
    std::string thing_version;

    AIGC_JSON_HELPER(relation, sn, domain, type, sub_type, device_secret, nonce, thing_version)
};

struct StatusUpdateTopoNorthboundMsg {
    std::string domain;
    int type = 0;
    int sub_type = 0;
    std::string device_secret;
    std::string nonce;
    std::string thing_version;
    std::vector<DeviceTopoSubDeviceInfo> sub_devices;

    AIGC_JSON_HELPER(domain, type, sub_type, device_secret, nonce, thing_version, sub_devices)
};
///------ 用户自定义数据结构 end ------

class StatusUpdateTopoNorthbound final : public MessageV1 {
public:
    StatusUpdateTopoNorthbound();
    explicit StatusUpdateTopoNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    StatusUpdateTopoNorthboundMsg msg;
};

}