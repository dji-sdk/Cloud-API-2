// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// 安全服务模块接口，提供信号地图查询、自定义飞行区域管理、地形障碍物离线地图管理等功能。
class ISafetyServiceModule {
public:
    virtual ~ISafetyServiceModule() = default;

    /// @brief 获取信号地图
    /// @param device_sns 设备 SN 列表
    /// @param data_types 待查询的数据类型, 比如 ["4g", "sdr"]
    /// @param callback 结果回调，成功时返回信号地图数据
    ///
    /// @internal
    /// 链路: HTTP | POST /safety/api/v1/signal-maps/search
    /// @endinternal
    virtual void searchSignalMaps(
        const std::vector<std::string> &device_sns,
        const std::vector<std::string> &data_types,
        const std::function<void(FlightHubError err, const SignalMapsResults& results)>& callback) = 0;

    /// @brief 创建自定义飞行区域
    /// @param request_params 创建参数
    /// @param callback 结果回调，成功时返回创建结果
    ///
    /// @internal
    /// 链路: HTTP | POST /safety/api/v2/flight-areas
    /// @endinternal
    virtual void createCustomizedFlightAreas(
        const CreateCustomizedFlightAreasParams &request_params,
        const std::function<void(FlightHubError err, const CreateFlightAreasResults& results)>& callback) = 0;

    /// @brief 获取自定义飞行区域
    /// @param request_params 查询参数
    /// @param callback 结果回调，成功时返回飞行区域列表
    ///
    /// @internal
    /// 链路: HTTP | GET /safety/api/v2/flight-areas
    /// @endinternal
    virtual void getCustomizedFlightAreas(
        const GetCustomizedFlightAreasParams &request_params,
        const std::function<void(FlightHubError err, const GetFlightAreasResults& results)>& callback) = 0;

    /// @brief 更新自定义飞行区域
    /// @param request_params 更新参数
    /// @param callback 结果回调，成功时返回更新结果
    ///
    /// @internal
    /// 链路: HTTP | PUT /safety/api/v2/flight-areas/{flight-area-id}
    /// @endinternal
    virtual void updateCustomizedFlightAreas(
        const UpdateCustomizedFlightAreasParams &request_params,
        const std::function<void(FlightHubError err, const UpdateFlightAreasResults& results)>& callback) = 0;

