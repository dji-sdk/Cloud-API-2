// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#if defined(__unix__) || defined(__APPLE__)

#include "blocking_detector.h"
#include <base/async/thread_pool_executor.h>
#include <common/executors/executors.h>

namespace flight_hub_sdk {

// 将 Executors::Main() 的 ThreadPoolExecutor 适配为 BlockingDetector 的 TaskStatistics
class MainExecutorBlockingDetectorAdapter : public blocking_detector::TaskStatistics {
public:
    MainExecutorBlockingDetectorAdapter();
    int GetPostedTaskCount() const override;
    int GetExecutedTaskCount() const override;
    pthread_t GetTargetThreadId() const override;

private:
    drc_sdk::ThreadPoolExecutor* main_executor_{nullptr};
};

}  // namespace flight_hub_sdk

#endif  // __unix__ || __APPLE__
