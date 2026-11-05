// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "session/session.h"
#include "workspace/workspace_impl.h"
#include "drone/drone_impl.h"
#include "dock/dock_impl.h"
#include "remote_controller/remote_controller_impl.h"
#include "base_station/base_station_impl.h"
#include "relay/relay_impl.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

std::string Session::buildNotifyUrl(const std::string& base_url, const std::string& node_id) {
    if (base_url.empty()) {
        return base_url;
    }
    const char* sep = (base_url.find('?') != std::string::npos) ? "&" : "?";
    return base_url + sep + "mesh-dst-idc=" + node_id;
}

Session::Session(
    int session_id,
    const std::string& node_id,
    const std::string& display_name,
    const SessionDeps& deps)
    : session_id_(session_id),
      node_id_(node_id),
      display_name_(display_name),
      nc_(std::make_shared<NotificationCenter>()),
      deps_(deps) {
    LOG_INFO("[Session] created: session_id=%d, node_id=\"%s\"", session_id_, node_id_.c_str());
}

Session::~Session() {
    shutdown();
    LOG_INFO("[Session] destroyed: session_id=%d", session_id_);
}

SessionInfo Session::getSessionInfo() const {
    SessionInfo i;
    i.session_id = session_id_;
    i.node_id = node_id_;
    i.display_name = display_name_;
    i.notify_url = notify_url_;
    return i;
}

bool Session::initialize(const SessionInitParams& params) {
    notify_url_ = buildNotifyUrl(params.notify_base_url, node_id_);
    if (notify_url_.empty()) {
        LOG_ERROR("[Session] initialize: notify_url empty, session_id=%d", session_id_);
        return false;
    }

    websocket_auth_headers_ = params.websocket_auth_headers;

    std::shared_ptr<NotificationCenter> nc;
    {
        std::lock_guard<std::mutex> lock(mutex_);
        nc = nc_;
    }
    if (!nc) {
        LOG_ERROR("[Session] initialize: NC null, session_id=%d", session_id_);
        return false;
    }

    if (deps_.ws) {
        nc->setWebSocket(deps_.ws);
    }
    // NC 失败多可自愈（进 CONNECTING 后 OnClose/OnError 重连）；不阻拦入表，但保留排障入口
    if (!nc->initialize(notify_url_, websocket_auth_headers_,
                        params.verify_ssl, params.ssl_ca_path)) {
        LOG_ERROR("[Session] initialize: NC initialize failed, session_id=%d, ws_url=%s "
                  "(session still usable; NC may auto-reconnect)",
                  session_id_, notify_url_.c_str());
    }
    LOG_INFO("[Session] initialized: session_id=%d, ws_url=%s",
             session_id_, notify_url_.c_str());
    return true;
}

void Session::updateWebsocketAuthHeaders(const std::vector<HeaderKeyValue>& headers) {
    websocket_auth_headers_ = headers;
    std::shared_ptr<NotificationCenter> nc;
    {
        std::lock_guard<std::mutex> lock(mutex_);
        nc = nc_;
    }
    if (nc) {
        nc->setWebSocketAuthHeaders(headers);
    }
}

std::shared_ptr<notification_center::BusinessManager> Session::getBusinessManagerLocked() {
    // 无锁版: nc_ 构造时创建、仅 shutdown 时 move 走; shutdown 后实例构造本就不该发生,
    // 此处读到的即使是被 move 后的空指针也会走下面的判空兜底。
    if (!nc_) {
        LOG_ERROR("[Session] getBusinessManagerLocked: NC null, session_id=%d (shutdown 后?)", session_id_);
        return nullptr;
    }
    return nc_->getBusinessManager();
}

std::shared_ptr<notification_center::BusinessManager> Session::getBusinessManager() {
    std::shared_ptr<NotificationCenter> nc;
    {
        std::lock_guard<std::mutex> lock(mutex_);
        nc = nc_;
    }
    if (!nc) {
        LOG_ERROR("[Session] getBusinessManager: NC null, session_id=%d (RAII 下不应发生)", session_id_);
        return nullptr;
    }
    return nc->getBusinessManager();
}

void Session::shutdown() {
    std::shared_ptr<NotificationCenter> nc;
    {
        std::lock_guard<std::mutex> lock(mutex_);
        drone_instances_.clear();
        dock_instances_.clear();
        remote_controller_instances_.clear();
        base_station_instances_.clear();
        relay_instances_.clear();
        workspace_instance_.reset();
        nc = std::move(nc_);
    }
    if (nc) {
        nc->cleanup();
        LOG_INFO("[Session] shutdown: session_id=%d", session_id_);
    }
}

std::shared_ptr<IWorkspace> Session::getWorkspaceInstance() {
    std::lock_guard<std::mutex> lock(mutex_);
    if (!workspace_instance_) {
        workspace_instance_ = std::make_shared<WorkspaceImpl>(session_id_);
        LOG_INFO("[Session] getWorkspaceInstance: created, session_id=%d", session_id_);
    }
    return workspace_instance_;
}

