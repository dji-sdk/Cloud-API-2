// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcModeEnterCmdVolc {
    std::string app_id;
    std::string room_id;
    std::string user_id;
    std::string token;
    int expire_time = 0;

    AIGC_JSON_HELPER(app_id, room_id, user_id, token, expire_time)
};

struct DrcModeEnterCmdDrtc {
    std::string app_id;
    std::string channel_name;
    std::string room_id;
    std::string client_id;
    std::string token;
    int expire_time = 0;

    AIGC_JSON_HELPER(app_id, channel_name, room_id, client_id, token, expire_time)
};

struct DrcModeEnterCmdMqtt {
    std::string address;
    std::string client_id;
    bool enable_tls = false;
    int expire_time = 0;
    std::string password;
    std::string username;

    AIGC_JSON_HELPER(address, client_id, enable_tls, expire_time, password, username)
};

struct DrcMethodFrequency {
    int drc_batteries_info_push = 0;
    int drc_camera_osd_push = 0;
    int drc_geo_connect_info_push = 0;
    int hsi_info_push = 0;
    int osd_info_push = 0;

    AIGC_JSON_HELPER(drc_batteries_info_push, drc_camera_osd_push, drc_geo_connect_info_push, hsi_info_push, osd_info_push)
};

struct ServicesDrcModeEnterSouthboundMsg {
    std::string provider;
    DrcModeEnterCmdVolc volc_rtc;
    DrcModeEnterCmdDrtc drtc;
    DrcModeEnterCmdMqtt mqtt_broker;
    int default_frequency = 0;
    DrcMethodFrequency method_frequency;

    AIGC_JSON_HELPER(provider, volc_rtc, drtc, mqtt_broker, default_frequency, method_frequency)
};
///------ 用户自定义数据结构 end ------

class ServicesDrcModeEnterSouthbound final : public MessageV1 {
public:
    ServicesDrcModeEnterSouthbound();
    explicit ServicesDrcModeEnterSouthbound(const ServicesDrcModeEnterSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesDrcModeEnterSouthboundMsg msg;
};

}