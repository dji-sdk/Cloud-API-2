// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "notification_center.h"

#include <SDKManager.h>
#include <BusinessManager.h>
#include <WebsocketEvent.h>
#include <Common.h>
#include <base/common/logger.h>
#include <chrono>

#include "SDKFactory.h"
#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

constexpr auto kWebsocketRetryInterval = 1000; // websocket 断连重连间隔 ms

// 内部 WebSocket 事件监听器实现
class NotificationCenter::InternalWebsocketEventListener : public notification_center::WebsocketEvent, public drc_sdk::common::AsyncCaptureProtect{
public:
    explicit InternalWebsocketEventListener(NotificationCenter* notification_center)
        : notification_center_(notification_center) {}

    void OnOpen() override {
        if (notification_center_) {
            // 转换到 CONNECTED 状态
            if (notification_center_->tryTransition(NotificationCenterState::CONNECTING, NotificationCenterState::CONNECTED)) {
                LOG_INFO("[NotificationCenter] WebSocket 连接已建立");
            } else {
                LOG_WARN("[NotificationCenter] 连接建立但状态转换失败，当前状态: %d", static_cast<int>(notification_center_->getState()));
            }
            notification_center_->notifyConnectionCallbacks();
        }
    }

    void OnClose() override {
        if (notification_center_) {
            // 转换到 DISCONNECTED 状态
            NotificationCenterState current = notification_center_->getState();
            if (current == NotificationCenterState::CONNECTED || current == NotificationCenterState::CONNECTING) {
                notification_center_->tryTransition(current, NotificationCenterState::DISCONNECTED);
            }
            
            LOG_INFO("[NotificationCenter] WebSocket 连接已关闭");
            notification_center_->notifyConnectionCallbacks();
            
            // 检查是否可以重连，防止 OnError 和 OnClose 同时触发时重复调度
            // 尝试转换到 RECONNECTING 状态，如果成功则调度重连
            if (notification_center_->tryTransition(NotificationCenterState::DISCONNECTED, NotificationCenterState::RECONNECTING)) {
                WeakDummy(weak_ptr);
                drc_sdk::common::Executors::Main()->PostDelay([this, weak_ptr]()->void {
                    WeakDummyReturn(weak_ptr);
                    notification_center_->reconnect();
                }, std::chrono::milliseconds(kWebsocketRetryInterval));
            } else {
                LOG_DEBUG("[NotificationCenter] reconnect 已调度，忽略重复请求（OnClose）");
            }
        }
    }

    void OnError(const std::string& error) override {
        if (notification_center_) {
            // 转换到 DISCONNECTED 状态
            NotificationCenterState current = notification_center_->getState();
            if (current == NotificationCenterState::CONNECTED || current == NotificationCenterState::CONNECTING) {
                notification_center_->tryTransition(current, NotificationCenterState::DISCONNECTED);
            }
            
            LOG_ERROR("[NotificationCenter] WebSocket 错误: %s", error.c_str());
            notification_center_->notifyConnectionCallbacks();
            
            // 检查是否可以重连，防止 OnError 和 OnClose 同时触发时重复调度
            // 尝试转换到 RECONNECTING 状态，如果成功则调度重连
            if (notification_center_->tryTransition(NotificationCenterState::DISCONNECTED, NotificationCenterState::RECONNECTING)) {
                WeakDummy(weak_ptr);
                drc_sdk::common::Executors::Main()->PostDelay([this, weak_ptr]()->void {
                    WeakDummyReturn(weak_ptr);
                    notification_center_->reconnect();
                }, std::chrono::milliseconds(kWebsocketRetryInterval));
            } else {
                LOG_DEBUG("[NotificationCenter] reconnect 已调度，忽略重复请求（OnError）");
            }
        }
    }

private:
    NotificationCenter* notification_center_;
};

NotificationCenter::NotificationCenter()
    : state_(NotificationCenterState::UNINITIALIZED)
    , next_callback_id_(1) {
    // 创建 SDKManager 实例
    sdk_manager_ = notification_center::SDKFactory::createSDKManager();
}

NotificationCenter::~NotificationCenter() {
    cleanup();
    notification_center::SDKFactory::destroySDKManager(sdk_manager_);
}

