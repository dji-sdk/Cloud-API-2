// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/safety_service_module.h"
#include "common/util/async_capture_protect.h"
#include "bll/manager/fly_safe_mgr/fly_safe_mgr.h"
#include "tool/callback_container.h"

#include <vector>
#include <string>
#include <functional>
#include <unordered_map>

namespace flight_hub_sdk {

class SafetyServiceModuleImpl : public ISafetyServiceModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit SafetyServiceModuleImpl();

    // 析构函数;
    ~SafetyServiceModuleImpl();

    // [信号地图];
    // 获取信号地图;
    void searchSignalMaps(
        const std::vector<std::string> &device_sns, const std::vector<std::string> &data_types,
        const std::function<void(FlightHubError err, const SignalMapsResults& results)>& callback) override;

    // [自定义飞行区域];
    // 创建自定义飞行区域;
    void createCustomizedFlightAreas(
        const CreateCustomizedFlightAreasParams &request_params,
        const std::function<void(FlightHubError err, const CreateFlightAreasResults& results)>& callback) override;

    // [自定义飞行区域];
    // 获取自定义飞行区域;
    void getCustomizedFlightAreas(
        const GetCustomizedFlightAreasParams &request_params,
        const std::function<void(FlightHubError err, const GetFlightAreasResults& results)>& callback) override;

    // [自定义飞行区域];
    // 更新自定义飞行区域;
    void updateCustomizedFlightAreas(
        const UpdateCustomizedFlightAreasParams &request_params,
        const std::function<void(FlightHubError err, const UpdateFlightAreasResults& results)>& callback) override;

    // [自定义飞行区域];
    // 删除自定义飞行区域;
    void deleteCustomizedFlightAreas(
        const std::string &flight_area_id,
        const std::function<void(FlightHubError err)>& callback) override;

    // [自定义飞行区域];
    // 同步自定义飞行区域;
    void syncCustomizedFlightAreas(
        const std::vector<std::string> &device_sns,  // 待同步设备的SN，支持网关类型设备(机场1、2、3 以及 地面站);
        const std::function<void(FlightHubError err, const SyncFlightAreasResults& results)>& callback) override;

    // [自定义飞行区域];
    // 获取设备同步状态;
    void getFlightAreasSyncStatus(
        const std::vector<std::string> &device_sns,  // 待同步设备的SN，支持网关类型设备(机场1、2、3 以及 地面站);
        const std::function<void(FlightHubError err, const GetAreasSyncStatusResults& results)>& callback) override;

    // [地形障碍物];
    // 查询离线地图模型信息;
    void getOfflineMapsModelInfo(
        const std::function<void(FlightHubError err, const GetOfflineMapsModelInfoResults& results)>& callback) override;

    // [地形障碍物];
    // 创建/更新离线地图模型;
    void updateOfflineMapsModelInfo(
        const UpdateOfflineMapsParams &request_params,
        const std::function<void(FlightHubError err)>& callback) override;

    // [地形障碍物];
    // 取消离线地图生成;
    void cancelOfflineMapsModelInfo(
        const std::function<void(FlightHubError err)>& callback) override;

    // [地形障碍物];
    // 修改离线地图(开启/关闭);
    void setOfflineMaps(
        const SetOfflineMapsParams &request_params,
        const std::function<void(FlightHubError err)>& callback) override;

    // [地形障碍物];
    // 离线地图同步;
    void syncOfflineMaps(
        const std::vector<std::string> &device_sns,  // 待同步设备的SN，支持网关类型设备(机场1、2、3 以及 地面站);
        const std::function<void(FlightHubError err, const OfflineMapsSyncResults& results)>& callback) override;

    // [地形障碍物];
    // 获取设备同步状态;
    void getOfflineMapsSyncStatus(
        const std::vector<std::string> &device_sns,  // 待同步设备的SN;
        const std::function<void(FlightHubError err, const GetOfflineMapsSyncStatusResults& results)>& callback) override;

    void getUnlockLicenseList(const QueryDeviceUnlockLicenseListParam& param, std::function<void(FlightHubError err, QueryDeviceUnlockLicenseListResult result)> callback) override;
    void switchUnlockLicense(const SwitchUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback) override;
    void checkUnlockLicense(const CheckUnlockLicenseParam& param, std::function<void(FlightHubError err, CheckUnlockLicenseResult result)> callback) override;
    void submitUnlockLicense(const ImportUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback) override;
    void clearDeviceUnlockLicense(const std::string& device_sn, std::function<void(FlightHubError err)> callback) override;
    void uploadUnlockLicenseFile(const std::string& file_path, std::function<void(FlightHubError err, const UploadUnlockLicenseFileResult& result)> callback) override;

    int listenDroneOfflineMapEnable(
        const std::string& drone_sn,
        const std::function<void(const DroneOfflineMapEnable& msg)>& callback) override;
    void cancelListenDroneOfflineMapEnable(int listen_id) override;

private:
    FlySafeManager fly_safe_mgr_;

    // NC listen IDs for offline map enable topic（本模块无固定绑定设备，一个 module 允许多个 drone 同时订阅，故用 map）
    std::unordered_map<int, int64_t> offline_map_enable_nc_listen_ids_;
    int offline_map_enable_next_id_ = 0;
};

} // namespace flight_hub_sdk