// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include <condition_variable>

#include "executor.h"
#include "thread_pool_executor.h"

#ifdef __EMSCRIPTEN__
#include "emscripten_event_executor.h"
#endif

namespace drc_sdk {

SP<Executor> Executor::CreateThreadPoolExecutor(const ThreadPoolParam& param) {
  return MK_SP<ThreadPoolExecutor>(param);
}

SP<Executor> Executor::CreateSingleThreadExecutor(const std::string& name,bool need_observe) {
    return MK_SP<ThreadPoolExecutor>(ThreadPoolParam{1, 1, 0, need_observe, name});
}

#ifdef __EMSCRIPTEN__
SP<Executor> Executor::CreateEmscriptenEventExecutor(const std::string& name, bool need_observe) {
    return MK_SP<EmscriptenEventExecutor>(name, need_observe);
}
#endif

} // namespace drc_sdk
