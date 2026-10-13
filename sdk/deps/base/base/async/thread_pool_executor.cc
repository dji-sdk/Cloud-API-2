// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "thread_pool_executor.h"

#include <cassert>
#include <type_traits>
#include <utility>

#include "base/common/macros.h"
#include "base/common/logger.h"
#include <chrono>
#if defined(__unix__) || defined(__APPLE__)
#include <pthread.h>
#endif
static constexpr int gLimitWaitCount = 1000;
static constexpr int gMaxWaitCount = 10000;
static constexpr int gMaxExceptionCheckCount = 7;

namespace drc_sdk {

#if defined(__unix__) || defined(__APPLE__)
template <typename PT = pthread_t, typename std::enable_if<std::is_integral<PT>::value, int>::type = 0>
static inline uintptr_t EncodePthread(PT tid) {
    return static_cast<uintptr_t>(tid);
}

template <typename PT = pthread_t, typename std::enable_if<!std::is_integral<PT>::value, int>::type = 0>
static inline uintptr_t EncodePthread(PT tid) {
    return reinterpret_cast<uintptr_t>(tid);
}

template <typename PT = pthread_t, typename std::enable_if<std::is_integral<PT>::value, int>::type = 0>
static inline PT DecodePthread(uintptr_t tid) {
    return static_cast<PT>(tid);
}

template <typename PT = pthread_t, typename std::enable_if<!std::is_integral<PT>::value, int>::type = 0>
static inline PT DecodePthread(uintptr_t tid) {
    return reinterpret_cast<PT>(tid);
}
#endif

class WorkerTaskFullException : public std::exception{
public:
    explicit WorkerTaskFullException(const std::string& task_info,const std::string& thread_info){
        info_ = "Worker Task Full Exception, thread -> "+thread_info+" task_info = " + task_info;
    }
    const char* what() const noexcept override {
        return info_.c_str();
    }

private:
    std::string info_;
};

ThreadPoolExecutor::ThreadPoolExecutor(const ThreadPoolParam& thread_pool_param) : thread_pool_param_(thread_pool_param) {
    assert(thread_pool_param.core_num <= thread_pool_param.max_thread_num);
    assert(thread_pool_param.max_thread_num > 0);
    AddThread(thread_pool_param.core_num);
}

ThreadPoolExecutor::~ThreadPoolExecutor() {
    Stop();
}

int ThreadPoolExecutor::Post(RunnableFunc&& func) {
    return PostAtTime(std::move(func), std::chrono::steady_clock::now());
}

int ThreadPoolExecutor::PostAtTime(RunnableFunc&& func, std::chrono::steady_clock::time_point time_point) {
    if (!run_) {
        if (thread_pool_param_.need_observe) {
            LOG_ERROR("post on ThreadPool is stopped.");
        }
        return -1;
    }

    int id = -1;

    {
        SYNCHRONIZED(lock_);
        CleanTerminatedThreads();
        id = id_++;
    }

    //延迟任务，由延迟任务执行线程执行
    if (time_point > std::chrono::steady_clock::now()) {

        {
            SYNCHRONIZED(delayed_lock_);
            delayed_execute_task_.emplace(Task{id, std::move(func), time_point});
            ++delayed_debug_info_.post_task_count_;
            StartDelayedExecuteThread();
        }
        delayed_task_cv_.notify_one();
    } else {
        {
            SYNCHRONIZED(lock_);
            ++debug_info_.post_task_count_;
            tasks_.emplace(Task{id, std::move(func), time_point});
            if (idleThrNum_ < 1 && pool_.size() < thread_pool_param_.max_thread_num) {
                AddThread(1);
            }
        }
        task_cv_.notify_one();
    }
    LogDebugInfo();
    return id;
}

void ThreadPoolExecutor::CleanTerminatedThreads() {
    pool_.erase(std::remove_if(pool_.begin(), pool_.end(), [](SP<Thread>& thread) {
        return !thread->IsAlive() && thread->IsJoinAble();
    }), pool_.end());
}

void ThreadPoolExecutor::AddThread(size_t size) {
    for (; pool_.size() < thread_pool_param_.max_thread_num && size > 0; --size) {
        std::string thread_name;
        if (!thread_pool_param_.name.empty()) {
            thread_name = GetTreadName();
        } else {
            thread_name = Thread::GetDefaultThreadName();
        }
        debug_info_.worker_name_ = thread_name;
        LOG_INFO("add thread: %s", thread_name.c_str());
        auto thread = MK_SP<Thread>([this] {
            {
                std::stringstream ss;
                ss << std::this_thread::get_id();
                debug_info_.worker_thread_id_ = ss.str();
#if defined(__unix__) || defined(__APPLE__)
                if (thread_pool_param_.core_num == 1 && thread_pool_param_.max_thread_num == 1) {
                    worker_pthread_.store(EncodePthread(pthread_self()));
                }
#endif
            }
            while (run_) {
                RunnableFunc runnable;
                {
                    // 获取一个待执行的task
                    std::unique_lock<std::mutex> lock{lock_};
                    auto pred = [this] {
                        return !run_ || !tasks_.empty();
                    };
                    if (thread_pool_param_.keep_alive_time > 0) {
                        task_cv_.wait_for(lock,
                                          std::chrono::milliseconds(thread_pool_param_.keep_alive_time),
                                          pred);
                    } else {
                        task_cv_.wait(lock, pred);
                    }
                    // 线程运行，减少空闲线程数
                    idleThrNum_--;
                    if (!run_) {
                        return;
                    }
                    // 当等待超时还是没有任务，同时当前空闲线程数超过核心线程数时，销毁多余的空闲线程
                    if (tasks_.empty() && idleThrNum_ > thread_pool_param_.core_num) {
                        return;
                    }

                    // 若空闲线程数不足核心线程数，则保持当前线程的运转
                    if (tasks_.empty()) {
                        continue;
                    }

                    while(!tasks_.empty()) {
                        runnable = std::move(const_cast<RunnableFunc&>(tasks_.front().runnable));
                        tasks_.pop();
                        debug_info_.cur_task_p_ = &runnable;
                        lock.unlock();
                        auto pre_time = std::chrono::steady_clock::now();
                        runnable();
                        debug_info_.cur_task_p_ = nullptr;
                        ++debug_info_.exec_task_count_;
                        auto post_time = std::chrono::steady_clock::now();
                        auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(post_time - pre_time);
                        if (duration.count() > 50 && thread_pool_param_.need_observe) {
                            LOG_INFO("execute time: %d, runnable name: %s", duration.count(), runnable.target_type().name());
                        }
                        lock.lock();
                    }
                    //任务执行完了，看看延迟任务还有没有
                    delayed_task_cv_.notify_one();
                    // 线程执行完任务，增加空闲线程数
                    idleThrNum_++;
                }
            }
        }, thread_name);

        thread->Start();
        pool_.emplace_back(thread);
        idleThrNum_++;
    }
}

void ThreadPoolExecutor::StartDelayedExecuteThread() {
    if (delayed_execute_thread_) {
        return;
    }
    delayed_execute_thread_ = MK_SP<Thread>([this] {
        while (run_) {
            RunnableFunc runnable;
            {
                // 获取一个待执行的task
                std::unique_lock<std::mutex> lock{delayed_lock_};
                auto pred = [this] {
                    return !run_ || !delayed_execute_task_.empty();
                };

                delayed_task_cv_.wait(lock, pred);

                if (!run_) {
                    return;
                }

                // 若空闲线程数不足核心线程数，则保持当前线程的运转
                if (delayed_execute_task_.empty()) {
                    continue;
                }

                // 检查任务是否到达执行时间，未到达时间则等待超时并进入下一次循环，
                auto target_execute_time = delayed_execute_task_.top_().run_time;
                if (target_execute_time > std::chrono::steady_clock::now()) {
                    delayed_task_cv_.wait_until(lock, target_execute_time);
                    continue;
                }

                while(!delayed_execute_task_.empty() && delayed_execute_task_.top_().run_time < std::chrono::steady_clock::now()) {
                    Post(std::move(const_cast<RunnableFunc&>(delayed_execute_task_.top_().runnable)));
                    ++delayed_debug_info_.exec_task_count_;
                    delayed_execute_task_.pop();
                    }
                }
            }
        },
       "d-" + GetTreadName());
    delayed_execute_thread_->Start();
}

std::string ThreadPoolExecutor::GetTreadName() {
    if (thread_pool_param_.max_thread_num <= 1) {
        return thread_pool_param_.name;
    }
    return thread_pool_param_.name + "-" + std::to_string((pool_.size() + 1));
}

void ThreadPoolExecutor::CancelTask(int id) {
    {
        SYNCHRONIZED(delayed_lock_);
        delayed_execute_task_.remove(id);
    }
}

void ThreadPoolExecutor::Flush() {
    if (!run_) {
        return;  // 线程池已停止，无需刷新
    }
    
    // 如果当前线程就是工作线程，直接返回（避免死锁）
    if (CurrentThreadInExecutor()) {
        return;
    }
    
    // 提交一个标记任务，等待它执行完成
    // 这样可以确保所有在此之前提交的任务都已经执行完毕
    std::mutex flush_mutex;
    std::condition_variable flush_cv;
    bool flush_completed = false;
    
    Post([&flush_mutex, &flush_cv, &flush_completed]() {
        std::lock_guard<std::mutex> lock(flush_mutex);
        flush_completed = true;
        flush_cv.notify_one();
    });
    
    // 等待标记任务执行完成
    std::unique_lock<std::mutex> lock(flush_mutex);
    flush_cv.wait(lock, [&flush_completed]() { return flush_completed; });
}

void ThreadPoolExecutor::LogDebugInfo(){
    if (!thread_pool_param_.need_observe) {
        return;
    }

    SYNCHRONIZED(lock_);
    auto now_time_point_ = std::chrono::steady_clock::now();
    auto elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(now_time_point_ - debug_info_.last_log_time_point_);
    if(elapsed.count() > 1000){
        LOG_DEBUG("[worker] cur task wait size = %d  post count = %d exec count = %d",(debug_info_.post_task_count_ - debug_info_.exec_task_count_),(debug_info_.post_task_count_ - debug_info_.last_post_task_count_),(debug_info_.exec_task_count_ - debug_info_.last_exec_task_count_));
        debug_info_.last_log_time_point_ = now_time_point_;
        debug_info_.last_post_task_count_ = debug_info_.post_task_count_;
        //以下情况累积均会触发主动abort：
        //(1)触发任务数大于最大(Max)阈值: 为了避免消费者消费过慢导致任务堆积，引起匿名页大量占用；
        //(2)任务数大于限制(Limit)阈值且距离上次打日志没有被消耗过任务：为了避免消费者线程长期不处理任务。
        if ((tasks_.size() > gMaxWaitCount) || (tasks_.size() > gLimitWaitCount && debug_info_.last_exec_task_count_ == debug_info_.exec_task_count_)){
            debug_info_.exception_check_count_++;
            std::string cur_task_info = "(null)";
            if(debug_info_.cur_task_p_){
                cur_task_info = debug_info_.cur_task_p_->target_type().name();
            }
            LOG_ERROR("[worker] WorkerTaskFullException %s info = %s",debug_info_.worker_name_.c_str(),cur_task_info.c_str());
            if(debug_info_.exception_check_count_ >= gMaxExceptionCheckCount){
                // 暂时注释掉崩溃逻辑，先观察问题，收集更多日志信息
                // std::this_thread::sleep_for(std::chrono::seconds(1));       // 这里sleep之后再抛出崩溃异常，主线程sleep，log线程仍在执行，保证能将崩溃信息写入
                // ThreadPoolExecutor* p = nullptr;
                // p->Post(nullptr);
                //                throw WorkerTaskFullException(cur_task_info ,debug_info_.worker_name_);
                LOG_ERROR("[worker] WorkerTaskFullException reached max check count, but crash logic is disabled for debugging");
            }
        } else {
            debug_info_.exception_check_count_ = 0;
        }
        debug_info_.last_exec_task_count_ = debug_info_.exec_task_count_;
    }
}

int ThreadPoolExecutor::GetPostedTaskCount() const {
    return debug_info_.post_task_count_.load(std::memory_order_relaxed);
}

int ThreadPoolExecutor::GetExecutedTaskCount() const {
    return debug_info_.exec_task_count_.load(std::memory_order_relaxed);
}

#if defined(__unix__) || defined(__APPLE__)
pthread_t ThreadPoolExecutor::GetWorkerPthread() const {
    return DecodePthread(worker_pthread_.load(std::memory_order_relaxed));
}
#endif

void ThreadPoolExecutor::Stop() {
    run_ = false;
    task_cv_.notify_all();
    delayed_task_cv_.notify_all();
    for (SP<Thread>& thread : pool_) {
        thread->Join();
    }
    if (delayed_execute_thread_ != nullptr) {
        delayed_execute_thread_->Join();
    }
    pool_.clear();;
}
}  // namespace drc_sdk
