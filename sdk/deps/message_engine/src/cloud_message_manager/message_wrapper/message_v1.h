// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message_wrapper.h"

#include <optional>

namespace message_engine {

/* Example:
{
    "tid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxx",
    "bid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxx",
    "timestamp": 1654070968655,
    "method": "ir_metering_area_set"
    "data": {
        "height": 0.5,
        "payload_index": "39-0-7",
        "width": 0.5,
        "x": 0.5,
        "y": 0.5
    },
}
*/

// json string, 含有bid、tid等字段的消息
class MessageV1 : public MessageWrapper {
public:
    ~MessageV1() override = default;

    MessageV1();
    MessageV1(std::string topic, std::string tid, std::string bid, std::string data);

    std::string GetUuid() override;
    bool IsValidUuid() override;
    std::string ToJsonString() override {
        return {};
    }
    drc_sdk::ByteBuffer ToBinary() override {
        return {};
    }

    [[nodiscard]] std::string GetTopic() const;
    [[nodiscard]] std::optional<bool> GetIsNeedReply() const;
    [[nodiscard]] std::string GetTid() const;
    [[nodiscard]] std::string GetBid() const;
    [[nodiscard]] uint64_t GetTimestamp() const;
    [[nodiscard]] std::string GetData() const;

    void SetIsNeedReply(bool is_need_reply);
    void SetTopic(std::string topic);
    void SetBid(const std::string& bid);
    void SetTidBid(const std::shared_ptr<MessageV1>& other_message);

protected:
    std::optional<bool> is_need_reply_;
    std::string topic_;
    std::string tid_;
    std::string bid_;
    uint64_t timestamp_ = std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count();
    std::string data_;
};

}  // namespace message_engine