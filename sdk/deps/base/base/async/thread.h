// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <atomic>
#include <chrono>
#include <thread>
#include <string>
#include <sstream>

#include "base/common/macros.h"
#include "base/utils/thread_util.h"
#include "runnable.h"

#ifdef __WIN32__
#undef Yield
#endif

namespace drc_sdk {

static const char* kNamePrefix = "Crossing-";

enum class ThreadState {
  kInitialized = 0,
  kRunnable,
  kRunning,
  kTerminated
};

class Thread : Runnable {
 public:
  Thread() = delete;
  explicit Thread(SP<Runnable>&& runnable);
  explicit Thread(std::string name);
  Thread(SP<Runnable>&& runnable, std::string name);

  explicit Thread(std::function<void()>&& fn);
  Thread(std::function<void()>&& fn, std::string name);

  ~Thread() override;

  // Runnable override empty impl
  void Run() override {}

  // Thread function
  bool Start();

  void Join();

  bool IsAlive();

  bool IsRunning();

  bool IsJoinAble();

  std::string name() const {
    return name_;
  }

  void set_name(const std::string& name) {
    name_ = name;
  }

  void set_priority(float priority) {
    priority_ = priority;
  }

  uint64_t tid() const {
    return thread_id_;
  }

  static void Sleep(int64_t millis) {
    std::this_thread::sleep_for(std::chrono::milliseconds(millis));
  }

  static void Yield() {
    std::this_thread::yield();
  }

  static uint64_t CurrentThreadId();

  static std::string CurrentThreadName();

  static std::string GetDefaultThreadName();

 private:
  void Invoke();
  void SetThreadName();

  static std::atomic<int> count;

  std::string name_;
  UP<std::thread> thread_;
  SP<Runnable> runnable_ = nullptr;
  float priority_ = 0;
  std::atomic<ThreadState> state_;
  uint64_t thread_id_ = 0;

  DISALLOW_COPY_AND_ASSIGN(Thread);
};
}  // namespace drc_sdk
