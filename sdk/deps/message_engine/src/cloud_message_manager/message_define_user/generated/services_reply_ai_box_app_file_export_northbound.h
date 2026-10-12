// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct AiBoxAppFileExportOutput {
    std::string sn;

    AIGC_JSON_HELPER(sn)
};

struct ServicesReplyAiBoxAppFileExportNorthboundMsg {
    int result = 0;
    AiBoxAppFileExportOutput output;

    AIGC_JSON_HELPER(result, output)
};
///------ 用户自定义数据结构 end ------

class ServicesReplyAiBoxAppFileExportNorthbound final : public MessageV1 {
public:
    ServicesReplyAiBoxAppFileExportNorthbound();
    explicit ServicesReplyAiBoxAppFileExportNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesReplyAiBoxAppFileExportNorthboundMsg msg;
};

}