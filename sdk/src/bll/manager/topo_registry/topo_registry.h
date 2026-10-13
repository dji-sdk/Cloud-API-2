// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"

#include <optional>
#include <shared_mutex>
#include <string>
#include <unordered_map>
#include <vector>

namespace flight_hub_sdk {

/// 内部拓扑节点（比对外 DeviceTopo 更丰富）
struct DeviceNode {
    std::string sn;
    std::string device_class;
    std::string model_key;
    std::string device_name;
    std::string nickname;
    bool is_online = false;
    std::string gateway_sn;              ///< 上云/DRC 网关；网联飞机为自身
    std::string host_gateway_sn;         ///< 拓扑根（机场/基站/RC）；ATC 如 poi-enter 用这个
    std::vector<std::string> parent_sns; ///< 来自 API（relay 等场景）
    bool is_gateway = false;
};

/// 进程级拓扑注册表 + Gateway 解析（静态版，后续接 WS topo-change）
class TopoRegistry {
public:
    static TopoRegistry& getInstance();

    /// 用设备列表全量刷新拓扑图（DeviceListManager 拉列表后调用）
    void updateFromDeviceTopoList(const std::vector<DeviceTopo>& device_topo_list);

    std::optional<DeviceNode> getNode(const std::string& device_sn) const;
    std::optional<std::string> resolveGatewaySn(const std::string& device_sn) const;
    /// 拓扑根 SN。查飞机：EA340 → 基站，EA230 → 机场。
    std::optional<std::string> resolveHostGatewaySn(const std::string& device_sn) const;
    std::vector<std::string> getGatewayMembers(const std::string& gateway_sn) const;
    std::optional<DeviceTopo> getGatewayTopo(const std::string& gateway_sn) const;

    /// 判断 device_class 是否为网关角色（自身即 gateway）
    static bool isGatewayDeviceClass(const std::string& device_class);

    /// 网联飞机（如 EA340T model_key 0-130-0）自身即 gateway；EA230 等机场飞机返回 false
    static bool isNetworkDrone(const Device& device);

private:
    TopoRegistry() = default;

    void rebuildFromDeviceTopoList(const std::vector<DeviceTopo>& device_topo_list);

    mutable std::shared_mutex mutex_;
    std::unordered_map<std::string, DeviceNode> node_lookup_;
    std::unordered_map<std::string, std::vector<std::string>> gateway_members_;
    std::vector<DeviceTopo> gateway_topo_list_;
};

}  // namespace flight_hub_sdk
