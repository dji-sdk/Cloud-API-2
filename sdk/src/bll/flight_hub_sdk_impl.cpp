// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flight_hub_sdk_impl.h"
#include "version.h"
#include <base/common/logger.h>
#include <common/executors/executors.h>
#include <atomic>
#include <cctype>
#include <string>
#include "drone/drone_impl.h"
#include "dock/dock_impl.h"
#include "remote_controller/remote_controller_impl.h"
#include "base_station/base_station_impl.h"
#include "relay/relay_impl.h"
#include <common/network/network.h>
#include <common/network/impl/network_impl.h>
#include "notification_center/notification_center.h"
#include "workspace/workspace_impl.h"
#include "stream/stream_impl.h"
#if defined(__unix__) || defined(__APPLE__)
#include "blocking_detector.h"
#include <cstdlib>
#include <cstring>
#endif

using namespace flight_hub_sdk;

namespace {

std::map<std::string, std::string> headersToMap(const std::vector<HeaderKeyValue>& headers) {
    std::map<std::string, std::string> result;
    for (const auto& kv : headers) {
        if (!kv.key.empty()) {
            result[kv.key] = kv.value;
        }
    }
    return result;
}

// 解析日志级别字符串：大小写不敏感，并兼容 "warn" / "warning" 两种写法。
// 与 Java 层保持对齐；无法识别时回退到 info。
drc_sdk::LogLevel parseLogLevel(const std::string& level) {
    std::string normalized;
    normalized.reserve(level.size());
    for (char c : level) {
        normalized.push_back(static_cast<char>(std::tolower(static_cast<unsigned char>(c))));
    }

    if (normalized == "none")   return drc_sdk::LogLevel::kNone;
    if (normalized == "debug")  return drc_sdk::LogLevel::kDebug;
    if (normalized == "info")   return drc_sdk::LogLevel::kInfo;
    if (normalized == "warn" || normalized == "warning") return drc_sdk::LogLevel::kWarning;
    if (normalized == "error")  return drc_sdk::LogLevel::kError;
    if (normalized == "fatal")  return drc_sdk::LogLevel::kFatal;

    return drc_sdk::LogLevel::kInfo; // 默认 info
}

} // anonymous namespace

FlightHubSdkImpl::FlightHubSdkImpl() : initialized_(false) {
}

