// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "executors.h"

namespace drc_sdk {
namespace common {

std::shared_ptr<Executor> Executors::main_ = nullptr;
std::shared_ptr<Executor> Executors::compute_ = nullptr;
std::shared_ptr<Executor> Executors::io_ = nullptr;

void Executors::Init() {
    if (main_){
        return;
    }
    
#ifdef __EMSCRIPTEN__
    // Emscripten 环境：使用单线程事件执行器（不使用 std::thread，避免 pthread 依赖）
    main_ = Executor::CreateEmscriptenEventExecutor("drc-sdk-main", true);
    compute_ = Executor::CreateEmscriptenEventExecutor("drc-sdk-compute");
    io_ = Executor::CreateEmscriptenEventExecutor("drc-sdk-io"); // IO 也改为单线程事件执行器
#else
    // 原生环境（Java SDK）：使用原来的实现
    main_ = Executor::CreateSingleThreadExecutor("drc-sdk-main", true);
    compute_ = Executor::CreateSingleThreadExecutor("drc-sdk-compute");
    // 网络并发请求需要多线程处理
    io_ = Executor::CreateThreadPoolExecutor(ThreadPoolParam{2, 16, 10000, true, "drc-sdk-io"});
#endif
}

std::shared_ptr<Executor>& Executors::Main() {
    return main_;
}

std::shared_ptr<Executor>& Executors::Compute() {
    return compute_;
}

std::shared_ptr<Executor>& Executors::IO() {
    return io_;
}

void Executors::Shutdown() {
    if (main_) {
        main_->Stop();
        main_.reset();
    }
    if (compute_) {
        compute_->Stop();
        compute_.reset();
    }
    if (io_) {
        io_->Stop();
        io_.reset();
    }
}

}  // namespace common
}  // namespace drc_sdk
