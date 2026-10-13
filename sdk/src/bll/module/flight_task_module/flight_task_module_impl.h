// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/flight_task_module.h"
#include "http_interface/http_interface.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class FlightTaskModuleImpl : public IFlightTaskModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit FlightTaskModuleImpl();
    ~FlightTaskModuleImpl() override;

    void getFlightTaskList(const FlightTaskListParams& params, std::function<void(FlightHubError err, const FlightTaskListResult& result)> callback) override;
    void getCommandTaskList(const GetCommandTaskListParams& params, std::function<void(FlightHubError err, const GetCommandTaskListResults& results)> callback) override;
    void getFlightTaskTrack(const GetFlightTaskTrackParams& params, std::function<void(FlightHubError err, const GetFlightTaskTrackResults& results)> callback) override;
    void createFlightTask(const CreateTaskReq& req, std::function<void(FlightHubError err, const CreateTaskResp& result)> callback) override;
    void getFlightTaskDetail(const std::string& task_uuid, std::function<void(FlightHubError err, const TaskDetail& result)> callback) override;
    void editFlightTask(const std::string& task_uuid, const EditTaskReq& req, std::function<void(FlightHubError err, const CreateTaskResp& result)> callback) override;
    void deleteFlightTask(const std::string& task_uuid, std::function<void(FlightHubError err)> callback) override;
    void updateFlightTaskStatus(const std::string& task_uuid, int status, std::function<void(FlightHubError err)> callback) override;
    void batchUpdateFlightTaskStatus(const BatchUpdateStatusReq& req, std::function<void(FlightHubError err)> callback) override;
    void getDeviceRecommendation(const DeviceRecommendationReq& req, std::function<void(FlightHubError err, const DeviceRecommendationResp& result)> callback) override;
    void getDeviceTcas(const std::vector<std::string>& center_sn, std::function<void(FlightHubError err, const GetTCAInfosResp& result)> callback) override;
    void updateDeviceTcas(const std::vector<std::string>& center_sn, std::function<void(FlightHubError err)> callback) override;
    void startRoaming(const StartRoamingParams& params, std::function<void(FlightHubError err)> callback) override;
    void setRoamingStationSn(const SetRoamingStationSnParams& params,
                             std::function<void(FlightHubError err, const SetRoamingStationSnResult& result)> callback) override;
    void getInFlightTaskTopos(const std::vector<std::string>& drone_sns,
                              std::function<void(FlightHubError err, const GetInFlightTaskToposResult& result)> callback) override;
    int listenTaskTopoChanged(const std::function<void(const TaskTopoChangedEvent&)>& callback) override;
    void cancelListenTaskTopoChanged(int listen_id) override;
};

}  // namespace flight_hub_sdk
