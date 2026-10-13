// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include <string>
#include <vector>

#include "message.h"

namespace flight_hub_sdk {

/// 设备管理模块接口，提供设备列表、详情、改名、解绑及工作空间设备变更监听等功能。
class IDeviceManageModule {
public:
    virtual ~IDeviceManageModule() = default;

    /// @brief 获取设备列表（含拓扑关系）
    /// @param callback 回调，返回设备拓扑列表与错误信息
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/device/topologies
    /// @endinternal
    virtual void getDeviceList(std::function<void(const std::vector<DeviceTopo>&, const FlightHubError err)> callback) = 0;

    /// @brief 获取设备详情
    /// @param device_sn 设备序列号
    /// @param callback 回调，返回设备详情与错误信息
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/device/detail
    /// @endinternal
    virtual void getDeviceDetailInfo(const std::string& device_sn, std::function<void(FlightHubError err, const DeviceDetailInfo& device_detail)> callback) = 0;

    /// @brief 修改设备名称
    /// @param device_list 修改参数
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | PUT /device/api/v1/manager/device/info
    /// @endinternal
    virtual void modifyDeviceName(const ModifyDeviceNameParam& device_list, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 监听工作空间设备变更
    /// @param callback 设备变更事件回调
    /// @return 监听 ID，用于取消订阅
    /// @internal
    /// 链路: WebSocket | topic: topic_workspace_device_change
    /// @endinternal
    virtual int listenWorkspaceDeviceChanged(const std::function<void(const WorkspaceDeviceChange& msg)>& callback) = 0;

    /// @brief 取消订阅工作空间设备变更
    /// @param listen_id 监听 ID
    virtual void cancelListenWorkspaceDeviceChanged(int listen_id) = 0;

    /// @brief 订阅设备拓扑变更（工作空间内全部网关）
    /// @param callback 拓扑变更事件回调
    /// @return 监听 ID，用于取消订阅
    /// @internal
    /// 链路: WebSocket | topic: topic_device_topo_change
    /// @endinternal
    virtual int listenDeviceTopoChanged(const std::function<void(const DeviceTopoChange& msg)>& callback) = 0;

    /// @brief 取消订阅设备拓扑变更
    /// @param listen_id 监听 ID
    virtual void cancelListenDeviceTopoChanged(int listen_id) = 0;

    /// @brief 解绑设备
    /// @param device_sns 设备序列号列表
    /// @param callback 回调，返回错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/unbind-devices
    /// @endinternal
    virtual void unbindDevice(const std::vector<std::string>& device_sns, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 获取机场设备状态
    /// @param device_sns 设备序列号列表
    /// @param callback 回调，返回机场设备状态列表与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/device-properties
    /// @endinternal
    virtual void getDockDeviceStatus(const std::vector<std::string>& device_sns, std::function<void(FlightHubError err, const DockDeviceStatusResultList& status)> callback) = 0;

    /// @brief 获取飞机设备状态
    /// @param device_sns 设备序列号列表
    /// @param callback 回调，返回飞机设备状态列表与错误信息
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/device-properties
    /// @endinternal
    virtual void getDroneDeviceStatus(const std::vector<std::string>& device_sns, std::function<void(FlightHubError err, const DroneDeviceStatusResultList& status)> callback) = 0;
};

}  // namespace flight_hub_sdk
