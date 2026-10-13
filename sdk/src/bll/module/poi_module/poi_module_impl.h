// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/poi_module.h"
#include "tool/callback_container.h"
#include "i_message_engine.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class PoiModuleImpl : public IPoiModule, public drc_sdk::common::AsyncCaptureProtect{
public:
    explicit PoiModuleImpl(std::shared_ptr<message_engine::IMessageEngine> message_engine, std::string drone_sn, const std::string& gateway_sn, const std::string& drone_model_key);
    ~PoiModuleImpl() override;

    void enter(const POIEnterParams& params, const std::function<void(FlightHubError err, const std::string& task_uuid)>& callback) override;
    void exit(const std::function<void(FlightHubError err)>& callback) override;
    void updateSpeed(double speed, const std::function<void(FlightHubError err)>& callback) override;
    void getTask(const std::function<void(FlightHubError err, const POITaskInfo& task)>& callback) override;
    int listenPoiTaskEvent(const std::function<void(const PoiTaskEvent& msg)>& callback) override;
    void cancelListenPoiTaskEvent(int listen_id) override;
    int listenFlightPoiProgress(const std::function<void(const FlightPoiProgress& msg)>& callback) override;
    void cancelListenFlightPoiProgress(int listen_id) override;

private:
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;
    std::string drone_sn_;
    std::string gateway_sn_;
    std::string drone_model_key_;
    CallbackContainer<PoiTaskEvent> poi_task_event_callbacks_;
    int poi_task_event_listen_id_ = -1;
    CallbackContainer<FlightPoiProgress> flight_poi_progress_callbacks_;
    int flight_poi_progress_listen_id_ = -1;
};

} // namespace flight_hub_sdk