FlightHubSdkErrorCode FlightHubSdkImpl::initialize(const FlightHubSdkConfig& config) {
    if (initialized_.load()) {
        return FlightHubSdkErrorCode::ERROR_ALREADY_INITIALIZED;
    }

    // 解析日志级别（大小写不敏感，兼容 warn/warning），与 Java 层对齐
    auto log_level = parseLogLevel(config.log_level);

    std::cout << "[FlightHubSdkImpl] log_level: " << static_cast<int>(log_level) << ", " << config.log_level << std::endl;

    drc_sdk::PlogLogger::InitializePlog(static_cast<int>(log_level),
                                       config.log_file_path.c_str(),
                                       config.log_to_console,
                                       config.log_max_file_size,
                                       config.log_max_files);
    LOG_INFO("[FlightHubSdkImpl] log_to_console: %s, log_file: %s, max_file_size: %zu, max_files: %d",
             config.log_to_console ? "true" : "false",
             config.log_file_path.c_str(),
             config.log_max_file_size,
             config.log_max_files);


    // TODO: 检查配置有效性，比如 address 是否有效
    config_ = config;

    // 打印SDK版本和构建信息
    LOG_INFO("FlightHubSdk version: %s", getVersion().c_str());
    LOG_INFO("FlightHubSdk build info:\n%s", getBuildInfo().c_str());

    // 初始化 drc_sdk executors
    LOG_INFO("SDK startup, init drc_sdk executors");
    drc_sdk::common::Executors::Init();
    LOG_INFO("SDK startup, init drc_sdk executors done");

#if defined(__unix__) || defined(__APPLE__)
    const char* enable_env = std::getenv("FLIGHTHUB_ENABLE_BLOCKING_DETECTOR");
    const bool enable_blocking_detector = enable_env != nullptr
        && (std::strcmp(enable_env, "1") == 0 || std::strcmp(enable_env, "true") == 0);
    if (enable_blocking_detector) {
        blocking_detector_adapter_ = std::make_unique<MainExecutorBlockingDetectorAdapter>();
        blocking_detector::BlockingDetectorConfig det_config;
        det_config.max_wait_count = 10000;
        det_config.block_wait_count = 2000;
        det_config.check_interval_ms = 2000;
        det_config.crash_dir = "./blocking_detector";
        det_config.abort_on_blocking = false;
        det_config.dump_all_threads = true;
        blocking_detector_ = std::make_unique<blocking_detector::BlockingDetector>(
            blocking_detector_adapter_.get(), det_config);
        if (blocking_detector_->Init()) {
            blocking_detector_->Start();
            LOG_INFO("SDK startup, BlockingDetector started");
        } else {
            LOG_WARN("SDK startup, BlockingDetector init failed, continue without it");
            blocking_detector_.reset();
            blocking_detector_adapter_.reset();
        }
    } else {
        LOG_INFO("SDK startup, BlockingDetector disabled");
    }
#endif

    // TODO: 都改为同一个类名，在common的Cmake中，需要根据平台来选择不同的实现
    drc_sdk::common::Network::Instance().Init();

    // HTTPS 通道 TLS 证书验证配置统一下发。
    // 与 WSS 通道(P0-01)共用 FlightHubSdkConfig 的 verify_ssl/ssl_ca_path 字段:
    // verify_ssl 安全默认开启(校验证书链+主机名), 私有化自签名部署经 ssl_ca_path 提供私有 CA,
    // verify_ssl=false 会产生 WARN 日志(不安全, 仅限受控测试环境)。
    drc_sdk::common::Network::Instance().SetTlsConfig(config.verify_ssl, config.ssl_ca_path);

    // 如果注入了 WebSocket，先传递给 NotificationCenter（必须在 initialize 之前）
    if (websocket_) {
        LOG_INFO("[FlightHubSdkImpl] 注入 WebSocket 到 NotificationCenter: %p", websocket_.get());
        NotificationCenter::getInstance()->setWebSocket(websocket_.get());
    }

    std::ostringstream ws_headers_log;
    bool first_ws_header = true;
    for (const auto& item : websocket_auth_headers_) {
        if (!first_ws_header) {
            ws_headers_log << ";";
        }
        ws_headers_log << item.key << "=" << item.value;
        first_ws_header = false;
    }
    LOG_INFO("[FlightHubSdkImpl] initialize websocket_auth_headers_: size=%zu, values=%s",
             websocket_auth_headers_.size(), ws_headers_log.str().c_str());

    // notification center sdk 初始化（会在内部调用 connect，此时 WebSocket 应该已经注入）
    // 透传 WSS TLS 证书验证配置(verify_ssl 安全默认开启; 私有化部署经 ssl_ca_path 提供 CA)
    NotificationCenter::getInstance()->initialize(
        "",
        static_cast<int>(log_level),
        config.log_to_console,
        config.notification_server_address,
        headersToMap(websocket_auth_headers_),
        config.verify_ssl,
        config.ssl_ca_path);

    initialized_.store(true);
    LOG_INFO("SDK startup done");
    return FlightHubSdkErrorCode::SUCCESS;
}

