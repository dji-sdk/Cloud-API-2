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
#include "session/session.h"
#include "session/session_manager.h"
#include "api/session.h"
#include "api/session_manager.h"
#include "http_interface/http_interface.h"  // kBeaconReportAPI
#include "cloud_storage_uploader/cloud_storage_factory.h"
#if defined(__unix__) || defined(__APPLE__)
#include "blocking_detector.h"
#include <cstdlib>
#include <cstring>
#endif

using namespace flight_hub_sdk;

namespace {

// 日志级别白名单校验:大小写不敏感,仅放行 javadoc 声明的四值(debug/info/warn/error)。
// 不做 trim 等静默归一,带空格或前后缀空白一律视为非法,避免掩盖调用方拼写失误。
static bool isValidLogLevel(const std::string& level) {
    std::string normalized;
    normalized.reserve(level.size());
    for (char c : level) {
        normalized.push_back(static_cast<char>(std::tolower(static_cast<unsigned char>(c))));
    }

    return normalized == "debug" || normalized == "info"
        || normalized == "warn"  || normalized == "error";
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

    drc_sdk::PlogLogger::InitializePlog(static_cast<int>(log_level),
                                       config.log_file_path.c_str(),
                                       config.log_to_console,
                                       config.log_max_file_size,
                                       config.log_max_files);
    // 日志级别的回显必须走 logger: 早先这里是一句裸 std::cout, 它不受 log_to_console
    // 约束, log_to_console=false 时照样打到控制台。
    LOG_INFO("[FlightHubSdkImpl] log_level: %s(%d), log_to_console: %s, log_file: %s, max_file_size: %zu, max_files: %d",
             config.log_level.c_str(),
             static_cast<int>(log_level),
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

    // 数据上报是高频后台请求, 而 libcurl 的 verbose 抓包
    // 会把请求/响应头直写 stderr, 绕开 plog 连 log_to_console=false 也拦不住；上报又跑在
    // IO 线程上，回包时会插进宿主程序正在等待输入的提示行里。故单独把它的抓包关掉。
    drc_sdk::common::Network::Instance().AddQuietUrlPattern(kBeaconReportAPI);

    // HTTPS 通道 TLS 证书验证配置统一下发。
    // 与 WSS 通道共用 FlightHubSdkConfig 的 verify_ssl/ssl_ca_path 字段:
    // verify_ssl 安全默认开启(校验证书链+主机名), 私有化自签名部署经 ssl_ca_path 提供私有 CA,
    // verify_ssl=false 会产生 WARN 日志(不安全, 仅限受控测试环境)。
    drc_sdk::common::Network::Instance().SetTlsConfig(config.verify_ssl, config.ssl_ca_path);
    // S3 直传通道（独立 AWS SDK/libcurl 静态栈）同一套 TLS 配置来源
    CloudStorageFactory::SetTlsConfig(config.verify_ssl, config.ssl_ca_path);

    // 如果注入了 WebSocket，先传递给 NotificationCenter（必须在 initialize 之前）
    // NC 现在由 Session 持有，通过 SessionManager 初始化

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

    // 拼 SessionManager 初始化参数 + session 0（之后只认 int session_id）
    SessionManagerInitParams sm_params;
    // deps：rtc / mqtt / ws 同类注入；Web SDK 历史入口，Java 不用
    sm_params.deps.rtc = rtc_channel_manager_;
    sm_params.deps.mqtt = mqtt_client_manager_;
    sm_params.deps.ws = websocket_ ? websocket_.get() : nullptr;
    sm_params.notify_base_url = config.notification_server_address;
    sm_params.websocket_auth_headers = websocket_auth_headers_;
    sm_params.verify_ssl = config.verify_ssl;
    sm_params.ssl_ca_path = config.ssl_ca_path;

    if (!session_manager_->initialize(sm_params)) {
        LOG_ERROR("[FlightHubSdkImpl] SessionManager::initialize failed");
        return FlightHubSdkErrorCode::ERROR_INIT_FAILED;
    }

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

    // 清理 SessionManager（所有 Session 及其设备/NC）；成员本身不销毁，可再次 initialize
    {
        std::lock_guard<std::mutex> lock(mutex_);
        stream_instance_.reset();
    }
    session_manager_->shutdown();

    // NC cleanup 已由 Session 析构处理

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

    // 先校验：白名单外的级别串直接拒绝，不落盘、不生效，保证"拒绝即不变更"
    if (!isValidLogLevel(level)) {
        LOG_ERROR("Invalid log level: %s", level.c_str());
        return FlightHubSdkErrorCode::ERROR_INVALID_PARAM;
    }

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

    // 更新已存在 Session 的 NC 鉴权（含 session 0；全量刷新）
    if (session_manager_) {
        for (const auto& info : session_manager_->getAllSessionInfo()) {
            if (auto s = session_manager_->getSession(info.session_id)) {
                s->updateWebsocketAuthHeaders(headers);
            }
        }
    }

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
    return getWorkspaceInstance(kDefaultSessionId);
}

std::shared_ptr<IWorkspace> FlightHubSdkImpl::getWorkspaceInstance(int session_id) {
    if (!initialized_.load()) {
        LOG_ERROR("getWorkspaceInstance: SDK not initialized");
        return nullptr;
    }
    auto session = session_manager_->getSession(session_id);
    if (!session) {
        LOG_ERROR("getWorkspaceInstance: session_id=%d not found", session_id);
        return nullptr;
    }
    return session->getWorkspaceInstance();
}

std::shared_ptr<IDrone> FlightHubSdkImpl::getDroneInstance(const std::string& sn) {
    return getDroneInstance(kDefaultSessionId, sn);
}

std::shared_ptr<IDrone> FlightHubSdkImpl::getDroneInstance(int session_id, const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getDroneInstance: SDK not initialized");
        return nullptr;
    }
    auto session = session_manager_->getSession(session_id);
    if (!session) {
        LOG_ERROR("getDroneInstance: session_id=%d not found", session_id);
        return nullptr;
    }
    return session->getDroneInstance(sn);
}

std::shared_ptr<IDock> FlightHubSdkImpl::getDockInstance(const std::string& sn) {
    return getDockInstance(kDefaultSessionId, sn);
}

std::shared_ptr<IDock> FlightHubSdkImpl::getDockInstance(int session_id, const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getDockInstance: SDK not initialized");
        return nullptr;
    }
    auto session = session_manager_->getSession(session_id);
    if (!session) {
        LOG_ERROR("getDockInstance: session_id=%d not found", session_id);
        return nullptr;
    }
    return session->getDockInstance(sn);
}

