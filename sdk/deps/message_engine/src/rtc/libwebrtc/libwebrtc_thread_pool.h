// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "rtc_base/thread.h"
#include <memory>
#include <mutex>
#include <atomic>

namespace message_engine {

// WebRTC共享线程池管理器
// 所有WebRTC客户端共享同一组线程，避免线程泄漏
class LibwebrtcThreadPool {
public:
    // 获取单例实例
    static LibwebrtcThreadPool& GetInstance();
    
    // 获取共享线程（引用计数管理）
    rtc::Thread* GetNetworkThread();
    rtc::Thread* GetWorkerThread();
    rtc::Thread* GetSignalingThread();
    
    // 释放线程引用（仅用于引用计数管理，线程不会停止）
    void ReleaseThreads();
    
    // 获取当前引用计数（用于调试）
    int GetRefCount() const { return ref_count_.load(); }
    
    // 初始化线程池（如果还未初始化）
    bool Initialize();
    
    // 清理线程池（仅用于日志记录，线程不会析构）
    // 注意：线程池的线程创建后不会析构，会一直保留到程序退出
    // 这样可以避免线程析构时的竞态条件和崩溃问题
    void Cleanup();
    
    // 检查是否已初始化（用于SSL等资源的初始化检查）
    bool IsInitialized() const { 
        return initialized_; 
    }

private:
    LibwebrtcThreadPool() = default;
    ~LibwebrtcThreadPool();
    LibwebrtcThreadPool(const LibwebrtcThreadPool&) = delete;
    LibwebrtcThreadPool& operator=(const LibwebrtcThreadPool&) = delete;
    
    // 内部初始化实现，不获取锁（由调用者保证线程安全）
    bool InitializeInternal();
    
    std::mutex mutex_;
    std::atomic<int> ref_count_{0};
    
    std::unique_ptr<rtc::Thread> network_thread_;
    std::unique_ptr<rtc::Thread> worker_thread_;
    std::unique_ptr<rtc::Thread> signaling_thread_;
    
    bool initialized_{false};
};

} // namespace message_engine

