// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "base/async/timer.h"

namespace drc_sdk {

class TimerImpl : public std::enable_shared_from_this<TimerImpl> {
 public:
    ~TimerImpl() {
        Cancel();
    }
    void Start(const std::shared_ptr<Executor>& executor, std::function<void()>&& func, std::chrono::steady_clock::duration timeout, bool persistent = false);
    void Reset();
    void Cancel();

    friend class Timer;
 private:
    void Run();

    bool is_started_ = false;
    bool is_ran_ = false;

    bool canceled_ = false;
    int cancel_id_ = -1;
    std::shared_ptr<Executor> executor_;
    std::function<void()> function_;
    std::chrono::steady_clock::duration timeout_{};
    bool persistent_ = false;
};

void TimerImpl::Start(const std::shared_ptr<Executor>& executor,
                      std::function<void()>&& func,
                      std::chrono::steady_clock::duration timeout,
                      bool persistent) {
    if (is_started_) {
        return;
    }

    executor_ = executor;
    function_ = std::move(func);
    timeout_ = timeout;
    persistent_ = persistent;

    canceled_ = false;
    is_started_ = true;

    Run();
}

void TimerImpl::Cancel() {
    canceled_ = true;
    if (!is_started_) {
        return;
    }

    if (!persistent_ && is_ran_) {
        return;
    }

    executor_->CancelTask(cancel_id_);
}
void TimerImpl::Reset() {
    // FIX: 当前线程模型实现中，CancelTask有可能会失败，导致CancelTask失败，此时就会导致一个定时器内有两个定时任务
    executor_->CancelTask(cancel_id_);
    Run();
}
void TimerImpl::Run() {
    cancel_id_ = executor_->PostDelay([this, protector = std::weak_ptr<TimerImpl>(shared_from_this())] {
        auto thizz = protector.lock();
        if (!thizz) {
            return;
        }

        if (canceled_) {
            return;
        }

        function_();

        if (!persistent_) {
            is_ran_ = true;
        }

        if (persistent_) {
            Run();
        }
    }, timeout_);
}

void Timer::Start(const std::shared_ptr<Executor>& executor, std::function<void()>&& func, std::chrono::steady_clock::duration timeout, bool persistent) {
    impl_ = std::make_shared<TimerImpl>();
    impl_->Start(executor, std::move(func), timeout, persistent);
}

void Timer::Cancel() {
    if (impl_) {
        impl_->Cancel();
    }
}
void Timer::Reset() {
    if (impl_) {
        impl_->Cancel();
        auto executor = impl_->executor_;
        auto func = std::move(impl_->function_);
        auto timeout = impl_->timeout_;
        auto persistent = impl_->persistent_;

        auto impl_new = std::make_shared<TimerImpl>();
        impl_ = impl_new;
        impl_->Start(executor, std::move(func), timeout, persistent);
    }
}
Timer::~Timer() {
    Cancel();
}

}  // namespace drc_sdk
