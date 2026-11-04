// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include <AIGCJson.hpp>

namespace message_engine {

///------ 用户自定义数据结构 start ------
struct ServicesCustomDataTransmissionToPsdkSouthboundMsg {
    std::string value;

    AIGC_JSON_HELPER(value)
};
///------ 用户自定义数据结构 end ------

class ServicesCustomDataTransmissionToPsdkSouthbound final : public MessageV1 {
public:
    ServicesCustomDataTransmissionToPsdkSouthbound();
    explicit ServicesCustomDataTransmissionToPsdkSouthbound(const ServicesCustomDataTransmissionToPsdkSouthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    ServicesCustomDataTransmissionToPsdkSouthboundMsg msg;
};

}