FlightHubSdkErrorCode FlightHubSdkImpl::shutdown() {
    if (!initialized_.load()) {
        LOG_ERROR("SDK not initialized, please call initialize first");
        return FlightHubSdkErrorCode::ERROR_NOT_INITIALIZED;
    }
    LOG_INFO("Shutting down SDK...");

    // 先置标志位，阻止其他线程进入 get*Instance（修复 TOCTOU）
    initialized_.store(false);

    // 清理所有设备对象，确保定时器和监听被正确停止
    {
        std::lock_guard<std::mutex> lock(mutex_);
        LOG_INFO("Cleaning up drone instances, count: %zu", drone_instances_.size());
        drone_instances_.clear();
        LOG_INFO("Cleaning up dock instances, count: %zu", dock_instances_.size());
        dock_instances_.clear();
        LOG_INFO("Cleaning up remote controller instances, count: %zu", remote_controller_instances_.size());
        remote_controller_instances_.clear();
        LOG_INFO("Cleaning up base station instances, count: %zu", base_station_instances_.size());
        base_station_instances_.clear();
        LOG_INFO("Cleaning up relay instances, count: %zu", relay_instances_.size());
        relay_instances_.clear();
        workspace_instance_.reset();
        stream_instance_.reset();
    }

    // 清理 notification center
    NotificationCenter::getInstance()->cleanup();

#if defined(__unix__) || defined(__APPLE__)
    if (blocking_detector_) {
        blocking_detector_->Stop();
        blocking_detector_.reset();
        blocking_detector_adapter_.reset();
        LOG_INFO("BlockingDetector stopped");
    }
#endif

    // 关闭所有 Executor 线程池，确保所有线程都能正确退出
    LOG_INFO("Shutting down Executors...");
    drc_sdk::common::Executors::Shutdown();
    LOG_INFO("Executors shutdown completed");

    LOG_INFO("SDK shutdown completed");

    return FlightHubSdkErrorCode::SUCCESS;
}

FlightHubSdkConfig FlightHubSdkImpl::getConfig() {
    return config_;
}

bool FlightHubSdkImpl::isInitialized() {
    return initialized_.load();
}

std::string FlightHubSdkImpl::getVersion() {
    std::ostringstream oss;
    oss << FLIGHT_HUB_VERSION_MAJOR << "."
        << FLIGHT_HUB_VERSION_MINOR << "."
        << FLIGHT_HUB_VERSION_ALTER << "."
        << FLIGHT_HUB_VERSION_TWEAK;
    return oss.str();
}

std::string FlightHubSdkImpl::getBuildInfo() {
    // 简化字符串操作以避免WebAssembly环境中的问题
    std::string info = "Version: ";
    info += getVersion();
    info += "\nBuild Date: ";
    info += FLIGHT_HUB_BUILD_DATE;
    info += " ";
    info += FLIGHT_HUB_BUILD_TIME;
    info += "\nPlatform: ";
    info += FLIGHT_HUB_BUILD_PLAT;
    info += " (";
    info += FLIGHT_HUB_BUILD_ARCH;
    info += ")";
    info += "\nCompiler: ";
    info += FLIGHT_HUB_BUILD_COMPILER;
    info += "\nGit Branch: ";
    info += FLIGHT_HUB_GIT_BRANCH;
    info += "\nGit Commit: ";
    info += FLIGHT_HUB_GIT_COMMIT_ID;
    return info;
}

FlightHubSdkErrorCode FlightHubSdkImpl::setLogLevel(const std::string& level) {
    if (!initialized_.load()) {
        LOG_ERROR("SDK not initialized");
        return FlightHubSdkErrorCode::ERROR_NOT_INITIALIZED;
    }

    std::lock_guard<std::mutex> lock(mutex_);
    auto log_level = parseLogLevel(level);
    config_.log_level = level;
    // 先在旧级别下记录一次变更，再应用新级别（否则提升级别后这条确认日志会被自身过滤掉）
    LOG_INFO("Log level set to: %s (severity=%d)", level.c_str(), static_cast<int>(log_level));

    drc_sdk::PlogLogger::SetLogLevel(static_cast<int>(log_level));

    return FlightHubSdkErrorCode::SUCCESS;
}

FlightHubSdkErrorCode FlightHubSdkImpl::setWebSocketAuthHeaders(const std::vector<HeaderKeyValue>& headers) {
    {
        std::lock_guard<std::mutex> lock(mutex_);
        websocket_auth_headers_ = headers;
        LOG_INFO("setWebSocketAuthHeaders: cached %zu header(s), initialized=%s",
                 websocket_auth_headers_.size(), initialized_.load() ? "true" : "false");
    }

    NotificationCenter::getInstance()->setWebSocketAuthHeaders(headersToMap(headers));

    return FlightHubSdkErrorCode::SUCCESS;
}

