// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct OneTakeOffRtcmInfo {
    std::string mount_point;
    std::string port;
    std::string host;
    int rtcm_device_type = 0;
    int source_type = 0;

    AIGC_JSON_HELPER(mount_point, port, host, rtcm_device_type, source_type)
};

struct OneTakeOffAlternateLandPoint {
    double latitude = 0.0;
    double longitude = 0.0;
    double safe_land_height = 0.0;
    int is_configured = 0;

    AIGC_JSON_HELPER(latitude, longitude, safe_land_height, is_configured)
};

struct OneTakeOffDeviceInfo {
    double latitude = 0.0;
    double longitude = 0.0;
    double height = 0.0;
    int heading = 0;
    int home_position_is_valid = 0;
    int index = 0;
    std::string sn;
    OneTakeOffRtcmInfo rtcm_info;
    OneTakeOffAlternateLandPoint alternate_land_point;

    AIGC_JSON_HELPER(latitude, longitude, height, heading, home_position_is_valid, index, sn, rtcm_info, alternate_land_point)
};

struct OneTakeOffConnectDeviceInfo {
    int index = 0;
    std::string sn;
    OneTakeOffRtcmInfo rtcm_info;

    AIGC_JSON_HELPER(index, sn, rtcm_info)
};

struct OneTakeOffCenterNode {
    int sdr_id = 0;
    std::string sn;
    std::string model;
    int device_type = 0;

    AIGC_JSON_HELPER(sdr_id, sn, model, device_type)
};

struct OneTakeOffLeafNode {
    int sdr_id = 0;
    int control_source_index = 0;
    std::string sn;
    std::string model;
    int device_type = 0;

    AIGC_JSON_HELPER(sdr_id, control_source_index, sn, model, device_type)
};

struct OneTakeOffWirelessLinkTopo {
    std::vector<int> secret_code;
    OneTakeOffCenterNode center_node;
    std::vector<OneTakeOffLeafNode> leaf_nodes;

    AIGC_JSON_HELPER(secret_code, center_node, leaf_nodes)
};

struct OneTakeOffMultiDeviceTask {
    OneTakeOffDeviceInfo take_off_device_info;
    std::vector<OneTakeOffDeviceInfo> landing_device_infos;
    std::vector<OneTakeOffConnectDeviceInfo> connect_device_infos;
    OneTakeOffWirelessLinkTopo wireless_link_topo;

    AIGC_JSON_HELPER(take_off_device_info, landing_device_infos, connect_device_infos, wireless_link_topo)
};

struct ServicesTakeoffToPointSouthboundMsg {
    std::string flight_id;
    std::string cloud_to_cloud_id;
    int commander_flight_height = 0;
    int commander_flight_mode = 0;
    int commander_mode_lost_action = 0;
    int flight_safety_advance_check = 0;
    int max_speed = 0;
    int rc_lost_action = 0;
    int rth_altitude = 0;
    int rth_mode = 0;
    int security_takeoff_height = 0;
    double target_height = 0.0;
    double target_latitude = 0.0;
    double target_longitude = 0.0;
    OneTakeOffMultiDeviceTask multi_device_task;

    AIGC_JSON_HELPER(flight_id, cloud_to_cloud_id, commander_flight_height, commander_flight_mode, commander_mode_lost_action, flight_safety_advance_check, max_speed, rc_lost_action, rth_altitude, rth_mode, security_takeoff_height, target_height, target_latitude, target_longitude, multi_device_task)
};
///------ 用户自定义数据结构 end ------

class ServicesTakeoffToPointSouthbound final : public MessageV1 {
public:
    ServicesTakeoffToPointSouthbound();
    explicit ServicesTakeoffToPointSouthbound(const ServicesTakeoffToPointSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesTakeoffToPointSouthboundMsg msg;
};

}