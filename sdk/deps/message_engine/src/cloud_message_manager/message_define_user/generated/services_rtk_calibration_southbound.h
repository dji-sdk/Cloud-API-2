// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RtkCalibrationDeviceData {
    std::string account;
    std::string host;
    std::string mountPoint;
    std::string mount_point;
    std::string password;
    int port = 0;
    int version = 0;
    double height = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;

    AIGC_JSON_HELPER(account, host, mountPoint, mount_point, password, port, version, height, latitude, longitude)
};

struct RtkCalibrationDevice {
    RtkCalibrationDeviceData data;
    std::string module;
    std::string sn;
    int type = 0;

    AIGC_JSON_HELPER(data, module, sn, type)
};

struct ServicesRtkCalibrationSouthboundMsg {
    std::vector<RtkCalibrationDevice> devices;

    AIGC_JSON_HELPER(devices)
};
///------ 用户自定义数据结构 end ------

class ServicesRtkCalibrationSouthbound final : public MessageV1 {
public:
    ServicesRtkCalibrationSouthbound();
    explicit ServicesRtkCalibrationSouthbound(const ServicesRtkCalibrationSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesRtkCalibrationSouthboundMsg msg;
};

}