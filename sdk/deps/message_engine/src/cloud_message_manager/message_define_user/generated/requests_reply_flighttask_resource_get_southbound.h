// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RequestsReplyFlighttaskResourceGetSouthboundMsgOutputFile {
    std::string fingerprint;
    std::string url;

    AIGC_JSON_HELPER(fingerprint, url)
};

struct RequestsReplyFlighttaskResourceGetSouthboundMsgOutput {
    RequestsReplyFlighttaskResourceGetSouthboundMsgOutputFile file;

    AIGC_JSON_HELPER(file)
};

struct RequestsReplyFlighttaskResourceGetSouthboundMsg {
    RequestsReplyFlighttaskResourceGetSouthboundMsgOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class RequestsReplyFlighttaskResourceGetSouthbound final : public MessageV1 {
public:
    RequestsReplyFlighttaskResourceGetSouthbound();
    explicit RequestsReplyFlighttaskResourceGetSouthbound(const RequestsReplyFlighttaskResourceGetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    RequestsReplyFlighttaskResourceGetSouthboundMsg msg;
};

}