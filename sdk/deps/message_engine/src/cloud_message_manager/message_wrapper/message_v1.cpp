// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "message_v1.h"

#include "common/util/guid.h"

using namespace message_engine;


MessageV1::MessageV1() {
    this->protocol_type_ = MessageProtocolType::V1;
}

MessageV1::MessageV1(std::string topic, std::string tid, std::string bid, std::string data) : MessageV1() {
    topic_ = std::move(topic);
    tid_ = std::move(tid);
    bid_ = std::move(bid);
    data_ = std::move(data);
}

std::string MessageV1::GetUuid() {
    if (!IsValidUuid()) {
        // 生成一份固定的uuid
        if (tid_.empty()) {
            tid_ = xg::newGuid().str();
        }

        if (bid_.empty()) {
            bid_ = xg::newGuid().str();
        }
    }
    return "UUID/V1/" + tid_ + "/" + bid_;
}

std::string MessageV1::GetTopic() const {
    return topic_;
}

void MessageV1::SetTidBid(const std::shared_ptr<MessageV1>& other_message) {
    tid_ = other_message->tid_;
    bid_ = other_message->bid_;
}

void MessageV1::SetBid(const std::string& bid) {
    bid_ = bid;
}

bool MessageV1::IsValidUuid() {
    return !tid_.empty() && !bid_.empty();
}

std::string MessageV1::GetTid() const {
    return tid_;
}

std::optional<bool> MessageV1::GetIsNeedReply() const {
    return is_need_reply_;
}

std::string MessageV1::GetBid() const {
    return bid_;
}

uint64_t MessageV1::GetTimestamp() const {
    return timestamp_;
}

std::string MessageV1::GetData() const {
    return data_;
}

void MessageV1::SetTopic(std::string topic) {
    topic_ = std::move(topic);
}

void MessageV1::SetIsNeedReply(bool is_need_reply) {
    is_need_reply_ = is_need_reply;
}
