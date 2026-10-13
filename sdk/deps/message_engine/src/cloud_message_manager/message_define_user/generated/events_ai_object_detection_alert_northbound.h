// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct AiAlertLocation {
    double latitude = 0.0;
    double longitude = 0.0;
    double altitude = 0.0;

    AIGC_JSON_HELPER(latitude, longitude, altitude)
};

struct AiTargetAlertInfo {
    int target_type = 0;
    int target_value = 0;
    bool use_min_threshold = false;
    bool use_max_threshold = false;
    double target_max_threshold = 0.0;
    double target_min_threshold = 0.0;

    AIGC_JSON_HELPER(target_type, target_value, use_min_threshold, use_max_threshold, target_max_threshold, target_min_threshold)
};

struct AiTargetTypeLabel {
    int index = 0;
    std::string name;

    AIGC_JSON_HELPER(index, name)
};

struct AiTriggerAction {
    int action = 0;
    int duration = 0;

    AIGC_JSON_HELPER(action, duration)
};

struct AlertContent {
    std::string content;
    std::string title;

    AIGC_JSON_HELPER(content, title)
};

struct EventsAiObjectDetectionAlertNorthboundMsg {
    std::string flight_id;
    std::string drone_sn;
    std::string alert_uuid;
    double alert_time = 0.0;
    int stream_source = 0;
    int model_idx = 0;
    int payload_index = 0;
    double media_index = 0.0;
    AiAlertLocation alert_location;
    std::vector<AiTargetAlertInfo> target_alert_infos;
    std::vector<AiTargetTypeLabel> target_type_labels;
    std::vector<AiTriggerAction> trigger_actions;
    int waypoint_index = 0;
    int algorithm_source = 0;
    AlertContent alert_content;

    AIGC_JSON_HELPER(flight_id, drone_sn, alert_uuid, alert_time, stream_source, model_idx, payload_index, media_index, alert_location, target_alert_infos, target_type_labels, trigger_actions, waypoint_index, algorithm_source, alert_content)
};
///------ 用户自定义数据结构 end ------

class EventsAiObjectDetectionAlertNorthbound final : public MessageV1 {
public:
    EventsAiObjectDetectionAlertNorthbound();
    explicit EventsAiObjectDetectionAlertNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsAiObjectDetectionAlertNorthboundMsg msg;
};

}