// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/flight_task_control.h"
#include "i_message_engine.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

    class FlightTaskControlModuleImpl : public IFlightTaskControlModule, public drc_sdk::common::AsyncCaptureProtect{
    public:
        explicit FlightTaskControlModuleImpl(std::shared_ptr<message_engine::IMessageEngine> message_engine, const std::string& drone_sn, const std::string& gateway_sn);
        ~FlightTaskControlModuleImpl() override;

        int listenWaylineTaskEvent(const std::function<void(const WaylineTaskEvent& msg)>& callback) override;
        void cancelListenWaylineTaskEvent(int listen_id) override;

        void pause(const std::function<void(FlightHubError err)>& callback) override;
        void resume(const std::function<void(FlightHubError err)>& callback) override;

    private:
        void initListen();

        std::shared_ptr<message_engine::IMessageEngine> message_engine_;
        std::string drone_sn_;
        std::string gateway_sn_;

        /// callback container
        CallbackContainer<WaylineTaskEvent> wayline_task_event_callbacks_;

        int wayline_task_event_listen_id_ = -1;
    };

} // namespace flight_hub_sdk