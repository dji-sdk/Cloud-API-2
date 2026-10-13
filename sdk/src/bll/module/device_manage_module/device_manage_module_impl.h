// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/device_manage_module.h"
#include "common/util/async_capture_protect.h"
#include "bll/tool/callback_container.h"
#include "bll/manager/device_list_mgr/device_list_mgr.h"

#include <vector>

namespace flight_hub_sdk {

class DeviceManageModuleImpl : public IDeviceManageModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit DeviceManageModuleImpl();
    ~DeviceManageModuleImpl() override;

    void getDeviceList(std::function<void(const std::vector<DeviceTopo>&, const FlightHubError err)> callback) override;
    void getDeviceDetailInfo(const std::string& device_sn, std::function<void(FlightHubError err, const DeviceDetailInfo& device_detail)> callback) override;
    void modifyDeviceName(const ModifyDeviceNameParam& device_list, std::function<void(FlightHubError err)> callback) override;
    int listenWorkspaceDeviceChanged(const std::function<void(const WorkspaceDeviceChange& msg)>& callback) override;
    void cancelListenWorkspaceDeviceChanged(int listen_id) override;
    int listenDeviceTopoChanged(const std::function<void(const DeviceTopoChange& msg)>& callback) override;
    void cancelListenDeviceTopoChanged(int listen_id) override;
    void unbindDevice(const std::vector<std::string>& device_sns, std::function<void(FlightHubError err)> callback) override;
    void getDockDeviceStatus(const std::vector<std::string>& device_sns, std::function<void(FlightHubError err, const DockDeviceStatusResultList& status)> callback) override;
    void getDroneDeviceStatus(const std::vector<std::string>& device_sns, std::function<void(FlightHubError err, const DroneDeviceStatusResultList& status)> callback) override;

    std::vector<DeviceTopo> getCachedDeviceList();

private:
    void initWorkspaceDeviceChangeListening();
    void initDeviceTopoChangeListening();

    DeviceListManager device_list_mgr_;

    CallbackContainer<WorkspaceDeviceChange> workspace_device_change_callbacks_;
    CallbackContainer<DeviceTopoChange> device_topo_change_callbacks_;
    int workspace_device_change_listen_id_ = -1;
    int device_topo_change_listen_id_ = -1;
};

}  // namespace flight_hub_sdk
