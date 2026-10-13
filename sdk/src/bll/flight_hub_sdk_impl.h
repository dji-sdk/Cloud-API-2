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
    std::shared_ptr<IDrone> getDroneInstance(const std::string& sn);
    std::shared_ptr<IDock> getDockInstance(const std::string& sn);
    std::shared_ptr<IRemoteController> getRemoteControllerInstance(const std::string& sn);
    std::shared_ptr<IBaseStation> getBaseStationInstance(const std::string& sn);
    std::shared_ptr<IRelay> getRelayInstance(const std::string& sn);

    std::shared_ptr<IStream> getStream();

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

    // 全局单例 Workspace
    std::shared_ptr<WorkspaceImpl> workspace_instance_;
    // sn -> Device 实例
    std::map<std::string, std::shared_ptr<IDrone>> drone_instances_;
    std::map<std::string, std::shared_ptr<IDock>> dock_instances_;
    std::map<std::string, std::shared_ptr<IRemoteController>> remote_controller_instances_;
    std::map<std::string, std::shared_ptr<IBaseStation>> base_station_instances_;
    std::map<std::string, std::shared_ptr<IRelay>> relay_instances_;
    std::shared_ptr<IStream> stream_instance_;

#if defined(__unix__) || defined(__APPLE__)
    std::unique_ptr<MainExecutorBlockingDetectorAdapter> blocking_detector_adapter_;
    std::unique_ptr<blocking_detector::BlockingDetector> blocking_detector_;
#endif
};

} // namespace flight_hub_sdk
