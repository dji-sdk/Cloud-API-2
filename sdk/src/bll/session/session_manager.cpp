// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "session/session_manager.h"
#include "api/flight_hub_sdk.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

SessionManager::SessionManager() = default;

SessionManager::~SessionManager() {
    shutdown();
}

bool SessionManager::initialize(const SessionManagerInitParams& params) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (initialized_) {
        LOG_WARN("[SessionManager] initialize: already initialized");
        return true;
    }
    if (params.notify_base_url.empty()) {
        LOG_ERROR("[SessionManager] initialize: notify_base_url empty");
        return false;
    }

    deps_ = params.deps;
    notify_base_url_ = params.notify_base_url;
    verify_ssl_ = params.verify_ssl;
    ssl_ca_path_ = params.ssl_ca_path;

    auto session = std::make_shared<Session>(
        kDefaultSessionId, /*node_id=*/"", /*display_name=*/"", deps_);

    SessionInitParams init;
    init.notify_base_url = notify_base_url_;
    init.websocket_auth_headers = params.websocket_auth_headers;
    init.verify_ssl = verify_ssl_;
    init.ssl_ca_path = ssl_ca_path_;
    if (!session->initialize(init)) {
        LOG_ERROR("[SessionManager] initialize: Session::initialize failed for session 0");
        return false;
    }

    sessions_[kDefaultSessionId] = session;
    // session_id 单调不减：re-init 不回收已发出的 id，避免用户手上旧 id 撞号（KOC2）
    if (next_id_ < 1) {
        next_id_ = 1;
    }
    initialized_ = true;
    LOG_INFO("[SessionManager] initialize: %s", session->getSessionInfo().ToString().c_str());
    return true;
}

void SessionManager::shutdown() {
    std::lock_guard<std::mutex> lock(mutex_);
    if (!initialized_) {
        return;
    }
    for (auto& [id, session] : sessions_) {
        if (session) {
            session->shutdown();
        }
    }
    sessions_.clear();
    // 不重置 next_id_：同实例 re-init 后继续递增
    initialized_ = false;
    LOG_INFO("[SessionManager] shutdown");
}

int SessionManager::registerSession(const SessionConfig& config) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (!initialized_) {
        LOG_ERROR("[SessionManager] registerSession: not initialized");
        return -1;
    }
    if (config.node_id.empty()) {
        LOG_ERROR("[SessionManager] registerSession: child node_id must be non-empty");
        return -1;
    }

    for (const auto& [id, s] : sessions_) {
        if (s && s->getSessionInfo().node_id == config.node_id) {
            LOG_INFO("[SessionManager] registerSession: idempotent hit node_id=%s → session_id=%d",
                     config.node_id.c_str(), id);
            return id;
        }
    }

    if (notify_base_url_.empty()) {
        LOG_ERROR("[SessionManager] registerSession: notify_base_url empty");
        return -1;
    }

    const int id = next_id_++;
    auto session = std::make_shared<Session>(
        id, config.node_id, config.display_name, deps_);

    SessionInitParams init;
    init.notify_base_url = notify_base_url_;
    init.websocket_auth_headers = FlightHubSdk::getWebSocketAuthHeaders();
    init.verify_ssl = verify_ssl_;
    init.ssl_ca_path = ssl_ca_path_;
    if (!session->initialize(init)) {
        LOG_ERROR("[SessionManager] registerSession: Session::initialize failed, node_id=%s",
                  config.node_id.c_str());
        // 已消耗的 id 不回退，保持单调
        return -1;
    }

    sessions_[id] = session;
    LOG_INFO("[SessionManager] registerSession: %s", session->getSessionInfo().ToString().c_str());
    return id;
}

std::shared_ptr<Session> SessionManager::getSession(int session_id) {
    std::lock_guard<std::mutex> lock(mutex_);
    auto it = sessions_.find(session_id);
    if (it == sessions_.end() || !it->second) {
        LOG_ERROR("[SessionManager] getSession: session_id=%d not found", session_id);
        return nullptr;
    }
    return it->second;
}

std::vector<SessionInfo> SessionManager::getAllSessionInfo() const {
    std::lock_guard<std::mutex> lock(mutex_);
    std::vector<SessionInfo> out;
    out.reserve(sessions_.size());
    for (const auto& [id, s] : sessions_) {
        if (s) {
            out.push_back(s->getSessionInfo());
        }
    }
    return out;
}

}  // namespace flight_hub_sdk
