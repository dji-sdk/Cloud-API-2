// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/deploy_module.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class DeployModuleImpl : public IDeployModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    DeployModuleImpl(const std::string& sn);
    ~DeployModuleImpl() override;

    int listenDockDeparturePoint(const std::function<void(const DockDeparturePoint& msg)>& callback) override;
    void cancelListenDockDeparturePoint(int listen_id) override;

    int listenAircraftVisualScoutStatus(const std::function<void(const AircraftVisualScoutStatus& msg)>& callback) override;
    void cancelListenAircraftVisualScoutStatus(int listen_id) override;

    int listenAircraftVisualScoutAbnormalInfo(const std::function<void(const AircraftVisualScoutAbnormalInfo& msg)>& callback) override;
    void cancelListenAircraftVisualScoutAbnormalInfo(int listen_id) override;

    int listenAircraftVisualScoutObstacleInfo(const std::function<void(const AircraftVisualScoutObstacleInfo& msg)>& callback) override;
    void cancelListenAircraftVisualScoutObstacleInfo(int listen_id) override;

    int listenAircraftEphemerisDownloadState(const std::function<void(const EphemerisDownloadState& msg)>& callback) override;
    void cancelListenAircraftEphemerisDownloadState(int listen_id) override;

    int listenAircraftEphemerisInfo(const std::function<void(const EphemerisInfo& msg)>& callback) override;
    void cancelListenAircraftEphemerisInfo(int listen_id) override;

    void downloadEphemeris(const DownloadEphemerisParams& params, const std::function<void(FlightHubError err)>& callback) override;
    void executeInOutWaylineDetect(const InOutWaylineDetectParams& params, const std::function<void(FlightHubError err)>& callback) override;
    void setInOutWaylineFlight(const InOutWaylineFlightSettingParams& params, const std::function<void(FlightHubError err)>& callback) override;
    void getInOutWaylineFlightSetting(const std::function<void(const InOutWaylineFlightSettingParams& params, FlightHubError err)>& callback) override;

private:
    void registerNotificationCenterListeners();

    std::string sn_;

    CallbackContainer<DockDeparturePoint> dock_departure_point_callbacks_;
    CallbackContainer<AircraftVisualScoutStatus> aircraft_visual_scout_status_callbacks_;
    CallbackContainer<AircraftVisualScoutAbnormalInfo> aircraft_visual_scout_abnormal_info_callbacks_;
    CallbackContainer<AircraftVisualScoutObstacleInfo> aircraft_visual_scout_obstacle_info_callbacks_;
    CallbackContainer<EphemerisDownloadState> aircraft_ephemeris_download_state_callbacks_;
    CallbackContainer<EphemerisInfo> aircraft_ephemeris_info_callbacks_;

    int dock_departure_point_listen_id_ = -1;
    int aircraft_visual_scout_status_listen_id_ = -1;
    int aircraft_visual_scout_abnormal_info_listen_id_ = -1;
    int aircraft_visual_scout_obstacle_info_listen_id_ = -1;
    int aircraft_ephemeris_download_state_listen_id_ = -1;
    int aircraft_ephemeris_info_listen_id_ = -1;
};

} // namespace flight_hub_sdk
