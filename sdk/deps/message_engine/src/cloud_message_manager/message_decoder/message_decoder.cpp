// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "message_decoder.h"

#include <base/common/logger.h>
#include <unordered_set>

using namespace message_engine;

// 没有按照标准协议格式定义的历史包袱
std::unordered_set<std::string> black_list_method = {
    "heart_beat",
};

std::shared_ptr<MessageWrapper> MessageDecoder::Decode(const std::string& string_data) {
    auto protocol_type = GetProtocolType(string_data);
    if (protocol_type.has_value() == false) {
        return nullptr;
    }
    switch (protocol_type.value()) {
        case MessageProtocolType::V1:
            return DecodeMessageV1(string_data);
        case MessageProtocolType::V2:
            return DecodeMessageV2(string_data);
        case MessageProtocolType::V3:
            return DecodeMessageV3(string_data);
    }
    return nullptr;
}

std::shared_ptr<MessageWrapper> MessageDecoder::Decode(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data) {
    auto protocol_type = GetProtocolType(buffer_data);
    if (protocol_type.has_value() == false) {
        return nullptr;
    }
    switch (protocol_type.value()) {
        case MessageProtocolType::V1:
            return DecodeMessageV1(buffer_data);
        case MessageProtocolType::V2:
            return DecodeMessageV2(buffer_data);
        case MessageProtocolType::V3:
            return DecodeMessageV3(buffer_data);
    }
    return nullptr;
}

std::optional<MessageProtocolType> MessageDecoder::GetProtocolType(const std::string& string_data) {
    if (IsMessageV1(string_data)) {
        return MessageProtocolType::V1;
    }
    if (IsMessageV2(string_data)) {
        return MessageProtocolType::V2;
    }
    if (IsMessageV3(string_data)) {
        return MessageProtocolType::V3;
    }
    return {};
}

std::optional<MessageProtocolType> MessageDecoder::GetProtocolType(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data) {
    if (IsMessageV1(buffer_data)) {
        return MessageProtocolType::V1;
    }
    if (IsMessageV2(buffer_data)) {
        return MessageProtocolType::V2;
    }
    if (IsMessageV3(buffer_data)) {
        return MessageProtocolType::V3;
    }
    return {};
}

bool MessageDecoder::IsMessageV1(const std::string& string_data) {
    if (IsValidJson(string_data) == false) {
        return false;
    }
    std::string err;
    json11::Json json = json11::Json::parse(string_data, err);
    if (json["tid"].is_string() && json["bid"].is_string()) {
        return true;
    }
    return false;
}

bool MessageDecoder::IsMessageV1(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data) {
    std::string string_data(buffer_data->data(), buffer_data->size());
    return IsMessageV1(string_data);
}

bool MessageDecoder::IsMessageV2(const std::string& string_data) {
    if (IsValidJson(string_data) == false) {
        return false;
    }
    std::string err;
    json11::Json json = json11::Json::parse(string_data, err);

    // 历史包袱, 加白名单
    if (json["method"].is_string() && black_list_method.contains(json["method"].string_value())) {
        return true;
    }

    if (json["seq"].is_number() && json["method"].is_string()) {
        return true;
    }
    return false;
}

bool MessageDecoder::IsMessageV2(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data) {
    std::string string_data(buffer_data->data(), buffer_data->size());
    return IsMessageV2(string_data);
}

bool MessageDecoder::IsMessageV3(const std::string& string_data) {
    auto buffer_data = std::make_shared<drc_sdk::ByteBuffer>();
    buffer_data->Write(string_data.data(), string_data.size());
    return IsMessageV3(buffer_data);
}

bool MessageDecoder::IsMessageV3(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data) {
    std::string string_data(buffer_data->data(), buffer_data->size());
    if (IsValidJson(string_data) == true) {
        return false;
    }
    if (buffer_data->size() < sizeof(MessageV3Header)) {
        return false;
    }
    const auto v3_header_ptr = reinterpret_cast<MessageV3Header*>(buffer_data->data());
    auto command_id = v3_header_ptr->command_id;
    auto seq = v3_header_ptr->seq;
    if (command_id != V3CommandId::UNKNOWN && seq != 0) {
        return true;
    }
    return false;
}

