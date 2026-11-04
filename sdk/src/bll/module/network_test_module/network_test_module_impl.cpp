// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "network_test_module_impl.h"

#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include <base/common/logger.h>
#include "session/session_bm.h"

namespace flight_hub_sdk {

NetworkTestModuleImpl::NetworkTestModuleImpl(const std::string& sn, int session_id)
    : sn_(sn), session_id_(session_id) {
    LOG_INFO("[NetworkTestModuleImpl] constructor, sn: %s", sn_.c_str());
}

NetworkTestModuleImpl::~NetworkTestModuleImpl() = default;

void NetworkTestModuleImpl::startNetworkTest(
    const std::function<void(FlightHubError err, NetworkTestProgressResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_start_network_speed_test(sn_, [this, weak_ptr, callback](const StartNetworkSpeedTestResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response, this, weak_ptr]() {
            WeakDummyReturn(weak_ptr);
            // 埋点: 开始网络测速; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_NETWORK_TEST_START, err, session_id_, "[NetworkTestModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("startNetworkTest: err: %d, message: %s", (int)err.error_code, response.status.c_str());
            }
            NetworkTestProgressResult result;
            result.message = response.status;
            result.bid = response.bid;
            result.download_speed = 0.0;
            result.upload_speed = 0.0;
            if (callback) {
                callback(err, result);
            }
        });
    }, session_id_);
}

void NetworkTestModuleImpl::stopNetworkTest(
    const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    put_stop_network_speed_test(sn_, [this, weak_ptr, callback](const StopNetworkSpeedTestResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response, this, weak_ptr]() {
            WeakDummyReturn(weak_ptr);
            // 埋点: 停止网络测速; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_NETWORK_TEST_STOP, err, session_id_, "[NetworkTestModuleImpl]");
            LOG_INFO("stopNetworkTest: err: %d, message: %s", (int)err.error_code, response.status.c_str());
            if (callback) {
                callback(err);
            }
        });
    }, session_id_);
}

void NetworkTestModuleImpl::getNetworkTestProgress(
    const std::function<void(FlightHubError err, NetworkTestProgressResult result)>& callback) {
    WeakDummy(weak_ptr);
    get_network_speed_test_progress(sn_, [this, weak_ptr, callback](const NetworkSpeedTestProgressResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response, this, weak_ptr]() {
            WeakDummyReturn(weak_ptr);
            // 埋点: 获取网络测速进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_NETWORK_TEST_GET_PROGRESS, err, session_id_, "[NetworkTestModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("getNetworkTestProgress: err: %d, message: %s", (int)err.error_code, response.status.c_str());
            }
            NetworkTestProgressResult result;
            result.message = response.status;
            result.create_time = response.create_time;
            result.update_time = response.update_time;
            result.download_speed = response.speed_result.download_speed;
            result.upload_speed = response.speed_result.upload_speed;
            if (callback) {
                callback(err, result);
            }
        });
    }, session_id_);
}

int NetworkTestModuleImpl::subscribeNetworkTestProgress(
    const std::function<void(NetworkTestProgressResult result)>& callback) {
    auto bm = ResolveBusinessManager(session_id_);
    LOG_INFO("subscribeNetworkTestProgress: sn: %s", sn_.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = -1;
    if (bm) {
        listen_id = bm->ListenDeviceNetworkSpeedTestProgress(
        [this, weak_ptr, callback](const notification_center::DeviceNetworkSpeedTestProgress& msg) -> void {
            WeakDummyReturn(weak_ptr);
            if (!callback) {
                return;
            }
            NetworkTestProgressResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            result.download_speed = msg.speed_result.download_speed;
            result.upload_speed = msg.speed_result.upload_speed;
            LOG_INFO("[NetworkTestModuleImpl] subscribeNetworkTestProgress, bid: %s, result: %d",
                     msg.bid.c_str(), msg.result);
            callback(result);
        },
        [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_INFO("[NetworkTestModuleImpl] subscribeNetworkTestProgress, error: %d", error_code);
            }
        },
        sn_, notification_center::NotificationFrequency_OnChanged);
    } else {
        LOG_ERROR("[session_bm] null, skip Listen for session");
    }
    return listen_id;
}

void NetworkTestModuleImpl::unsubscribeNetworkTestProgress(int listen_id) {
    auto bm = ResolveBusinessManager(session_id_);
    if (bm) {
        bm->CancelObserve(listen_id);
    }
}

} // namespace flight_hub_sdk
