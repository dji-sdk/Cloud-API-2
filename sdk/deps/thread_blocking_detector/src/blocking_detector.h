// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <atomic>
#include <chrono>
#include <functional>
#include <memory>
#include <string>
#include <thread>
#include <pthread.h>
#include <signal.h>
#include <cstdint>

namespace blocking_detector {

// 阻塞检测配置
struct BlockingDetectorConfig {
    // 最大等待任务数阈值（超过此值判定为异常）
    int max_wait_count = 10000;
    
    // 阻塞阈值（任务执行速度为0且待执行任务数超过此值判定为阻塞）
    int block_wait_count = 2000;
    
    // 检测间隔（毫秒）
    int check_interval_ms = 1000;
    
    // 堆栈捕获深度
    int stack_depth = 64;
    
    // 堆栈信息保存目录
    std::string crash_dir = "/tmp/blocking_detector";
    
    // 是否在检测到阻塞后终止程序
    bool abort_on_blocking = true;
    
    // 是否在阻塞时转储进程中所有线程的堆栈（便于分析多线程死锁）；仅 Linux 有效
    bool dump_all_threads = false;
};

// 堆栈帧信息
struct StackFrame {
    void* pc;                    // 程序计数器
    uintptr_t offset;            // ELF 文件偏移量
    std::string library;         // 库文件名
    std::string symbol;          // 符号名（mangled）
    std::string demangled_name;  // 符号名（demangled）
};

// 任务统计回调接口
// 用户需要实现此接口来提供任务统计信息
class TaskStatistics {
public:
    virtual ~TaskStatistics() = default;
    
    // 获取已提交任务数
    virtual int GetPostedTaskCount() const = 0;
    
    // 获取已执行任务数
    virtual int GetExecutedTaskCount() const = 0;
    
    // 获取目标线程 ID（用于发送信号）
    virtual pthread_t GetTargetThreadId() const = 0;
};

// 线程阻塞检测器
// 用于监控任务队列，检测线程阻塞并捕获堆栈信息
class BlockingDetector {
public:
    explicit BlockingDetector(TaskStatistics* stats, 
                             const BlockingDetectorConfig& config = BlockingDetectorConfig());
    ~BlockingDetector();
    
    // 禁止拷贝和移动
    BlockingDetector(const BlockingDetector&) = delete;
    BlockingDetector& operator=(const BlockingDetector&) = delete;
    BlockingDetector(BlockingDetector&&) = delete;
    BlockingDetector& operator=(BlockingDetector&&) = delete;
    
    // 初始化阻塞检测器（设置信号处理等）
    bool Init();
    
    // 开始监控
    void Start();
    
    // 停止监控
    void Stop();
    
    // 手动触发堆栈捕获（用于测试）
    void TriggerStackCapture();

private:
    // 阻塞检测主循环
    void CheckLoop();
    
    // 检查是否阻塞
    void CheckBlocking();
    
    // 检测到阻塞时的处理
    void OnBlockingDetected(int wait_size, int post_speed, int exec_speed);
    
    // 发送信号触发堆栈捕获
    void SendStackCaptureSignal();
    
    // 信号处理函数（静态）
    static void StackDumpHandler(int sig);
    
    // 捕获堆栈
    int CaptureStack(void** buffer, int max_depth);
    
    // 解析符号信息
    int ResolveSymbols(void** pc_ptrs, int count, StackFrame* frames);
    
    // 过滤内核相关堆栈帧
    int FilterKernelFrames(StackFrame* frames, int count);
    
    // 格式化堆栈信息
    std::string FormatStackTrace(StackFrame* frames, int count);
    
    // 保存堆栈信息到文件
    void SaveStackInfo(const std::string& stack_info);
    
    // 初始化信号栈
    bool InitSignalStack();
    
    // 注册信号处理
    bool RegisterSignalHandler();
    
    // 获取可执行文件路径
    static std::string GetExecutablePath();
    
    // 使用 addr2line 解析地址
    static std::string ResolveAddressWithAddr2line(void* addr, const char* executable_path);
    
    // Demangle C++ 符号名
    static std::string DemangleSymbol(const char* mangled_name);
    
    // 从文件路径提取文件名
    static std::string GetFileName(const char* path);

private:
    TaskStatistics* stats_;
    BlockingDetectorConfig config_;
    
    // 任务统计（用于计算速度）
    int last_post_count_ = 0;
    int last_exec_count_ = 0;
    std::chrono::steady_clock::time_point last_check_time_;
    
    // 监控线程
    std::atomic<bool> stopped_{false};
    std::thread monitor_thread_;
    
    // 信号处理相关
    void* alt_stack_ = nullptr;
    static BlockingDetector* instance_;  // 用于信号处理函数访问
    
    // 堆栈捕获缓冲区（预分配，避免在信号处理中分配内存）
    static constexpr int kMaxStackDepth = 128;
    void* stack_buffer_[kMaxStackDepth];
    StackFrame frame_buffer_[kMaxStackDepth];

    // 同一堆栈不重复输出：上次已上报的堆栈指纹，恢复非阻塞时清零
    std::atomic<uint64_t> last_reported_stack_hash_{0};

    // 全线程转储时由监控线程打开的文件 fd，各线程 handler 向其追加堆栈；-1 表示单线程模式
    int multi_dump_fd_ = -1;

    // 阻塞时若配置了 dump_all_threads，转储所有线程堆栈到同一文件（仅 Linux）
    void DumpAllThreadsToFile(int wait_size, int post_speed, int exec_speed);
};

} // namespace blocking_detector

