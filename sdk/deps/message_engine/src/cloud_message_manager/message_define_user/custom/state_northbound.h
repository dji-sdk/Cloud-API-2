// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>
#include "drc_drc_camera_state_push_northbound.h"

namespace message_engine {

///------ 用户自定义数据结构 start ------
struct DeviceDrcCapability {
    bool support = true;

    AIGC_JSON_HELPER(support)
};

struct DeviceCapabilitySet {
    DeviceDrcCapability drc_capability;

    AIGC_JSON_HELPER(drc_capability)
};

struct StateNorthboundConnectionStateMsg {
    int state = 0;
    int time_cost = 0;

    AIGC_JSON_HELPER(state, time_cost)
};

struct StateNorthboundLiveConnectionStateMsg {
    std::string payload_index;
    int state = 0;
    int time_cost = 0;

    AIGC_JSON_HELPER(payload_index, state, time_cost)
};

struct StateNorthboundDroneConnectionStatusMsg {
    StateNorthboundConnectionStateMsg network_state;
    StateNorthboundConnectionStateMsg gateway_state;
    StateNorthboundConnectionStateMsg rtc_drc_state;
    StateNorthboundConnectionStateMsg rtc_v1_forward_state;
    std::vector<StateNorthboundLiveConnectionStateMsg> rtc_live_state;

    AIGC_JSON_HELPER(network_state, gateway_state, rtc_drc_state, rtc_v1_forward_state, rtc_live_state)
};

struct StateNorthboundMsg {
    std::optional<DeviceCapabilitySet> capability_set;
    std::optional<CameraStatePushCameraWatermarkMsg> camera_watermark_settings;
    std::optional<StateNorthboundDroneConnectionStatusMsg> drone_connection_status;
};
///------ 用户自定义数据结构 end ------

class StateNorthbound final : public MessageV1 {
public:
    StateNorthbound();
    explicit StateNorthbound(const StateNorthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    StateNorthboundMsg msg;
};

}