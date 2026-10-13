// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct RequestsRequestLandingPermissionNorthboundMsg {
    std::string drone_sn;
    std::string target_dock_sn;
    std::string flight_id;
    int session_id = 0;

    AIGC_JSON_HELPER(drone_sn, target_dock_sn, flight_id, session_id)
};
///------ 用户自定义数据结构 end ------

class RequestsRequestLandingPermissionNorthbound final : public MessageV1 {
public:
    RequestsRequestLandingPermissionNorthbound();
    explicit RequestsRequestLandingPermissionNorthbound(const std::shared_ptr<MessageV1>& message);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    RequestsRequestLandingPermissionNorthboundMsg msg;
};

}