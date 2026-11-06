// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/flight_hub_sdk.h"
#include "session/session_manager.h"
#include "session/session.h"
#include <memory>

namespace flight_hub_sdk {

/// 按 session_id 解析 BusinessManager（唯一强引用在 Session/NC）
///
/// 死锁警示: 本函数会被"Session::mutex_ 持锁链内"的模块构造路径调用
/// (如 Session::getDroneInstance 持锁构造 DroneImpl → DeviceStateModuleImpl ctor),
/// 因此这里必须走 Session::getBusinessManagerLocked() 无锁路径,
/// 严禁改回加锁的 Session::getBusinessManager(), 否则同线程二次加锁自死锁。
inline std::shared_ptr<notification_center::BusinessManager>
ResolveBusinessManager(int session_id) {
    auto iface = FlightHubSdk::getSessionManager();
    auto* mgr = iface ? static_cast<SessionManager*>(iface.get()) : nullptr;
    if (!mgr) {
        return nullptr;
    }
    auto session = mgr->getSession(session_id);
    return session ? session->getBusinessManagerLocked() : nullptr;
}

}  // namespace flight_hub_sdk
