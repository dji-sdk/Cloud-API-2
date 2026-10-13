// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RelayScanResultEventExtDeviceSsid {
    std::string group_name;
    std::string device_name;

    AIGC_JSON_HELPER(group_name, device_name)
};

struct RelayScanResultEventExtDevice {
    double uav_id = 0.0;
    int signal_val = 0;
    bool is_broadcast_hide = false;
    std::string working_dev;
    bool is_relay = false;
    bool is_occupied = false;
    int passwd_mod_count = 0;
    double position_lon = 0.0;
    double position_lat = 0.0;
    RelayScanResultEventExtDeviceSsid ssid;
    bool allow_occupied = false;
    int connected_dev_type = 0;

    AIGC_JSON_HELPER(uav_id, signal_val, is_broadcast_hide, working_dev, is_relay, is_occupied, passwd_mod_count, position_lon, position_lat, ssid, allow_occupied, connected_dev_type)
};

struct RelayScanResultEventExt {
    std::vector<RelayScanResultEventExtDevice> devices;

    AIGC_JSON_HELPER(devices)
};

struct RelayScanResultEventProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct RelayScanResultEventOutput {
    RelayScanResultEventExt ext;
    RelayScanResultEventProgress progress;
    std::string status;

    AIGC_JSON_HELPER(ext, progress, status)
};

struct EventsReplyRelayScanResultNorthboundMsg {
    RelayScanResultEventOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsReplyRelayScanResultNorthbound final : public MessageV1 {
public:
    EventsReplyRelayScanResultNorthbound();
    explicit EventsReplyRelayScanResultNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyRelayScanResultNorthboundMsg msg;
};

}