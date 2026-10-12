// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v3.h>

namespace message_engine {

struct CompressedSticksControlSouthboundMsg {
    uint16_t roll = 1024;
    uint16_t pitch = 1024;
    uint16_t throttle = 1024;
    uint16_t yaw = 1024;
    uint16_t gimbal_pitch = 1024;
};

class CompressedSticksControlSouthbound final : public MessageV3 {
public:
    CompressedSticksControlSouthbound();
    explicit CompressedSticksControlSouthbound(const CompressedSticksControlSouthboundMsg& message);

    drc_sdk::ByteBuffer ToBinary() override;

    CompressedSticksControlSouthboundMsg msg{};
};

}