// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <cstddef>
#include <string>

namespace drc_sdk {
class ThreadUtil {
 public:
  static uint64_t GetThreadId();
  static void SetCurrentThreadName(const std::string& name);
  static std::string GetCurrentThreadName();
};
}  // namespace drc_sdk
