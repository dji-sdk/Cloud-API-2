// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <memory>
#include <string>
#include <atomic>
#include <mutex>
#include <functional>
#include <unordered_map>
#include <map>
#include <vector>

#include "api/message.h"
#include "base/async/timer.h"
#include "common/util/async_capture_protect.h"

// 前向声明
namespace notification_center {
    class SDKManager;
    class BusinessManager;
    class WebsocketEvent;
    class WebSocketBase;  // 添加 WebSocketBase 前向声明
}

namespace flight_hub_sdk {

/**
 * NotificationCenter 连接状态枚举
 * 
 * 状态流转图：
 * 
 *     UNINITIALIZED (初始状态)
 *          |
 *          | initialize()
 *          v
 *     INITIALIZED
 *          |
 *          | connect() (在 initialize() 中自动调用)
 *          v
 *     CONNECTING
 *          |
 *          | 连接成功 (OnOpen())
 *          v
 *     CONNECTED
 *          |
 *          | OnClose() 或 OnError()
 *          v
 *     DISCONNECTED
 *          |
 *          | tryTransition(DISCONNECTED -> RECONNECTING) (延迟调度，防止重复)
 *          v
 *     RECONNECTING
 *          |
 *          | reconnect() -> connect()
 *          v
 *     CONNECTING
 *          |
 *          | 连接成功 (OnOpen())
 *          v
 *     CONNECTED
 * 
 * 状态说明：
 * - UNINITIALIZED: 未初始化，需要调用 initialize()
 * - INITIALIZED: 已初始化但未连接，可以连接
 * - CONNECTING: 正在连接中，不允许重复连接（通过原子状态转换保证）
 * - CONNECTED: 已连接，正常工作状态
 * - DISCONNECTED: 已断开连接（但已初始化），可以重连
 * - RECONNECTING: 重连调度中，等待重连（防止 OnError 和 OnClose 同时触发时重复调度）
 * 
 * 线程安全保证：
 * - 所有状态转换使用 compare_exchange_strong 原子操作
 * - 同一时间只能有一个状态值
 * - OnError 和 OnClose 通过 DISCONNECTED -> RECONNECTING 转换防止重复调度 reconnect
 * - 状态转换失败时会记录日志并忽略操作
 */
enum class NotificationCenterState {
    UNINITIALIZED,  // 未初始化
    INITIALIZED,    // 已初始化但未连接
    CONNECTING,     // 连接中
    CONNECTED,      // 已连接
    RECONNECTING,   // 重连中
    DISCONNECTED    // 已断开（但已初始化）
};

/**
 * NotificationCenter 通知中心管理类
 * 可实例化，每个 Session 持有自己的 NC 实例；无全局 default。
 */
class NotificationCenter : public drc_sdk::common::AsyncCaptureProtect {
public:
    NotificationCenter();
    ~NotificationCenter() override;

    uint64_t setConnectionCallback(const std::function<void(bool connected)>& callback);

    
    void removeConnectionCallback(uint64_t id);

    /**
     * 初始化通知中心（连 WS / 建 BM）。
     * 日志由上层 SDK（或 NCSDK 默认）配置；InitializePlog 有重入保护。
     * @param websocket_url WebSocket 服务器 URL
     * @param websocket_auth_headers WebSocket 鉴权头
     * @param verify_ssl WSS 证书验证开关（安全默认开启）；开启后校验证书链与主机名/IP，关闭会产生 WARN 日志，仅限受控测试环境
     * @param ssl_ca_path 私有 CA 证书文件路径（PEM）；为空时使用系统 CA 信任库
     * @return 是否初始化成功
     */
    bool initialize(const std::string& websocket_url,
                    const std::vector<HeaderKeyValue>& websocket_auth_headers,
                    bool verify_ssl = true,
                    const std::string& ssl_ca_path = "");
    void setWebSocketAuthHeaders(const std::vector<HeaderKeyValue>& headers);

    /**
     * 设置 WebSocket 实现（用于 WASM 环境注入）
     * @param ws WebSocket 实现指针，由调用者负责生命周期管理
     */
    void setWebSocket(notification_center::WebSocketBase* ws);

    /**
     * 获取业务管理器，用于订阅各种通知
     * @return BusinessManager 智能指针，如果未初始化则返回 nullptr
     */
    std::shared_ptr<notification_center::BusinessManager> getBusinessManager();

    /**
     * 检查是否已初始化
     * @return 是否已初始化
     */
    bool isInitialized() const;

    /**
     * 检查连接状态
     * @return 是否已连接到服务器
     */
    bool isConnected() const;

    /**
     * 清理资源并关闭连接
     */
    void cleanup();

    void reconnect();

private:
    // 内部 WebSocket 事件监听器
    class InternalWebsocketEventListener;

    static std::map<std::string, std::string> headersToMap(
        const std::vector<HeaderKeyValue>& headers);

    // 通知所有连接回调
    void notifyConnectionCallbacks();
    
    // 状态转换辅助函数（私有，供内部使用）
    bool tryTransition(NotificationCenterState expected, NotificationCenterState desired);
    NotificationCenterState getState() const;
    bool isState(NotificationCenterState s) const;
    bool canReconnect() const;  // 内部状态检查

private:
    std::string websocket_url_;
    std::shared_ptr<notification_center::SDKManager> sdk_manager_;
    std::shared_ptr<InternalWebsocketEventListener> websocket_listener_;
    
    std::atomic<NotificationCenterState> state_;  // 通知中心状态

    // 连接状态回调管理
    std::unordered_map<uint64_t, std::function<void(bool)>> connection_callbacks_;
    std::atomic<uint64_t> next_callback_id_;
    mutable std::mutex callbacks_mutex_;
};

} // namespace flight_hub_sdk
