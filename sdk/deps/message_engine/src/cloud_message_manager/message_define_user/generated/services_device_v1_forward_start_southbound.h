// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DeviceV1ForwardStartCmdVolc {
    std::string app_id;
    std::string room_id;
    std::string user_id;
    std::string token;
    int expire_time = 0;

    AIGC_JSON_HELPER(app_id, room_id, user_id, token, expire_time)
};

struct DeviceV1ForwardStartCmdDrtc {
    std::string app_id;
    std::string channel_name;
    std::string room_id;
    std::string client_id;
    std::string token;
    int expire_time = 0;

    AIGC_JSON_HELPER(app_id, channel_name, room_id, client_id, token, expire_time)
};

struct ServicesDeviceV1ForwardStartSouthboundMsg {
    std::string provider;
    std::vector<std::string> target_user;
    DeviceV1ForwardStartCmdVolc volc_rtc;
    DeviceV1ForwardStartCmdDrtc drtc;

    AIGC_JSON_HELPER(provider, target_user, volc_rtc, drtc)
};
///------ 用户自定义数据结构 end ------

class ServicesDeviceV1ForwardStartSouthbound final : public MessageV1 {
public:
    ServicesDeviceV1ForwardStartSouthbound();
    explicit ServicesDeviceV1ForwardStartSouthbound(const ServicesDeviceV1ForwardStartSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesDeviceV1ForwardStartSouthboundMsg msg;
};

}