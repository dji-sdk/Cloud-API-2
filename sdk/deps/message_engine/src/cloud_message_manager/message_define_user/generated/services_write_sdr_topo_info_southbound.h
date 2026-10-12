// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct UavNode {
    double sdr_id = 0.0;
    int device_type = 0;
    int index = 0;
    int model = 0;

    AIGC_JSON_HELPER(sdr_id, device_type, index, model)
};

struct RoamingRtcmInfo {
    std::string host;
    std::string mount_point;
    std::string port;
    int rtcm_device_type = 0;
    int source_type = 0;

    AIGC_JSON_HELPER(host, mount_point, port, rtcm_device_type, source_type)
};

struct GndNodes {
    double sdr_id = 0.0;
    int device_type = 0;
    int index = 0;
    int model = 0;
    double height = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;
    RoamingRtcmInfo rtcm_info;

    AIGC_JSON_HELPER(sdr_id, device_type, index, model, height, latitude, longitude, rtcm_info)
};

struct WirelessLinkTopoMsg {
    std::vector<int> secret_code;
    UavNode uav_node;
    std::vector<GndNodes> gnd_nodes;

    AIGC_JSON_HELPER(secret_code, uav_node, gnd_nodes)
};

struct ServicesWriteSdrTopoInfoSouthboundMsg {
    std::string flight_id;
    int seq_num = 0;
    WirelessLinkTopoMsg wireless_link_topo_msg;

    AIGC_JSON_HELPER(flight_id, seq_num, wireless_link_topo_msg)
};
///------ 用户自定义数据结构 end ------

class ServicesWriteSdrTopoInfoSouthbound final : public MessageV1 {
public:
    ServicesWriteSdrTopoInfoSouthbound();
    explicit ServicesWriteSdrTopoInfoSouthbound(const ServicesWriteSdrTopoInfoSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesWriteSdrTopoInfoSouthboundMsg msg;
};

}