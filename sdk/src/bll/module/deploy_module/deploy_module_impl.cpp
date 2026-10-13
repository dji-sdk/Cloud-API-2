// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "deploy_module_impl.h"

#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include "http_interface/http_interface.h"

using namespace flight_hub_sdk;

DeployModuleImpl::DeployModuleImpl(const std::string& sn)
    : sn_(sn) {
    LOG_INFO("[DeployModuleImpl] constructor, sn: %s", sn_.c_str());
    registerNotificationCenterListeners();
}

DeployModuleImpl::~DeployModuleImpl() {
    LOG_INFO("[DeployModuleImpl] destructor, sn: %s", sn_.c_str());
    if (dock_departure_point_listen_id_ >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_departure_point_listen_id_);
    }
    if (aircraft_visual_scout_status_listen_id_ >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_visual_scout_status_listen_id_);
    }
    if (aircraft_visual_scout_abnormal_info_listen_id_ >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_visual_scout_abnormal_info_listen_id_);
    }
    if (aircraft_visual_scout_obstacle_info_listen_id_ >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_visual_scout_obstacle_info_listen_id_);
    }
    if (aircraft_ephemeris_download_state_listen_id_ >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_ephemeris_download_state_listen_id_);
    }
    if (aircraft_ephemeris_info_listen_id_ >= 0) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(aircraft_ephemeris_info_listen_id_);
    }
}

