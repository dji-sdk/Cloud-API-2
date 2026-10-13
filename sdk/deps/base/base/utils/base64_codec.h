// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>

namespace drc_sdk {
std::string base64_encode(const std::string& str);
std::string base64_encode(const void* input, size_t length);
std::string base64_decode(const std::string& str);
std::string base64_decode(const void* input, size_t length);
}  // namespace drc_sdk
