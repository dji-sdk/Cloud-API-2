// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "thread.h"

#include <utility>

#include "base/common/logger.h"

namespace drc_sdk {

namespace {

template<typename T>
SP<Runnable> CreateFunctionRunnable(T&& fn) {
  return MK_SP<FunctionRunnable<void ()>>(fn);
}
}

std::atomic<int> Thread::count(1);

Thread::Thread(SP<Runnable>&& runnable):
    Thread(std::move(runnable), GetDefaultThreadName()) {
}

Thread::Thread(std::string name): Thread(SP<Runnable>(nullptr), name) {
}

Thread::Thread(SP<Runnable>&& runnable, std::string name) :
    runnable_(std::move(runnable)),
    name_(std::move(name)),
    state_(ThreadState::kInitialized) {
  LOG_INFO("Thread create, name: %s", name_.c_str());
}

Thread::Thread(std::function<void()>&& fn) :
    Thread(CreateFunctionRunnable(fn)) {
}

Thread::Thread(std::function<void()>&& fn, std::string name) :
    Thread(CreateFunctionRunnable(fn), std::move(name)) {
}

Thread::~Thread() {
  Join();
  LOG_INFO("Thread destroy. name: %s", name_.c_str());
}

bool Thread::Start() {
  state_ = ThreadState::kRunnable;
  thread_ = MK_UP<std::thread>(&Thread::Invoke, this);
  return false;
}

void Thread::Join() {
    if (IsJoinAble() && thread_->get_id() != std::this_thread::get_id()) {
        thread_->join();
    }
}

bool Thread::IsAlive() {
  return state_ == ThreadState::kRunning || state_ == ThreadState::kRunnable;
}

bool Thread::IsRunning() {
  return state_ == ThreadState::kRunning;
}

bool Thread::IsJoinAble() {
  if (thread_ == nullptr) {
    return false;
  }
  return thread_->joinable();
}

void Thread::Invoke() {
  LOG_INFO("Thread started! name: %s", name_.c_str());
  thread_id_ = CurrentThreadId();
  state_ = ThreadState::kRunning;
  SetThreadName();
  if (runnable_ != nullptr) {
    runnable_->Run();
  } else {
    Run();
  }
  state_ = ThreadState::kTerminated;
  LOG_DEBUG("Thread terminated! name: %s", name_.c_str());
}

void Thread::SetThreadName() {
  if (!name_.empty()) {
    ThreadUtil::SetCurrentThreadName(name_);
  }
}

std::string Thread::GetDefaultThreadName() {
  return std::string(kNamePrefix) + std::to_string(count.fetch_add(1));
}

uint64_t Thread::CurrentThreadId() {
  return ThreadUtil::GetThreadId();
}

std::string Thread::CurrentThreadName() {
  return ThreadUtil::GetCurrentThreadName();
}

}  // namespace drc_sdk