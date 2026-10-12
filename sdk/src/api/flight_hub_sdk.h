// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>
#include <memory>
#include <cstdint>
#include <map>
#include <vector>
#include "drone.h"
#include "dock.h"
#include "remote_controller.h"
#include "base_station.h"
#include "relay.h"
#include "workspace.h"
#include "message.h"
#include "stream.h"
#include "value.h"
#include "bridge/i_rtc_channel_manager_bridge.h"
#include "bridge/i_mqtt_client_manager_bridge.h"

namespace flight_hub_sdk {

/// FHSDK 主入口类，提供 SDK 初始化/销毁、配置管理、鉴权设置、实例获取等功能。
class FlightHubSdk {
public:
    virtual ~FlightHubSdk() = default;

    /// @brief 使用指定配置初始化 SDK
    /// @param config SDK 配置（服务器地址、端口、日志级别等）
    /// @return 初始化结果错误码
    ///
    /// @internal
    /// 链路: SDK 内部 | 初始化日志、HTTP 客户端、NotificationCenter 等内部组件
    /// @endinternal
    static FlightHubSdkErrorCode initialize(const FlightHubSdkConfig& config);

    /// @brief 使用默认配置初始化 SDK
    /// @return 初始化结果错误码
    static FlightHubSdkErrorCode initialize();

    /// @brief 销毁 SDK，释放所有资源
    /// @return 销毁结果错误码
    static FlightHubSdkErrorCode shutdown();

    /// @brief 获取当前 SDK 配置
    /// @return 当前配置信息
    static FlightHubSdkConfig getConfig();

    /// @brief 查询 SDK 是否已初始化
    /// @return true 表示已初始化
    static bool isInitialized();

    /// @brief 获取 SDK 版本号
    /// @return 版本号字符串
    static std::string getVersion();

    /// @brief 获取 SDK 构建信息（版本、分支、提交等）
    /// @return 构建信息字符串
    static std::string getBuildInfo();

    /// @brief 设置日志级别
    /// @param level 日志级别：debug / info / warn / error
    /// @return 设置结果错误码
    static FlightHubSdkErrorCode setLogLevel(const std::string& level);

    /// @brief 设置 WebSocket 鉴权头（用于消息通知连接）
    /// @param headers 鉴权头键值对列表
    /// @return 设置结果错误码
    static FlightHubSdkErrorCode setWebSocketAuthHeaders(const std::vector<HeaderKeyValue>& headers);

    /// @brief 获取当前 WebSocket 鉴权头
    /// @return 鉴权头键值对列表
    static std::vector<HeaderKeyValue> getWebSocketAuthHeaders();

    /// @brief 设置 HTTP 请求头（用于所有 HTTP 接口，由外部注入 gateway 鉴权头）
    /// @param headers 请求头键值对列表
    /// @return 设置结果错误码
    static FlightHubSdkErrorCode setHttpHeaders(const std::vector<HeaderKeyValue>& headers);

    /// @brief 获取当前 HTTP 请求头
    /// @return 请求头键值对列表
    static std::vector<HeaderKeyValue> getHttpHeaders();

    // ---- 设备实例获取 ----

    /// @brief 获取 Workspace 实例（全局单例）
    /// @return Workspace 实例
    static std::shared_ptr<IWorkspace> getWorkspaceInstance();

    /// @brief 通过设备 SN 获取 Drone 实例
    /// @param sn 飞行器序列号
    /// @return Drone 实例
    static std::shared_ptr<IDrone> getDroneInstance(const std::string& sn);

    /// @brief 通过设备 SN 获取 Dock 实例
    /// @param sn 机场序列号
    /// @return Dock 实例
    static std::shared_ptr<IDock> getDockInstance(const std::string& sn);

    /// @brief 通过设备 SN 获取 RemoteController 实例
    /// @param sn 遥控器序列号
    /// @return RemoteController 实例
    static std::shared_ptr<IRemoteController> getRemoteControllerInstance(const std::string& sn);

    /// @brief 通过设备 SN 获取 BaseStation 实例
    /// @param sn 基站序列号
    /// @return BaseStation 实例
    static std::shared_ptr<IBaseStation> getBaseStationInstance(const std::string& sn);

    /// @brief 通过设备 SN 获取 Relay 实例
    /// @param sn 中继序列号
    /// @return Relay 实例
    static std::shared_ptr<IRelay> getRelayInstance(const std::string& sn);

    /// @brief 获取 Stream 实例（全局单例，用于获取直播和 CustomSEI 模块）
    /// @return Stream 实例
    static std::shared_ptr<IStream> getStream();

    /// @brief 注入 RTC 频道管理器（第三方 SDK 注入）
    /// @param rtc_channel_manager RTC 频道管理器实例
    /// @return 注入结果错误码
    static FlightHubSdkErrorCode injectRtcChannelManager(std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc_channel_manager);

    /// @brief 注入 MQTT 客户端管理器（第三方 SDK 注入）
    /// @param mqtt_client_manager MQTT 客户端管理器实例
    /// @return 注入结果错误码
    static FlightHubSdkErrorCode injectMqttClientManager(std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt_client_manager);

    /// @brief 注入 WebSocket 实例（用于 WASM 环境）
    /// @param ws WebSocket 实例
    /// @return 注入结果错误码
    static FlightHubSdkErrorCode injectWebSocket(std::shared_ptr<void> ws);
};

} // namespace flight_hub_sdk

