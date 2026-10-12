// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct AiModelsConfigSet {
    int stream_type = 0;
    std::vector<int> models;

    AIGC_JSON_HELPER(stream_type, models)
};

struct DrcDrcAiModelsConfigSouthboundMsg {
    std::vector<AiModelsConfigSet> ai_models_config;

    AIGC_JSON_HELPER(ai_models_config)
};
///------ 用户自定义数据结构 end ------

class DrcDrcAiModelsConfigSouthbound final : public MessageV2 {
public:
    DrcDrcAiModelsConfigSouthbound();
    explicit DrcDrcAiModelsConfigSouthbound(const DrcDrcAiModelsConfigSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcDrcAiModelsConfigSouthboundMsg msg;
};

}