std::shared_ptr<MessageV1> MessageDecoder::DecodeMessageV1(const std::string& string_data) {
    if (IsValidJson(string_data) == false) {
        return nullptr;
    }
    std::string err;
    json11::Json json = json11::Json::parse(string_data, err);
    if (json["tid"].is_string() && json["bid"].is_string()) {
        std::string tid = json["tid"].string_value();
        std::string bid = json["bid"].string_value();
        auto json_data = json["data"];
        auto data_string = json11::Json(json_data).dump();
        auto v1_message = std::make_shared<MessageV1>("", tid, bid, data_string);
        v1_message->SetRawData(string_data);
        return v1_message;
    }
    return nullptr;
}

std::shared_ptr<MessageV1> MessageDecoder::DecodeMessageV1(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data) {
    std::string string_data(buffer_data->data(), buffer_data->size());
    return DecodeMessageV1(string_data);
}

std::shared_ptr<MessageV2> MessageDecoder::DecodeMessageV2(const std::string& string_data) {
    if (IsValidJson(string_data) == false) {
        return nullptr;
    }
    std::string err;
    json11::Json json = json11::Json::parse(string_data, err);
    if (json["seq"].is_number() && json["method"].is_string()) {
        std::string method = json["method"].string_value();
        uint64_t seq = static_cast<uint64_t>(json["seq"].number_value());
        auto json_data = json["data"];
        auto data_string = json11::Json(json_data).dump();
        auto v2_message = std::make_shared<MessageV2>(seq, method, data_string);
        v2_message->SetRawData(string_data);
        LOG_INFO("[MessageDecoder] DecodeMessageV2, method: %s, raw_data: %s", method.c_str(), string_data.c_str());
        return v2_message;
    } else if (json["method"].is_string() && black_list_method.contains(json["method"].string_value())) {
        std::string method = json["method"].string_value();
        uint64_t seq = 0;
        auto json_data = json["data"];
        auto data_string = json11::Json(json_data).dump();
        auto v2_message = std::make_shared<MessageV2>(seq, method, data_string);
        return v2_message;
    }
    return nullptr;
}

std::shared_ptr<MessageV2> MessageDecoder::DecodeMessageV2(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data) {
    std::string string_data(buffer_data->data(), buffer_data->size());
    return DecodeMessageV2(string_data);
}

std::shared_ptr<MessageV3> MessageDecoder::DecodeMessageV3(const std::string& string_data) {
    auto buffer_data = std::make_shared<drc_sdk::ByteBuffer>();
    buffer_data->Write(string_data.data(), string_data.size());
    return DecodeMessageV3(buffer_data);
}

std::shared_ptr<MessageV3> MessageDecoder::DecodeMessageV3(const std::shared_ptr<drc_sdk::ByteBuffer>& buffer_data) {
    if (buffer_data->size() < sizeof(MessageV3Header)) {
        return nullptr;
    }
    const auto v3_header_ptr = reinterpret_cast<MessageV3Header*>(buffer_data->data());
    auto command_id = v3_header_ptr->command_id;
    auto seq = v3_header_ptr->seq;
    if (command_id != V3CommandId::UNKNOWN && seq != 0) {
        // 是合法的V3 Message
        auto body_buffer = std::make_shared<drc_sdk::ByteBuffer>();
        body_buffer->Write(buffer_data->data() + sizeof(MessageV3Header), buffer_data->size() - sizeof(MessageV3Header));
        auto v3_message = std::make_shared<MessageV3>(static_cast<uint16_t>(command_id), seq, body_buffer);
        return v3_message;
    }
    return nullptr;
}

bool MessageDecoder::IsValidJson(const std::string& jsonString) {
    std::string err;
    json11::Json json = json11::Json::parse(jsonString, err);
    return err.empty();
}