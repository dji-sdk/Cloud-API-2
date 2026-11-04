// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include <vector>
#include "session.h"
#include "value.h"

namespace flight_hub_sdk {

/// 默认 Session（initialize 后自动创建，node_id=""）
constexpr int kDefaultSessionId = 0;

/// SessionManager 对外：注册 + 枚举（getSession 仅 SDK 内部，见 bll SessionManager）
class ISessionManager {
public:
    virtual ~ISessionManager() = default;

    /// 注册子 Session；同 node_id 幂等返回已有 id。返回 session_id（>=1）；失败返回 -1
    virtual int registerSession(const SessionConfig& config) = 0;

    /// 列出全部 SessionInfo（含 session 0）
    virtual std::vector<SessionInfo> getAllSessionInfo() const = 0;
};

}  // namespace flight_hub_sdk
