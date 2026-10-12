// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <mutex>
#include <queue>
#include <vector>
#include <algorithm>
#include <set>
#include <condition_variable>
#include <atomic>
#if defined(__unix__) || defined(__APPLE__)
#include <pthread.h>
#endif

#include "thread.h"
#include "executor.h"

namespace drc_sdk {

using time_point = std::chrono::steady_clock::time_point;

class ThreadPoolExecutor : public Executor {
 public:
    explicit ThreadPoolExecutor(const ThreadPoolParam& thread_pool_param);

    ~ThreadPoolExecutor() override;

    void Stop() override;

    int Post(RunnableFunc&& func) override;

    int PostAtTime(RunnableFunc&& func, std::chrono::steady_clock::time_point time_point) override;

    // 空闲线程数量
    int IdleCount() const override {
        return idleThrNum_;
    }

    // 当前线程总数量
    int ThreadCount() const override {
        return static_cast<int>(std::count_if(pool_.begin(), pool_.end(), [](SP<Thread> thread) {
            return thread->IsAlive();
        }));
    }

    bool CurrentThreadInExecutor() const override {
        return std::find_if(pool_.begin(), pool_.end(), [](const SP<Thread>& thread) {
            return thread->tid() == Thread::CurrentThreadId();
        }) != pool_.end();
    }

    void CancelTask(int id) override;

    void Flush() override;

    void LogDebugInfo();

    // 供 ThreadBlockingDetector 等阻塞检测使用：线程安全读任务统计与 Main worker 的 pthread_t
    int GetPostedTaskCount() const;
    int GetExecutedTaskCount() const;
#if defined(__unix__) || defined(__APPLE__)
    pthread_t GetWorkerPthread() const;  // 仅单线程 executor 有效，用于 pthread_kill 抓堆栈
#endif

 private:
    struct Task {
        int id;
        RunnableFunc runnable;
        time_point run_time;
        bool cancel = false;

        friend bool operator<(const Task& a, const Task& b) {
            if (a.run_time == b.run_time) {
                return a.id > b.id;
            }
            return a.run_time > b.run_time;  // 升序
        }
    };

    class TaskQueue : public std::priority_queue<Task> {
     public:
        bool empty_() {
            this->cleanup();
            return this->empty();
        }

        void pop_() {
            this->cleanup();
            this->pop();
        }

        const Task& top_() {
            this->cleanup();
            return this->top();
        }

        void remove(int id) {
            auto it = std::find_if(this->c.begin(), this->c.end(), [id] (const Task& t) {
                return t.id == id;
            });
            if (it != this->c.end()) { // found target id
                (*it).cancel = true;
            }
        }

     protected:
        void cleanup() {
            if (this->empty()) {
                return;
            }
            while (this->top().cancel) {
                this->pop();
                if (this->empty()) {
                    return;
                }
            }
        }
    };

    void AddThread(size_t size);
    void StartDelayedExecuteThread();
    void CleanTerminatedThreads();
    std::string GetTreadName();

    struct DebugInfo {
        std::string worker_name_;
        std::string worker_thread_id_;

        std::function<void()>* cur_task_p_ = nullptr; //正在运行的任务指针
        std::chrono::steady_clock::time_point last_log_time_point_;
        bool benchmark_enable_ = false;
        std::atomic_int post_task_count_{0}; //抛到这个worker到任务总数统计
        int last_post_task_count_ = 0; //上次抛到这个worker到任务总数统计
        std::atomic_int exec_task_count_{0}; //执行的任务总数统计
        int last_exec_task_count_ = 0; //上次执行的任务总数统计
        int exception_check_count_ = 0;
    };

    DebugInfo debug_info_;
    DebugInfo delayed_debug_info_;


 private:
    std::queue<Task> tasks_;                   // 处理非延迟任务的队列
    std::mutex lock_;                   // 同步锁
    std::mutex delayed_lock_;                   // 同步锁
    std::vector<SP<Thread>> pool_;      // 线程池
    SP<Thread> delayed_execute_thread_; // 专门处理延迟任务的线程
    TaskQueue delayed_execute_task_;    // 专门处理延迟任务的队列
    std::condition_variable task_cv_;   // 条件阻塞
    std::condition_variable delayed_task_cv_;   // 条件阻塞
    std::atomic<bool> run_{true};    // 线程池是否工作
    std::atomic<int> idleThrNum_{0}; // 空闲线程数量
    ThreadPoolParam thread_pool_param_; // 线程池配置
    std::atomic_int id_{0};
#if defined(__unix__) || defined(__APPLE__)
    std::atomic<uintptr_t> worker_pthread_{0};  // 单线程时 Main worker 的 pthread_t，供 BlockingDetector 发 SIGUSR1
#endif
};

} // namespace drc_sdk
