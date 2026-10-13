// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <chrono>
#include <functional>
#include <string>

#include "base/common/macros.h"
#include "runnable.h"

namespace drc_sdk {

struct ThreadPoolParam {
    int core_num = 4;
    int max_thread_num = 16;
    int keep_alive_time = 0;  // 单位：毫秒，空闲线程等待任务的超时
    bool need_observe = true;
    std::string name;  // 线程池名称
};

class Executor {
 public:
    virtual ~Executor() = default;

    virtual int Post(RunnableFunc&& func) = 0;

    int Dispatch(RunnableFunc&& func) {
        if (CurrentThreadInExecutor()) {
            func();
            return -1;
        }
        return Post(std::move(func));
    }

    int ForceDispatch(RunnableFunc&& func) {
        return Post(std::move(func));
    }

    virtual int PostAtTime(RunnableFunc&& func, std::chrono::steady_clock::time_point time_point) = 0;

    int PostDelay(RunnableFunc&& func, const std::chrono::steady_clock::duration& duration) {
        return PostAtTime(std::move(func), std::chrono::steady_clock::now() + duration);
    }

    inline int PostDelayMs(RunnableFunc&& func, int duration) {
        return PostDelay(std::move(func), std::chrono::milliseconds(duration));
    }

    virtual void Stop() {}

    virtual void CancelTask(int id) = 0;

    virtual bool CurrentThreadInExecutor() const {
        return false;
    }

    virtual int IdleCount() const = 0;
    virtual int ThreadCount() const = 0;

    // 等待所有待执行的任务完成（刷新队列）
    // 这会等待所有已提交到队列的任务执行完毕
    virtual void Flush() {}

    static SP<Executor> CreateThreadPoolExecutor(const ThreadPoolParam&);
    static SP<Executor> CreateSingleThreadExecutor(const std::string& name,bool need_observe = false);
    
#ifdef __EMSCRIPTEN__
    static SP<Executor> CreateEmscriptenEventExecutor(const std::string& name, bool need_observe = false);
#endif
};
}  // namespace drc_sdk
