// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk;

import com.dji.flighthubsdk.core.FlightHubSdkManager;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.BaseStation;
import com.dji.flighthubsdk.core.RemoteController;
import com.dji.flighthubsdk.core.Relay;
import com.dji.flighthubsdk.core.Stream;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.core.SessionManager;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.core.JniLibraryLoader;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.util.Logger;

/**
 * Flight Hub SDK 主入口类，提供初始化、设备管理等核心能力。
 *
 * <p><b>错误码</b>：本类返回的 {@code FlightHubSdkErrorCode} 为 SDK 内部使用的枚举类型，其整型取值与
 * {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode} 各常量一致。查阅各常量含义与完整列表，
 * 请见 {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode}。</p>
 *
 * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
 */
public class FlightHubSDK {
    
    private static boolean initialized = false;
    private static final Object initLock = new Object();

    /** setLogLevel 合法入参白名单（小写，比较时大小写不敏感） */
    private static final java.util.Set<String> VALID_LOG_LEVELS =
            java.util.Set.of("debug", "info", "warn", "error");
    
    /**
     * 私有构造函数，防止实例化
     */
    private FlightHubSDK() {
        throw new UnsupportedOperationException("FlightHubSDK cannot be instantiated");
    }
    
    /**
     * 初始化 SDK。
     *
     * @param config SDK 配置信息（服务器地址、端口、日志级别等）
     * @return SDK 层错误码；整型语义见 {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode}
     * @implNote 链路: SDK 内部 | 初始化日志、HTTP 客户端、NotificationCenter 等内部组件
     * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
     */
    public static FlightHubSdkErrorCode initialize(FlightHubSdkConfig config) {
        synchronized (initLock) {
            if (initialized) {
                return FlightHubSdkErrorCode.ERROR_ALREADY_INITIALIZED;
            }
            
            try {
                // 初始化日志系统
                // 解析日志路径：如果配置的是文件名（包含.log），则提取目录部分；如果是目录，则直接使用
                String logFilePath = config.getLogFilePath() != null && !config.getLogFilePath().isEmpty()
                    ? config.getLogFilePath() : "./logs";
                String logDir;
                String logFileName;
                
                if (logFilePath.endsWith(".log")) {
                    // 如果配置的是文件名，提取目录部分
                    java.io.File file = new java.io.File(logFilePath);
                    java.io.File parent = file.getParentFile();
                    logDir = parent != null ? parent.getPath() : ".";
                    // 从文件名中提取前缀（去掉 .log 扩展名）
                    String baseName = file.getName();
                    logFileName = baseName.substring(0, baseName.length() - 4); // 去掉 .log
                } else {
                    // 如果配置的是目录，直接使用
                    logDir = logFilePath;
                    logFileName = "flight_hub_sdk";
                }
                
                long maxFileSize = config.getLogMaxFileSize() > 0
                    ? config.getLogMaxFileSize() : 10 * 1024 * 1024; // 默认10MB
                int maxFiles = config.getLogMaxFiles() > 0
                    ? config.getLogMaxFiles() : 5;
                Logger.LogLevel logLevel = parseLogLevel(config.getLogLevel());
                boolean logToFile = true;
                boolean logToConsole = config.isLogToConsole();
                
                Logger.initialize(logDir, logFileName, logLevel, logToFile, logToConsole, maxFileSize, maxFiles);
                
                // 确保JNI库已加载
                if (!JniLibraryLoader.isLibraryLoaded()) {
                    JniLibraryLoader.loadLibrary();
                }
                
                // 调用底层初始化
                FlightHubSdkErrorCode result = FlightHubSdkManager.initialize(config);
                if (result == FlightHubSdkErrorCode.SUCCESS) {
                    initialized = true;
                    Logger.info("FlightHubSDK", "SDK initialized successfully");
                }
                
                return result;
            } catch (Exception e) {
                Logger.error("FlightHubSDK", "Initialize error: " + e.getMessage(), e);
                return FlightHubSdkErrorCode.ERROR_INIT_FAILED;
            }
        }
    }
    
