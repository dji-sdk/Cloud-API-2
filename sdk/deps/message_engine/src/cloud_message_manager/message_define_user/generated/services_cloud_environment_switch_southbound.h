// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct CloudEnvironmentSwitchDeviceInfo {
    std::string callsign;
    std::string device_model_key;
    std::string device_sn;

    AIGC_JSON_HELPER(callsign, device_model_key, device_sn)
};

struct CloudEnvironmentSwitchDevicesList {
    std::vector<CloudEnvironmentSwitchDeviceInfo> parents;

    AIGC_JSON_HELPER(parents)
};

struct PrivateCloudConfig {
    std::string bind_code;
    std::string org_id;
    std::string url;

    AIGC_JSON_HELPER(bind_code, org_id, url)
};

struct PublicCloudConfig {
    std::string bind_code;
    std::string org_id;
    std::string zone_id;

    AIGC_JSON_HELPER(bind_code, org_id, zone_id)
};

struct ThirdCloudConfig {
    std::string mqtt_addr;
    std::string mqtt_password;
    std::string mqtt_username;

    AIGC_JSON_HELPER(mqtt_addr, mqtt_password, mqtt_username)
};

struct CloudConfig {
    PrivateCloudConfig private_cloud_config;
    PublicCloudConfig public_cloud_config;
    ThirdCloudConfig third_cloud_config;
    int type = 0;

    AIGC_JSON_HELPER(private_cloud_config, public_cloud_config, third_cloud_config, type)
};

struct ServicesCloudEnvironmentSwitchSouthboundMsg {
    std::vector<CloudEnvironmentSwitchDevicesList> devices_list;
    CloudConfig origin_config;
    std::string switch_callback;
    CloudConfig target_config;

    AIGC_JSON_HELPER(devices_list, origin_config, switch_callback, target_config)
};
///------ 用户自定义数据结构 end ------

class ServicesCloudEnvironmentSwitchSouthbound final : public MessageV1 {
public:
    ServicesCloudEnvironmentSwitchSouthbound();
    explicit ServicesCloudEnvironmentSwitchSouthbound(const ServicesCloudEnvironmentSwitchSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesCloudEnvironmentSwitchSouthboundMsg msg;
};

}