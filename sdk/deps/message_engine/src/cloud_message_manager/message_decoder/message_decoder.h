// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message_wrapper.h"
#include "message_v1.h"
#include "message_v2.h"
#include "message_v3.h"

#include <json11.hpp>

namespace message_engine {

class MessageDecoder {
public:
    /// 从 std::string 和 ByteBuffer 中解析 MessageWrapper 消息

    static std::shared_ptr<MessageWrapper> Decode(const std::string& string_data);
    static std::shared_ptr<MessageWrapper> Decode(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data);


    /// 判断消息的协议类型

    static std::optional<MessageProtocolType> GetProtocolType(const std::string& string_data);
    static std::optional<MessageProtocolType> GetProtocolType(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data);


    /// 判断是否是 V1/V2/V3 Message

    static bool IsMessageV1(const std::string& string_data);
    static bool IsMessageV1(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data);

    static bool IsMessageV2(const std::string& string_data);
    static bool IsMessageV2(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data);

    static bool IsMessageV3(const std::string& string_data);
    static bool IsMessageV3(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data);


    /// V1/V2/V3 Message解析

    static std::shared_ptr<MessageV1> DecodeMessageV1(const std::string& string_data);
    static std::shared_ptr<MessageV1> DecodeMessageV1(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data);

    static std::shared_ptr<MessageV2> DecodeMessageV2(const std::string& string_data);
    static std::shared_ptr<MessageV2> DecodeMessageV2(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data);

    static std::shared_ptr<MessageV3> DecodeMessageV3(const std::string& string_data);
    static std::shared_ptr<MessageV3> DecodeMessageV3(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data);


    /// 辅助函数

    static bool IsValidJson(const std::string& jsonString);
};

}  // namespace message_engine