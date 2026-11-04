// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/flight_task_media_upload_module.h"
#include "common/util/async_capture_protect.h"
#include "i_message_engine.h"
#include "notification_center/notification_center.h"
#include <functional>
#include <unordered_map>
#include <mutex>

namespace flight_hub_sdk {

class FlightTaskMediaUploadModuleImpl : public IFlightTaskMediaUploadModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    FlightTaskMediaUploadModuleImpl(
        const std::string& drone_sn,
        const std::string& gateway_sn,
        std::shared_ptr<message_engine::IMessageEngine> message_engine,
        int session_id);
    ~FlightTaskMediaUploadModuleImpl() override;

    void uploadFlighttaskMediaPrioritize(
        const std::string& flight_id,
        const std::function<void(FlightHubError err)>& callback) override;

    int listenHighestPriorityUploadFlighttaskMedia(
        const std::function<void(const HighestPriorityUploadFlighttaskMedia& msg)>& callback) override;
    void cancelListenHighestPriorityUploadFlighttaskMedia(int listen_id) override;

private:
    std::string drone_sn_;
    std::string gateway_sn_;
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;
    int session_id_;

    // highest_priority 监听映射：user_listen_id ↔ nc_listen_id
    std::unordered_map<int, int64_t> highest_priority_nc_listen_ids_;
    int highest_priority_next_id_ = 0;
    std::mutex listen_mutex_;
};

} // namespace flight_hub_sdk