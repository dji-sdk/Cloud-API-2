// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct SelfDiagnosticEventExtHms {
    std::string code;
    int sensor_index = 0;
    int level = 0;

    AIGC_JSON_HELPER(code, sensor_index, level)
};

struct SelfDiagnosticEventExt {
    std::vector<SelfDiagnosticEventExtHms> hms_list;
    std::string device_type;

    AIGC_JSON_HELPER(hms_list, device_type)
};

struct SelfDiagnosticEventProgress {
    int current_step = 0;
    int percent = 0;

    AIGC_JSON_HELPER(current_step, percent)
};

struct SelfDiagnosticEventOutput {
    SelfDiagnosticEventExt ext;
    SelfDiagnosticEventProgress progress;
    std::string status;

    AIGC_JSON_HELPER(ext, progress, status)
};

struct EventsReplySelfDiagnosticNorthboundMsg {
    SelfDiagnosticEventOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class EventsReplySelfDiagnosticNorthbound final : public MessageV1 {
public:
    EventsReplySelfDiagnosticNorthbound();
    explicit EventsReplySelfDiagnosticNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplySelfDiagnosticNorthboundMsg msg;
};

}