// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <vector>
#include "AIGCJson.hpp"

namespace flight_hub_sdk {

/// 原样保存任意 JSON 值（对象/数组/标量）的字符串。
/// 用于后端字段类型不固定、但需要原样透传给调用方的场景。
struct RawJsonString {
    std::string value;

    bool AIGCJsonToObject(aigc::JsonHelperPrivate&, rapidjson::Value& jsonValue,
                          std::vector<std::string>&) {
        rapidjson::StringBuffer buffer;
        rapidjson::Writer<rapidjson::StringBuffer> writer(buffer);
        jsonValue.Accept(writer);
        value = buffer.GetString();
        return true;
    }

    bool AIGCObjectToJson(aigc::JsonHelperPrivate&, rapidjson::Value& jsonValue,
                          rapidjson::Document::AllocatorType& allocator,
                          std::vector<std::string>&) {
        rapidjson::Document doc;
        doc.Parse(value.c_str());
        if (!doc.HasParseError()) {
            jsonValue.CopyFrom(doc, allocator);
        } else {
            jsonValue.SetString(value.c_str(), static_cast<rapidjson::SizeType>(value.size()), allocator);
        }
        return true;
    }
};

}  // namespace flight_hub_sdk
