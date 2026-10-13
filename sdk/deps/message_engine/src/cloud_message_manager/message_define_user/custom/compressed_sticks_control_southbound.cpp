// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "compressed_sticks_control_southbound.h"
#include <cstring>

using namespace message_engine;

CompressedSticksControlSouthbound::CompressedSticksControlSouthbound() {
    this->method_ = V3CommandIdMethodMap[V3CommandId::STICK_CONTROL];
    this->command_id_ = V3CommandId::STICK_CONTROL;
}

CompressedSticksControlSouthbound::CompressedSticksControlSouthbound(const CompressedSticksControlSouthboundMsg& message) : CompressedSticksControlSouthbound() {
    msg = message;
}

drc_sdk::ByteBuffer CompressedSticksControlSouthbound::ToBinary() {
    drc_sdk::ByteBuffer buffer;
    constexpr size_t msgSize = sizeof(CompressedSticksControlSouthboundMsg);
    buffer.Resize(msgSize);
    memcpy(buffer.data(), &msg, msgSize);
    return buffer;
}