// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "dock_control_module_impl.h"

#include <base/common/logger.h>
#include "bll/tool/flight_hub_utils.h"
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

using namespace flight_hub_sdk;
using namespace notification_center;

DockControlModuleImpl::DockControlModuleImpl(const std::string& sn)
    : sn_(sn) {
    LOG_INFO("[DockControlModuleImpl] constructor, sn: %s", sn_.c_str());
    registerNotificationCenterListeners();
}

DockControlModuleImpl::~DockControlModuleImpl() {
    if (dock_air_conditioner_status_info_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_air_conditioner_status_info_listen_id_);
        dock_air_conditioner_status_info_listen_id_ = -1;
    }
    if (dock_supplement_light_state_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(dock_supplement_light_state_listen_id_);
        dock_supplement_light_state_listen_id_ = -1;
    }
}

void DockControlModuleImpl::registerNotificationCenterListeners() {
    WeakDummy(p);

    dock_air_conditioner_status_info_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockAirConditionerStatusInfo(
        [this, p](const notification_center::DockAirConditionerStatusInfo& msg) -> void {
            WeakDummyReturn(p);
            DockAirConditionerStatusInfo status_info;
            status_info.air_conditioner_state = static_cast<int>(msg.air_conditioner_state);
            status_info.switch_time = msg.switch_time;

            LOG_DEBUG("[DockControlModuleImpl] listenDockAirConditionerStatusInfo, air_conditioner_state: %d, switch_time: %d",
                status_info.air_conditioner_state, status_info.switch_time);
            dock_air_conditioner_status_info_callbacks_.notifyCallbackIfChanged(status_info);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockControlModuleImpl] listenDockAirConditionerStatusInfo, error: %d", error_code);
            }
        },
        sn_, notification_center::NotificationFrequency_OnChanged);
    if (dock_air_conditioner_status_info_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockControlModuleImpl] ListenDockAirConditionerStatusInfo, error: %d", dock_air_conditioner_status_info_listen_id_);
    }

    dock_supplement_light_state_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDockSupplementLightState(
        [this, p](const notification_center::DockSupplementLightState& msg) -> void {
            WeakDummyReturn(p);
            DockSupplementLightState light_state;
            light_state.supplement_light_state = msg.supplement_light_state;

            LOG_DEBUG("[DockControlModuleImpl] listenDockSupplementLightState, supplement_light_state: %d",
                light_state.supplement_light_state);
            dock_supplement_light_state_callbacks_.notifyCallbackIfChanged(light_state);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DockControlModuleImpl] listenDockSupplementLightState, error: %d", error_code);
            }
        },
        sn_, notification_center::NotificationFrequency_OnChanged);
    if (dock_supplement_light_state_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DockControlModuleImpl] ListenDockSupplementLightState, error: %d", dock_supplement_light_state_listen_id_);
    }
}

void DockControlModuleImpl::changeDockCameraPosition(
    const LiveInfo& live_info,
    int camera_position,
    const std::string& gateway_sn,
    const std::function<void(FlightHubError err)>& callback) {
    change_dock_camera_position(live_info, camera_position, gateway_sn, [callback](FlightHubError err) {
        // 埋点: 切换机场舱内/舱外摄像头; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DOCK_CTRL_CHANGE_DOCK_CAMERA_POSITION, err, "[DockControlModuleImpl]");
        if (callback) {
            callback(err);
        }
    });
}

int DockControlModuleImpl::listenDockAirConditionerStatusInfo(const std::function<void(const DockAirConditionerStatusInfo& msg)>& callback) {
    return dock_air_conditioner_status_info_callbacks_.addCallback(callback);
}

void DockControlModuleImpl::cancelListenDockAirConditionerStatusInfo(int listen_id) {
    dock_air_conditioner_status_info_callbacks_.removeCallback(listen_id);
}

int DockControlModuleImpl::listenDockSupplementLightState(const std::function<void(const DockSupplementLightState& msg)>& callback) {
    return dock_supplement_light_state_callbacks_.addCallback(callback);
}

void DockControlModuleImpl::cancelListenDockSupplementLightState(int listen_id) {
    dock_supplement_light_state_callbacks_.removeCallback(listen_id);
}

void DockControlModuleImpl::setDockSupplementLightState(bool state, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("DockControlModuleImpl: setDockSupplementLightState, sn: %s, state: %s", sn_.c_str(), state ? "open" : "close");

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, state, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        set_dock_supplement_light_state(sn_, state, [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([callback, err]() -> void {
                // 埋点: 设置机场补光灯开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DOCK_CTRL_SET_DOCK_SUPPLEMENT_LIGHT_STATE, err, "[DockControlModuleImpl]");
                LOG_INFO("DockControlModuleImpl: setDockSupplementLightState, err_code: %d", (int)err.error_code);
                callback(err);
            });
        });
    });
}

void DockControlModuleImpl::setDockChargeState(bool state, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("DockControlModuleImpl: setDockChargeState, sn: %s, state: %s", sn_.c_str(), state ? "open" : "close");

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, state, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        set_dock_charge_state(sn_, state, [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([callback, err]() -> void {
                // 埋点: 设置机场充电开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DOCK_CTRL_SET_DOCK_CHARGE_STATE, err, "[DockControlModuleImpl]");
                LOG_INFO("DockControlModuleImpl: setDockChargeState, err_code: %d", (int)err.error_code);
                callback(err);
            });
        });
    });
}

void DockControlModuleImpl::setDockCoverState(int mode, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("DockControlModuleImpl: setDockCoverState, sn: %s, mode: %d", sn_.c_str(), mode);

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, mode, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        set_dock_cover_state(sn_, mode, [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([callback, err]() -> void {
                // 埋点: 设置机场舱盖开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DOCK_CTRL_SET_DOCK_COVER_STATE, err, "[DockControlModuleImpl]");
                LOG_INFO("DockControlModuleImpl: setDockCoverState, err_code: %d", (int)err.error_code);
                callback(err);
            });
        });
    });
}

void DockControlModuleImpl::setDockAirConditionerMode(int mode, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("DockControlModuleImpl: setDockAirConditionerMode, sn: %s, mode: %d", sn_.c_str(), mode);

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, mode, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        set_dock_air_conditioner_mode(sn_, mode, [this, weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([callback, err]() -> void {
                // 埋点: 设置机场空调模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DOCK_CTRL_SET_DOCK_AIR_CONDITIONER_MODE, err, "[DockControlModuleImpl]");
                LOG_INFO("DockControlModuleImpl: setDockAirConditionerMode, err_code: %d", (int)err.error_code);
                callback(err);
            });
        });
    });
}
