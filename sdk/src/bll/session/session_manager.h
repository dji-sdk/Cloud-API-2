// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "session/session.h"
#include "api/session.h"
#include "api/session_manager.h"
#include "api/message.h"
#include <map>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

namespace flight_hub_sdk {

struct SessionManagerInitParams {
    SessionDeps deps;
    std::string notify_base_url;
    std::vector<HeaderKeyValue> websocket_auth_headers;
    bool verify_ssl = true;
    std::string ssl_ca_path;
};

class SessionManager : public ISessionManager {
public:
    SessionManager();
    ~SessionManager() override;

    bool initialize(const SessionManagerInitParams& params);
    void shutdown();

    int registerSession(const SessionConfig& config) override;
    std::vector<SessionInfo> getAllSessionInfo() const override;

    /// SDK 内部：按 id 取 Session（不进公开 ISessionManager）
    std::shared_ptr<Session> getSession(int session_id);

private:
    mutable std::mutex mutex_;
    std::map<int, std::shared_ptr<Session>> sessions_;
    /// 子 Session 发号游标；shutdown/re-init 不回退，避免 id 复用撞号
    int next_id_ = 0;
    bool initialized_ = false;

    SessionDeps deps_;
    std::string notify_base_url_;
    bool verify_ssl_ = true;
    std::string ssl_ca_path_;
};

}  // namespace flight_hub_sdk
