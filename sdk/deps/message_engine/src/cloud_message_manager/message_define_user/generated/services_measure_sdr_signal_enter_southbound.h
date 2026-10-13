// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct MeasureUavNode {
    double sdr_id = 0.0;
    int device_type = 0;
    int index = 0;
    int model = 0;

    AIGC_JSON_HELPER(sdr_id, device_type, index, model)
};

struct MeasureGndNodes {
    double sdr_id = 0.0;
    int device_type = 0;
    int index = 0;
    int model = 0;

    AIGC_JSON_HELPER(sdr_id, device_type, index, model)
};

struct ServicesMeasureSdrSignalEnterSouthboundMsg {
    std::string flight_id;
    int seq_num = 0;
    MeasureUavNode uav_node;
    std::vector<MeasureGndNodes> gnd_nodes;

    AIGC_JSON_HELPER(flight_id, seq_num, uav_node, gnd_nodes)
};
///------ 用户自定义数据结构 end ------

class ServicesMeasureSdrSignalEnterSouthbound final : public MessageV1 {
public:
    ServicesMeasureSdrSignalEnterSouthbound();
    explicit ServicesMeasureSdrSignalEnterSouthbound(const ServicesMeasureSdrSignalEnterSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesMeasureSdrSignalEnterSouthboundMsg msg;
};

}