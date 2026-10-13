// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "compressed_sticks_control_northbound.h"
#include "base/common/logger.h"

using namespace message_engine;

CompressedSticksControlNorthbound::CompressedSticksControlNorthbound(const std::shared_ptr<MessageV3>& message) {
    this->seq_ = message->GetSeq();

    const auto byteBuffer = message->GetByteBuffer();
    constexpr size_t expectedSize = sizeof(CompressedSticksControlNorthboundMsg);

    if (byteBuffer->size() < expectedSize) {
        LOG_ERROR("CompressedSticksControlNorthbound error: byteBuffer->size() < expectedSize, expectedSize: %d, byteBuffer: %s", expectedSize, byteBuffer->ToString().c_str());
        return;
    }
    memcpy(&msg, byteBuffer->data(), expectedSize);
}

drc_sdk::ByteBuffer CompressedSticksControlNorthbound::ToBinary() {
    drc_sdk::ByteBuffer buffer;
    constexpr size_t msgSize = sizeof(CompressedSticksControlNorthboundMsg);
    buffer.Resize(msgSize);
    memcpy(buffer.data(), &msg, msgSize);
    return buffer;
}