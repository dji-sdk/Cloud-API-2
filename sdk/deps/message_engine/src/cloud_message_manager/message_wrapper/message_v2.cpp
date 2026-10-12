// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "message_v2.h"

using namespace message_engine;


MessageV2::MessageV2() {
    this->protocol_type_ = MessageProtocolType::V2;
}

MessageV2::MessageV2(uint64_t seq, std::string method, std::string data) : MessageV2() {
    seq_ = seq;
    method_ = std::move(method);
    data_ = std::move(data);
}

uint64_t MessageV2::GetSeq() const {
    return seq_;
}

std::string MessageV2::GetData() const {
    return data_;
}

std::string MessageV2::GetUuid() {
    if (!IsValidUuid()) {
        if (method_.empty()) {
            method_ = "unknown_method";
        }
        if (seq_ == 0) {
            seq_ = GetNextSeq();
        }
    }
    return "UUID/V2/" + method_ + "/" + std::to_string(seq_);
}

void MessageV2::SetSeq(const uint64_t& seq) {
    seq_ = seq;
}

bool MessageV2::IsValidUuid() {
    return !method_.empty() && seq_ != 0;
}
