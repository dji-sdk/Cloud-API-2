// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "base/async/executor.h"

namespace drc_sdk {
namespace common {

class Executors {
 public:
    Executors() = delete;

    static void Init();
    static void Shutdown();
    static std::shared_ptr<Executor>& Main();
    static std::shared_ptr<Executor>& Compute();
    static std::shared_ptr<Executor>& IO();

 private:
    static std::shared_ptr<Executor> main_;
    static std::shared_ptr<Executor> compute_;
    static std::shared_ptr<Executor> io_;
};

}  // namespace common
}  // namespace drc_sdk
