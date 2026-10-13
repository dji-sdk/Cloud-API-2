// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "common/util/async_capture_protect.h"
#include "message.h"


#include <memory>       // for std::shared_ptr, std::make_shared
#include <mutex>        // for std::mutex, std::lock_guard
#include <shared_mutex> // for std::shared_mutex
#include <functional>   // for std::function
#include <atomic>


namespace flight_hub_sdk {

class DeviceListManager: public drc_sdk::common::AsyncCaptureProtect {
private:

    // Level-1 Cache;
    mutable std::shared_mutex  device_topo_list_rw_mutex_;
    std::vector<DeviceTopo>    device_topo_list_;


public:
    DeviceListManager();

    virtual ~DeviceListManager();


   /**
    * @brief 请求最新的设备列表;
    *    允许多个线程同时进入此函数;
    * @param callback 回调函数;
    */
    void getDeviceList(std::function<void(const std::vector<DeviceTopo>& device_list, const FlightHubError err)> callback);

    void GetDeviceListByFilter(const std::string& gateway_model_key, const std::function<void(const std::vector<DeviceTopo>& device_list, const FlightHubError err)>& callback);

   /**
    * @brief 请求(当前)缓存的设备列表;
    *    允许多个线程同时进入此函数;
    * @return 返回当前缓存的设备列表;
    */
    std::vector<DeviceTopo> getCachedDeviceList() const;

private:
    void GetDeviceListByPages(uint32_t request_index, const std::string& gateway_model_key, int page_index, int page_number, const std::function<void(const std::vector<DeviceTopo>& device_list, const FlightHubError err)>& callback, const std::vector<DeviceTopo>& device_topo_list_result);

private:
    std::atomic<uint32_t> device_list_request_index_{0};
};

}  // namespace flight_hub_sdk