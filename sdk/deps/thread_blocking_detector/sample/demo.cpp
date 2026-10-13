// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "blocking_detector.h"
#include <iostream>
#include <thread>
#include <queue>
#include <mutex>
#include <condition_variable>
#include <atomic>
#include <chrono>
#include <unistd.h>

// 简单的任务执行器实现
class SimpleExecutor : public blocking_detector::TaskStatistics {
public:
    SimpleExecutor() : stopped_(false), post_count_(0), exec_count_(0), target_thread_id_(0) {
        // 使用同步机制确保线程 ID 正确获取
        std::mutex init_mutex;
        std::condition_variable init_cv;
        bool thread_ready = false;
        
        worker_thread_ = std::thread([this, &init_mutex, &init_cv, &thread_ready]() {
            // 在工作线程中获取线程 ID
            target_thread_id_ = pthread_self();
            {
                std::lock_guard<std::mutex> lock(init_mutex);
                thread_ready = true;
            }
            init_cv.notify_one();
            WorkerLoop();
        });
        
        // 等待线程 ID 设置完成
        std::unique_lock<std::mutex> lock(init_mutex);
        init_cv.wait(lock, [&thread_ready]() { return thread_ready; });
    }
    
    ~SimpleExecutor() {
        Stop();
        if (worker_thread_.joinable()) {
            worker_thread_.join();
        }
    }
    
    void Post(std::function<void()> task) {
        {
            std::lock_guard<std::mutex> lock(queue_mutex_);
            task_queue_.push(std::move(task));
            post_count_++;
        }
        cv_.notify_one();
    }
    
    void Stop() {
        stopped_ = true;
        cv_.notify_all();
    }
    
    // TaskStatistics 接口实现
    int GetPostedTaskCount() const override {
        return post_count_.load();
    }
    
    int GetExecutedTaskCount() const override {
        return exec_count_.load();
    }
    
    pthread_t GetTargetThreadId() const override {
        return target_thread_id_.load();
    }

private:
    void WorkerLoop() {
        while (!stopped_.load()) {
            std::function<void()> task;
            {
                std::unique_lock<std::mutex> lock(queue_mutex_);
                cv_.wait(lock, [this]() { return !task_queue_.empty() || stopped_; });
                
                if (stopped_ && task_queue_.empty()) {
                    break;
                }
                
                if (!task_queue_.empty()) {
                    task = std::move(task_queue_.front());
                    task_queue_.pop();
                }
            }
            
            if (task) {
                task();
                exec_count_++;
            }
        }
    }
    
    std::atomic<bool> stopped_;
    std::thread worker_thread_;
    mutable std::atomic<pthread_t> target_thread_id_;
    
    std::queue<std::function<void()>> task_queue_;
    std::mutex queue_mutex_;
    std::condition_variable cv_;
    
    std::atomic<int> post_count_;
    std::atomic<int> exec_count_;
};

// 模拟阻塞任务
void BlockingTask(int duration_ms) {
    std::this_thread::sleep_for(std::chrono::milliseconds(duration_ms));
}

// 正常任务
void NormalTask() {
    // 模拟一些工作
    std::this_thread::sleep_for(std::chrono::milliseconds(10));
}

int main(int argc, char* argv[]) {
    std::cout << "========================================" << std::endl;
    std::cout << "线程阻塞检测工具演示程序" << std::endl;
    std::cout << "========================================" << std::endl;
    
    // 创建执行器
    SimpleExecutor executor;
    
    // 配置阻塞检测器
    blocking_detector::BlockingDetectorConfig config;
    config.max_wait_count = 100;        // 降低阈值以便演示
    config.block_wait_count = 50;
    config.check_interval_ms = 500;     // 500ms 检查一次
    config.crash_dir = "./blocking_detector";
    config.abort_on_blocking = false;   // 演示时不终止程序
    
    // 创建阻塞检测器
    blocking_detector::BlockingDetector detector(&executor, config);
    
    // 初始化并启动
    if (!detector.Init()) {
        std::cerr << "初始化阻塞检测器失败" << std::endl;
        return 1;
    }
    
    detector.Start();
    std::cout << "阻塞检测器已启动" << std::endl;
    
    // 等待一下让检测器初始化
    std::this_thread::sleep_for(std::chrono::milliseconds(100));
    
    std::cout << "\n场景1: 正常任务执行（不会触发阻塞检测）" << std::endl;
    for (int i = 0; i < 10; i++) {
        executor.Post(NormalTask);
        std::this_thread::sleep_for(std::chrono::milliseconds(50));
    }
    
    std::this_thread::sleep_for(std::chrono::seconds(2));
    
    std::cout << "\n场景2: 快速提交大量任务（会触发阻塞检测）" << std::endl;
    std::cout << "提交 200 个阻塞任务（每个阻塞 100ms）..." << std::endl;
    
    for (int i = 0; i < 200; i++) {
        executor.Post([]() { BlockingTask(100); });
    }
    
    std::cout << "任务已提交，等待检测..." << std::endl;
    
    // 等待检测到阻塞
    std::this_thread::sleep_for(std::chrono::seconds(3));
    
    std::cout << "\n场景3: 单个长时间阻塞任务" << std::endl;
    std::cout << "提交一个阻塞 5 秒的任务..." << std::endl;
    executor.Post([]() { BlockingTask(5000); });
    
    // 在阻塞期间快速提交更多任务
    for (int i = 0; i < 100; i++) {
        executor.Post(NormalTask);
    }
    
    std::cout << "等待检测..." << std::endl;
    std::this_thread::sleep_for(std::chrono::seconds(3));
    
    std::cout << "\n演示完成" << std::endl;
    std::cout << "已提交任务数: " << executor.GetPostedTaskCount() << std::endl;
    std::cout << "已执行任务数: " << executor.GetExecutedTaskCount() << std::endl;
    std::cout << "待执行任务数: " << (executor.GetPostedTaskCount() - executor.GetExecutedTaskCount()) << std::endl;
    std::cout << "\n堆栈信息已保存到: " << config.crash_dir << std::endl;
    
    // 停止
    detector.Stop();
    executor.Stop();
    
    return 0;
}

