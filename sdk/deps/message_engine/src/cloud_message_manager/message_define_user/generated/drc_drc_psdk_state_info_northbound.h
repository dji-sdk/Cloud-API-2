// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcUpDrcPsdkStateInfoNorthboundSpeakerMsg {
    int work_mode = 0;
    int play_mode = 0;
    int system_state = 0;
    int play_volume = 0;
    std::string play_file_name;
    std::string play_file_md5;
    int tts_volume = 0;
    int tts_type = 0;
    int tts_language = 0;
    int tts_speed = 0;

    AIGC_JSON_HELPER(work_mode, play_mode, system_state, play_volume, play_file_name, play_file_md5, tts_volume, tts_type, tts_language, tts_speed)
};

struct DrcUpDrcPsdkStateInfoNorthboundLightMsg {
    int work_mode = 0;
    int lighting_type = 0;
    int brightness = 0;
    int calibration_status = 0;
    int calibration_progress = 0;
    double left_value = 0.0;
    double right_value = 0.0;
    bool wide_field_mode = false;
    bool light_gimbal_control = false;

    AIGC_JSON_HELPER(work_mode, lighting_type, brightness, calibration_status, calibration_progress, left_value, right_value, wide_field_mode, light_gimbal_control)
};

struct DrcUpDrcPsdkWidgetValueMsg {
    int index = 0;
    int value = 0;

    AIGC_JSON_HELPER(index, value)
};

struct DrcDrcPsdkStateInfoNorthboundMsg {
    int psdk_index = 0;
    int psdk_type = 0;
    std::string psdk_name;
    std::string psdk_sn;
    std::string psdk_version;
    std::string psdk_lib_version;
    int psdk_restrict_state = 0;
    DrcUpDrcPsdkStateInfoNorthboundSpeakerMsg speaker;
    DrcUpDrcPsdkStateInfoNorthboundLightMsg light;
    std::vector<DrcUpDrcPsdkWidgetValueMsg> values;

    AIGC_JSON_HELPER(psdk_index, psdk_type, psdk_name, psdk_sn, psdk_version, psdk_lib_version, psdk_restrict_state, speaker, light, values)
};
///------ 用户自定义数据结构 end ------

class DrcDrcPsdkStateInfoNorthbound final : public MessageV2 {
public:
    DrcDrcPsdkStateInfoNorthbound();
    explicit DrcDrcPsdkStateInfoNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcPsdkStateInfoNorthboundMsg msg;
};

}