NotificationCenter* NotificationCenter::getInstance() {
    static NotificationCenter instance;
    return &instance;
}

uint64_t NotificationCenter::setConnectionCallback(const std::function<void(bool connected)>& callback) {
    uint64_t callback_id;
    bool current_connected;
    
    {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        callback_id = next_callback_id_.fetch_add(1);
        connection_callbacks_[callback_id] = callback;
        current_connected = isConnected();
    }
    
    // 在锁外立即回调当前连接状态
    callback(current_connected);
    
    LOG_INFO("[NotificationCenter] 注册连接状态回调，ID: %llu，当前状态: %s", callback_id, current_connected ? "已连接" : "未连接");
    
    return callback_id;
}

void NotificationCenter::removeConnectionCallback(uint64_t id) {
    std::lock_guard<std::mutex> lock(callbacks_mutex_);
    
    auto it = connection_callbacks_.find(id);
    if (it != connection_callbacks_.end()) {
        connection_callbacks_.erase(it);
        LOG_INFO("[NotificationCenter] 移除连接状态回调，ID: %llu", id);
    } else {
        LOG_INFO("[NotificationCenter] 未找到连接状态回调，ID: %llu", id);
    }
}

bool NotificationCenter::initialize(const std::string& log_path, int log_level, bool log_to_console, const std::string& websocket_url, const std::map<std::string, std::string>& websocket_auth_headers, bool verify_ssl, const std::string& ssl_ca_path) {
    LOG_INFO("[NotificationCenter] initialize, url: %s, log_level: %d, log_path: %s",
             websocket_url.c_str(), log_level, log_path.c_str());

    websocket_url_ = websocket_url;

    // 检查是否已经初始化
    if (!tryTransition(NotificationCenterState::UNINITIALIZED, NotificationCenterState::INITIALIZED)) {
        LOG_ERROR("[NotificationCenter] initialize 已经初始化过了，当前状态: %d", static_cast<int>(getState()));
        return false;
    }

    // 准备初始化信息
    notification_center::SdkInitializeInfo init_info;
    init_info.log_path = log_path;
    init_info.log_level = static_cast<notification_center::SdkLogLevel>(log_level);
    init_info.log_to_console = log_to_console;
    // WSS TLS 证书验证配置透传(verify_ssl 安全默认开启)
    init_info.verify_ssl = verify_ssl;
    init_info.ssl_ca_path = ssl_ca_path;

    // 初始化 SDK
    sdk_manager_->init(init_info);

    if (!sdk_manager_->isInit()) {
        LOG_ERROR("[NotificationCenter] initialize 初始化失败");
        // 初始化失败，回退到 UNINITIALIZED 状态
        tryTransition(NotificationCenterState::INITIALIZED, NotificationCenterState::UNINITIALIZED);
        return false;
    }

    // 创建并设置 WebSocket 事件监听器
    websocket_listener_ = std::make_shared<InternalWebsocketEventListener>(this);
    sdk_manager_->setWebsocketEventListener(websocket_listener_);
    sdk_manager_->setWebSocketAuthHeaders(websocket_auth_headers);
    LOG_INFO("[NotificationCenter] websocket auth headers 已下发，数量: %zu", websocket_auth_headers.size());

    // 连接到 WebSocket 服务器，转换到 CONNECTING 状态
    if (tryTransition(NotificationCenterState::INITIALIZED, NotificationCenterState::CONNECTING)) {
        sdk_manager_->connect(websocket_url_);
    } else {
        LOG_WARN("[NotificationCenter] 状态转换失败，无法连接，当前状态: %d", static_cast<int>(getState()));
    }
    
    LOG_INFO("[NotificationCenter] initialize 初始化成功");
    return true;
}

void NotificationCenter::setWebSocketAuthHeaders(const std::map<std::string, std::string>& headers) {
    if (!sdk_manager_) {
        LOG_WARN("[NotificationCenter] setWebSocketAuthHeaders skipped: sdk_manager is null");
        return;
    }
    sdk_manager_->setWebSocketAuthHeaders(headers);
    LOG_INFO("[NotificationCenter] setWebSocketAuthHeaders: applied %zu header(s)", headers.size());
}

