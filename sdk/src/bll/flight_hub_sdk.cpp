// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "api/flight_hub_sdk.h"
#include "version.h"
#include <base/common/logger.h>
#include <common/executors/executors.h>
#include "flight_hub_sdk_impl.h"
#include <websocket/WebSocketBase.h>

using namespace flight_hub_sdk;

FlightHubSdkImpl& GetImplInstance(){
    static FlightHubSdkImpl ins;
    return ins;
}

FlightHubSdkErrorCode FlightHubSdk::initialize(const FlightHubSdkConfig& config) {
    return GetImplInstance().initialize(config);
}

FlightHubSdkErrorCode FlightHubSdk::initialize() {
    FlightHubSdkConfig default_config;
    return initialize(default_config);
}

FlightHubSdkErrorCode FlightHubSdk::shutdown() {
    return GetImplInstance().shutdown();
}

FlightHubSdkConfig FlightHubSdk::getConfig() {
    return GetImplInstance().getConfig();
}

bool FlightHubSdk::isInitialized() {
    return GetImplInstance().isInitialized();
}

std::string FlightHubSdk::getVersion() {
    return GetImplInstance().getVersion();
}

std::string FlightHubSdk::getBuildInfo() {
    return GetImplInstance().getBuildInfo();
}

FlightHubSdkErrorCode FlightHubSdk::setLogLevel(const std::string& level) {
    return GetImplInstance().setLogLevel(level);
}

FlightHubSdkErrorCode FlightHubSdk::setWebSocketAuthHeaders(const std::vector<HeaderKeyValue>& headers) {
    return GetImplInstance().setWebSocketAuthHeaders(headers);
}

std::vector<HeaderKeyValue> FlightHubSdk::getWebSocketAuthHeaders() {
    return GetImplInstance().getWebSocketAuthHeaders();
}

FlightHubSdkErrorCode FlightHubSdk::setHttpHeaders(const std::vector<HeaderKeyValue>& headers) {
    return GetImplInstance().setHttpHeaders(headers);
}

std::vector<HeaderKeyValue> FlightHubSdk::getHttpHeaders() {
    return GetImplInstance().getHttpHeaders();
}

// ---- 设备实例获取 ----

std::shared_ptr<IWorkspace> FlightHubSdk::getWorkspaceInstance() {
    return GetImplInstance().getWorkspaceInstance();
}

std::shared_ptr<IDrone> FlightHubSdk::getDroneInstance(const std::string& sn) {
    return GetImplInstance().getDroneInstance(sn);
}

std::shared_ptr<IDock> FlightHubSdk::getDockInstance(const std::string& sn) {
    return GetImplInstance().getDockInstance(sn);
}

std::shared_ptr<IRemoteController> FlightHubSdk::getRemoteControllerInstance(const std::string& sn) {
    return GetImplInstance().getRemoteControllerInstance(sn);
}

std::shared_ptr<IBaseStation> FlightHubSdk::getBaseStationInstance(const std::string& sn) {
    return GetImplInstance().getBaseStationInstance(sn);
}

std::shared_ptr<IRelay> FlightHubSdk::getRelayInstance(const std::string& sn) {
    return GetImplInstance().getRelayInstance(sn);
}

std::shared_ptr<IStream> FlightHubSdk::getStream() {
    return GetImplInstance().getStream();
}

FlightHubSdkErrorCode FlightHubSdk::injectRtcChannelManager(std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc_channel_manager) {
    return GetImplInstance().injectRtcChannelManager(rtc_channel_manager);
}

FlightHubSdkErrorCode FlightHubSdk::injectMqttClientManager(std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt_client_manager) {
    return GetImplInstance().injectMqttClientManager(mqtt_client_manager);
}

FlightHubSdkErrorCode FlightHubSdk::injectWebSocket(std::shared_ptr<void> ws) {
    if (!ws) {
        return FlightHubSdkErrorCode::ERROR_INVALID_PARAM;
    }
    auto websocket = std::static_pointer_cast<notification_center::WebSocketBase>(ws);
    return GetImplInstance().injectWebSocket(websocket);
}