std::shared_ptr<IRemoteController> FlightHubSdkImpl::getRemoteControllerInstance(const std::string& sn) {
    return getRemoteControllerInstance(kDefaultSessionId, sn);
}

std::shared_ptr<IRemoteController> FlightHubSdkImpl::getRemoteControllerInstance(int session_id, const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getRemoteControllerInstance: SDK not initialized");
        return nullptr;
    }
    auto session = session_manager_->getSession(session_id);
    if (!session) {
        LOG_ERROR("getRemoteControllerInstance: session_id=%d not found", session_id);
        return nullptr;
    }
    return session->getRemoteControllerInstance(sn);
}

std::shared_ptr<IBaseStation> FlightHubSdkImpl::getBaseStationInstance(const std::string& sn) {
    return getBaseStationInstance(kDefaultSessionId, sn);
}

std::shared_ptr<IBaseStation> FlightHubSdkImpl::getBaseStationInstance(int session_id, const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getBaseStationInstance: SDK not initialized");
        return nullptr;
    }
    auto session = session_manager_->getSession(session_id);
    if (!session) {
        LOG_ERROR("getBaseStationInstance: session_id=%d not found", session_id);
        return nullptr;
    }
    return session->getBaseStationInstance(sn);
}

std::shared_ptr<IRelay> FlightHubSdkImpl::getRelayInstance(const std::string& sn) {
    return getRelayInstance(kDefaultSessionId, sn);
}

std::shared_ptr<IRelay> FlightHubSdkImpl::getRelayInstance(int session_id, const std::string& sn) {
    if (!initialized_.load()) {
        LOG_ERROR("getRelayInstance: SDK not initialized");
        return nullptr;
    }
    auto session = session_manager_->getSession(session_id);
    if (!session) {
        LOG_ERROR("getRelayInstance: session_id=%d not found", session_id);
        return nullptr;
    }
    return session->getRelayInstance(sn);
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

std::shared_ptr<ICascadeModule> FlightHubSdkImpl::getCascadeModule() {
    if (!initialized_.load()) {
        LOG_ERROR("getCascadeModule: SDK not initialized");
        return nullptr;
    }
    if (!cascade_module_) {
        cascade_module_ = std::make_shared<CascadeModuleImpl>(kDefaultSessionId);
        LOG_INFO("[FlightHubSdkImpl] getCascadeModule: created global cascade singleton");
    }
    return cascade_module_;
}

std::shared_ptr<ISessionManager> FlightHubSdkImpl::getSessionManager() {
    if (!initialized_.load()) {
        LOG_ERROR("getSessionManager: SDK not initialized");
        return nullptr;
    }
    return session_manager_;
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
