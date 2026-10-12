// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <message_v3.h>

namespace message_engine {

struct CompressedSticksControlNorthboundMsg {
    uint8_t ret_code = 0;
};

class CompressedSticksControlNorthbound final : public MessageV3 {
public:
    CompressedSticksControlNorthbound() = default;
    explicit CompressedSticksControlNorthbound(const std::shared_ptr<MessageV3>& message);

    drc_sdk::ByteBuffer ToBinary() override;

    CompressedSticksControlNorthboundMsg msg{};
};

}