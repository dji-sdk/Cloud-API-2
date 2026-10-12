// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct InFlightWaylineFile {
    std::string fingerprint;
    std::string url;

    AIGC_JSON_HELPER(fingerprint, url)
};

struct ServicesInFlightWaylineDeliverSouthboundMsg {
    int out_of_control_action = 0;
    InFlightWaylineFile file;
    std::string in_flight_wayline_id;
    int rth_altitude = 0;
    int rth_mode = 0;
    int wayline_precision_type = 0;
    std::string wayline_uuid;
    std::string wayline_name;
    bool fast_reconstruction = false;

    AIGC_JSON_HELPER(out_of_control_action, file, in_flight_wayline_id, rth_altitude, rth_mode, wayline_precision_type, wayline_uuid, wayline_name, fast_reconstruction)
};
///------ 用户自定义数据结构 end ------

class ServicesInFlightWaylineDeliverSouthbound final : public MessageV1 {
public:
    ServicesInFlightWaylineDeliverSouthbound();
    explicit ServicesInFlightWaylineDeliverSouthbound(const ServicesInFlightWaylineDeliverSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesInFlightWaylineDeliverSouthboundMsg msg;
};

}