void DeployModuleImpl::registerNotificationCenterListeners() {
    WeakDummy(p);

    dock_departure_point_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockDeparturePoint(
        [this, p](const notification_center::DockDeparturePoint& msg) -> void {
            WeakDummyReturn(p);
            DockDeparturePoint dock_departure_point;
            dock_departure_point.longitude = msg.longitude;
            dock_departure_point.latitude = msg.latitude;
            dock_departure_point.height = msg.height;

            LOG_DEBUG("[DeployModuleImpl] listenDockDeparturePoint, lon: %f, lat: %f, height: %f",
                dock_departure_point.longitude, dock_departure_point.latitude,
                dock_departure_point.height);
            dock_departure_point_callbacks_.notifyCallbackIfChanged(dock_departure_point);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DeployModuleImpl] listenDockDeparturePoint, error: %d", error_code);
            }
        }, sn_, notification_center::NotificationFrequency_OnChanged);

    aircraft_visual_scout_status_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftVisualScoutStatus(
        [this, p](const notification_center::AircraftVisualScoutStatus& msg) -> void {
            WeakDummyReturn(p);
            flight_hub_sdk::AircraftVisualScoutStatus res{};
            res.task_id = msg.task_id;
            res.visual_scout_stage = msg.visual_scout_stage;
            res.scout_wayline_generate_remain_time = msg.scout_wayline_generate_remain_time;

            LOG_INFO("[DeployModuleImpl] [%s] ListenAircraftVisualScoutStatus: task_id: \"%s\", stage: %d, remain_time: %ld",
                sn_.c_str(), msg.task_id.c_str(), msg.visual_scout_stage, (long)msg.scout_wayline_generate_remain_time);

            aircraft_visual_scout_status_callbacks_.notifyCallbackIfChanged(res);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DeployModuleImpl] [%s] ListenAircraftVisualScoutStatus error: %d", sn_.c_str(), error_code);
            }
        }, sn_, notification_center::NotificationFrequency_OnChanged);

    aircraft_visual_scout_abnormal_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftVisualScoutAbnormalInfo(
        [this, p](const notification_center::AircraftVisualScoutAbnormalInfo& msg) -> void {
            WeakDummyReturn(p);
            flight_hub_sdk::AircraftVisualScoutAbnormalInfo res{};
            res.task_id = msg.task_id;
            res.err_code = msg.err_code;

            LOG_INFO("[DeployModuleImpl] [%s] ListenAircraftVisualScoutAbnormalInfo: task_id: \"%s\", err_code: %d",
                sn_.c_str(), msg.task_id.c_str(), msg.err_code);

            aircraft_visual_scout_abnormal_info_callbacks_.notifyCallbackIfChanged(res);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DeployModuleImpl] [%s] ListenAircraftVisualScoutAbnormalInfo error: %d", sn_.c_str(), error_code);
            }
        }, sn_, notification_center::NotificationFrequency_OnChanged);

    aircraft_visual_scout_obstacle_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftVisualScoutObstacleInfo(
        [this, p](const notification_center::AircraftVisualScoutObstacleInfo& msg) -> void {
            WeakDummyReturn(p);
            flight_hub_sdk::AircraftVisualScoutObstacleInfo res{};
            res.task_id = msg.task_id;
            res.obstacle_tag = msg.obstacle_tag;

            LOG_INFO("[DeployModuleImpl] [%s] ListenAircraftVisualScoutObstacleInfo: task_id: \"%s\", obstacle_tag: %s",
                sn_.c_str(), msg.task_id.c_str(), msg.obstacle_tag ? "true" : "false");

            aircraft_visual_scout_obstacle_info_callbacks_.notifyCallbackIfChanged(res);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DeployModuleImpl] [%s] ListenAircraftVisualScoutObstacleInfo error: %d", sn_.c_str(), error_code);
            }
        }, sn_, notification_center::NotificationFrequency_OnChanged);

    aircraft_ephemeris_download_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftEphemerisDownloadState(
        [this, p](const notification_center::EphemerisDownloadState& msg) -> void {
            WeakDummyReturn(p);
            flight_hub_sdk::EphemerisDownloadState res{};
            res.state = msg.state;

            LOG_INFO("[DeployModuleImpl] [%s] ListenAircraftEphemerisDownloadState: state: %d", sn_.c_str(), msg.state);

            aircraft_ephemeris_download_state_callbacks_.notifyCallbackIfChanged(res);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DeployModuleImpl] [%s] ListenAircraftEphemerisDownloadState error: %d", sn_.c_str(), error_code);
            }
        }, sn_, notification_center::NotificationFrequency_OnChanged);

    aircraft_ephemeris_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenAircraftEphemerisInfo(
        [this, p](const notification_center::EphemerisInfo& msg) -> void {
            WeakDummyReturn(p);
            flight_hub_sdk::EphemerisInfo res{};
            res.ephemeris = msg.ephemeris;

            LOG_INFO("[DeployModuleImpl] [%s] ListenAircraftEphemerisInfo: ephemeris: %s", sn_.c_str(), msg.ephemeris.c_str());

            aircraft_ephemeris_info_callbacks_.notifyCallbackIfChanged(res);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DeployModuleImpl] [%s] ListenAircraftEphemerisInfo error: %d", sn_.c_str(), error_code);
            }
        }, sn_, notification_center::NotificationFrequency_OnChanged);
}

int DeployModuleImpl::listenDockDeparturePoint(const std::function<void(const DockDeparturePoint& msg)>& callback) {
    auto listen_id = dock_departure_point_callbacks_.addCallback(callback);
    LOG_INFO("[DeployModuleImpl][%s] listenDockDeparturePoint listen_id: %d", sn_.c_str(), listen_id);
    return listen_id;
}

void DeployModuleImpl::cancelListenDockDeparturePoint(int listen_id) {
    LOG_INFO("[DeployModuleImpl][%s] cancelListenDockDeparturePoint listen_id: %d", sn_.c_str(), listen_id);
    dock_departure_point_callbacks_.removeCallback(listen_id);
}

int DeployModuleImpl::listenAircraftVisualScoutStatus(const std::function<void(const AircraftVisualScoutStatus& msg)>& callback) {
    auto listen_id = aircraft_visual_scout_status_callbacks_.addCallback(callback);
    LOG_INFO("[DeployModuleImpl][%s] listenAircraftVisualScoutStatus listen_id: %d", sn_.c_str(), listen_id);
    return listen_id;
}

