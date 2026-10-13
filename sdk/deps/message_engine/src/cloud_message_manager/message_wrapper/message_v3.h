// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message_wrapper.h"

#include <unordered_map>

namespace message_engine {

/*
    压缩二进制协议格式
    little endian
    offset: 0 +--------------------------------------------------+
              |                                                  |
            1 +                V3CommandId(uint16)               +
              |                                                  |
            2 +--------------------------------------------------+
              |                                                  |
            3 +                   seq(uint16)                    +
              |                                                  |
            4 +--------------------------------------------------+
              |                     data[n]                      |
        n + 4 +--------------------------------------------------+
*/

enum class V3CommandId : uint16_t {
    UNKNOWN = 0,
    STICK_CONTROL = 1,        // 杆量控制消息，从云端推送到设备端
    STICK_CONTROL_REPLY = 2,  // 杆量控制消息回复，从设备端推送到云端
    DUML_TRANSFER = 3,        // V1消息通过网联通道传输
};

static std::unordered_map<V3CommandId, std::string> V3CommandIdMethodMap = {
    {V3CommandId::STICK_CONTROL, "stick_control"},
    {V3CommandId::STICK_CONTROL_REPLY, "stick_control"},
    {V3CommandId::DUML_TRANSFER, "duml_trans"},
};

struct MessageV3Header {
    V3CommandId command_id;
    uint16_t seq;
};

// compressed 二进制消息，小端字节序的内存数据，减少数据体积，用于杆量/duml消息转发等特殊协议
class MessageV3 : public MessageWrapper {
public:
    ~MessageV3() override = default;

    MessageV3();
    MessageV3(uint16_t command_id, uint16_t seq, const std::shared_ptr<drc_sdk::ByteBuffer>& byte_buffer);

    std::string GetUuid() override;
    bool IsValidUuid() override;
    std::string ToJsonString() override {
        return {};
    }
    drc_sdk::ByteBuffer ToBinary() override {
        return {};
    }

    [[nodiscard]] uint16_t GetSeq() const;
    [[nodiscard]] std::shared_ptr<drc_sdk::ByteBuffer> GetByteBuffer() const;
    [[nodiscard]] V3CommandId GetCommandId() const;

    void SetSeq(const std::shared_ptr<MessageV3>& other_message);

protected:
    V3CommandId command_id_ = V3CommandId::UNKNOWN;
    uint16_t seq_ = 0;
    std::shared_ptr<drc_sdk::ByteBuffer> byte_buffer_ = nullptr;
};


} // namespace message_engine