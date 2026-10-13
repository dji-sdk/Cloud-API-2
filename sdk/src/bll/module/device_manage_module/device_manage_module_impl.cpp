// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "device_manage_module_impl.h"
#include <base/common/logger.h>
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include "common/executors/executors.h"
#include <BusinessManager.h>

using namespace flight_hub_sdk;
using namespace notification_center;

namespace flight_hub_sdk {

DeviceManageModuleImpl::DeviceManageModuleImpl()
    :
      device_list_mgr_() {
    LOG_INFO("[DeviceManageModuleImpl] constructor");
}

void DeviceManageModuleImpl::initWorkspaceDeviceChangeListening() {
    if (workspace_device_change_listen_id_ != -1) {
        return;
    }
    WeakDummy(p);
    workspace_device_change_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenWorkspaceDeviceChanged(
        [this, p](const notification_center::WorkspaceDeviceChange& msg)-> void {
            WeakDummyReturn(p);
            WorkspaceDeviceChange workspace_device_change;

            for (const auto& device_change : msg.device_changes) {
                DeviceChange change;
                change.change_type = device_change.change_type;
                change.change_devices = device_change.change_devices;
                workspace_device_change.device_changes.push_back(change);
            }

            LOG_DEBUG("[DeviceManageModuleImpl] listenWorkspaceDeviceChanged, device_changes count: %zu",
                workspace_device_change.device_changes.size());
            workspace_device_change_callbacks_.notifyCallbackIfChanged(workspace_device_change);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DeviceManageModuleImpl] listenWorkspaceDeviceChanged, error: %d", error_code);
            }
        }, "", notification_center::NotificationFrequency_OnChanged);
    if (workspace_device_change_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DeviceManageModuleImpl] ListenWorkspaceDeviceChanged, error: %d", workspace_device_change_listen_id_);
        workspace_device_change_listen_id_ = -1;
        return;
    }
    LOG_INFO("[DeviceManageModuleImpl] ListenWorkspaceDeviceChanged, listen_id: %d", workspace_device_change_listen_id_);
}

void DeviceManageModuleImpl::initDeviceTopoChangeListening() {
    if (device_topo_change_listen_id_ != -1) {
        return;
    }
    WeakDummy(p);
    device_topo_change_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDeviceTopoChanged(
        [this, p](const notification_center::DeviceTopoChange& msg) -> void {
            WeakDummyReturn(p);
            DeviceTopoChange device_topo_change;
            device_topo_change.gateway_sn = msg.gateway_sn;
            device_topo_change.devices = msg.devices;
            LOG_DEBUG("[DeviceManageModuleImpl] listenDeviceTopoChanged, gateway_sn: %s, devices count: %zu",
                device_topo_change.gateway_sn.c_str(), device_topo_change.devices.size());
            device_topo_change_callbacks_.notifyCallbackIfChanged(device_topo_change);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[DeviceManageModuleImpl] listenDeviceTopoChanged, error: %d", error_code);
            }
        }, "", notification_center::NotificationFrequency_OnChanged);
    if (device_topo_change_listen_id_ < notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
        LOG_ERROR("[DeviceManageModuleImpl] ListenDeviceTopoChanged, error: %d", device_topo_change_listen_id_);
        device_topo_change_listen_id_ = -1;
        return;
    }
    LOG_INFO("[DeviceManageModuleImpl] ListenDeviceTopoChanged, listen_id: %d", device_topo_change_listen_id_);
}

DeviceManageModuleImpl::~DeviceManageModuleImpl() {
    auto* notification_center = NotificationCenter::getInstance();
    if (!notification_center) {
        return;
    }
    auto business_manager = notification_center->getBusinessManager();
    if (!business_manager) {
        return;
    }
    if (workspace_device_change_listen_id_ != -1) {
        business_manager->CancelObserve(workspace_device_change_listen_id_);
        workspace_device_change_listen_id_ = -1;
    }
    if (device_topo_change_listen_id_ != -1) {
        business_manager->CancelObserve(device_topo_change_listen_id_);
        device_topo_change_listen_id_ = -1;
    }
}

void DeviceManageModuleImpl::getDeviceList(std::function<void(const std::vector<DeviceTopo>&, const FlightHubError err)> callback) {
    device_list_mgr_.getDeviceList(callback);
}

std::vector<DeviceTopo> DeviceManageModuleImpl::getCachedDeviceList() {
    return device_list_mgr_.getCachedDeviceList();
}

