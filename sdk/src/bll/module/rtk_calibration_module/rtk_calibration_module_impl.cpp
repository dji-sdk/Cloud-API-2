// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "rtk_calibration_module_impl.h"

#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include <base/common/logger.h>

namespace flight_hub_sdk {

RtkCalibrationModuleImpl::RtkCalibrationModuleImpl(const std::string& sn, std::string device_module)
    : sn_(sn), device_module_(std::move(device_module)) {
    LOG_INFO("[RtkCalibrationModuleImpl] constructor, sn: %s, device_module: %s",
             sn_.c_str(), device_module_.c_str());
}

RtkCalibrationModuleImpl::~RtkCalibrationModuleImpl() = default;

void RtkCalibrationModuleImpl::calibrationByNetwork(
    const NetworkRtkCalibrationParams& params,
    const std::function<void(FlightHubError err, RtkCalibrationResult result)>& callback) {
    WeakDummy(weak_ptr);
    NetworkRtkCalibrationRequest request;
    request.device_sn = sn_;
    request.device_module = device_module_;
    request.calibration_type = 2;
    request.custom_calibration_data.account = params.account;
    request.custom_calibration_data.password = params.password;
    request.custom_calibration_data.host = params.host;
    request.custom_calibration_data.port = params.port;
    request.custom_calibration_data.mount_point = params.mount_point;
    request.custom_calibration_data.version = params.version;
    post_rtk_calibration_by_network(request, [this, weak_ptr, callback](const RtkCalibrationResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: RTK 标定（网络模式）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_RTK_CALIB_BY_NETWORK, err, "[RtkCalibrationModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("calibrationByNetwork: err: %d, message: %s", (int)err.error_code, response.status.c_str());
            }
            RtkCalibrationResult result;
            result.message = response.status;
            result.bid = response.bid;
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void RtkCalibrationModuleImpl::calibrationByManual(
    const ManulRtkCalibrationParams& params,
    const std::function<void(FlightHubError err, RtkCalibrationResult result)>& callback) {
    WeakDummy(weak_ptr);
    ManualRtkCalibrationRequest request;
    request.device_sn = sn_;
    request.device_module = device_module_;
    request.calibration_type = 1;
    request.manual_calibration_data.longitude = params.longitude;
    request.manual_calibration_data.latitude = params.latitude;
    request.manual_calibration_data.height = params.height;
    post_rtk_calibration_by_manual(request, [this, weak_ptr, callback](const RtkCalibrationResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: RTK 标定（手动模式）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_RTK_CALIB_BY_MANUAL, err, "[RtkCalibrationModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("calibrationByManual: err: %d, message: %s", (int)err.error_code, response.status.c_str());
            }
            RtkCalibrationResult result;
            result.message = response.status;
            result.bid = response.bid;
            if (callback) {
                callback(err, result);
            }
        });
    });
}

int RtkCalibrationModuleImpl::subscribeCalibrationProgress(
    const std::function<void(RtkCalibrationResult result)>& callback) {
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceRtkCalibration(
        [this, weak_ptr, callback](const notification_center::DeviceRtkCalibration& msg) -> void {
            WeakDummyReturn(weak_ptr);
            if (!callback) {
                return;
            }
            RtkCalibrationResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[RtkCalibrationModuleImpl] subscribeCalibrationProgress, bid: %s, result: %d",
                     msg.bid.c_str(), msg.result);
            callback(result);
        },
        [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_INFO("[RtkCalibrationModuleImpl] subscribeCalibrationProgress, error: %d", error_code);
            }
        },
        sn_, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void RtkCalibrationModuleImpl::unsubscribeCalibrationProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

} // namespace flight_hub_sdk