void DeployModuleImpl::cancelListenAircraftVisualScoutStatus(int listen_id) {
    LOG_INFO("[DeployModuleImpl][%s] cancelListenAircraftVisualScoutStatus listen_id: %d", sn_.c_str(), listen_id);
    aircraft_visual_scout_status_callbacks_.removeCallback(listen_id);
}

int DeployModuleImpl::listenAircraftVisualScoutAbnormalInfo(const std::function<void(const AircraftVisualScoutAbnormalInfo& msg)>& callback) {
    auto listen_id = aircraft_visual_scout_abnormal_info_callbacks_.addCallback(callback);
    LOG_INFO("[DeployModuleImpl][%s] listenAircraftVisualScoutAbnormalInfo listen_id: %d", sn_.c_str(), listen_id);
    return listen_id;
}

void DeployModuleImpl::cancelListenAircraftVisualScoutAbnormalInfo(int listen_id) {
    LOG_INFO("[DeployModuleImpl][%s] cancelListenAircraftVisualScoutAbnormalInfo listen_id: %d", sn_.c_str(), listen_id);
    aircraft_visual_scout_abnormal_info_callbacks_.removeCallback(listen_id);
}

int DeployModuleImpl::listenAircraftVisualScoutObstacleInfo(const std::function<void(const AircraftVisualScoutObstacleInfo& msg)>& callback) {
    auto listen_id = aircraft_visual_scout_obstacle_info_callbacks_.addCallback(callback);
    LOG_INFO("[DeployModuleImpl][%s] listenAircraftVisualScoutObstacleInfo listen_id: %d", sn_.c_str(), listen_id);
    return listen_id;
}

void DeployModuleImpl::cancelListenAircraftVisualScoutObstacleInfo(int listen_id) {
    LOG_INFO("[DeployModuleImpl][%s] cancelListenAircraftVisualScoutObstacleInfo listen_id: %d", sn_.c_str(), listen_id);
    aircraft_visual_scout_obstacle_info_callbacks_.removeCallback(listen_id);
}

int DeployModuleImpl::listenAircraftEphemerisDownloadState(const std::function<void(const EphemerisDownloadState& msg)>& callback) {
    auto listen_id = aircraft_ephemeris_download_state_callbacks_.addCallback(callback);
    LOG_INFO("[DeployModuleImpl][%s] listenAircraftEphemerisDownloadState listen_id: %d", sn_.c_str(), listen_id);
    return listen_id;
}

void DeployModuleImpl::cancelListenAircraftEphemerisDownloadState(int listen_id) {
    LOG_INFO("[DeployModuleImpl][%s] cancelListenAircraftEphemerisDownloadState listen_id: %d", sn_.c_str(), listen_id);
    aircraft_ephemeris_download_state_callbacks_.removeCallback(listen_id);
}

int DeployModuleImpl::listenAircraftEphemerisInfo(const std::function<void(const EphemerisInfo& msg)>& callback) {
    auto listen_id = aircraft_ephemeris_info_callbacks_.addCallback(callback);
    LOG_INFO("[DeployModuleImpl][%s] listenAircraftEphemerisInfo listen_id: %d", sn_.c_str(), listen_id);
    return listen_id;
}

void DeployModuleImpl::cancelListenAircraftEphemerisInfo(int listen_id) {
    LOG_INFO("[DeployModuleImpl][%s] cancelListenAircraftEphemerisInfo listen_id: %d", sn_.c_str(), listen_id);
    aircraft_ephemeris_info_callbacks_.removeCallback(listen_id);
}