std::shared_ptr<IDrone> Session::getDroneInstance(const std::string& sn) {
    if (sn.empty()) {
        LOG_ERROR("[Session] getDroneInstance: sn empty");
        return nullptr;
    }
    // 锁内只做查缓存 + 取 workspace 快照；impl 构造移出锁外，
    // 避免 impl/子模块 ctor 经 ResolveBusinessManager 重入本类 mutex_ 导致自死锁。
    std::shared_ptr<WorkspaceImpl> workspace;
    {
        std::lock_guard<std::mutex> lock(mutex_);
        auto it = drone_instances_.find(sn);
        if (it != drone_instances_.end()) {
            return it->second;
        }
        if (!workspace_instance_) {
            LOG_ERROR("[Session] getDroneInstance: call getWorkspaceInstance first, session_id=%d", session_id_);
            return nullptr;
        }
        workspace = workspace_instance_;
    }

    // 锁外匹配设备拓扑并构造 DroneImpl
    std::shared_ptr<IDrone> drone;
    auto device_topo_list = workspace->getDeviceList();
    for (const auto& device_topo : device_topo_list) {
        for (const auto& sub : device_topo.sub_devices) {
            if (sub.device_sn == sn) {
                drone = std::make_shared<DroneImpl>(
                    device_topo, session_id_, deps_.rtc, deps_.mqtt);
                break;
            }
        }
        if (drone) {
            break;
        }
    }
    if (!drone) {
        LOG_ERROR("[Session] getDroneInstance: sn=%s not found, session_id=%d", sn.c_str(), session_id_);
        return nullptr;
    }

    // 重新持锁双检写入：并发下若他人已先建，复用其实例，丢弃本次构造
    std::lock_guard<std::mutex> lock(mutex_);
    auto it = drone_instances_.find(sn);
    if (it != drone_instances_.end()) {
        return it->second;
    }
    drone_instances_[sn] = drone;
    LOG_INFO("[Session] getDroneInstance: created sn=%s, session_id=%d", sn.c_str(), session_id_);
    return drone;
}

std::shared_ptr<IDock> Session::getDockInstance(const std::string& sn) {
    if (sn.empty()) {
        LOG_ERROR("[Session] getDockInstance: sn empty");
        return nullptr;
    }
    {
        std::lock_guard<std::mutex> lock(mutex_);
        auto it = dock_instances_.find(sn);
        if (it != dock_instances_.end()) {
            return it->second;
        }
    }

    // 锁外构造，避免 DockImpl/子模块 ctor 重入 mutex_
    auto dock = std::make_shared<DockImpl>(sn, session_id_, deps_.rtc, deps_.mqtt);

    std::lock_guard<std::mutex> lock(mutex_);
    auto it = dock_instances_.find(sn);
    if (it != dock_instances_.end()) {
        return it->second;
    }
    dock_instances_[sn] = dock;
    LOG_INFO("[Session] getDockInstance: created sn=%s, session_id=%d", sn.c_str(), session_id_);
    return dock;
}

std::shared_ptr<IRemoteController> Session::getRemoteControllerInstance(const std::string& sn) {
    if (sn.empty()) {
        LOG_ERROR("[Session] getRemoteControllerInstance: sn empty");
        return nullptr;
    }
    {
        std::lock_guard<std::mutex> lock(mutex_);
        auto it = remote_controller_instances_.find(sn);
        if (it != remote_controller_instances_.end()) {
            return it->second;
        }
    }

    auto instance = std::make_shared<RemoteControllerImpl>(sn, session_id_);

    std::lock_guard<std::mutex> lock(mutex_);
    auto it = remote_controller_instances_.find(sn);
    if (it != remote_controller_instances_.end()) {
        return it->second;
    }
    remote_controller_instances_[sn] = instance;
    return instance;
}

std::shared_ptr<IBaseStation> Session::getBaseStationInstance(const std::string& sn) {
    if (sn.empty()) {
        LOG_ERROR("[Session] getBaseStationInstance: sn empty");
        return nullptr;
    }
    {
        std::lock_guard<std::mutex> lock(mutex_);
        auto it = base_station_instances_.find(sn);
        if (it != base_station_instances_.end()) {
            return it->second;
        }
    }

    auto instance = std::make_shared<BaseStationImpl>(
        sn, session_id_, deps_.rtc, deps_.mqtt);

    std::lock_guard<std::mutex> lock(mutex_);
    auto it = base_station_instances_.find(sn);
    if (it != base_station_instances_.end()) {
        return it->second;
    }
    base_station_instances_[sn] = instance;
    return instance;
}

std::shared_ptr<IRelay> Session::getRelayInstance(const std::string& sn) {
    if (sn.empty()) {
        LOG_ERROR("[Session] getRelayInstance: sn empty");
        return nullptr;
    }
    {
        std::lock_guard<std::mutex> lock(mutex_);
        auto it = relay_instances_.find(sn);
        if (it != relay_instances_.end()) {
            return it->second;
        }
    }

    auto instance = std::make_shared<RelayImpl>(sn, session_id_);

    std::lock_guard<std::mutex> lock(mutex_);
    auto it = relay_instances_.find(sn);
    if (it != relay_instances_.end()) {
        return it->second;
    }
    relay_instances_[sn] = instance;
    return instance;
}

}  // namespace flight_hub_sdk
