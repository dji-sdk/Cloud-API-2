// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <mutex>

#include "base/async/executor.h"

namespace drc_sdk {

class TimerImpl;

/// 一个最简单的timer实现
class Timer {
 public:
    Timer() = default;
    ~Timer();
    void Start(const std::shared_ptr<Executor>& executor, std::function<void()>&& func, std::chrono::steady_clock::duration timeout, bool persistent = false);
    void Reset();
    void Cancel();

 private:
    std::shared_ptr<TimerImpl> impl_;
    DISALLOW_COPY_AND_ASSIGN(Timer);
};
}  // namespace drc_sdk
