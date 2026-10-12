// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "device_debug_module_impl.h"

#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include <base/common/logger.h>
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

namespace flight_hub_sdk {

DeviceDebugModuleImpl::DeviceDebugModuleImpl() {
    LOG_INFO("[DeviceDebugModuleImpl] constructor");
}

DeviceDebugModuleImpl::~DeviceDebugModuleImpl() {}

void DeviceDebugModuleImpl::openDebugMode(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDebugModeResult& result)>& callback) {
    WeakDummy(weak_ptr);
    post_open_debug_mode(device_sn, [this, weak_ptr, callback](const OpenDebugModeResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 打开调试模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_OPEN_DEBUG_MODE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("openDebugMode: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                OpenDebugModeResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::closeDebugMode(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDebugModeResult& result)>& callback) {
    WeakDummy(weak_ptr);
    post_close_debug_mode(device_sn, [this, weak_ptr, callback](const CloseDebugModeResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 关闭调试模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_CLOSE_DEBUG_MODE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("closeDebugMode: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                CloseDebugModeResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::openDockCover(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDockCoverResult& result)>& callback) {
    WeakDummy(weak_ptr);
    post_open_dock_cover(device_sn, [this, weak_ptr, callback](const OpenDockCoverResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 打开机场舱盖; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_OPEN_DOCK_COVER, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("openDockCover: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                OpenDockCoverResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::getOpenDockCoverProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDockCoverProgressResult& result)> callback) {
    WeakDummy(weak_ptr);
    get_open_dock_cover_progress(device_sn, [this, weak_ptr, callback](const OpenDockCoverProgressResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取打开机场舱盖进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_GET_OPEN_DOCK_COVER_PROGRESS, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("getOpenDockCoverProgress: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                OpenDockCoverProgressResult result;
                result.message = response.status;
                result.bid = response.bid;
                result.create_time = response.create_time;
                result.update_time = response.update_time;
                if (callback) {
                    callback(err, result);
                }
            });
    });
}

void DeviceDebugModuleImpl::closeDockCover(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDockCoverResult& result)>& callback) {
    WeakDummy(weak_ptr);
    post_close_dock_cover(device_sn, [this, weak_ptr, callback](const CloseDockCoverResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 关闭机场舱盖; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_CLOSE_DOCK_COVER, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("closeDockCover: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                CloseDockCoverResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::getCloseDockCoverProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDockCoverProgressResult& result)> callback) {
    WeakDummy(weak_ptr);
    get_close_dock_cover_progress(device_sn, [this, weak_ptr, callback](const CloseDockCoverProgressResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取关闭机场舱盖进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_GET_CLOSE_DOCK_COVER_PROGRESS, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("getCloseDockCoverProgress: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                CloseDockCoverProgressResult result;
                result.message = response.status;
                result.bid = response.bid;
                result.create_time = response.create_time;
                result.update_time = response.update_time;
                if (callback) {
                    callback(err, result);
                }
            });
    });
}

void DeviceDebugModuleImpl::deviceReboot(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceRebootResult& result)>& callback) {
    WeakDummy(weak_ptr);
    post_device_reboot(device_sn, [this, weak_ptr, callback](const DeviceRebootResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 设备重启; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_DEVICE_REBOOT, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("deviceReboot: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                DeviceRebootResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::getDeviceRebootProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceRebootProgressResult& result)> callback) {
    WeakDummy(weak_ptr);
    get_device_reboot_progress(device_sn, [this, weak_ptr, callback](const DeviceRebootProgressResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取设备重启进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_GET_DEVICE_REBOOT_PROGRESS, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("getDeviceRebootProgress: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                DeviceRebootProgressResult result;
                result.message = response.status;
                result.bid = response.bid;
                result.create_time = response.create_time;
                result.update_time = response.update_time;
                if (callback) {
                    callback(err, result);
                }
            });
    });
}

void DeviceDebugModuleImpl::deviceFormat(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceFormatResult& result)>& callback) {
    WeakDummy(weak_ptr);
    post_device_format(device_sn, [this, weak_ptr, callback](const DeviceFormatResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 设备格式化; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_DEVICE_FORMAT, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("deviceFormat: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                DeviceFormatResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::getDeviceFormatProgress(const std::string& device_sn, const std::function<void(FlightHubError err, const DeviceFormatProgressResult& result)> callback) {
    WeakDummy(weak_ptr);
    get_device_format_progress(device_sn, [this, weak_ptr, callback](const DeviceFormatProgressResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取设备格式化进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_GET_DEVICE_FORMAT_PROGRESS, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("getDeviceFormatProgress: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                DeviceFormatProgressResult result;
                result.message = response.status;
                result.bid = response.bid;
                result.create_time = response.create_time;
                result.update_time = response.update_time;
                if (callback) {
                    callback(err, result);
                }
            });
    });
}

void DeviceDebugModuleImpl::openDrone(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenDroneResult& data)>& callback) {
    WeakDummy(weak_ptr);
    post_drone_open(device_sn, [this, weak_ptr, callback](const DroneOpenResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 飞行器开机; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_OPEN_DRONE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("openDrone: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                OpenDroneResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::closeDrone(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseDroneResult& data)>& callback) {
    WeakDummy(weak_ptr);
    post_drone_close(device_sn, [this, weak_ptr, callback](const DroneCloseResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 飞行器关机; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_CLOSE_DRONE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("closeDrone: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                CloseDroneResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::openCharge(const std::string& device_sn, const std::function<void(FlightHubError err, const OpenChargeResult& data)>& callback) {
    WeakDummy(weak_ptr);
    post_open_charge(device_sn, [this, weak_ptr, callback](const OpenChargeResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 打开充电; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_OPEN_CHARGE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("openDrone: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                OpenChargeResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::closeCharge(const std::string& device_sn, const std::function<void(FlightHubError err, const CloseChargeResult& data)>& callback) {
    WeakDummy(weak_ptr);
    post_close_charge(device_sn, [this, weak_ptr, callback](const CloseChargeResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 关闭充电; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_CLOSE_CHARGE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("openDrone: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                CloseChargeResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::setDockAirConditionerMode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    post_dock_air_conditioner_mode(device_sn, mode, [this, weak_ptr, callback](const DockAirConditionerResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([callback, err, response]()->void {
                // 埋点: 设置机场空调模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_DOCK_AIR_CONDITIONER_MODE, err, "[DeviceDebugModuleImpl]");
                LOG_INFO("setDockAirConditionerMode, err: %d, message: %s", (int)err.error_code, response.status.c_str());
                if (callback) {
                    callback(err);
                }
            });
        });
}

void DeviceDebugModuleImpl::setDockSilentMode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    post_dock_silent_mode(device_sn, mode, [this, weak_ptr, callback](const DockSilentModeResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 设置机场静音模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_DOCK_SILENT_MODE, err, "[DeviceDebugModuleImpl]");
                LOG_INFO("setDockSilentMode: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                if (callback) {
                    callback(err);
                }
            });
        });
}

void DeviceDebugModuleImpl::droneFormat(const std::string& device_sn, const std::function<void(FlightHubError err, const DroneFormatResult& data)>& callback) {
    WeakDummy(weak_ptr);
    post_drone_format(device_sn, [this, weak_ptr, callback](const DroneFormatResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 飞行器格式化（过程操作）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_DRONE_FORMAT, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("droneFormat: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                DroneFormatResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::droneSdrWorkMode(const std::string& device_sn, int mode, const std::function<void(FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    post_drone_sdr_work_mode(device_sn, mode, [this, weak_ptr, callback](const DroneSdrWorkModeResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 切换飞行器 SDR 工作模式（即时操作）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_DRONE_SDR_WORK_MODE, err, "[DeviceDebugModuleImpl]");
                LOG_INFO("droneSdrWorkMode: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                if (callback) {
                    callback(err);
                }
            });
        });
}

void DeviceDebugModuleImpl::setDistanceLimit(const std::string& device_sn, int distance_limit_state, int distance_limit_value, const std::function<void(FlightHubError err, SetDistanceLimitResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_set_distance_limit(device_sn, distance_limit_state, distance_limit_value, [this, weak_ptr, callback](const SetDistanceLimitResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 设置限远距离; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_DISTANCE_LIMIT, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("setDistanceLimit: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                SetDistanceLimitResult result;
                result.message = response.status;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::setHeightLimit(const std::string& device_sn, int height_limit, const std::function<void(FlightHubError err, SetHeightLimitResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_set_height_limit(device_sn, height_limit, [this, weak_ptr, callback](const SetHeightLimitResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 设置限高; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_HEIGHT_LIMIT, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("setHeightLimit: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                SetHeightLimitResult result;
                result.message = response.status;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::setObstacleAvoidance(const std::string& device_sn, int obstacle_avoidance_state, const std::function<void(FlightHubError err, SetObstacleAvoidanceResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_set_obstacle_avoidance(device_sn, obstacle_avoidance_state, [this, weak_ptr, callback](const SetObstacleAvoidanceResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 设置避障状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_OBSTACLE_AVOIDANCE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("setObstacleAvoidance: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                SetObstacleAvoidanceResult result;
                result.message = response.status;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::setRemainingPowerForRth(const std::string& device_sn, int remaining_power_for_rth, const std::function<void(FlightHubError err, SetRemainingPowerForRthResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_set_remaining_power_for_rth(device_sn, remaining_power_for_rth, [this, weak_ptr, callback](const SetRemainingPowerForRthResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 设置返航剩余电量; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_REMAINING_POWER_FOR_RTH, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("setRemainingPowerForRth: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                SetRemainingPowerForRthResult result;
                result.message = response.status;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::setNightLightsState(const std::string& device_sn, int night_lights_state, const std::function<void(FlightHubError err, SetNightLightsStateResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_set_night_lights_state(device_sn, night_lights_state, [this, weak_ptr, callback](const SetNightLightsStateResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 设置夜航灯状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_NIGHT_LIGHTS_STATE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("setNightLightsState: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                SetNightLightsStateResult result;
                result.message = response.status;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::switchAlarmLightState(const std::string& device_sn, int alarm_state, const std::function<void(FlightHubError err, SwitchAlarmStateResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_switch_alarm_state(device_sn, alarm_state, [this, weak_ptr, callback](const AlarmStateSwitchResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 切换报警灯状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SWITCH_ALARM_LIGHT_STATE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("switchAlarmLightState: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                SwitchAlarmStateResult result;
                result.message = response.status;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::esimOperatorSwitch(const std::string& device_sn, const std::string& imei, int operator_type, const std::string& device_type, const std::function<void(FlightHubError err, EsimOperatorSwitchResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_esim_operator_switch(device_sn, imei, operator_type, device_type, [this, weak_ptr, callback](const EsimOperatorSwitchResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: eSIM 运营商切换; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_ESIM_OPERATOR_SWITCH, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("esimOperatorSwitch: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                EsimOperatorSwitchResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::simSlotSwitch(const std::string& device_sn, const std::string& imei, int sim_slot, const std::string& device_type, const std::function<void(FlightHubError err, SimSlotSwitchResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_sim_slot_switch(device_sn, imei, sim_slot, device_type, [this, weak_ptr, callback](const SimSlotSwitchResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: SIM 卡槽切换; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SIM_SLOT_SWITCH, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("simSlotSwitch: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                SimSlotSwitchResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::setBatteryStoreMode(const std::string& device_sn, int battery_store_mode, const std::function<void(FlightHubError err, SetBatteryStoreModeResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_set_battery_store_mode(device_sn, battery_store_mode, [this, weak_ptr, callback](const SetBatteryStoreModeResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 设置电池存储模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_BATTERY_STORE_MODE, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("setBatteryStoreMode: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                SetBatteryStoreModeResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::startManualBatterySwap(const std::string& device_sn, int slot_id, const std::function<void(FlightHubError err, ManualBatterySwapResult result)>& callback) {
    WeakDummy(weak_ptr);
    put_manual_battery_swap(device_sn, slot_id, [this, weak_ptr, callback](const ManualBatterySwapResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 发起手动换电; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_START_MANUAL_BATTERY_SWAP, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("startManualBatterySwap: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                ManualBatterySwapResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::getManualBatterySwapProgress(const std::string& device_sn, const std::function<void(FlightHubError err, ManualBatterySwapProgressResult result)>& callback) {
    WeakDummy(weak_ptr);
    get_manual_battery_swap(device_sn, [this, weak_ptr, callback](const ManualBatterySwapProgressResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 查询手动换电进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_GET_MANUAL_BATTERY_SWAP_PROGRESS, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("getManualBatterySwapProgress: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                ManualBatterySwapProgressResult result;
                result.message = response.status;
                result.bid = response.bid;
                result.data_json = response.data_json;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::setDonglePowerSwitch(const std::string& device_sn, int power_state, const std::function<void(FlightHubError err, DonglePowerSwitchResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_dongle_power_switch(device_sn, power_state, [this, weak_ptr, callback](const DonglePowerSwitchResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: Dongle 电源开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_DONGLE_POWER_SWITCH, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("setDonglePowerSwitch: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                DonglePowerSwitchResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::setLowPowerSwitch(const std::string& device_sn, int low_power_mode, const std::function<void(FlightHubError err, LowPowerSwitchResult result)>& callback) {
    WeakDummy(weak_ptr);
    post_low_power_switch(device_sn, low_power_mode, [this, weak_ptr, callback](const LowPowerSwitchResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 机场低功耗开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_LOW_POWER_SWITCH, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("setLowPowerSwitch: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                LowPowerSwitchResult result;
                result.message = response.status;
                result.bid = response.bid;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void DeviceDebugModuleImpl::getEsimOperatorSwitchProgress(const std::string& device_sn, const std::function<void(FlightHubError err, EsimOperatorSwitchProgressResult result)>& callback) {
    WeakDummy(weak_ptr);
    get_esim_operator_switch(device_sn, [this, weak_ptr, callback](const EsimOperatorSwitchProgressResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 查询 eSIM 运营商切换进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_GET_ESIM_OPERATOR_SWITCH_PROGRESS, err, "[DeviceDebugModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("getEsimOperatorSwitchProgress: err: %d, message: %s", (int)err.error_code, response.status.c_str());
                }
                EsimOperatorSwitchProgressResult result;
                result.message = response.status;
                result.bid = response.bid;
                result.data_json = response.data_json;
                if (callback) {
                    callback(err, result);
                }
            });
        });
}

int DeviceDebugModuleImpl::subscribeDeviceRebootProgress(const std::string& device_sn, const std::function<void(DeviceRebootResult result)>& callback) {
    LOG_INFO("subscribeDeviceReboot: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceReboot(
        [this, weak_ptr, callback](const notification_center::DeviceReboot& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            DeviceRebootResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceReboot, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceReboot, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDeviceRebootProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDeviceFormatProgress(const std::string& device_sn, const std::function<void(DeviceFormatResult result)>& callback) {
    LOG_INFO("subscribeDeviceFormat: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceFormat(
        [this, weak_ptr, callback](const notification_center::DeviceFormat& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            DeviceFormatResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceFormat, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceFormat, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDeviceFormatProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDeviceSimSlotSwitchProgress(const std::string& device_sn, const std::function<void(SimSlotSwitchResult result)>& callback) {
    LOG_INFO("subscribeDeviceSimSlotSwitch: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceSimSlotSwitch(
        [this, weak_ptr, callback](const notification_center::DeviceSimSlotSwitch& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            SimSlotSwitchResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceSimSlotSwitch, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceSimSlotSwitch, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDeviceSimSlotSwitchProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDeviceEsimOperatorSwitchProgress(const std::string& device_sn, const std::function<void(EsimOperatorSwitchResult result)>& callback) {
    LOG_INFO("subscribeDeviceEsimOperatorSwitch: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceEsimOperatorSwitch(
        [this, weak_ptr, callback](const notification_center::DeviceEsimOperatorSwitch& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            EsimOperatorSwitchResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceEsimOperatorSwitch, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceEsimOperatorSwitch, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDeviceEsimOperatorSwitchProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDroneOpenProgress(const std::string& device_sn, const std::function<void(OpenDroneResult result)>& callback) {
    LOG_INFO("subscribeDroneOpen: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneOpen(
        [this, weak_ptr, callback](const notification_center::DroneOpen& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            OpenDroneResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneOpen, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneOpen, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDroneOpenProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDroneCloseProgress(const std::string& device_sn, const std::function<void(CloseDroneResult result)>& callback) {
    LOG_INFO("subscribeDroneClose: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneClose(
        [this, weak_ptr, callback](const notification_center::DroneClose& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            CloseDroneResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneClose, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneClose, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDroneCloseProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDroneFormatProgress(const std::string& device_sn, const std::function<void(DroneFormatResult result)>& callback) {
    LOG_INFO("subscribeDroneFormat: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneFormat(
        [this, weak_ptr, callback](const notification_center::DroneFormat& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            DroneFormatResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneFormat, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneFormat, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDroneFormatProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDroneChargeOpenProgress(const std::string& device_sn, const std::function<void(OpenChargeResult result)>& callback) {
    LOG_INFO("subscribeDroneChargeOpen: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneChargeOpen(
        [this, weak_ptr, callback](const notification_center::DroneChargeOpen& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            OpenChargeResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneChargeOpen, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneChargeOpen, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDroneChargeOpenProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDroneChargeCloseProgress(const std::string& device_sn, const std::function<void(CloseChargeResult result)>& callback) {
    LOG_INFO("subscribeDroneChargeClose: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneChargeClose(
        [this, weak_ptr, callback](const notification_center::DroneChargeClose& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            CloseChargeResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneChargeClose, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDroneChargeClose, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDroneChargeCloseProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDockCoverOpenProgress(const std::string& device_sn, const std::function<void(OpenDockCoverResult result)>& callback) {
    LOG_INFO("subscribeDockCoverOpen: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDockCoverOpen(
        [this, weak_ptr, callback](const notification_center::DockCoverOpen& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            OpenDockCoverResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDockCoverOpen, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDockCoverOpen, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDockCoverOpenProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

int DeviceDebugModuleImpl::subscribeDockCoverCloseProgress(const std::string& device_sn, const std::function<void(CloseDockCoverResult result)>& callback) {
    LOG_INFO("subscribeDockCoverClose: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDockCoverClose(
        [this, weak_ptr, callback](const notification_center::DockCoverClose& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            CloseDockCoverResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDockCoverClose, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDockCoverClose, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::unsubscribeDockCoverCloseProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

void DeviceDebugModuleImpl::setGeoCagingEnable(const std::string& device_sn, int geo_caging_switch, const std::vector<GeoCagingInfoParam>& geo_caging_infos, const std::function<void(FlightHubError err)>& callback) {
    // 将 SDK 公开的 GeoCagingInfoParam 转换为内部 GeoCagingInfo
    std::vector<GeoCagingInfo> infos;
    for (const auto& param : geo_caging_infos) {
        GeoCagingInfo info;
        info.fg_longitude = param.fg_longitude;
        info.fg_latitude = param.fg_latitude;
        info.fg_radius = param.fg_radius;
        info.fg_height = param.fg_height;
        info.cv_longitude = param.cv_longitude;
        info.cv_latitude = param.cv_latitude;
        info.cv_radius = param.cv_radius;
        info.cv_height = param.cv_height;
        infos.push_back(info);
    }
    WeakDummy(weak_ptr);
    post_set_geo_caging_enable(device_sn, geo_caging_switch, infos, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 设置 Geo Caging（地理围栏）开关及围栏参数; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DEVICE_DEBUG_SET_GEO_CAGING_ENABLE, err, "[DeviceDebugModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("setGeoCagingEnable: err: %d", (int)err.error_code);
            }
            if (callback) {
                callback(err);
            }
        });
    });
}

namespace {
flight_hub_sdk::CloudEnvDeviceInfoRequest ToCloudEnvDeviceInfoRequest(const flight_hub_sdk::CloudEnvDeviceInfo& info) {
    flight_hub_sdk::CloudEnvDeviceInfoRequest request;
    request.callsign = info.callsign;
    request.device_model_key = info.device_model_key;
    request.device_sn = info.device_sn;
    return request;
}

flight_hub_sdk::CloudEnvDeviceListRequest ToCloudEnvDeviceListRequest(const flight_hub_sdk::CloudEnvDeviceList& list) {
    flight_hub_sdk::CloudEnvDeviceListRequest request;
    for (const auto& item : list.host) {
        request.host.push_back(ToCloudEnvDeviceInfoRequest(item));
    }
    for (const auto& item : list.nodes) {
        request.nodes.push_back(ToCloudEnvDeviceInfoRequest(item));
    }
    for (const auto& item : list.parents) {
        request.parents.push_back(ToCloudEnvDeviceInfoRequest(item));
    }
    return request;
}

flight_hub_sdk::CloudEnvTargetConfigRequest ToCloudEnvTargetConfigRequest(const flight_hub_sdk::CloudEnvTargetConfig& config) {
    flight_hub_sdk::CloudEnvTargetConfigRequest request;
    request.type = config.type;
    request.public_cloud_config.bind_code = config.public_cloud_config.bind_code;
    request.public_cloud_config.org_id = config.public_cloud_config.org_id;
    request.public_cloud_config.zone_id = config.public_cloud_config.zone_id;
    request.private_cloud_config.bind_code = config.private_cloud_config.bind_code;
    request.private_cloud_config.org_id = config.private_cloud_config.org_id;
    request.private_cloud_config.url = config.private_cloud_config.url;
    request.third_cloud_config.bind_code = config.third_cloud_config.bind_code;
    request.third_cloud_config.org_id = config.third_cloud_config.org_id;
    request.third_cloud_config.mqtt_addr = config.third_cloud_config.mqtt_addr;
    request.third_cloud_config.mqtt_password = config.third_cloud_config.mqtt_password;
    request.third_cloud_config.mqtt_username = config.third_cloud_config.mqtt_username;
    return request;
}
} // namespace

void DeviceDebugModuleImpl::cloudEnvironmentSwitch(const CloudEnvironmentSwitchParam& param, const std::function<void(FlightHubError err)>& callback) {
    CloudEnvironmentSwitchRequest request;
    for (const auto& list : param.devices_list) {
        request.devices_list.push_back(ToCloudEnvDeviceListRequest(list));
    }
    request.target_config = ToCloudEnvTargetConfigRequest(param.target_config);

    WeakDummy(weak_ptr);
    post_cloud_environment_switch(request, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("cloudEnvironmentSwitch: err: %d", (int)err.error_code);
            }
            if (callback) {
                callback(err);
            }
        });
    });
}

void DeviceDebugModuleImpl::cloudEnvironmentSwitchCallback(const CloudEnvironmentSwitchCallbackParam& param, const std::function<void(FlightHubError err)>& callback) {
    CloudEnvironmentSwitchCallbackRequest request;
    request.code = param.code;
    request.status = param.status;
    request.device_auth_verify.sn = param.device_auth_verify.sn;
    request.device_auth_verify.secret = param.device_auth_verify.secret;
    request.device_auth_verify.nonce = param.device_auth_verify.nonce;
    for (const auto& list : param.devices_list) {
        request.devices_list.push_back(ToCloudEnvDeviceListRequest(list));
    }
    request.target_config = ToCloudEnvTargetConfigRequest(param.target_config);

    WeakDummy(weak_ptr);
    post_cloud_environment_switch_callback(request, [this, weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_INFO("cloudEnvironmentSwitchCallback: err: %d", (int)err.error_code);
            }
            if (callback) {
                callback(err);
            }
        });
    });
}

int DeviceDebugModuleImpl::listenDeviceCloudEnvironmentSwitchProgress(const std::string& device_sn, const std::function<void(CloudEnvironmentSwitchResult result)>& callback) {
    LOG_INFO("subscribeDeviceCloudEnvironmentSwitch: device_sn: %s", device_sn.c_str());
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceCloudEnvironmentSwitch(
        [this, weak_ptr, callback](const notification_center::DeviceCloudEnvironmentSwitch& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            CloudEnvironmentSwitchResult result;
            result.message = msg.status;
            result.bid = msg.bid;
            result.progress = msg.progress;
            LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceCloudEnvironmentSwitch, bid: %s, result: %d", msg.bid.c_str(), msg.result);
            callback(result);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DeviceDebugModuleImpl] subscribeDeviceCloudEnvironmentSwitch, error: %d", error_code);
            }
        }, device_sn, notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void DeviceDebugModuleImpl::cancelListenDeviceCloudEnvironmentSwitchProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

} // namespace flight_hub_sdk