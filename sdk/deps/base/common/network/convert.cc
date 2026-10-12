// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <vector>
#include <string>
#include "network.h"
#include "injector.h"

namespace drc_sdk {
namespace common {

injector::Bytes Network::ConvertBytes(const std::string &data) {
    return std::vector<uint8_t>(data.begin(), data.end());
}

std::string Network::ConvertBytes(const injector::Bytes &data) {
    return std::string(data.begin(), data.end());
}

std::vector<injector::Bytes> Network::ConvertByteArrays(const std::vector<std::string> &certs) {
    std::vector<injector::Bytes> http_certs;
    for (auto &cert : certs) {
        http_certs.emplace_back(ConvertBytes(cert));
    }
    return http_certs;
}

std::vector<std::string> Network::ConvertByteArrays(const std::vector<injector::Bytes> &certs) {
    std::vector<std::string> http_certs;
    for (auto &cert : certs) {
        http_certs.emplace_back(ConvertBytes(cert));
    }
    return http_certs;
}

HttpHeaders Network::ConvertHeaders(const std::vector<injector::HttpHeader> &headers) {
    HttpHeaders http_headers;
    for (auto &[key, value] : headers) {
        http_headers[key] = value;
    }
    return http_headers;
}

std::vector<injector::HttpHeader> Network::ConvertHeaders(const HttpHeaders &headers) {
    std::vector<injector::HttpHeader> http_headers;
    for (auto &[key, value] : headers) {
        http_headers.push_back({
            .key = key,
            .value = value,
        });
    }
    return http_headers;
}

} // namespace common
} // namespace drc_sdk
