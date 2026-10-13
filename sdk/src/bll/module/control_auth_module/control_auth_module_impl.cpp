// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "control_auth_module_impl.h"

#include <base/common/logger.h>
#include "http_interface/http_interface.h"

using namespace flight_hub_sdk;

ControlAuthModuleImpl::ControlAuthModuleImpl(
    std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr,
    std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr)
    : flight_ctrl_mgr_(std::move(flight_ctrl_mgr)),
      payload_ctrl_mgr_(std::move(payload_ctrl_mgr)) {
    LOG_INFO("[ControlAuthModuleImpl] constructor");
}

void ControlAuthModuleImpl::acquireFlightControlAuthority(const std::function<void(FlightHubError)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[ControlAuthModuleImpl] acquireFlightControlAuthority: flight_ctrl_mgr_ is not initialized");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 抢夺飞行控制权(飞行控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_ACQUIRE_FLIGHT_CONTROL_AUTHORITY, state_err, "[ControlAuthModuleImpl]");

        return;
    }
    flight_ctrl_mgr_->acquireFlightControlAuthority(callback);
}

void ControlAuthModuleImpl::releaseFlightControlAuthority(const std::function<void(FlightHubError)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[ControlAuthModuleImpl] releaseFlightControlAuthority: flight_ctrl_mgr_ is not initialized");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 释放飞行控制权(飞行控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_RELEASE_FLIGHT_CONTROL_AUTHORITY, state_err, "[ControlAuthModuleImpl]");

        return;
    }
    flight_ctrl_mgr_->releaseFlightControlAuthority(callback);
}

int ControlAuthModuleImpl::listenFlightControlAuthority(const std::function<void(bool)>& callback) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[ControlAuthModuleImpl] listenFlightControlAuthority: flight_ctrl_mgr_ is not initialized");
        return -1;
    }
    return flight_ctrl_mgr_->listenFlightControlAuthority(callback);
}

void ControlAuthModuleImpl::cancelListenFlightControlAuthority(int listen_id) {
    if (!flight_ctrl_mgr_) {
        LOG_ERROR("[ControlAuthModuleImpl] cancelListenFlightControlAuthority: flight_ctrl_mgr_ is not initialized");
        return;
    }
    flight_ctrl_mgr_->cancelListenFlightControlAuthority(listen_id);
}

void ControlAuthModuleImpl::acquirePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[ControlAuthModuleImpl] acquirePayloadControlAuthority: payload_ctrl_mgr_ is not initialized");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 抢夺负载控制权(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_ACQUIRE_PAYLOAD_CONTROL_AUTHORITY, state_err, "[ControlAuthModuleImpl]");

        return;
    }
    payload_ctrl_mgr_->acquirePayloadControlAuthority(payload_index, callback);
}

void ControlAuthModuleImpl::releasePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[ControlAuthModuleImpl] releasePayloadControlAuthority: payload_ctrl_mgr_ is not initialized");

        auto state_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_STATE);

        // 埋点: 释放负载控制权(负载控制管理器未初始化, 指令未下发); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_CONTROL_AUTH_RELEASE_PAYLOAD_CONTROL_AUTHORITY, state_err, "[ControlAuthModuleImpl]");

        return;
    }
    payload_ctrl_mgr_->releasePayloadControlAuthority(payload_index, callback);
}

int ControlAuthModuleImpl::listenPayloadControlAuthority(const std::string& payload_index, const std::function<void(bool)>& callback) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[ControlAuthModuleImpl] listenPayloadControlAuthority: payload_ctrl_mgr_ is not initialized");
        return -1;
    }
    return payload_ctrl_mgr_->listenPayloadControlAuthority(payload_index, callback);
}

void ControlAuthModuleImpl::cancelListenPayloadControlAuthority(const std::string& payload_index, int listen_id) {
    if (!payload_ctrl_mgr_) {
        LOG_ERROR("[ControlAuthModuleImpl] cancelListenPayloadControlAuthority: payload_ctrl_mgr_ is not initialized");
        return;
    }
    payload_ctrl_mgr_->cancelListenPayloadControlAuthority(payload_index, listen_id);
}
