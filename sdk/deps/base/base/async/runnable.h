// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include <tuple>
#include <type_traits>
#include <utility>

namespace drc_sdk {

class Runnable {
 public:
  virtual void Run() = 0;
  virtual ~Runnable() = default;
};

// 前置声明
template <typename R, typename... Args>
class FunctionRunnable;

template <typename R, typename... Args>
class FunctionRunnable<R(Args...)> : public Runnable {
  using Func = std::function<R(Args...)>;

 public:
  template <typename T, typename... Args2>
  explicit FunctionRunnable(T&& fn, Args2&&... args): args_(std::forward<Args2>(args)...) {
    func_ = Func(std::forward<T>(fn));
  }
  ~FunctionRunnable() override = default;

  void Run() override {
    func_(std::forward<Args>(args_)...);
  }

 private:
  Func func_;
  std::tuple<Args...> args_;
};

}  // namespace drc_sdk