    /**
     * 解析日志级别字符串
     */
    private static Logger.LogLevel parseLogLevel(String level) {
        if (level == null || level.isEmpty()) {
            return Logger.LogLevel.INFO;
        }
        try {
            return Logger.LogLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Logger.LogLevel.INFO;
        }
    }
    
    /**
     * 使用默认配置初始化 SDK。
     *
     * @return SDK 层错误码；整型语义见 {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode}
     * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
     */
    public static FlightHubSdkErrorCode initialize() {
        FlightHubSdkConfig defaultConfig = new FlightHubSdkConfig();
        return initialize(defaultConfig);
    }
    
    /**
     * 关闭 SDK。
     *
     * @return SDK 层错误码；整型语义见 {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode}
     * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
     */
    public static FlightHubSdkErrorCode shutdown() {
        synchronized (initLock) {
            if (!initialized) {
                return FlightHubSdkErrorCode.ERROR_NOT_INITIALIZED;
            }
            
            try {
                FlightHubSdkErrorCode result = FlightHubSdkManager.shutdown();
                if (result == FlightHubSdkErrorCode.SUCCESS) {
                    initialized = false;
                    Logger.info("FlightHubSDK", "SDK shutdown successfully");
                }
                // 关闭日志系统
                Logger.shutdown();
                return result;
            } catch (Exception e) {
                Logger.error("FlightHubSDK", "Shutdown error: " + e.getMessage(), e);
                return FlightHubSdkErrorCode.ERROR_OPERATION_FAILED;
            }
        }
    }
    
    /**
     * 检查 SDK 是否已初始化
     * @return true 表示已初始化
     */
    public static boolean isInitialized() {
        synchronized (initLock) {
            return initialized && FlightHubSdkManager.isInitialized();
        }
    }
    
