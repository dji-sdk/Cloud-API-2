// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/fly_to_point.h"
#include "tool/callback_container.h"
#include "i_message_engine.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class FlyToPointModuleImpl : public IFlyToPointModule, public drc_sdk::common::AsyncCaptureProtect{
public:
    explicit FlyToPointModuleImpl(std::shared_ptr<message_engine::IMessageEngine> message_engine, const std::string& drone_sn, const std::string& gateway_sn);
    ~FlyToPointModuleImpl();

    void start(const FlyToPointParams& params, const std::function<void(FlightHubError err, std::string fly_to_id)> callback) override;
    void drcStop(const std::function<void(FlightHubError err)> callback) override;
    void updateTargetPoints(const FlyToUpdateTargetPointsParams& params, const std::function<void(FlightHubError err)>& callback) override;
    int listenFlyToTaskEvent(const std::function<void(const FlyToTaskEvent& msg)>& callback) override;
    void cancelListenFlyToTaskEvent(int listen_id) override;
    int subscribeFlyToDroneSelfPlanPath(const std::function<void(const FlyToDroneSelfPlanPath& path)>& callback) override;
    void unsubscribeFlyToDroneSelfPlanPath(int listen_id) override;
    int subscribeFlyToPointProgress(const std::function<void(const FlyToPointProgress& progress)>& callback) override;
    void unsubscribeFlyToPointProgress(int listen_id) override;

private:
    std::shared_ptr<message_engine::IMessageEngine> message_engine_;
    std::string drone_sn_;
    std::string gateway_sn_;
    CallbackContainer<FlyToTaskEvent> flyto_task_event_callbacks_;
    int flyto_task_event_listen_id_ = -1;
};

} // namespace flight_hub_sdk