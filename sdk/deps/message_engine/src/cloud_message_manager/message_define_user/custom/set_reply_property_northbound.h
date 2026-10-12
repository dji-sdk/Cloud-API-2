// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v1.h>
#include "set_property_southbound.h"

namespace message_engine {

struct SetReplyPropertyNorthboundMsg {
    PropertyType type = PropertyType::UNKNOWN;
    int result = 0;
};

class SetReplyPropertyNorthbound final : public MessageV1 {
public:
    SetReplyPropertyNorthbound();
    explicit SetReplyPropertyNorthbound(const SetReplyPropertyNorthboundMsg& msg);

    std::string ToJsonString() override;
    drc_sdk::ByteBuffer ToBinary() override;

    SetReplyPropertyNorthboundMsg msg;
};

}