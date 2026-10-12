// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
//以下通过脚本生成
struct ServicesCameraFocalLengthSetSouthboundMsg {
    std::string payload_index;
    std::string camera_type;
    int zoom_factor = 0;

    AIGC_JSON_HELPER(payload_index, camera_type, zoom_factor)
};
///------ 用户自定义数据结构 end ------

class ServicesCameraFocalLengthSetSouthbound final : public MessageV1 {
public:
    ServicesCameraFocalLengthSetSouthbound();
    explicit ServicesCameraFocalLengthSetSouthbound(const ServicesCameraFocalLengthSetSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesCameraFocalLengthSetSouthboundMsg msg;
};

}