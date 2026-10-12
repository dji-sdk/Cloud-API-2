// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <map>
#include <sstream>
#include <string>

namespace cloud_api2 {

// 对齐原 Urllib::Decode::decode() 的实际调用路径（has_base_url = false）：
// 整段字符串百分号解码，再按 & / 第一个 = 拆参数。不按 ? 截 query。
inline std::map<std::string, std::string> parse_query(const std::string& url) {
    std::string parameters;
    for (size_t pos = 0; pos < url.size(); ++pos) {
        if (url[pos] == '%') {
            if (pos + 2 >= url.size()) {
                break;
            }
            std::istringstream iss(url.substr(pos + 1, 2));
            int value = 0;
            iss >> std::hex >> value;
            parameters += static_cast<char>(value);
            pos += 2;
            continue;
        }
        parameters += url[pos];
    }

    std::map<std::string, std::string> params;
    if (parameters.empty()) {
        return params;
    }

    std::istringstream iss(parameters);
    std::string token;
    while (std::getline(iss, token, '&')) {
        if (token.empty()) {
            continue;
        }
        const auto eq = token.find('=');
        if (eq == std::string::npos) {
            continue;
        }
        params[token.substr(0, eq)] = token.substr(eq + 1);
    }
    return params;
}

inline std::string query_param(const std::map<std::string, std::string>& params,
                               const std::string& key) {
    const auto it = params.find(key);
    return it == params.end() ? std::string() : it->second;
}

}  // namespace cloud_api2
