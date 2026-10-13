// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#ifdef __EMSCRIPTEN__

#include "executor.h"
#include <queue>
#include <mutex>
#include <atomic>
#include <string>
#include <chrono>
#include <emscripten.h>

namespace drc_sdk {

/**
 * Emscripten 单线程事件执行器
 * 
 * 特点：
 * - 不使用 std::thread（避免 pthread 依赖）
 * - 使用 emscripten_async_call 实现异步执行
 * - 依赖浏览器的 Event Loop
 * - 所有任务在当前线程执行
 */
class EmscriptenEventExecutor : public Executor {
 public:
    explicit EmscriptenEventExecutor(const std::string& name, bool need_observe = false);
    ~EmscriptenEventExecutor() override;

    int Post(RunnableFunc&& func) override;
    int PostAtTime(RunnableFunc&& func, std::chrono::steady_clock::time_point time_point) override;
    void CancelTask(int id) override;
    void Stop() override;

    bool CurrentThreadInExecutor() const override {
        return true; // 总是在当前线程执行
    }

    int IdleCount() const override {
        return 0; // 单线程，无空闲概念
    }

    int ThreadCount() const override {
        return 1; // 单线程
    }

 private:
    struct Task {
        int id;
        RunnableFunc runnable;
        std::chrono::steady_clock::time_point run_time;
        bool cancel = false;

        friend bool operator<(const Task& a, const Task& b) {
            if (a.run_time == b.run_time) {
                return a.id > b.id;
            }
            return a.run_time > b.run_time; // 升序（优先队列）
        }
    };

    struct TaskWrapper {
        EmscriptenEventExecutor* executor;
        RunnableFunc func;
    };

    void ProcessTasks();
    static void ProcessTasksCallback(void* userData);
    static void DelayedTaskCallback(void* userData);

    void ScheduleNext();
    void ScheduleDelayedTask();

    std::queue<Task> tasks_;
    std::mutex lock_;
    std::atomic<bool> run_{true};
    std::atomic<int> id_{0};
    std::string name_;
    bool need_observe_;

    // 延迟任务队列（优先队列，按时间排序）
    std::priority_queue<Task> delayed_tasks_;
    std::mutex delayed_lock_;
    std::atomic<bool> delayed_scheduled_{false};
};

} // namespace drc_sdk

#endif // __EMSCRIPTEN__