std::vector<HeaderKeyValue> FlightHubSdkImpl::getWebSocketAuthHeaders() {
    std::lock_guard<std::mutex> lock(mutex_);
    return websocket_auth_headers_;
}

FlightHubSdkErrorCode FlightHubSdkImpl::setHttpHeaders(const std::vector<HeaderKeyValue>& headers) {
    std::lock_guard<std::mutex> lock(mutex_);
    http_headers_ = headers;
    LOG_INFO("setHttpHeaders: updated %zu header(s)", http_headers_.size());
    return FlightHubSdkErrorCode::SUCCESS;
}

std::vector<HeaderKeyValue> FlightHubSdkImpl::getHttpHeaders() {
    std::lock_guard<std::mutex> lock(mutex_);
    return http_headers_;
}

// ---- 设备实例获取 ----

std::shared_ptr<IWorkspace> FlightHubSdkImpl::getWorkspaceInstance() {
    if (!initialized_.load()) {
        LOG_ERROR("getWorkspaceInstance: SDK not initialized");
        return nullptr;
    }

    std::lock_guard<std::mutex> lock(mutex_);
    if (!workspace_instance_) {
        workspace_instance_ = std::make_shared<WorkspaceImpl>();
        LOG_INFO("getWorkspaceInstance: created global workspace singleton");
    }
    return workspace_instance_;
}

std::shared_ptr<IDrone> FlightHubSdkImpl::getDroneInstance(const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getDroneInstance: SDK not initialized");
        return nullptr;
    }
    if (sn.empty()) {
        LOG_ERROR("getDroneInstance: sn is empty");
        return nullptr;
    }

    std::lock_guard<std::mutex> lock(mutex_);

    auto it = drone_instances_.find(sn);
    if (it != drone_instances_.end()) {
        return it->second;
    }

    // 从 workspace 的设备列表中查找并创建
    if (!workspace_instance_) {
        LOG_ERROR("getDroneInstance: workspace not created, call getWorkspaceInstance first");
        return nullptr;
    }
    auto device_topo_list = workspace_instance_->getDeviceList();
    for (const auto& device_topo : device_topo_list) {
        if (!device_topo.sub_devices.empty() && device_topo.sub_devices[0].device_sn == sn) {
            auto drone = std::make_shared<DroneImpl>(device_topo, rtc_channel_manager_, mqtt_client_manager_);
            drone_instances_[sn] = drone;
            LOG_INFO("getDroneInstance: created sn=%s", sn.c_str());
            return drone;
        }
    }

    LOG_ERROR("getDroneInstance: sn=%s not found in device list", sn.c_str());
    return nullptr;
}

std::shared_ptr<IDock> FlightHubSdkImpl::getDockInstance(const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getDockInstance: SDK not initialized");
        return nullptr;
    }
    if (sn.empty()) {
        LOG_ERROR("getDockInstance: sn is empty");
        return nullptr;
    }

    std::lock_guard<std::mutex> lock(mutex_);

    auto it = dock_instances_.find(sn);
    if (it != dock_instances_.end()) {
        return it->second;
    }

    auto dock = std::make_shared<DockImpl>(sn, rtc_channel_manager_, mqtt_client_manager_);
    dock_instances_[sn] = dock;
    LOG_INFO("getDockInstance: created sn=%s", sn.c_str());
    return dock;
}