void NotificationCenter::setWebSocket(notification_center::WebSocketBase* ws) {
    if (sdk_manager_ && ws) {
        LOG_INFO("[NotificationCenter] 设置 WebSocket 实现: %p", ws);
        sdk_manager_->setWebSocket(ws);
    } else {
        LOG_WARN("[NotificationCenter] 无法设置 WebSocket，sdk_manager_: %p, ws: %p", sdk_manager_.get(), ws);
    }
}

std::shared_ptr<notification_center::BusinessManager> NotificationCenter::getBusinessManager() {
    if (!isInitialized() || !sdk_manager_) {
        LOG_INFO("[NotificationCenter] getBusinessManager error");
        return nullptr;
    }
    return sdk_manager_->getBusinessManager();
}

bool NotificationCenter::isInitialized() const {
    return state_.load() != NotificationCenterState::UNINITIALIZED;
}

bool NotificationCenter::isConnected() const {
    return state_.load() == NotificationCenterState::CONNECTED;
}

void NotificationCenter::cleanup() {
    // 重置到 UNINITIALIZED 状态
    state_.store(NotificationCenterState::UNINITIALIZED);

    // 清理连接回调
    {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        connection_callbacks_.clear();
        LOG_INFO("[NotificationCenter] 清理了所有连接状态回调");
    }

    // 清理资源
    websocket_listener_.reset();

    LOG_INFO("[NotificationCenter] 资源清理完成");
}

void NotificationCenter::reconnect() {
    LOG_INFO("[NotificationCenter] reconnect");
    
    // 从 RECONNECTING 状态转换到 CONNECTING 状态
    if (tryTransition(NotificationCenterState::RECONNECTING, NotificationCenterState::CONNECTING)) {
        sdk_manager_->connect(websocket_url_);
    } else {
        // 如果状态转换失败，可能是状态已经改变（比如已经连接或正在连接）
        NotificationCenterState current = getState();
        LOG_DEBUG("[NotificationCenter] reconnect 状态转换失败，当前状态: %d", static_cast<int>(current));
        
        // 如果当前是 DISCONNECTED 状态，也可以尝试连接
        if (current == NotificationCenterState::DISCONNECTED) {
            if (tryTransition(NotificationCenterState::DISCONNECTED, NotificationCenterState::CONNECTING)) {
                sdk_manager_->connect(websocket_url_);
            }
        }
    }
}

// 状态转换辅助函数实现
bool NotificationCenter::tryTransition(NotificationCenterState expected, NotificationCenterState desired) {
    NotificationCenterState exp = expected;
    bool success = state_.compare_exchange_strong(exp, desired);
    if (success) {
        LOG_DEBUG("[NotificationCenter] 状态转换 %d -> %d", static_cast<int>(expected), static_cast<int>(desired));
    } else {
        LOG_DEBUG("[NotificationCenter] 状态转换失败，期望 %d，实际 %d", static_cast<int>(expected), static_cast<int>(exp));
    }
    return success;
}

NotificationCenterState NotificationCenter::getState() const {
    return state_.load();
}

bool NotificationCenter::isState(NotificationCenterState s) const {
    return state_.load() == s;
}

bool NotificationCenter::canReconnect() const {
    NotificationCenterState s = state_.load();
    return s == NotificationCenterState::DISCONNECTED || 
           s == NotificationCenterState::CONNECTED;
}

void NotificationCenter::notifyConnectionCallbacks() {
    // 先复制回调列表，避免在持有锁时调用回调导致死锁
    std::unordered_map<uint64_t, std::function<void(bool)>> callbacks_copy;
    
    {
        std::lock_guard<std::mutex> lock(callbacks_mutex_);
        callbacks_copy = connection_callbacks_;
    }
    
    // 获取当前连接状态
    bool connected = isConnected();
    
    // 在锁外调用回调
    for (const auto& [id, callback] : callbacks_copy) {
        callback(connected);
    }
    
    LOG_INFO("[NotificationCenter] 通知了 %zu 个连接状态回调，状态: %s", callbacks_copy.size(), connected ? "已连接" : "未连接");
}

} // namespace flight_hub_sdk
