// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "topo_registry.h"

#include "bll/manager/device_capability_mgr/device_capability_mgr.h"

#include <algorithm>
#include <cctype>
#include <mutex>
#include <base/common/logger.h>

namespace flight_hub_sdk {

namespace {

std::string toLower(std::string value) {
    std::transform(value.begin(), value.end(), value.begin(),
                   [](unsigned char c) { return static_cast<char>(std::tolower(c)); });
    return value;
}

/// 网联飞机（如 EA340T）自身即 gateway；机场/基站场景下的普通飞机（EA230 等）不是 gateway。
DeviceNode makeNode(const Device& device, const std::string& gateway_sn,
                    const std::string& host_gateway_sn, bool is_gateway) {
    DeviceNode node;
    node.sn = device.device_sn;
    node.device_class = device.device_class;
    node.model_key = device.model_key;
    node.device_name = device.device_name;
    node.nickname = device.nickname;
    node.is_online = device.is_online;
    node.parent_sns = device.parent_sns;
    node.gateway_sn = gateway_sn;
    node.host_gateway_sn = host_gateway_sn;
    node.is_gateway = is_gateway;
    return node;
}

}  // namespace

TopoRegistry& TopoRegistry::getInstance() {
    static TopoRegistry instance;
    return instance;
}

bool TopoRegistry::isGatewayDeviceClass(const std::string& device_class) {
    const auto cls = toLower(device_class);
    return cls == "base_station" || cls == "airport" || cls == "rc";
}

bool TopoRegistry::isNetworkDrone(const Device& device) {
    const auto cls = toLower(device.device_class);
    if (cls != "drone") {
        return false;
    }
    return device.model_key == std::string(kModelKey_0_130_0);
}

void TopoRegistry::updateFromDeviceTopoList(const std::vector<DeviceTopo>& device_topo_list) {
    std::unique_lock<std::shared_mutex> lock(mutex_);
    rebuildFromDeviceTopoList(device_topo_list);
}

void TopoRegistry::rebuildFromDeviceTopoList(const std::vector<DeviceTopo>& device_topo_list) {
    node_lookup_.clear();
    gateway_members_.clear();
    gateway_topo_list_ = device_topo_list;

    for (const auto& topo : device_topo_list) {
        const auto& gateway = topo.device;
        const std::string gateway_sn = gateway.device_sn;

        node_lookup_[gateway_sn] = makeNode(gateway, gateway_sn, gateway_sn, true);
        auto& members = gateway_members_[gateway_sn];
        members.push_back(gateway_sn);

        for (const auto& sub : topo.sub_devices) {
            std::string member_gateway = gateway_sn;
            if (!sub.parent_sns.empty()) {
                member_gateway = sub.parent_sns[0];
            }

            bool member_is_gateway = isGatewayDeviceClass(sub.device_class);
            std::string resolved_gateway = member_is_gateway ? sub.device_sn : member_gateway;
            // 网联飞机 gateway=self，但 ATC 宿主仍是这棵树的根（基站/机场）
            const std::string host_gateway = member_is_gateway ? sub.device_sn : gateway_sn;

            if (TopoRegistry::isNetworkDrone(sub)) {
                member_is_gateway = true;
                resolved_gateway = sub.device_sn;
                LOG_INFO("[TopoRegistry] network drone sn=%s model_key=%s, gateway=self host=%s",
                         sub.device_sn.c_str(), sub.model_key.c_str(), host_gateway.c_str());
            }

            node_lookup_[sub.device_sn] = makeNode(sub, resolved_gateway, host_gateway, member_is_gateway);
            members.push_back(sub.device_sn);

            if (member_is_gateway && gateway_members_.find(sub.device_sn) == gateway_members_.end()) {
                gateway_members_[sub.device_sn] = {sub.device_sn};
            }
        }
    }

    LOG_INFO("[TopoRegistry] rebuild: gateways=%zu, nodes=%zu",
             gateway_topo_list_.size(), node_lookup_.size());
}

std::optional<DeviceNode> TopoRegistry::getNode(const std::string& device_sn) const {
    std::shared_lock<std::shared_mutex> lock(mutex_);
    auto it = node_lookup_.find(device_sn);
    if (it == node_lookup_.end()) {
        return std::nullopt;
    }
    return it->second;
}

std::optional<std::string> TopoRegistry::resolveGatewaySn(const std::string& device_sn) const {
    auto node = getNode(device_sn);
    if (!node.has_value()) {
        LOG_WARN("[TopoRegistry] resolveGatewaySn: unknown sn=%s, fallback to self", device_sn.c_str());
        return device_sn;
    }
    return node->gateway_sn;
}

std::optional<std::string> TopoRegistry::resolveHostGatewaySn(const std::string& device_sn) const {
    auto node = getNode(device_sn);
    if (!node.has_value()) {
        LOG_WARN("[TopoRegistry] resolveHostGatewaySn: unknown sn=%s", device_sn.c_str());
        return std::nullopt;
    }
    return node->host_gateway_sn.empty() ? node->gateway_sn : node->host_gateway_sn;
}

std::vector<std::string> TopoRegistry::getGatewayMembers(const std::string& gateway_sn) const {
    std::shared_lock<std::shared_mutex> lock(mutex_);
    auto it = gateway_members_.find(gateway_sn);
    if (it == gateway_members_.end()) {
        return {gateway_sn};
    }
    return it->second;
}

std::optional<DeviceTopo> TopoRegistry::getGatewayTopo(const std::string& gateway_sn) const {
    std::shared_lock<std::shared_mutex> lock(mutex_);
    for (const auto& topo : gateway_topo_list_) {
        if (topo.device.device_sn == gateway_sn) {
            return topo;
        }
    }
    return std::nullopt;
}

}  // namespace flight_hub_sdk
