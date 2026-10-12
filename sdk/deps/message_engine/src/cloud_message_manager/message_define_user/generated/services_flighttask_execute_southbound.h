// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RtcmInfo {
    std::string mount_point;
    std::string port;
    std::string host;
    int rtcm_device_type = 0;
    int source_type = 0;

    AIGC_JSON_HELPER(mount_point, port, host, rtcm_device_type, source_type)
};

struct AlternateLandPoint {
    double latitude = 0.0;
    double longitude = 0.0;
    double safe_land_height = 0.0;
    int is_configured = 0;

    AIGC_JSON_HELPER(latitude, longitude, safe_land_height, is_configured)
};

struct DeviceInfo {
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
    int heading = 0;
    int home_position_is_valid = 0;
    int index = 0;
    std::string sn;
    RtcmInfo rtcm_info;
    AlternateLandPoint alternate_land_point;

    AIGC_JSON_HELPER(latitude, longitude, height, heading, home_position_is_valid, index, sn, rtcm_info, alternate_land_point)
};

struct ConnectDeviceInfo {
    int index = 0;
    std::string sn;
    RtcmInfo rtcm_info;

    AIGC_JSON_HELPER(index, sn, rtcm_info)
};

struct CenterNode {
    int sdr_id = 0;
    std::string sn;
    std::string model;
    int device_type = 0;

    AIGC_JSON_HELPER(sdr_id, sn, model, device_type)
};

struct LeafNode {
    int sdr_id = 0;
    int control_source_index = 0;
    std::string sn;
    std::string model;
    int device_type = 0;

    AIGC_JSON_HELPER(sdr_id, control_source_index, sn, model, device_type)
};

struct WirelessLinkTopo {
    std::vector<int> secret_code;
    CenterNode center_node;
    std::vector<LeafNode> leaf_nodes;

    AIGC_JSON_HELPER(secret_code, center_node, leaf_nodes)
};

struct MultiDeviceTask {
    DeviceInfo take_off_device_info;
    std::vector<DeviceInfo> landing_device_infos;
    std::vector<ConnectDeviceInfo> connect_device_infos;
    WirelessLinkTopo wireless_link_topo;

    AIGC_JSON_HELPER(take_off_device_info, landing_device_infos, connect_device_infos, wireless_link_topo)
};

struct ServicesFlighttaskExecuteSouthboundMsg {
    std::string flight_id;
    MultiDeviceTask multi_device_task;

    AIGC_JSON_HELPER(flight_id, multi_device_task)
};
///------ 用户自定义数据结构 end ------

class ServicesFlighttaskExecuteSouthbound final : public MessageV1 {
public:
    ServicesFlighttaskExecuteSouthbound();
    explicit ServicesFlighttaskExecuteSouthbound(const ServicesFlighttaskExecuteSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesFlighttaskExecuteSouthboundMsg msg;
};

}