// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "relay_control_module_impl.h"

#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

RelayControlModuleImpl::RelayControlModuleImpl(const std::string& sn)
    : sn_(sn) {
    LOG_INFO("[RelayControlModuleImpl] constructor, sn: %s", sn_.c_str());
}

RelayControlModuleImpl::~RelayControlModuleImpl() = default;

void RelayControlModuleImpl::setRelayBroadcastHide(bool is_broadcast_hide, const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    post_relay_broadcast_hide(sn_, is_broadcast_hide, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 设置中继站广播隐藏状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_RELAY_SET_BROADCAST_HIDE, err, "[RelayControlModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("setRelayBroadcastHide: err: %d", (int)err.error_code);
            }
            if (callback) {
                callback(err);
            }
        });
    });
}

void RelayControlModuleImpl::setRelaySSID(const std::string& device_name, const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    post_relay_ssid(sn_, device_name, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 设置中继站 SSID; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_RELAY_SET_SSID, err, "[RelayControlModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("setRelaySSID: err: %d", (int)err.error_code);
            }
            if (callback) {
                callback(err);
            }
        });
    });
}

void RelayControlModuleImpl::setRelayPassword(const std::string& passwd, const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    post_relay_password(sn_, passwd, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 设置中继站密码; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_RELAY_SET_PASSWORD, err, "[RelayControlModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("setRelayPassword: err: %d", (int)err.error_code);
            }
            if (callback) {
                callback(err);
            }
        });
    });
}

void RelayControlModuleImpl::setRelaySearchMode(int relay_search_mode, const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    post_relay_search_mode(sn_, relay_search_mode, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 设置中继站搜索模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_RELAY_SET_SEARCH_MODE, err, "[RelayControlModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("setRelaySearchMode: err: %d", (int)err.error_code);
            }
            if (callback) {
                callback(err);
            }
        });
    });
}

void RelayControlModuleImpl::setRelayOccupiedStatus(int occupied_status, const std::function<void(FlightHubError err, SetRelayOccupiedStatusResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_relay_occupied_status(sn_, occupied_status, [this, weak_ptr, callback](const SetRelayOccupiedStatusResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 设置中继站占用状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_RELAY_SET_OCCUPIED_STATUS, err, "[RelayControlModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("setRelayOccupiedStatus: err: %d, message: %s", (int)err.error_code, response.status.c_str());
            }
            SetRelayOccupiedStatusResult result;
            result.status = response.status;
            result.bid = response.bid;
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void RelayControlModuleImpl::relayReboot(const std::function<void(FlightHubError err, RelayRebootResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_relay_reboot(sn_, [this, weak_ptr, callback](const RelayRebootResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 中继站重启; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_RELAY_REBOOT, err, "[RelayControlModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("relayReboot: err: %d, message: %s", (int)err.error_code, response.status.c_str());
            }
            RelayRebootResult result;
            result.status = response.status;
            result.bid = response.bid;
            if (callback) {
                callback(err, result);
            }
        });
    });
}

void RelayControlModuleImpl::relayDisconnect(const std::function<void(FlightHubError err, RelayDisconnectResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_relay_disconnect(sn_, [this, weak_ptr, callback](const RelayDisconnectResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 中继站断开连接; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_RELAY_DISCONNECT, err, "[RelayControlModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("relayDisconnect: err: %d, message: %s", (int)err.error_code, response.status.c_str());
            }
            RelayDisconnectResult result;
            result.status = response.status;
            result.bid = response.bid;
            if (callback) {
                callback(err, result);
            }
        });
    });
}

} // namespace flight_hub_sdk
