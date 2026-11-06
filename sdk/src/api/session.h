// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <vector>
#include <sstream>
#include "message.h"

namespace flight_hub_sdk {

/// 注册 Session 时的用户入参（仅业务标识；鉴权/URL 由 SDK 加工）
struct SessionConfig {
    std::string node_id;       ///< mesh 值；子平台必填非空；session0 由 SDK 内部创建时为 ""
    std::string display_name;  ///< 可选，如 Cascade 节点名

    SessionConfig() = default;
    explicit SessionConfig(const std::string& nid) : node_id(nid) {}
};

/// Session 只读信息（getAllSessionInfo / 日志 dump）— SDK 加工后的快照
struct SessionInfo {
    int session_id = 0;
    std::string node_id;
    std::string display_name;
    std::string notify_url;  ///< 已含 mesh-dst-idc query 的 WS URL

    std::string ToString() const {
        std::ostringstream oss;
        oss << "{id=" << session_id
            << " node=\"" << node_id << "\""
            << " name=\"" << display_name << "\""
            << " url=\"" << notify_url << "\"}";
        return oss.str();
    }
};

}  // namespace flight_hub_sdk
