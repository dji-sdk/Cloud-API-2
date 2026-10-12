// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "libwebrtc_thread_pool.h"
#include "base/common/logger.h"
#include <thread>
#include <chrono>

namespace message_engine {

LibwebrtcThreadPool& LibwebrtcThreadPool::GetInstance() {
    static LibwebrtcThreadPool instance;
    return instance;
}

bool LibwebrtcThreadPool::Initialize() {
    std::lock_guard<std::mutex> lock(mutex_);
    return InitializeInternal();
}

// 内部实现，不获取锁（由调用者保证线程安全）
bool LibwebrtcThreadPool::InitializeInternal() {
    if (initialized_) {
        return true;
    }
    
    LOG_INFO("[LibWebRtc][ThreadPool] 初始化共享线程池（首次初始化）...");
    
    // 创建共享线程
    network_thread_ = rtc::Thread::CreateWithSocketServer();
    worker_thread_ = rtc::Thread::Create();
    signaling_thread_ = rtc::Thread::Create();
    
    if (!network_thread_ || !worker_thread_ || !signaling_thread_) {
        LOG_ERROR("[LibWebRtc][ThreadPool] 创建共享线程失败");
        network_thread_.reset();
        worker_thread_.reset();
        signaling_thread_.reset();
        return false;
    }
    
    // 设置线程名称
    network_thread_->SetName("WebRTC-Shared-Network", network_thread_.get());
    worker_thread_->SetName("WebRTC-Shared-Worker", worker_thread_.get());
    signaling_thread_->SetName("WebRTC-Shared-Signaling", signaling_thread_.get());
    
    // 启动线程
    network_thread_->Start();
    worker_thread_->Start();
    signaling_thread_->Start();
    
    initialized_ = true;
    LOG_INFO("[LibWebRtc][ThreadPool] 共享线程池初始化成功 (3个线程)");
    
    return true;
}

rtc::Thread* LibwebrtcThreadPool::GetNetworkThread() {
    std::lock_guard<std::mutex> lock(mutex_);
    
    if (!initialized_) {
        if (!InitializeInternal()) {
            return nullptr;
        }
    }
    
    ref_count_.fetch_add(1);
    return network_thread_.get();
}

rtc::Thread* LibwebrtcThreadPool::GetWorkerThread() {
    std::lock_guard<std::mutex> lock(mutex_);
    
    if (!initialized_) {
        if (!InitializeInternal()) {
            return nullptr;
        }
    }
    
    // 注意：引用计数在GetNetworkThread中已经增加
    // 因为3个线程是同时使用的，一个客户端会同时获取3个线程
    // 所以只需要在GetNetworkThread中增加一次引用计数
    // 但为了安全起见，如果单独调用GetWorkerThread，也需要增加引用计数
    // 实际使用中，应该总是先调用GetNetworkThread，然后调用GetWorkerThread和GetSignalingThread
    return worker_thread_.get();
}

rtc::Thread* LibwebrtcThreadPool::GetSignalingThread() {
    std::lock_guard<std::mutex> lock(mutex_);
    
    if (!initialized_) {
        if (!InitializeInternal()) {
            return nullptr;
        }
    }
    
    // 注意：引用计数在GetNetworkThread中已经增加
    // 因为3个线程是同时使用的，一个客户端会同时获取3个线程
    // 所以只需要在GetNetworkThread中增加一次引用计数
    // 但为了安全起见，如果单独调用GetSignalingThread，也需要增加引用计数
    // 实际使用中，应该总是先调用GetNetworkThread，然后调用GetWorkerThread和GetSignalingThread
    return signaling_thread_.get();
}

void LibwebrtcThreadPool::ReleaseThreads() {
    std::lock_guard<std::mutex> lock(mutex_);
    
    int count = ref_count_.fetch_sub(1);
    if (count <= 1) {
        // 最后一个引用被释放
        LOG_INFO("[LibWebRtc][ThreadPool] 最后一个客户端释放，引用计数: %d", count - 1);
        // 注意：这里不立即清理线程，因为可能很快又有新的客户端
        // 真正的清理在Cleanup()中完成
    } else {
        LOG_INFO("[LibWebRtc][ThreadPool] 客户端释放，当前引用计数: %d", count - 1);
    }
}

void LibwebrtcThreadPool::Cleanup() {
    std::lock_guard<std::mutex> lock(mutex_);
    
    if (!initialized_) {
        return;
    }
    
    // 策略：线程池的线程创建后就不析构了，一直保存到程序退出
    // 原因：
    // 1. 线程创建和销毁成本高，且容易产生竞态条件
    // 2. 程序退出时操作系统会自动清理所有线程资源
    // 3. 避免析构时的复杂同步问题
    // 4. 3个线程的资源占用很小，可以接受
    
    LOG_INFO("[LibWebRtc][ThreadPool] 线程池清理被调用，但线程不会析构（保留到程序退出）");
    LOG_INFO("[LibWebRtc][ThreadPool] 当前引用计数: %d", ref_count_.load());
    
    // 注意：这里不停止线程，也不释放线程对象
    // 线程会一直运行到程序退出，由操作系统自动清理
    // 这样可以避免所有与线程析构相关的竞态条件和崩溃问题
}

LibwebrtcThreadPool::~LibwebrtcThreadPool() {
    // 析构函数也不清理线程，让线程保留到程序退出
    // 这样可以避免在程序退出时出现复杂的线程同步问题
    LOG_INFO("[LibWebRtc][ThreadPool] 线程池析构，线程保留到程序退出");
}

} // namespace message_engine

