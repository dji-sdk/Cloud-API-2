// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct AiSpotlightZoomStatus {
    int state = 0;
    int state_reason = 0;

    AIGC_JSON_HELPER(state, state_reason)
};

struct AiModelItem {
    int index = 0;
    std::string signed_name;
    std::string uuid;

    AIGC_JSON_HELPER(index, signed_name, uuid)
};

struct AiModelsConfig {
    int stream_type = 0;
    std::vector<int> models;

    AIGC_JSON_HELPER(stream_type, models)
};

struct AiModelsStatus {
    int index = 0;
    int light_type = 0;
    int score = 0;
    int score_mode = 0;
    std::vector<int> filters;

    AIGC_JSON_HELPER(index, light_type, score, score_mode, filters)
};

struct AiModelLabel {
    int index = 0;
    std::string name;

    AIGC_JSON_HELPER(index, name)
};

struct AiModelsInfo {
    int index = 0;
    std::vector<int> image_source;
    std::vector<int> digital_effect;
    std::vector<AiModelLabel> labels;

    AIGC_JSON_HELPER(index, image_source, digital_effect, labels)
};

struct SelectedAiModel {
    int index = 0;
    int score = 0;
    int score_mode = 0;
    std::vector<int> image_source;
    std::vector<int> digital_effect;
    std::vector<int> filters;
    std::vector<AiModelLabel> labels;

    AIGC_JSON_HELPER(index, score, score_mode, image_source, digital_effect, filters, labels)
};

struct AiWaylineStatus {
    bool sequence_shot = false;
    bool wait_control = false;
    bool record = false;
    bool normal_shot = false;
    int count_down_time = 0;
    std::string alert_uuid;

    AIGC_JSON_HELPER(sequence_shot, wait_control, record, normal_shot, count_down_time, alert_uuid)
};

struct DrcDrcAiInfoPushNorthboundMsg {
    int identify_on = 0;
    int spotlight_zoom_on = 0;
    AiSpotlightZoomStatus ai_spotlight_zoom;
    std::vector<AiModelItem> ai_model_list;
    std::vector<AiModelsConfig> ai_models_config;
    std::vector<AiModelsStatus> selected_ai_models_status;
    std::vector<AiModelsInfo> ai_models_infos;
    SelectedAiModel selected_ai_model;
    AiWaylineStatus ai_wayline_state;

    AIGC_JSON_HELPER(identify_on, spotlight_zoom_on, ai_spotlight_zoom, ai_model_list, ai_models_config, selected_ai_models_status, ai_models_infos, selected_ai_model, ai_wayline_state)
};
///------ 用户自定义数据结构 end ------

class DrcDrcAiInfoPushNorthbound final : public MessageV2 {
public:
    DrcDrcAiInfoPushNorthbound();
    explicit DrcDrcAiInfoPushNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcAiInfoPushNorthboundMsg msg;
};

}