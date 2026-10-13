// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "device_list_mgr.h"
#include "base/common/logger.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/request_message.h"
#include "http_interface/http_interface.h"
#include "api/flight_hub_sdk.h"
#include <BusinessManager.h>
#include "tool/flight_hub_utils.h"

#include "device_capability_mgr/device_capability_mgr.h"
#include "topo_registry/topo_registry.h"

namespace flight_hub_sdk {

DeviceListManager::DeviceListManager() {
    LOG_INFO("[DeviceListManager] constructor");
}

DeviceListManager::~DeviceListManager() {
    LOG_INFO("[DeviceListManager] DeviceListManager: destructor invoked.");
}


void DeviceListManager::getDeviceList(std::function<void(const std::vector<DeviceTopo>& device_list, const FlightHubError err)> callback) {
    LOG_INFO("[DeviceListManager] getDeviceList()");
    // 分页拉取内部会多次回调, 故埋点在最外层出口做一次, 保证一次业务调用只上报一次。
    GetDeviceListByFilter("", [callback](const std::vector<DeviceTopo>& device_list, const FlightHubError err) {
        // 埋点: 获取设备列表（含拓扑关系）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DEVICE_MANAGE_GET_DEVICE_LIST, err, "[DeviceListManager]");
        if (callback) {
            callback(device_list, err);
        }
    });
}

std::vector<DeviceTopo> DeviceListManager::getCachedDeviceList() const {
    // 读操作：使用 shared_lock (共享锁);
    // 允许多个线程同时进入此函数;
    std::shared_lock<std::shared_mutex> lk(this->device_topo_list_rw_mutex_);

    return this->device_topo_list_;
}

void DeviceListManager::GetDeviceListByFilter(const std::string& gateway_model_key, const std::function<void(const std::vector<DeviceTopo>& device_list, const FlightHubError err)>& callback) {
    auto request_index = device_list_request_index_++;
    std::vector<DeviceTopo> device_topo_list_result;
    LOG_INFO("[DeviceListManager] GetDeviceListByPages request_index: %ld, invoke gateway_model_key = %s", request_index, gateway_model_key.c_str());
    GetDeviceListByPages(request_index, gateway_model_key, 1, 1000, std::move(callback), device_topo_list_result);
}

void DeviceListManager::GetDeviceListByPages(uint32_t request_index, const std::string& gateway_model_key, int page_index, int page_number, const std::function<void(const std::vector<DeviceTopo>& device_list, const FlightHubError err)>& callback, const std::vector<DeviceTopo>& device_topo_list_result) {
    LOG_DEBUG("[DeviceListManager] GetDeviceListByPages request_index: %ld, invoke for page %d", request_index, page_index);
    WeakDummy(weak_ptr);
    get_device_list_by_filter(gateway_model_key, page_index, page_number, 10000,
        [this, weak_ptr, request_index, gateway_model_key, page_index, page_number, callback, device_topo_list_result](const DeviceTopoListResponse& response, const FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        std::vector<DeviceTopo> device_topo_list{device_topo_list_result};
        LOG_DEBUG("[DeviceListManager] GetDeviceListByPages request_index: %ld, response for page %d, size = %d, total = %d", request_index, page_index, response.list.size(), response.pagination.total);
        if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
            LOG_ERROR("[DeviceListManager] GetDeviceListByPages request_index: %ld, response for page %d failed, err=%d", request_index, page_index, static_cast<int>(err.error_code));
            if (callback) {
                callback(device_topo_list, err);
            }
            return;
        }

        /*
         * 收集设备 SN 和简要 model_info;
         * 设备数量是有限可估计的(几千台以内);
         */
        std::unordered_map<std::string, ConciseModelInfo> device_model_info;

        for (const auto& item : response.list) {
            DeviceTopo dev_topo;
            dev_topo.device.device_sn       = item.device_sn;
            dev_topo.device.device_class    = item.model_info.device_class;
            dev_topo.device.device_name     = item.model_info.name;
            dev_topo.device.nickname        = item.nickname;
            dev_topo.device.model_key       = item.model_info.model_key;
            dev_topo.device.is_online       = item.online_status;

            /* 收集主设备(Device)的简要 model_info; */
            device_model_info.insert_or_assign(
                item.device_sn,
                ConciseModelInfo{item.model_info.model_key, item.model_info.name, item.model_info.device_class}
            );
            LOG_INFO("[DeviceListManager] Device ConciseModelInfo {device_sn: \"%s\", model_key: \"%s\", name: \"%s\", device_class: \"%s\"}, nickname: \"%s\" ",
                item.device_sn.c_str(),
                item.model_info.model_key.c_str(),
                item.model_info.name.c_str(),
                item.model_info.device_class.c_str(),
                item.nickname.c_str()
            );

            // LOG_INFO("[DeviceListManager] device list item: gateway device_sn=%s, model_key=%s", dev_topo.device.device_sn.c_str(), dev_topo.device.model_key.c_str());
            for (const auto& sub_device: item.sub_devices) {
                Device sub_dev;

                sub_dev.device_sn = sub_device.device_sn;
                sub_dev.device_class = sub_device.model_info.device_class;
                sub_dev.device_name = sub_device.model_info.name;
                sub_dev.nickname = sub_device.nickname;
                sub_dev.model_key = sub_device.model_info.model_key;
                sub_dev.is_online = sub_device.online_status;
                sub_dev.parent_sns = sub_device.parent_sns;

                /* 收集子设备(Sub-Device)的简要 model_info; */
                device_model_info.insert_or_assign(
                    sub_device.device_sn,
                    ConciseModelInfo{sub_device.model_info.model_key, sub_device.model_info.name, sub_device.model_info.device_class}
                );
                LOG_INFO("[DeviceListManager] Sub-Device ConciseModelInfo {device_sn: \"%s\", model_key: \"%s\", name: \"%s\", device_class: \"%s\"}, nickname: \"%s\" ",
                    sub_device.device_sn.c_str(),
                    sub_device.model_info.model_key.c_str(),
                    sub_device.model_info.name.c_str(),
                    sub_device.model_info.device_class.c_str(),
                    sub_device.nickname.c_str()
                );

                // LOG_INFO("[DeviceListManager] device list sub_device: device_sn=%s, model_key=%s", sub_dev.device_sn.c_str(), sub_dev.model_key.c_str());
                // LOG_DEBUG("[DeviceListManager] queryDeviceListViaPagination(), response list item: sub_device_sn: %s ", sub_dev.device_sn.c_str());
                dev_topo.sub_devices.emplace_back(std::move(sub_dev));
            }
            device_topo_list.emplace_back(std::move(dev_topo));
        }

        // 存储简要 model_info;
        DeviceCapabilityManager::getInstance().updateModelInfo(std::move(device_model_info));
        TopoRegistry::getInstance().updateFromDeviceTopoList(device_topo_list);

        int resp_list_size = static_cast<int>(response.list.size());
        if (resp_list_size == page_number) {
            GetDeviceListByPages(request_index, gateway_model_key, page_index + 1, page_number, std::move(callback), device_topo_list);
            return;
        }

        if (callback) {
            LOG_INFO("[DeviceListManager] GetDeviceListByPages request_index: %ld, all callback size = %d", request_index, device_topo_list.size());
            {
                std::unique_lock<std::shared_mutex> unique_lk(this->device_topo_list_rw_mutex_);
                this->device_topo_list_.assign(device_topo_list.cbegin(), device_topo_list.cend());
            }
            // 传成员引用而非局部 device_topo_list，避免回调返回后 Java 侧 use-after-free
            callback(this->device_topo_list_, FlightHubError::success());
        }
    });
}

} // namespace flight_hub_sdk;