// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RelayPasswdHashOutputExt {
    std::string result_value;

    AIGC_JSON_HELPER(result_value)
};

struct RelayPasswdHashOutput {
    std::string status;
    RelayPasswdHashOutputExt ext;

    AIGC_JSON_HELPER(status, ext)
};

struct ServicesReplyRelayPasswdHashNorthboundMsg {
    int result = 0;
    RelayPasswdHashOutput output;

    AIGC_JSON_HELPER(result, output)
};
///------ 用户自定义数据结构 end ------

class ServicesReplyRelayPasswdHashNorthbound final : public MessageV1 {
public:
    ServicesReplyRelayPasswdHashNorthbound();
    explicit ServicesReplyRelayPasswdHashNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesReplyRelayPasswdHashNorthboundMsg msg;
};

}