void DeviceManageModuleImpl::getDeviceDetailInfo(const std::string& device_sn, std::function<void(FlightHubError err, const DeviceDetailInfo& device_detail)> callback) {
    WeakDummy(weak_ptr);
    get_device_detail(device_sn, [weak_ptr, callback](const DeviceDetailResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 获取设备详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_MANAGE_GET_DEVICE_DETAIL_INFO, err, "[DeviceManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("getDeviceDetailInfo: err_code: %d", (int)err.error_code);
                }
                DeviceDetailInfo result;
                result.device_sn = response.device_sn;
                result.nickname = response.device_callsign;

                result.model_info.key = response.model_info.key;
                result.model_info.domain = response.model_info.domain;
                result.model_info.type = response.model_info.type;
                result.model_info.sub_type = response.model_info.sub_type;
                result.model_info.name = response.model_info.name;
                result.model_info.device_class = response.model_info.device_class;
                result.model_info.develop_code = response.model_info.develop_code;

                result.device_add_time = response.device_add_time;
                result.device_online_status = response.device_online_status;
                result.device_firmware_version = response.device_firmware_version;
                result.device_firmware_status = response.device_firmware_status;
                result.device_working_status = response.device_working_status;
                result.complete_machine_sn = response.complete_machine_sn;

                result.sub_devices.clear();
                for (const auto& sub : response.sub_devices) {
                    SubDeviceInfo sub_info;
                    sub_info.device_sn = sub.device_sn;
                    sub_info.nickname = sub.device_callsign;
                    sub_info.model_info.key = sub.model_info.key;
                    sub_info.model_info.domain = sub.model_info.domain;
                    sub_info.model_info.type = sub.model_info.type;
                    sub_info.model_info.sub_type = sub.model_info.sub_type;
                    sub_info.model_info.name = sub.model_info.name;
                    sub_info.model_info.device_class = sub.model_info.device_class;
                    sub_info.model_info.develop_code = sub.model_info.develop_code;
                    sub_info.device_online_status = sub.device_online_status;
                    sub_info.device_firmware_version = sub.device_firmware_version;
                    sub_info.device_firmware_status = sub.device_firmware_status;
                    sub_info.device_working_status = sub.device_working_status;
                    sub_info.flysafe_database_version = sub.flysafe_database_version;
                    sub_info.parent_sns = sub.parent_sns;
                    result.sub_devices.push_back(sub_info);
                }
                if (callback) {
                    callback(err, result);
                }
            });
    });
}

void DeviceManageModuleImpl::modifyDeviceName(const ModifyDeviceNameParam& device_list, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    ModifyDeviceNameRequest request;
    for (const auto& item : device_list.device_list) {
        request.devices.push_back({item.device_sn, item.device_callsign});
    }
    put_modify_device_name(request, [this, weak_ptr, callback](const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 修改设备名称; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_MANAGE_MODIFY_DEVICE_NAME, err, "[DeviceManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("modifyDeviceName: err_code: %d", (int)err.error_code);
                }
                if (callback) {
                    callback(err);
                }
            });
    });
}

int DeviceManageModuleImpl::listenWorkspaceDeviceChanged(const std::function<void(const WorkspaceDeviceChange& msg)>& callback) {
    int listen_id = workspace_device_change_callbacks_.addCallback(callback);
    initWorkspaceDeviceChangeListening();
    return listen_id;
}

void DeviceManageModuleImpl::cancelListenWorkspaceDeviceChanged(int listen_id) {
    workspace_device_change_callbacks_.removeCallback(listen_id);
}

int DeviceManageModuleImpl::listenDeviceTopoChanged(const std::function<void(const DeviceTopoChange& msg)>& callback) {
    int listen_id = device_topo_change_callbacks_.addCallback(callback);
    initDeviceTopoChangeListening();
    return listen_id;
}

void DeviceManageModuleImpl::cancelListenDeviceTopoChanged(int listen_id) {
    device_topo_change_callbacks_.removeCallback(listen_id);
}

void DeviceManageModuleImpl::unbindDevice(const std::vector<std::string>& device_sns, std::function<void(FlightHubError err)> callback) {
    WeakDummy(weak_ptr);
    UnBindDeviceRequest request;
    request.device_sns = device_sns;
    post_unbind_device(request, [this, weak_ptr, callback](const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 解绑设备; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_DEVICE_MANAGE_UNBIND_DEVICE, err, "[DeviceManageModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("unbindDevice: err_code: %d", (int)err.error_code);
                }
                if (callback) {
                    callback(err);
                }
            });
    });
}