void DeployModuleImpl::downloadEphemeris(const DownloadEphemerisParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[DeployModuleImpl] downloadEphemeris(), dock sn: %s", sn_.c_str());

    DroneDownloadEphemerisRequest request;
    request.sn = sn_;
    request.type = "flight";
    request.method = "ephemeris_download";
    request.data.ephemeris_url = params.ephemeris_url;
    request.data.ephemeris_file_name = params.ephemeris_file_name;

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        drone_download_ephemeris(request, [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() -> void {
                // 埋点: 下载星历数据; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEPLOY_DOWNLOAD_EPHEMERIS, err, "[DeployModuleImpl]");
                LOG_INFO("[DeployModuleImpl] downloadEphemeris(), err_code = %d", (int)err.error_code);
                if (err.error_code == FlightHubSdkErrorCode::SUCCESS) {
                    callback(FlightHubError::success());
                } else {
                    callback(err);
                }
            });
        });
    });
}

void DeployModuleImpl::executeInOutWaylineDetect(const InOutWaylineDetectParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[DeployModuleImpl] executeInOutWaylineDetect(), dock sn: %s", sn_.c_str());

    DroneInOutWaylineDetectRequest request;
    request.sn = sn_;
    request.type = "flight";
    request.method = "visual_scout_cmd";
    request.data.task_id = params.task_id;
    request.data.task_cmd = params.task_cmd;
    request.data.is_auto_visual_scout = params.is_auto_visual_scout;
    request.data.scout_relative_height = params.scout_relative_height;
    request.data.patrol_relative_height = params.patrol_relative_height;

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        drone_in_out_wayline_detect(request, [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() -> void {
                // 埋点: 执行进离场航线侦察任务（开始/暂停）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEPLOY_EXECUTE_IN_OUT_WAYLINE_DETECT, err, "[DeployModuleImpl]");
                LOG_INFO("[DeployModuleImpl] executeInOutWaylineDetect(), err_code = %d", (int)err.error_code);
                if (err.error_code == FlightHubSdkErrorCode::SUCCESS) {
                    callback(FlightHubError::success());
                } else {
                    callback(err);
                }
            });
        });
    });
}

void DeployModuleImpl::setInOutWaylineFlight(const InOutWaylineFlightSettingParams& params, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[DeployModuleImpl] setInOutWaylineFlight(), dock sn: %s", sn_.c_str());

    DroneInOutWaylineFlightSettingRequest request;
    request.sn = params.sn.empty() ? sn_ : params.sn;
    request.min_return_home_height = params.min_return_home_height;
    request.lost_contact_action = params.lost_contact_action;
    request.max_height_limit = params.max_height_limit;
    request.max_distance_limit = params.max_distance_limit;

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        drone_in_out_wayline_flight_setting(request, [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() -> void {
                // 埋点: 设置进离场航线飞行参数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEPLOY_SET_IN_OUT_WAYLINE_FLIGHT, err, "[DeployModuleImpl]");
                LOG_INFO("[DeployModuleImpl] setInOutWaylineFlight(), err_code = %d", (int)err.error_code);
                if (err.error_code == FlightHubSdkErrorCode::SUCCESS) {
                    callback(FlightHubError::success());
                } else {
                    callback(err);
                }
            });
        });
    });
}

void DeployModuleImpl::getInOutWaylineFlightSetting(const std::function<void(const InOutWaylineFlightSettingParams& params, FlightHubError err)>& callback) {
    LOG_INFO("[DeployModuleImpl] getInOutWaylineFlightSetting(), dock sn: %s", sn_.c_str());

    WeakDummy(weak_ptr);
    get_scout_flight_setting(sn_, [this, weak_ptr, callback](const InOutWaylineFlightSettingParams& params, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([params, err, callback]() {
            // 埋点: 获取进离场航线飞行设置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DEPLOY_GET_IN_OUT_WAYLINE_FLIGHT_SETTING, err, "[DeployModuleImpl]");
            LOG_INFO("[DeployModuleImpl] getInOutWaylineFlightSetting(), err_code = %d", (int)err.error_code);
            if (callback) {
                callback(params, err);
            }
        });
    });
}
