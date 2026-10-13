// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <rapidjson/document.h>

namespace notification_center {
    inline bool isValidJson(const std::string &str) {
        rapidjson::Document doc;
        if (doc.Parse(str.c_str()).HasParseError()) {
            return false;
        }
        return true;
    }
}
