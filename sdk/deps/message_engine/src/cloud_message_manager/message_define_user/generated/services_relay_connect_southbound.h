// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RelayConnectSSID {
    std::string device_name;
    std::string passwd;

    AIGC_JSON_HELPER(device_name, passwd)
};

struct ServicesRelayConnectSouthboundMsg {
    int uav_id = 0;
    RelayConnectSSID ssid;

    AIGC_JSON_HELPER(uav_id, ssid)
};
///------ 用户自定义数据结构 end ------

class ServicesRelayConnectSouthbound final : public MessageV1 {
public:
    ServicesRelayConnectSouthbound();
    explicit ServicesRelayConnectSouthbound(const ServicesRelayConnectSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesRelayConnectSouthboundMsg msg;
};

}