void DeviceManageModuleImpl::getDockDeviceStatus(const std::vector<std::string>& device_sns, std::function<void(FlightHubError err, const DockDeviceStatusResultList& status)> callback) {
    get_dock_status(device_sns, [device_sns, callback](const DockStatusResponse& status, const FlightHubError err) {
        // 埋点: 获取机场设备状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DEVICE_MANAGE_GET_DOCK_DEVICE_STATUS, err, "[DeviceManageModuleImpl]");
        LOG_INFO("[DeviceManageModuleImpl] getDockDeviceStatus: device_sns size: %zu, err_code %d", device_sns.size(), (int)err.error_code);
        DockDeviceStatusResultList result;
        for (const auto& item : status.list) {
            DockStatusItemResult dock_device_status;
            dock_device_status.sn = item.sn;
            dock_device_status.online_status = item.online_status;
            dock_device_status.updated_time = item.updated_time;
            dock_device_status.device_model.device_class = item.device_model.device_class;
            dock_device_status.device_model.key = item.device_model.key;
            dock_device_status.device_model.name = item.device_model.name;
            if (item.device_model.device_class != "airport") {
                LOG_INFO("[DeviceManageModuleImpl] getDockDeviceStatus: device_class:%s", item.device_model.device_class.c_str());
                continue;
            }
            const auto& prop = item.properties;
            dock_device_status.properties.acc_time = prop.acc_time;
            dock_device_status.properties.acdc_power_input = prop.acdc_power_input;
            dock_device_status.properties.electric_supply_voltage = prop.electric_supply_voltage;
            dock_device_status.properties.environment_temperature = prop.environment_temperature;
            dock_device_status.properties.humidity = prop.humidity;
            dock_device_status.properties.job_number = prop.job_number;
            dock_device_status.properties.poe_link_status = prop.poe_link_status;
            dock_device_status.properties.poe_power_output = prop.poe_power_output;
            dock_device_status.properties.rainfall = prop.rainfall;
            dock_device_status.properties.temperature = prop.temperature;
            dock_device_status.properties.working_voltage = prop.working_voltage;
            dock_device_status.properties.air_conditioner.air_conditioner_state = prop.air_conditioner.air_conditioner_state;
            dock_device_status.properties.air_conditioner.switch_time = prop.air_conditioner.switch_time;
            dock_device_status.properties.alternate_land_point.height = prop.alternate_land_point.height;
            dock_device_status.properties.alternate_land_point.is_configured = prop.alternate_land_point.is_configured;
            dock_device_status.properties.alternate_land_point.latitude = prop.alternate_land_point.latitude;
            dock_device_status.properties.alternate_land_point.longitude = prop.alternate_land_point.longitude;
            dock_device_status.properties.alternate_land_point.safe_land_height = prop.alternate_land_point.safe_land_height;
            dock_device_status.properties.backup_battery.switch_state = prop.backup_battery.switch_;
            dock_device_status.properties.backup_battery.temperature = prop.backup_battery.temperature;
            dock_device_status.properties.backup_battery.voltage = prop.backup_battery.voltage;
            dock_device_status.properties.network_state.quality = prop.network_state.quality;
            dock_device_status.properties.network_state.rate = prop.network_state.rate;
            dock_device_status.properties.network_state.type = prop.network_state.type;
            dock_device_status.properties.position_state.gps_number = prop.position_state.gps_number;
            dock_device_status.properties.position_state.is_calibration = prop.position_state.is_calibration;
            dock_device_status.properties.position_state.is_fixed = prop.position_state.is_fixed;
            dock_device_status.properties.position_state.quality = prop.position_state.quality;
            dock_device_status.properties.position_state.rtk_number = prop.position_state.rtk_number;
            dock_device_status.properties.tilt_angle.valid = prop.tilt_angle.valid;
            dock_device_status.properties.tilt_angle.value = prop.tilt_angle.value;
            result.list.push_back(dock_device_status);
        }
        if (callback) {
            callback(err, result);
        }
    });
}