    /// @brief 删除自定义飞行区域
    /// @param flight_area_id 飞行区域 ID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | DELETE /safety/api/v2/flight-areas/{flight-area-id}
    /// @endinternal
    virtual void deleteCustomizedFlightAreas(
        const std::string &flight_area_id,
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 同步自定义飞行区域
    /// @param device_sns 待同步设备的 SN，支持网关类型设备（机场1、2、3 以及地面站）
    /// @param callback 结果回调，成功时返回同步结果
    ///
    /// @internal
    /// 链路: HTTP | POST /safety/api/v1/flight-areas/sync
    /// @endinternal
    virtual void syncCustomizedFlightAreas(
        const std::vector<std::string> &device_sns,
        const std::function<void(FlightHubError err, const SyncFlightAreasResults& results)>& callback) = 0;

    /// @brief 获取自定义飞行区域同步状态
    /// @param device_sns 待同步设备的 SN，支持网关类型设备（机场1、2、3 以及地面站）
    /// @param callback 结果回调，成功时返回同步状态
    ///
    /// @internal
    /// 链路: HTTP | GET /safety/api/v1/flight-areas/device-status
    /// @endinternal
    virtual void getFlightAreasSyncStatus(
        const std::vector<std::string> &device_sns,
        const std::function<void(FlightHubError err, const GetAreasSyncStatusResults& results)>& callback) = 0;

    /// @brief 查询离线地图模型信息
    /// @param callback 结果回调，成功时返回离线地图模型信息
    ///
    /// @internal
    /// 链路: HTTP | GET /safety/api/v1/offline-maps
    /// @endinternal
    virtual void getOfflineMapsModelInfo(
        const std::function<void(FlightHubError err, const GetOfflineMapsModelInfoResults& results)>& callback) = 0;

    /// @brief 创建/更新离线地图模型
    /// @param request_params 更新参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /safety/api/v1/offline-maps
    /// @endinternal
    virtual void updateOfflineMapsModelInfo(
        const UpdateOfflineMapsParams &request_params,
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 取消离线地图生成
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | DELETE /safety/api/v1/offline-maps
    /// @endinternal
    virtual void cancelOfflineMapsModelInfo(
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 修改离线地图（开启/关闭）
    /// @param request_params 设置参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | PUT /safety/api/v1/offline-maps
    /// @endinternal
    virtual void setOfflineMaps(
        const SetOfflineMapsParams &request_params,
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 离线地图同步
    /// @param device_sns 待同步设备的 SN，支持网关类型设备（机场1、2、3 以及地面站）
    /// @param callback 结果回调，成功时返回同步结果
    ///
    /// @internal
    /// 链路: HTTP | POST /safety/api/v1/offline-maps/sync
    /// @endinternal
    virtual void syncOfflineMaps(
        const std::vector<std::string> &device_sns,
        const std::function<void(FlightHubError err, const OfflineMapsSyncResults& results)>& callback) = 0;

    /// @brief 获取设备同步状态
    /// @param device_sns 待同步设备的 SN
    /// @param callback 结果回调，成功时返回同步状态
    ///
    /// @internal
    /// 链路: HTTP | GET /safety/api/v1/offline-maps/device-status
    /// @endinternal
    virtual void getOfflineMapsSyncStatus(
        const std::vector<std::string> &device_sns,
        const std::function<void(FlightHubError err, const GetOfflineMapsSyncStatusResults& results)>& callback) = 0;

    /// @brief 获取解锁证书列表
    /// @param param 查询参数
    /// @param callback 回调，返回解锁证书列表与错误信息
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/service/unlock-license/list
    /// @endinternal
    virtual void getUnlockLicenseList(const QueryDeviceUnlockLicenseListParam& param, std::function<void(FlightHubError err, QueryDeviceUnlockLicenseListResult result)> callback) = 0;

    /// @brief 切换解锁证书状态
    /// @param param 切换参数
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/unlock-license/switch
    /// @endinternal
    virtual void switchUnlockLicense(const SwitchUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 检查解锁证书
    /// @param param 检查参数
    /// @param callback 回调，返回检查结果与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/unlock-license/check
    /// @endinternal
    virtual void checkUnlockLicense(const CheckUnlockLicenseParam& param, std::function<void(FlightHubError err, CheckUnlockLicenseResult result)> callback) = 0;

    /// @brief 提交解锁证书
    /// @param param 提交参数
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/unlock-license/unlock
    /// @endinternal
    virtual void submitUnlockLicense(const ImportUnlockLicenseParam& param, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 清除设备解锁证书
    /// @param device_sn 设备序列号
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/service/unlock-license/clear
    /// @endinternal
    virtual void clearDeviceUnlockLicense(const std::string& device_sn, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 上传解锁证书文件
    /// @param file_path 本地文件路径
    /// @param callback 回调，返回上传结果与错误信息
    /// @internal
    /// 链路: SDK 内部 | 获取 STS 凭证后上传解锁证书文件
    /// @endinternal
    virtual void uploadUnlockLicenseFile(const std::string& file_path, std::function<void(FlightHubError err, const UploadUnlockLicenseFileResult& result)> callback) = 0;

    /// @brief 订阅飞行器离线地图开关
    /// @param drone_sn 飞行器 SN（本模块为 workspace 级单例，无固定绑定设备，需显式传入）
    /// @param callback 离线地图开关回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_offline_map_enable
    /// @endinternal
    virtual int listenDroneOfflineMapEnable(
        const std::string& drone_sn,
        const std::function<void(const DroneOfflineMapEnable& msg)>& callback) = 0;

    /// @brief 取消订阅飞行器离线地图开关
    /// @param listen_id 监听 ID
    virtual void cancelListenDroneOfflineMapEnable(int listen_id) = 0;
};  // End of ISafetyServiceModule;


}  // namespace flight_hub_sdk;
