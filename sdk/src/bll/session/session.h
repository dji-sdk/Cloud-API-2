// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/flight_hub_sdk.h"
#include "api/drone.h"
#include "api/dock.h"
#include "api/remote_controller.h"
#include "api/base_station.h"
#include "api/relay.h"
#include "api/workspace.h"
#include "api/session.h"
#include "api/message.h"
#include "bridge/i_rtc_channel_manager_bridge.h"
#include "bridge/i_mqtt_client_manager_bridge.h"
#include "notification_center/notification_center.h"
#include <map>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

namespace notification_center { class WebSocketBase; }

namespace flight_hub_sdk {

class WorkspaceImpl;

/// 前置声明(session_bm.h 定义): Session 的 friend, 走无锁路径解析 BM
std::shared_ptr<notification_center::BusinessManager> ResolveBusinessManager(int session_id);

/// 构造期注入依赖（rtc / mqtt / ws 同类；与 initialize 配置分离）
struct SessionDeps {
    std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc;
    std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt;
    notification_center::WebSocketBase* ws = nullptr;
};

/// Session 初始化参数（运行时配置；鉴权来自 SDK 全局快照，非用户 SessionConfig）
struct SessionInitParams {
    std::string notify_base_url;   ///< 未带 mesh query 的 WS base
    std::vector<HeaderKeyValue> websocket_auth_headers;
    bool verify_ssl = true;
    std::string ssl_ca_path;
};

/// SDK 内部 Session（不对用户暴露；无公开 ISession）
class Session {
    /// ResolveBusinessManager 需要访问 getBusinessManagerLocked():
    /// 模块实例构造可能发生在 Session::mutex_ 持锁链内(getDroneInstance 等),
    /// 必须走无锁路径, 否则对同一把非递归 mutex_ 二次加锁 = 自死锁。
    friend std::shared_ptr<notification_center::BusinessManager> ResolveBusinessManager(int session_id);
public:
    Session(
        int session_id,
        const std::string& node_id,
        const std::string& display_name,
        const SessionDeps& deps = {});

    ~Session();

    SessionInfo getSessionInfo() const;

    bool initialize(const SessionInitParams& params);

    /// 更新鉴权（vector；一期不断线重连）
    void updateWebsocketAuthHeaders(const std::vector<HeaderKeyValue>& headers);

    std::shared_ptr<notification_center::BusinessManager> getBusinessManager();

    /// 断开 WS / cleanup NC / 清设备缓存
    void shutdown();

    std::shared_ptr<IWorkspace> getWorkspaceInstance();
    std::shared_ptr<IDrone> getDroneInstance(const std::string& sn);
    std::shared_ptr<IDock> getDockInstance(const std::string& sn);
    std::shared_ptr<IRemoteController> getRemoteControllerInstance(const std::string& sn);
    std::shared_ptr<IBaseStation> getBaseStationInstance(const std::string& sn);
    std::shared_ptr<IRelay> getRelayInstance(const std::string& sn);

    static std::string buildNotifyUrl(const std::string& base_url, const std::string& node_id);

private:
    int session_id_;
    std::string node_id_;
    std::string display_name_;
    std::string notify_url_;
    std::vector<HeaderKeyValue> websocket_auth_headers_;

    /// 与 devices/workspace 同属 mutex_ 保护范围
    std::shared_ptr<NotificationCenter> nc_;

    /// 持锁版: 已持有 mutex_ 时取 BM(模块实例构造在锁内会经 ResolveBusinessManager 走到这里,
    /// 若再走公共 getBusinessManager() 的 lock_guard 会对同一把非递归 mutex_ 二次加锁 = 自死锁)
    std::shared_ptr<notification_center::BusinessManager> getBusinessManagerLocked();

    SessionDeps deps_;

    std::shared_ptr<WorkspaceImpl> workspace_instance_;
    std::map<std::string, std::shared_ptr<IDrone>> drone_instances_;
    std::map<std::string, std::shared_ptr<IDock>> dock_instances_;
    std::map<std::string, std::shared_ptr<IRemoteController>> remote_controller_instances_;
    std::map<std::string, std::shared_ptr<IBaseStation>> base_station_instances_;
    std::map<std::string, std::shared_ptr<IRelay>> relay_instances_;

    mutable std::mutex mutex_;
};

}  // namespace flight_hub_sdk
