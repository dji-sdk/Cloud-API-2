// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v2.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct DrcReplyDrcAssistantVideoModeSetNorthboundMsg {
    int result = 0;

    AIGC_JSON_HELPER(result)
};
///------ 用户自定义数据结构 end ------

class DrcReplyDrcAssistantVideoModeSetNorthbound final : public MessageV2 {
public:
    DrcReplyDrcAssistantVideoModeSetNorthbound();
    explicit DrcReplyDrcAssistantVideoModeSetNorthbound(const std::shared_ptr<MessageV2>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    DrcReplyDrcAssistantVideoModeSetNorthboundMsg msg;
};

}