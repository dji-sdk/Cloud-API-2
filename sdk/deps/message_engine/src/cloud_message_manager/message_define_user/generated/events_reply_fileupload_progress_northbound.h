// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct LogFileProgress {
    int current_step = 0;
    int progress = 0;
    std::string status;
    int total_step = 0;
    int upload_rate = 0;

    AIGC_JSON_HELPER(current_step, progress, status, total_step, upload_rate)
};

struct LogFileInfo {
    std::string device_sn;
    std::string key;
    std::string module;
    LogFileProgress progress;
    double size = 0.0;

    AIGC_JSON_HELPER(device_sn, key, module, progress, size)
};

struct LogExtInfo {
    std::vector<LogFileInfo> files;

    AIGC_JSON_HELPER(files)
};

struct LogOutput {
    LogExtInfo ext;
    std::string status;

    AIGC_JSON_HELPER(ext, status)
};

struct EventsReplyFileuploadProgressNorthboundMsg {
    LogOutput output;

    AIGC_JSON_HELPER(output)
};
///------ 用户自定义数据结构 end ------

class EventsReplyFileuploadProgressNorthbound final : public MessageV1 {
public:
    EventsReplyFileuploadProgressNorthbound();
    explicit EventsReplyFileuploadProgressNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    EventsReplyFileuploadProgressNorthboundMsg msg;
};

}