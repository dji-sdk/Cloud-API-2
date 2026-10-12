// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct LandingPermissionOutput {
    int session_id = 0;
    bool is_permitted = false;

    AIGC_JSON_HELPER(session_id, is_permitted)
};

struct RequestsReplyRequestLandingPermissionSouthboundMsg {
    LandingPermissionOutput output;
    int result = 0;

    AIGC_JSON_HELPER(output, result)
};
///------ 用户自定义数据结构 end ------

class RequestsReplyRequestLandingPermissionSouthbound final : public MessageV1 {
public:
    RequestsReplyRequestLandingPermissionSouthbound();
    explicit RequestsReplyRequestLandingPermissionSouthbound(const RequestsReplyRequestLandingPermissionSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    RequestsReplyRequestLandingPermissionSouthboundMsg msg;
};

}