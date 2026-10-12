// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <base/io/byte_buffer.h>
#include <chrono>
#include <string>
#include <atomic>

namespace message_engine {

using SeqType = int64_t;

// V2/V3 Message 中使用的seq字段生成
static SeqType GetNextSeq() {
    static std::atomic<SeqType> seq_{0};
    return ++seq_;
}

enum class MessageProtocolType {
    V1 = 0, // json string, 含有bid、tid等字段
    V2 = 1, // json string，data字段中含有seq的消息
    V3 = 2, // compressed 二进制消息，小端字节序的内存数据，减少数据体积，用于杆量/duml消息转发等特殊协议
};


enum class MessagePriority {
    PRIORITY_NORMAL = 5,
    PRIORITY_HIGH = 10,
};

constexpr auto TopicUserType = "gnd";

class MessageWrapper {
public:
    virtual ~MessageWrapper() = default;

    virtual std::string GetUuid() = 0;
    virtual bool IsValidUuid() = 0;
    virtual std::string ToJsonString() = 0;
    virtual drc_sdk::ByteBuffer ToBinary() = 0;

    [[nodiscard]] uint8_t GetVersion() const {
        return version_;
    }

    [[nodiscard]] MessageProtocolType GetMessageProtocolType() const {
        return protocol_type_;
    }

    [[nodiscard]] std::string GetPeerId() const {
        return peer_id_;
    }

    void SetPeerId(const std::string &id) {
        peer_id_ = id;
    }

    std::string GetMethod() {
        return method_;
    }

    void SetMethod(const std::string &method) {
        method_ = method;
    }

    [[nodiscard]] std::string GetRawData() const {
        return raw_data_;
    }

    void SetRawData(const std::string& raw_data) {
        raw_data_ = raw_data;
    }

    int remaining_send_times_ = 1; // 消息剩余发送次数，默认发送1次，即没有重试
    uint32_t send_interval_millisecond = 3000; // 消息发送间隔(单位:毫秒)
    std::chrono::steady_clock::time_point next_send_time = std::chrono::steady_clock::now(); // 消息下一次发送时间
    MessagePriority priority = MessagePriority::PRIORITY_NORMAL;

protected:
    uint8_t version_ = 1; // 数据结构版本号
    std::string peer_id_;
    std::string uuid_;
    std::string method_;
    MessageProtocolType protocol_type_ = MessageProtocolType::V1;
    std::string raw_data_;
};

} // namespace message_engine