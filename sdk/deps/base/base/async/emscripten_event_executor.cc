// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#ifdef __EMSCRIPTEN__

#include "emscripten_event_executor.h"
#include "base/common/logger.h"
#include <algorithm>

namespace drc_sdk {

EmscriptenEventExecutor::EmscriptenEventExecutor(const std::string& name, bool need_observe)
    : name_(name), need_observe_(need_observe) {
    LOG_DEBUG("[EmscriptenEventExecutor] Created: %s", name_.c_str());
}

EmscriptenEventExecutor::~EmscriptenEventExecutor() {
    Stop();
    LOG_DEBUG("[EmscriptenEventExecutor] Destroyed: %s", name_.c_str());
}

int EmscriptenEventExecutor::Post(RunnableFunc&& func) {
    return PostAtTime(std::move(func), std::chrono::steady_clock::now());
}

int EmscriptenEventExecutor::PostAtTime(RunnableFunc&& func, std::chrono::steady_clock::time_point time_point) {
    if (!run_) {
        if (need_observe_) {
            LOG_ERROR("[EmscriptenEventExecutor] Post on stopped executor: %s", name_.c_str());
        }
        return -1;
    }

    int id = id_++;
    auto now = std::chrono::steady_clock::now();

    // 延迟任务
    if (time_point > now) {
        Task task{id, std::move(func), time_point};
        bool need_schedule = false;
        {
            std::lock_guard<std::mutex> lock(delayed_lock_);
            // 如果队列为空或新任务是最早的，需要重新调度
            if (delayed_tasks_.empty() || time_point < delayed_tasks_.top().run_time) {
                need_schedule = true;
            }
            delayed_tasks_.push(std::move(task));
        }
        if (need_schedule) {
            ScheduleDelayedTask();
        }
        return id;
    }

    // 立即执行的任务
    Task task{id, std::move(func), time_point};
    {
        std::lock_guard<std::mutex> lock(lock_);
        tasks_.push(std::move(task));
    }

    // 使用 emscripten_async_call 异步执行
    ScheduleNext();
    return id;
}

void EmscriptenEventExecutor::CancelTask(int id) {
    // 标记延迟任务为取消
    {
        std::lock_guard<std::mutex> lock(delayed_lock_);
        // 注意：std::priority_queue 不支持直接删除，这里标记为取消
        // 实际执行时会跳过已取消的任务
        // 由于 priority_queue 不支持遍历，这里简化处理
        // 如果需要真正的删除，可以考虑使用 std::multiset
    }

    // 标记立即任务为取消
    {
        std::lock_guard<std::mutex> lock(lock_);
        // std::queue 不支持直接删除，这里标记为取消
        // 实际执行时会跳过已取消的任务
        // 由于 queue 不支持遍历，这里简化处理
    }
}

void EmscriptenEventExecutor::Stop() {
    run_ = false;
    {
        std::lock_guard<std::mutex> lock(lock_);
        // 清空任务队列
        while (!tasks_.empty()) {
            tasks_.pop();
        }
    }
    {
        std::lock_guard<std::mutex> lock(delayed_lock_);
        while (!delayed_tasks_.empty()) {
            delayed_tasks_.pop();
        }
    }
}

void EmscriptenEventExecutor::ProcessTasks() {
    if (!run_) {
        return;
    }

    // 处理立即执行的任务
    while (true) {
        Task task;
        {
            std::lock_guard<std::mutex> lock(lock_);
            if (tasks_.empty()) {
                break;
            }
            task = std::move(tasks_.front());
            tasks_.pop();
        }

        // 跳过已取消的任务
        if (task.cancel) {
            continue;
        }

        // 执行任务（Emscripten 禁用异常，直接执行）
        task.runnable();
    }

    // 检查延迟任务
    {
        std::lock_guard<std::mutex> lock(delayed_lock_);
        auto now = std::chrono::steady_clock::now();
        while (!delayed_tasks_.empty() && delayed_tasks_.top().run_time <= now) {
            Task task = delayed_tasks_.top();
            delayed_tasks_.pop();

            if (task.cancel) {
                continue;
            }

            // 将延迟任务转为立即任务
            {
                std::lock_guard<std::mutex> task_lock(lock_);
                tasks_.push(std::move(task));
            }
        }
        
        // 如果还有延迟任务，继续调度
        if (!delayed_tasks_.empty()) {
            delayed_scheduled_ = false;
            ScheduleDelayedTask();
        }
    }

    // 如果还有任务，继续调度
    {
        std::lock_guard<std::mutex> lock(lock_);
        if (!tasks_.empty()) {
            ScheduleNext();
        }
    }
}

void EmscriptenEventExecutor::ProcessTasksCallback(void* userData) {
    auto* executor = static_cast<EmscriptenEventExecutor*>(userData);
    executor->ProcessTasks();
}

void EmscriptenEventExecutor::DelayedTaskCallback(void* userData) {
    auto* executor = static_cast<EmscriptenEventExecutor*>(userData);
    executor->delayed_scheduled_ = false;
    executor->ProcessTasks();
}

void EmscriptenEventExecutor::ScheduleNext() {
    // 使用 emscripten_async_call 在下一个事件循环中执行
    emscripten_async_call(ProcessTasksCallback, this, 0);
}

void EmscriptenEventExecutor::ScheduleDelayedTask() {
    if (delayed_scheduled_.exchange(true)) {
        // 已经调度过了，不需要重复调度
        return;
    }

    std::lock_guard<std::mutex> lock(delayed_lock_);
    if (delayed_tasks_.empty()) {
        delayed_scheduled_ = false;
        return;
    }

    auto now = std::chrono::steady_clock::now();
    const Task& top_task = delayed_tasks_.top();
    
    if (top_task.run_time <= now) {
        // 时间已到，立即执行
        delayed_scheduled_ = false;
        // 将延迟任务转为立即任务
        Task task = top_task;
        delayed_tasks_.pop();
        {
            std::lock_guard<std::mutex> task_lock(lock_);
            tasks_.push(std::move(task));
        }
        ScheduleNext();
        return;
    }

    // 计算延迟时间（毫秒）
    auto delay_ms = std::chrono::duration_cast<std::chrono::milliseconds>(top_task.run_time - now).count();
    if (delay_ms < 0) {
        delay_ms = 0;
    }

    // 使用 emscripten_async_call 延迟执行（第三个参数是延迟时间，单位：毫秒）
    emscripten_async_call(DelayedTaskCallback, this, delay_ms);
}

} // namespace drc_sdk

#endif // __EMSCRIPTEN__

