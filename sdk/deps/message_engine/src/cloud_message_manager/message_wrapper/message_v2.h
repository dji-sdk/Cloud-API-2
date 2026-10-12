// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message_wrapper.h"

namespace message_engine {

/* Example:
{
    "method": "drone_control"
    "data": {
        "seq": 1,
        "h": 2.76,
        "w": 2.86,
        "x": 2.34,
        "y": -2.45
    },
}
*/

// json string，data字段中含有seq的消息
class MessageV2 : public MessageWrapper {
public:
    ~MessageV2() override = default;

    MessageV2();
    MessageV2(uint64_t seq, std::string method, std::string data);

    std::string GetUuid() override;
    bool IsValidUuid() override;
    std::string ToJsonString() override {
        return {};
    }
    drc_sdk::ByteBuffer ToBinary() override {
        return {};
    }

    static std::string GetDownTopic() {
        return "thing/product/{device_sn}/drc/down";
    }

    static std::string GetUpTopic() {
        return "thing/product/{device_sn}/drc/up";
    }

    [[nodiscard]] uint64_t GetSeq() const;
    [[nodiscard]] std::string GetData() const;

    void SetSeq(const uint64_t& seq);

protected:
    uint64_t seq_ = 0;
    std::string data_;
};

} // namespace message_engine