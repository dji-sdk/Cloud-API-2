// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "cpp_uuid.h"

#include <string>
#include <utility>

namespace xg {

// 请求号 / 消息 id 的薄封装。生成走 cloud_api2::generate_uuid_v4()（RFC 4122 v4）。
// 保留 xg::newGuid().str()，避免改 http_interface / message_v1 的上百处调用点。
class Guid {
public:
    const std::string& str() const { return value_; }
    operator std::string() const { return value_; }

private:
    friend Guid newGuid();
    explicit Guid(std::string value) : value_(std::move(value)) {}
    std::string value_;
};

inline Guid newGuid() {
    return Guid(cloud_api2::generate_uuid_v4());
}

}  // namespace xg
