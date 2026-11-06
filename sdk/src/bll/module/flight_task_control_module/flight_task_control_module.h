// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/flight_task_control.h"
#include "i_message_engine.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"
#include <memory>
#include <mutex>
#include <unordered_set>

namespace flight_hub_sdk {

    class FlightTaskControlModuleImpl : public IFlightTaskControlModule, public drc_sdk::common::AsyncCaptureProtect{
    public:
        explicit FlightTaskControlModuleImpl(std::shared_ptr<message_engine::IMessageEngine> message_engine, const std::string& drone_sn, const std::string& gateway_sn, int session_id);
        ~FlightTaskControlModuleImpl() override;

        int listenWaylineTaskEvent(const std::function<void(const WaylineTaskEvent& msg)>& callback) override;
        void cancelListenWaylineTaskEvent(int listen_id) override;

        int listenReturnHomeInfoEvent(const std::function<void(const ReturnHomeInfoEvent& msg)>& callback) override;
        void cancelListenReturnHomeInfoEvent(int listen_id) override;
        int listenReturnHomeInfoEventByDevice(const std::string& device_sn, const std::function<void(const ReturnHomeInfoEvent& msg)>& callback) override;
        void cancelListenReturnHomeInfoEventByDevice(int listen_id) override;

        int listenTaskExceptionEvent(const std::function<void(const TaskExceptionEvent& msg)>& callback) override;
        void cancelListenTaskExceptionEvent(int listen_id) override;

        void pause(const std::function<void(FlightHubError err)>& callback) override;
        void resume(const std::function<void(FlightHubError err)>& callback) override;

    private:
        void initListen();

        int session_id_;
        std::shared_ptr<message_engine::IMessageEngine> message_engine_;
        std::string drone_sn_;
        std::string gateway_sn_;

        /// callback container
        CallbackContainer<WaylineTaskEvent> wayline_task_event_callbacks_;
        CallbackContainer<ReturnHomeInfoEvent> return_home_info_event_callbacks_;
        CallbackContainer<TaskExceptionEvent> task_exception_event_callbacks_;

        int wayline_task_event_listen_id_ = -1;
        int return_home_info_event_listen_id_ = -1;
        int task_exception_event_listen_id_ = -1;
        /// listenReturnHomeInfoEventByDevice 建立的 TopicManager 订阅 ID 集合。
        /// 注意: 这些 ID 来自 TopicManager::Observe(进程内递增), 与
        /// return_home_info_event_callbacks_ 的 CallbackContainer ID 是两个独立
        /// 且数值区间重叠的空间, 因此两者的取消入口必须分开, 不可共用。
        std::unordered_set<int> return_home_info_event_by_device_listen_ids_;
        std::mutex return_home_info_event_by_device_listen_ids_mutex_;
    };

} // namespace flight_hub_sdk