void DeviceManageModuleImpl::getDroneDeviceStatus(const std::vector<std::string>& device_sns, std::function<void(FlightHubError err, const DroneDeviceStatusResultList& status)> callback) {
    get_drone_status(device_sns, [device_sns, callback](const DroneStatusResponse& status, const FlightHubError err) {
        // 埋点: 获取飞机设备状态; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DEVICE_MANAGE_GET_DRONE_DEVICE_STATUS, err, "[DeviceManageModuleImpl]");
        LOG_INFO("[DeviceManageModuleImpl] getDroneDeviceStatus: device_sns size: %zu, err_code %d", device_sns.size(), (int)err.error_code);
        DroneDeviceStatusResultList result;
        for (const auto& item : status.list) {
            DroneStatusItemResult drone_device_status;
            drone_device_status.sn = item.sn;
            drone_device_status.online_status = item.online_status;
            drone_device_status.updated_time = item.updated_time;
            drone_device_status.device_model.device_class = item.device_model.device_class;
            drone_device_status.device_model.key = item.device_model.key;
            drone_device_status.device_model.name = item.device_model.name;
            if (item.device_model.device_class != "drone") {
                LOG_INFO("[DeviceManageModuleImpl] getDroneDeviceStatus: device_class:%s", item.device_model.device_class.c_str());
                continue;
            }
            const auto& prop = item.properties;
            drone_device_status.properties.height_limit = prop.height_limit;
            drone_device_status.properties.home_distance = prop.home_distance;
            drone_device_status.properties.home_latitude = prop.home_latitude;
            drone_device_status.properties.home_longitude = prop.home_longitude;
            drone_device_status.properties.horizontal_speed = prop.horizontal_speed;
            drone_device_status.properties.low_battery_warning_threshold = prop.low_battery_warning_threshold;
            drone_device_status.properties.mode_code = prop.mode_code;
            drone_device_status.properties.mode_code_reason = prop.mode_code_reason;
            drone_device_status.properties.night_lights_state = prop.night_lights_state;
            drone_device_status.properties.remaining_power_for_return_home = prop.remaining_power_for_return_home;
            drone_device_status.properties.serious_low_battery_warning_threshold = prop.serious_low_battery_warning_threshold;
            drone_device_status.properties.total_flight_distance = prop.total_flight_distance;
            drone_device_status.properties.total_flight_sorties = prop.total_flight_sorties;
            drone_device_status.properties.total_flight_time = prop.total_flight_time;
            drone_device_status.properties.vertical_speed = prop.vertical_speed;
            for (const auto& battery : prop.battery.batteries) {
                DroneStatusBatteryItemResult battery_item{};
                battery_item.capacity_percent = battery.capacity_percent;
                battery_item.firmware_version = battery.firmware_version;
                battery_item.high_voltage_storage_days = battery.high_voltage_storage_days;
                battery_item.index = battery.index;
                battery_item.loop_times = battery.loop_times;
                battery_item.sn = battery.sn;
                battery_item.sub_type = battery.sub_type;
                battery_item.temperature = battery.temperature;
                battery_item.type = battery.type;
                battery_item.voltage = battery.voltage;
                drone_device_status.properties.battery.batteries.push_back(battery_item);
            }
            drone_device_status.properties.battery.capacity_percent = prop.battery.capacity_percent;
            drone_device_status.properties.battery.landing_power = prop.battery.landing_power;
            drone_device_status.properties.battery.remain_flight_time = prop.battery.remain_flight_time;
            drone_device_status.properties.battery.remain_job_time = prop.battery.remain_job_time;
            drone_device_status.properties.battery.return_home_power = prop.battery.return_home_power;
            drone_device_status.properties.distance_limit_status.distance_limit = prop.distance_limit_status.distance_limit;
            drone_device_status.properties.distance_limit_status.is_near_distance_limit = prop.distance_limit_status.is_near_distance_limit;
            drone_device_status.properties.distance_limit_status.state = prop.distance_limit_status.state;
            drone_device_status.properties.obstacle_avoidance.downside = prop.obstacle_avoidance.downside;
            drone_device_status.properties.obstacle_avoidance.horizon = prop.obstacle_avoidance.horizon;
            drone_device_status.properties.obstacle_avoidance.upside = prop.obstacle_avoidance.upside;
            drone_device_status.properties.position_state.gps_number = prop.position_state.gps_number;
            drone_device_status.properties.position_state.is_fixed = prop.position_state.is_fixed;
            drone_device_status.properties.position_state.quality = prop.position_state.quality;
            drone_device_status.properties.position_state.rtk_number = prop.position_state.rtk_number;
            drone_device_status.properties.storage.total = prop.storage.total;
            drone_device_status.properties.storage.used = prop.storage.used;

            result.list.push_back(drone_device_status);
        }
        if (callback) {
            callback(err, result);
        }
    });
}

}  // namespace flight_hub_sdk
