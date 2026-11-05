// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "api/flight_hub_sdk.h"
#include <base/common/logger.h>
#include <common/executors/executors.h>
#include <map>
#include <vector>
#include <mutex>
#include <atomic>
#include <memory>
#include "bridge/i_rtc_channel_manager_bridge.h"
#include "bridge/i_mqtt_client_manager_bridge.h"
#include "workspace/workspace_impl.h"
#include "module/cascade_module/cascade_module_impl.h"
#include "session/session_manager.h"
#include "api/cascade_module.h"
#if defined(__unix__) || defined(__APPLE__)
#include "blocking_detector_adapter.h"
#endif

// 前向声明
namespace notification_center {
    class WebSocketBase;
}

namespace flight_hub_sdk {

class FlightHubSdkImpl {
public:
    FlightHubSdkImpl();

    FlightHubSdkErrorCode initialize(const FlightHubSdkConfig& config);

    FlightHubSdkErrorCode shutdown();

    bool isInitialized();

    std::string getVersion();

    std::string getBuildInfo();

    FlightHubSdkErrorCode setLogLevel(const std::string& level);
    FlightHubSdkErrorCode setWebSocketAuthHeaders(const std::vector<HeaderKeyValue>& headers);
    std::vector<HeaderKeyValue> getWebSocketAuthHeaders();
    FlightHubSdkErrorCode setHttpHeaders(const std::vector<HeaderKeyValue>& headers);
    std::vector<HeaderKeyValue> getHttpHeaders();

    // ---- 设备实例获取 ----
    std::shared_ptr<IWorkspace> getWorkspaceInstance();
    std::shared_ptr<IWorkspace> getWorkspaceInstance(int session_id);
    std::shared_ptr<IDrone> getDroneInstance(const std::string& sn);
    std::shared_ptr<IDrone> getDroneInstance(int session_id, const std::string& sn);
    std::shared_ptr<IDock> getDockInstance(const std::string& sn);
    std::shared_ptr<IDock> getDockInstance(int session_id, const std::string& sn);
    std::shared_ptr<IRemoteController> getRemoteControllerInstance(const std::string& sn);
    std::shared_ptr<IRemoteController> getRemoteControllerInstance(int session_id, const std::string& sn);
    std::shared_ptr<IBaseStation> getBaseStationInstance(const std::string& sn);
    std::shared_ptr<IBaseStation> getBaseStationInstance(int session_id, const std::string& sn);
    std::shared_ptr<IRelay> getRelayInstance(const std::string& sn);
    std::shared_ptr<IRelay> getRelayInstance(int session_id, const std::string& sn);

    std::shared_ptr<IStream> getStream();
    std::shared_ptr<ICascadeModule> getCascadeModule();

    std::shared_ptr<ISessionManager> getSessionManager();

    FlightHubSdkErrorCode injectRtcChannelManager(std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc_channel_manager);

    FlightHubSdkErrorCode injectMqttClientManager(std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt_client_manager);

    FlightHubSdkErrorCode injectWebSocket(std::shared_ptr<notification_center::WebSocketBase> ws);

    FlightHubSdkConfig getConfig();
private:
    std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc_channel_manager_;
    std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt_client_manager_;
    std::shared_ptr<notification_center::WebSocketBase> websocket_; // 注入的 WebSocket（使用 shared_ptr 管理生命周期）

    std::atomic<bool> initialized_;
    std::mutex mutex_;

    // 配置信息
    FlightHubSdkConfig config_;
    std::vector<HeaderKeyValue> websocket_auth_headers_;
    std::vector<HeaderKeyValue> http_headers_;

    // Session 管理器（成员构造即存在；initialize 时挂上 session 0）
    std::shared_ptr<SessionManager> session_manager_ = std::make_shared<SessionManager>();

    std::shared_ptr<IStream> stream_instance_;
    std::shared_ptr<ICascadeModule> cascade_module_;

#if defined(__unix__) || defined(__APPLE__)
    std::unique_ptr<MainExecutorBlockingDetectorAdapter> blocking_detector_adapter_;
    std::unique_ptr<blocking_detector::BlockingDetector> blocking_detector_;
#endif
};

} // namespace flight_hub_sdk
