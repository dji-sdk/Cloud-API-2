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
    // 停止顺序必须与依赖方向一致: IO → Compute → Main。
    // HTTP 回调链路是 IO 线程收到响应后向 Main() Post(http_runtime.cpp), 若先停 Main,
    // IO 线程在途回调执行 Main()->Post() 时会解引用已 reset 的空指针导致 SIGSEGV。
    // 先停 IO 并 Join 其线程, 保证不会再有新的回调 Post 到 Main; Main 最后停,
    // 期间已入队的残留回调仍可被正常消费执行。
    if (io_) {
        io_->Stop();
        io_.reset();
    }
    if (compute_) {
        compute_->Stop();
        compute_.reset();
    }
    if (main_) {
        // 停 Main 前先 Flush: Main 是 ThreadPoolExecutor(1,1), Flush() 投递
        // 哨兵任务并等待, 把 IO 停掉前已 Post 进来的在途 HTTP 回调(如 beacon 上报)排空执行完,
        // 再销毁; 否则这批残留回调会随 Stop 一起丢弃。Flush() 内部对"当前线程即工作线程"
        // 已有直返保护, 从 Main 线程调 shutdown 也不会死锁。
        main_->Flush();
        main_->Stop();
        main_.reset();
    }
}

}  // namespace common
}  // namespace drc_sdk
