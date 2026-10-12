// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "message_v3.h"

using namespace message_engine;

MessageV3::MessageV3() {
    this->protocol_type_ = MessageProtocolType::V3;
}

MessageV3::MessageV3(uint16_t command_id, uint16_t seq, const std::shared_ptr<drc_sdk::ByteBuffer>& byte_buffer) : MessageV3() {
    this->command_id_ = static_cast<V3CommandId>(command_id);
    this->seq_ = seq;
    this->byte_buffer_ = byte_buffer;

    if (V3CommandIdMethodMap.contains(command_id_)) {
        this->method_ = V3CommandIdMethodMap[command_id_];
    } else {
        this->method_ = "unknown_method";
    }
}

std::string MessageV3::GetUuid() {
    if (!IsValidUuid()) {
        if (method_.empty()) {
            method_ = "unknown_method";
        }
        if (seq_ == 0) {
            // GetNextSeq is int64; MessageV3 seq is uint16. Avoid wrapping to 0 —
            // device IsMessageV3 / DecodeMessageV3 rejects seq==0.
            do {
                seq_ = static_cast<uint16_t>(GetNextSeq());
            } while (seq_ == 0);
        }
    }
    return "UUID/V3/" + method_ + "/" + std::to_string(seq_);
}

bool MessageV3::IsValidUuid() {
    return !method_.empty() && seq_ != 0;
}

uint16_t MessageV3::GetSeq() const {
    return seq_;
}

std::shared_ptr<drc_sdk::ByteBuffer> MessageV3::GetByteBuffer() const {
    return byte_buffer_;
}

void MessageV3::SetSeq(const std::shared_ptr<MessageV3>& other_message) {
    seq_ = other_message->GetSeq();
}

V3CommandId MessageV3::GetCommandId() const {
    return command_id_;
}
