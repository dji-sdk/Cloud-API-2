// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#if defined(__unix__) || defined(__APPLE__)

#include "blocking_detector_adapter.h"
#include <common/executors/executors.h>

namespace flight_hub_sdk {

MainExecutorBlockingDetectorAdapter::MainExecutorBlockingDetectorAdapter() {
    auto& main_ref = drc_sdk::common::Executors::Main();
    if (main_ref) {
        main_executor_ = dynamic_cast<drc_sdk::ThreadPoolExecutor*>(main_ref.get());
    }
}

int MainExecutorBlockingDetectorAdapter::GetPostedTaskCount() const {
    return main_executor_ ? main_executor_->GetPostedTaskCount() : 0;
}

int MainExecutorBlockingDetectorAdapter::GetExecutedTaskCount() const {
    return main_executor_ ? main_executor_->GetExecutedTaskCount() : 0;
}

pthread_t MainExecutorBlockingDetectorAdapter::GetTargetThreadId() const {
    return main_executor_ ? main_executor_->GetWorkerPthread() : static_cast<pthread_t>(0);
}

}  // namespace flight_hub_sdk

#endif  // __unix__ || __APPLE__