    /**
     * 获取 SDK 版本号
     * @return 版本号字符串
     */
    public static String getVersion() {
        try {
            return FlightHubSdkManager.getVersion();
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "GetVersion error: " + e.getMessage(), e);
            return "0.0.0";
        }
    }
    
    /**
     * 获取 SDK 构建信息（版本、分支、提交等）
     * @return 构建信息字符串
     */
    public static String getBuildInfo() {
        try {
            return FlightHubSdkManager.getBuildInfo();
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "GetBuildInfo error: " + e.getMessage(), e);
            return "Flight Hub SDK v0.0.0";
        }
    }
    
    /**
     * 设置日志级别
     *
     * @param level 日志级别：debug / info / warn / error（大小写不敏感）；
     *              白名单外的入参（含 {@code null}、空串、带空格串）将被拒绝，
     *              返回 {@code ERROR_INVALID_PARAM}，且当前日志级别保持原值不变
     * @return SDK 层错误码；整型语义见 {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode}
     * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
     */
    public static FlightHubSdkErrorCode setLogLevel(String level) {
        try {
            if (level == null || !VALID_LOG_LEVELS.contains(level.toLowerCase())) {
                Logger.warn("FlightHubSDK", "Invalid log level: " + level + ", rejected");
                return FlightHubSdkErrorCode.ERROR_INVALID_PARAM;
            }
            Logger.setLogLevel(level);
            return FlightHubSdkManager.setLogLevel(level);
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "SetLogLevel error: " + e.getMessage(), e);
            return FlightHubSdkErrorCode.ERROR_OPERATION_FAILED;
        }
    }

    /**
     * 设置 WebSocket 鉴权头（用于消息通知连接）
     *
     * @param headersKV 鉴权头键值对列表
     * @return SDK 层错误码；整型语义见 {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode}
     * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
     */
    public static FlightHubSdkErrorCode setWebSocketAuthHeaders(java.util.List<HeaderKeyValue> headersKV) {
        try {
            return FlightHubSdkManager.setWebSocketAuthHeaders(headersKV);
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "setWebSocketAuthHeaders error: " + e.getMessage(), e);
            return FlightHubSdkErrorCode.ERROR_OPERATION_FAILED;
        }
    }

    /**
     * 设置 HTTP 请求头（用于所有 HTTP 接口）
     *
     * @param headersKV 请求头键值对列表
     * @return SDK 层错误码；整型语义见 {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode}
     * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
     */
    public static FlightHubSdkErrorCode setHttpHeaders(java.util.List<HeaderKeyValue> headersKV) {
        try {
            return FlightHubSdkManager.setHttpHeaders(headersKV);
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "setHttpHeaders error: " + e.getMessage(), e);
            return FlightHubSdkErrorCode.ERROR_OPERATION_FAILED;
        }
    }

    /**
     * 获取 WebSocket 鉴权头
     * @return 鉴权头键值对列表
     */
    public static java.util.List<HeaderKeyValue> getWebSocketAuthHeaders() {
        try {
            return FlightHubSdkManager.getWebSocketAuthHeaders();
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "getWebSocketAuthHeaders error: " + e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 获取 HTTP 请求头
     * @return 请求头键值对列表
     */
    public static java.util.List<HeaderKeyValue> getHttpHeaders() {
        try {
            return FlightHubSdkManager.getHttpHeaders();
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "getHttpHeaders error: " + e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 获取当前 SDK 配置
     * @return SDK 配置信息
     */
    public static FlightHubSdkConfig getConfig() {
        try {
            return FlightHubSdkManager.getConfig();
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "GetConfig error: " + e.getMessage(), e);
            return new FlightHubSdkConfig();
        }
    }
    
    /**
     * 获取 Workspace 实例（等价于 session 0）
     * @return Workspace 实例
     */
    public static Workspace getWorkspaceInstance() {
        return getWorkspaceInstance(0);
    }

    /**
     * 按 session_id 获取 Workspace（业务入口；不要再从 Session 对象取设备）
     * @param sessionId Session 数字 id（0 为 initialize 后的默认 Session）
     * @return Workspace 实例；session 不存在时返回 null
     */
    public static Workspace getWorkspaceInstance(int sessionId) {
        try {
            Logger.info("FlightHubSDK", "Getting workspace instance, sessionId=" + sessionId);
            Workspace workspace = FlightHubSdkManager.getWorkspaceInstance(sessionId);
            if (workspace != null) {
                Logger.info("FlightHubSDK",
                        "Workspace instance retrieved successfully, sessionId=" + sessionId);
            } else {
                Logger.warn("FlightHubSDK", "Workspace instance is null, sessionId=" + sessionId);
            }
            return workspace;
        } catch (Exception e) {
            Logger.error("FlightHubSDK",
                    "GetWorkspaceInstance error: sessionId=" + sessionId + ", " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 Drone 实例（session 0）
     * @param sn 飞行器序列号
     * @return Drone 实例
     */
    public static Drone getDroneInstance(String sn) {
        return getDroneInstance(0, sn);
    }

    /**
     * 按 session_id 获取 Drone（门面入口）
     */
    public static Drone getDroneInstance(int sessionId, String sn) {
        try {
            Logger.debug("FlightHubSDK",
                    "Getting drone instance, sessionId=" + sessionId + ", SN: " + sn);
            Drone drone = FlightHubSdkManager.getDroneInstance(sessionId, sn);
            if (drone != null) {
                Logger.debug("FlightHubSDK",
                        "Drone instance retrieved successfully, sessionId=" + sessionId + ", SN: " + sn);
            } else {
                Logger.debug("FlightHubSDK",
                        "Drone instance is null, sessionId=" + sessionId + ", SN: " + sn);
            }
            return drone;
        } catch (Exception e) {
            Logger.error("FlightHubSDK",
                    "GetDroneInstance error: sessionId=" + sessionId + ", " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取 Dock 实例（session 0）
     * @param sn 机场序列号
     * @return Dock 实例
     */
    public static Dock getDockInstance(String sn) {
        return getDockInstance(0, sn);
    }

    /**
     * 按 session_id 获取 Dock（门面入口）
     */
    public static Dock getDockInstance(int sessionId, String sn) {
        try {
            Logger.debug("FlightHubSDK",
                    "Getting dock instance, sessionId=" + sessionId + ", SN: " + sn);
            Dock dock = FlightHubSdkManager.getDockInstance(sessionId, sn);
            if (dock != null) {
                Logger.debug("FlightHubSDK",
                        "Dock instance retrieved successfully, sessionId=" + sessionId + ", SN: " + sn);
            } else {
                Logger.debug("FlightHubSDK",
                        "Dock instance is null, sessionId=" + sessionId + ", SN: " + sn);
            }
            return dock;
        } catch (Exception e) {
            Logger.error("FlightHubSDK",
                    "GetDockInstance error: sessionId=" + sessionId + ", " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 BaseStation 实例
     * @param sn 基站序列号
     * @return BaseStation 实例
     */
    public static BaseStation getBaseStationInstance(String sn) {
        try {
            Logger.debug("FlightHubSDK", "Getting base station instance, SN: " + sn);
            BaseStation bs = FlightHubSdkManager.getBaseStationInstance(sn);
            if (bs != null) {
                Logger.debug("FlightHubSDK", "BaseStation instance retrieved successfully, SN: " + sn);
            } else {
                Logger.debug("FlightHubSDK", "BaseStation instance is null, SN: " + sn);
            }
            return bs;
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "GetBaseStationInstance error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 RemoteController 实例（暂无功能实现，暂不可用）
     * @param sn 遥控器序列号
     * @return RemoteController 实例
     */
    public static RemoteController getRemoteControllerInstance(String sn) {
        try {
            Logger.debug("FlightHubSDK", "Getting remote controller instance, SN: " + sn);
            RemoteController rc = FlightHubSdkManager.getRemoteControllerInstance(sn);
            if (rc != null) {
                Logger.debug("FlightHubSDK", "RemoteController instance retrieved successfully, SN: " + sn);
            } else {
                Logger.debug("FlightHubSDK", "RemoteController instance is null, SN: " + sn);
            }
            return rc;
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "GetRemoteControllerInstance error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 Relay 实例（中继：广播/SSID/密码/搜索/占用/重启/断开 + RTK）
     * @param sn 中继序列号
     * @return Relay 实例
     */
    public static Relay getRelayInstance(String sn) {
        try {
            Logger.debug("FlightHubSDK", "Getting relay instance, SN: " + sn);
            Relay relay = FlightHubSdkManager.getRelayInstance(sn);
            if (relay != null) {
                Logger.debug("FlightHubSDK", "Relay instance retrieved successfully, SN: " + sn);
            } else {
                Logger.debug("FlightHubSDK", "Relay instance is null, SN: " + sn);
            }
            return relay;
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "GetRelayInstance error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 Stream 全局实例（非设备直播的通用WHEP直播拉流 / CustomSEI 等流能力入口）
     * @return Stream 实例
     */
    public static Stream getStream() {
        try {
            Logger.info("FlightHubSDK", "Getting stream instance");
            Stream stream = FlightHubSdkManager.getStreamInstance();
            if (stream != null) {
                Logger.info("FlightHubSDK", "Stream instance retrieved successfully");
            } else {
                Logger.warn("FlightHubSDK", "Stream instance is null");
            }
            return stream;
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "GetStream error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 Cascade 全局实例（workspace 级级联管理入口）。
     * @return CascadeModule 实例
     */
    public static com.dji.flighthubsdk.core.CascadeModule getCascade() {
        try {
            Logger.info("FlightHubSDK", "Getting cascade instance");
            com.dji.flighthubsdk.core.CascadeModule cascade = FlightHubSdkManager.getCascadeInstance();
            if (cascade != null) {
                Logger.info("FlightHubSDK", "Cascade instance retrieved successfully");
            } else {
                Logger.warn("FlightHubSDK", "Cascade instance is null");
            }
            return cascade;
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "GetCascade error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取 SessionManager（唯一 Session 登记/查询入口）。
     *
     * @return SessionManager 包装实例；SDK 未初始化时返回 null
     */
    public static SessionManager getSessionManager() {
        try {
            return FlightHubSdkManager.getSessionManager();
        } catch (Exception e) {
            Logger.error("FlightHubSDK", "getSessionManager error: " + e.getMessage(), e);
            return null;
        }
    }
}