std::shared_ptr<IRemoteController> FlightHubSdkImpl::getRemoteControllerInstance(const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getRemoteControllerInstance: SDK not initialized");
        return nullptr;
    }
    if (sn.empty()) {
        LOG_ERROR("getRemoteControllerInstance: sn is empty");
        return nullptr;
    }

    std::lock_guard<std::mutex> lock(mutex_);

    auto it = remote_controller_instances_.find(sn);
    if (it != remote_controller_instances_.end()) {
        return it->second;
    }

    auto instance = std::make_shared<RemoteControllerImpl>(sn);
    remote_controller_instances_[sn] = instance;
    LOG_INFO("getRemoteControllerInstance: created sn=%s", sn.c_str());
    return instance;
}

std::shared_ptr<IBaseStation> FlightHubSdkImpl::getBaseStationInstance(const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getBaseStationInstance: SDK not initialized");
        return nullptr;
    }
    if (sn.empty()) {
        LOG_ERROR("getBaseStationInstance: sn is empty");
        return nullptr;
    }

    std::lock_guard<std::mutex> lock(mutex_);

    auto it = base_station_instances_.find(sn);
    if (it != base_station_instances_.end()) {
        return it->second;
    }

    auto instance = std::make_shared<BaseStationImpl>(sn, rtc_channel_manager_, mqtt_client_manager_);
    base_station_instances_[sn] = instance;
    LOG_INFO("getBaseStationInstance: created sn=%s", sn.c_str());
    return instance;
}

std::shared_ptr<IRelay> FlightHubSdkImpl::getRelayInstance(const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getRelayInstance: SDK not initialized");
        return nullptr;
    }
    if (sn.empty()) {
        LOG_ERROR("getRelayInstance: sn is empty");
        return nullptr;
    }

    std::lock_guard<std::mutex> lock(mutex_);

    auto it = relay_instances_.find(sn);
    if (it != relay_instances_.end()) {
        return it->second;
    }

    auto instance = std::make_shared<RelayImpl>(sn);
    relay_instances_[sn] = instance;
    LOG_INFO("getRelayInstance: created sn=%s", sn.c_str());
    return instance;
}

std::shared_ptr<IStream> FlightHubSdkImpl::getStream() {
    if (!initialized_.load()) {
        LOG_ERROR("SDK not initialized");
        return nullptr;
    }
    if (!stream_instance_) {
        stream_instance_ = std::make_shared<StreamImpl>(rtc_channel_manager_, mqtt_client_manager_);
    }
    return stream_instance_;
}

FlightHubSdkErrorCode FlightHubSdkImpl::injectRtcChannelManager(std::shared_ptr<message_engine::IRtcChannelManagerBridge> rtc_channel_manager) {
    if (initialized_.load()) {
        LOG_ERROR("SDK initialized, please call injectRtcChannelManager first");
        return FlightHubSdkErrorCode::ERROR_ALREADY_INITIALIZED;
    }
    rtc_channel_manager_ = rtc_channel_manager;
    return FlightHubSdkErrorCode::SUCCESS;
}

FlightHubSdkErrorCode FlightHubSdkImpl::injectMqttClientManager(std::shared_ptr<message_engine::IMqttClientManagerBridge> mqtt_client_manager) {
    if (initialized_.load()) {
        LOG_ERROR("SDK initialized, please call injectMqttClientManager first");
        return FlightHubSdkErrorCode::ERROR_ALREADY_INITIALIZED;
    }
    mqtt_client_manager_ = mqtt_client_manager;
    return FlightHubSdkErrorCode::SUCCESS;
}

FlightHubSdkErrorCode FlightHubSdkImpl::injectWebSocket(std::shared_ptr<notification_center::WebSocketBase> ws) {
    if (initialized_.load()) {
        LOG_ERROR("SDK initialized, please call injectWebSocket before initialize");
        return FlightHubSdkErrorCode::ERROR_ALREADY_INITIALIZED;
    }
    if (!ws) {
        LOG_ERROR("injectWebSocket: WebSocket pointer is null");
        return FlightHubSdkErrorCode::ERROR_INVALID_PARAM;
    }
    websocket_ = ws;
    LOG_INFO("[FlightHubSdkImpl] WebSocket injected: %p", ws.get());
    return FlightHubSdkErrorCode::SUCCESS;
}
