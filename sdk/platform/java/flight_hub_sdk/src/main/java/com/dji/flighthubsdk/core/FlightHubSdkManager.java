// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.flighthubsdk.core;

import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import com.dji.flighthubsdk.internal.convert.FlightHubSdkConvert;
import com.dji.flighthubsdk.util.Logger;

/**
 * Flight Hub SDK 内部管理器，负责衔接底层实现。
 *
 * <p><b>仅供 SDK 内部使用</b>：应用侧不应直接依赖本类，请统一通过
 * {@link com.dji.flighthubsdk.FlightHubSDK} 访问 SDK 能力。</p>
 *
 * <p><b>错误码</b>：本类返回的 {@code FlightHubSdkErrorCode} 的整型取值与
 * {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode} 一致；对外文档请以 value 包该枚举为准。</p>
 *
 * @see com.dji.flighthubsdk.FlightHubSDK
 * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
 */
public class FlightHubSdkManager {
    private static final java.util.concurrent.ConcurrentHashMap<String, Drone> droneRef = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, Dock> dockRef = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, RemoteController> remoteControllerRef = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, BaseStation> baseStationRef = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, Relay> relayRef = new java.util.concurrent.ConcurrentHashMap<>();
    private static Workspace workspaceRef = null;
    private static Stream stream = null;
    private static CascadeModule cascade = null;

    /**
     * 初始化 SDK。
     *
     * @param config SDK 配置
     * @return SDK 层错误码；整型语义见 {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode}
     * @see com.dji.flighthubsdk.value.FlightHubSdkErrorCode
     */
    public static FlightHubSdkErrorCode initialize(FlightHubSdkConfig config) {
        com.dji.flighthubsdk.generated.FlightHubSdkConfig swigConfig = null;
        try {
            if (config == null) {
                Logger.error("FlightHubSdkManager", "initialize: config is null");
                return FlightHubSdkErrorCode.ERROR_INVALID_PARAM;
            }
            swigConfig = FlightHubSdkConvert.toNativeFlightHubSdkConfig(config);
            return com.dji.flighthubsdk.generated.FlightHubSdk.initialize(swigConfig);
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "Initialize error: " + e.getMessage(), e);
            return FlightHubSdkErrorCode.ERROR_INIT_FAILED;
        } finally {
            if (swigConfig != null) {
                swigConfig.delete();
            }
        }
    }

    /**
     * 关闭 SDK。
     *
     * @return SDK 层错误码；整型语义见 {@link com.dji.flighthubsdk.value.FlightHubSdkErrorCode}
     */
    public static FlightHubSdkErrorCode shutdown() {
        try {
            return com.dji.flighthubsdk.generated.FlightHubSdk.shutdown();
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "Shutdown error: " + e.getMessage(), e);
            return FlightHubSdkErrorCode.ERROR_OPERATION_FAILED;
        }
    }

    public static boolean isInitialized() {
        try {
            return com.dji.flighthubsdk.generated.FlightHubSdk.isInitialized();
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "IsInitialized error: " + e.getMessage(), e);
            return false;
        }
    }

    public static String getVersion() {
        try {
            return com.dji.flighthubsdk.generated.FlightHubSdk.getVersion();
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "GetVersion error: " + e.getMessage(), e);
            return "3.1.0";
        }
    }

    public static String getBuildInfo() {
        try {
            return com.dji.flighthubsdk.generated.FlightHubSdk.getBuildInfo();
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "GetBuildInfo error: " + e.getMessage(), e);
            return "Flight Hub SDK v3.1.0";
        }
    }

    public static FlightHubSdkErrorCode setLogLevel(String level) {
        try {
            return com.dji.flighthubsdk.generated.FlightHubSdk.setLogLevel(level);
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "SetLogLevel error: " + e.getMessage(), e);
            return FlightHubSdkErrorCode.ERROR_OPERATION_FAILED;
        }
    }

    public static FlightHubSdkErrorCode setWebSocketAuthHeaders(java.util.List<HeaderKeyValue> headersKV) {
        try {
            if (!JniLibraryLoader.isLibraryLoaded()) {
                Logger.error("FlightHubSdkManager", "setWebSocketAuthHeaders: JNI library not loaded");
                return FlightHubSdkErrorCode.ERROR_NOT_INITIALIZED;
            }
            com.dji.flighthubsdk.generated.HeaderKeyValueVector nativeHeaders = new com.dji.flighthubsdk.generated.HeaderKeyValueVector();
            if (headersKV != null) {
                for (HeaderKeyValue item : headersKV) {
                    if (item == null || item.getKey() == null || item.getKey().isEmpty()) {
                        continue;
                    }
                    com.dji.flighthubsdk.generated.HeaderKeyValue nativeItem = new com.dji.flighthubsdk.generated.HeaderKeyValue();
                    nativeItem.setKey(item.getKey());
                    nativeItem.setValue(item.getValue() != null ? item.getValue() : "");
                    nativeHeaders.add(nativeItem);
                }
            }
            return com.dji.flighthubsdk.generated.FlightHubSdk.setWebSocketAuthHeaders(nativeHeaders);
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "setWebSocketAuthHeaders error: " + e.getMessage(), e);
            return FlightHubSdkErrorCode.ERROR_OPERATION_FAILED;
        }
    }

    public static FlightHubSdkErrorCode setHttpHeaders(java.util.List<HeaderKeyValue> headersKV) {
        try {
            if (!JniLibraryLoader.isLibraryLoaded()) {
                Logger.error("FlightHubSdkManager", "setHttpHeaders: JNI library not loaded");
                return FlightHubSdkErrorCode.ERROR_NOT_INITIALIZED;
            }
            com.dji.flighthubsdk.generated.HeaderKeyValueVector nativeHeaders = new com.dji.flighthubsdk.generated.HeaderKeyValueVector();
            if (headersKV != null) {
                for (HeaderKeyValue item : headersKV) {
                    if (item == null || item.getKey() == null || item.getKey().isEmpty()) {
                        continue;
                    }
                    com.dji.flighthubsdk.generated.HeaderKeyValue nativeItem = new com.dji.flighthubsdk.generated.HeaderKeyValue();
                    nativeItem.setKey(item.getKey());
                    nativeItem.setValue(item.getValue() != null ? item.getValue() : "");
                    nativeHeaders.add(nativeItem);
                }
            }
            return com.dji.flighthubsdk.generated.FlightHubSdk.setHttpHeaders(nativeHeaders);
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "setHttpHeaders error: " + e.getMessage(), e);
            return FlightHubSdkErrorCode.ERROR_OPERATION_FAILED;
        }
    }

    public static java.util.List<HeaderKeyValue> getWebSocketAuthHeaders() {
        try {
            if (!JniLibraryLoader.isLibraryLoaded()) {
                Logger.error("FlightHubSdkManager", "getWebSocketAuthHeaders: JNI library not loaded");
                return java.util.Collections.emptyList();
            }
            com.dji.flighthubsdk.generated.HeaderKeyValueVector nativeHeaders =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getWebSocketAuthHeaders();
            java.util.List<HeaderKeyValue> result = new java.util.ArrayList<>();
            if (nativeHeaders != null) {
                for (int i = 0; i < nativeHeaders.size(); i++) {
                    com.dji.flighthubsdk.generated.HeaderKeyValue nkv = nativeHeaders.get(i);
                    result.add(new HeaderKeyValue(nkv.getKey(), nkv.getValue()));
                }
            }
            return result;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "getWebSocketAuthHeaders error: " + e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    public static java.util.List<HeaderKeyValue> getHttpHeaders() {
        try {
            if (!JniLibraryLoader.isLibraryLoaded()) {
                Logger.error("FlightHubSdkManager", "getHttpHeaders: JNI library not loaded");
                return java.util.Collections.emptyList();
            }
            com.dji.flighthubsdk.generated.HeaderKeyValueVector nativeHeaders =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getHttpHeaders();
            java.util.List<HeaderKeyValue> result = new java.util.ArrayList<>();
            if (nativeHeaders != null) {
                for (int i = 0; i < nativeHeaders.size(); i++) {
                    com.dji.flighthubsdk.generated.HeaderKeyValue nkv = nativeHeaders.get(i);
                    result.add(new HeaderKeyValue(nkv.getKey(), nkv.getValue()));
                }
            }
            return result;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "getHttpHeaders error: " + e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    public static FlightHubSdkConfig getConfig() {
        try {
            com.dji.flighthubsdk.generated.FlightHubSdkConfig swigConfig =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getConfig();
            return FlightHubSdkConvert.toFlightHubSdkConfigBean(swigConfig);
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "GetConfig error: " + e.getMessage(), e);
            return new FlightHubSdkConfig();
        }
    }

    // ---- 设备实例获取（去 SaaS 后无 session/binding 概念） ----

    /**
     * 获取 Workspace 实例（等价于 session 0）
     */
    public static synchronized Workspace getWorkspaceInstance() {
        return getWorkspaceInstance(0);
    }

    /**
     * 按 session_id 获取 Workspace；session 0 走缓存，其它 session 每次向 native 取。
     */
    public static synchronized Workspace getWorkspaceInstance(int sessionId) {
        try {
            if (sessionId == 0) {
                if (workspaceRef != null) {
                    return workspaceRef;
                }
                com.dji.flighthubsdk.generated.IWorkspace swigWorkspace =
                        com.dji.flighthubsdk.generated.FlightHubSdk.getWorkspaceInstance(0);
                if (swigWorkspace != null) {
                    workspaceRef = new Workspace(swigWorkspace);
                    return workspaceRef;
                }
                Logger.warn("FlightHubSdkManager", "getWorkspaceInstance(0): null returned");
                return null;
            }
            com.dji.flighthubsdk.generated.IWorkspace swigWorkspace =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getWorkspaceInstance(sessionId);
            if (swigWorkspace != null) {
                return new Workspace(swigWorkspace);
            }
            Logger.warn("FlightHubSdkManager",
                    "getWorkspaceInstance: null returned, sessionId=" + sessionId);
            return null;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager",
                    "getWorkspaceInstance error: sessionId=" + sessionId + ", " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通过设备 SN 获取 Drone 实例（session 0）
     */
    public static synchronized Drone getDroneInstance(String sn) {
        return getDroneInstance(0, sn);
    }

    /**
     * 按 session_id 获取 Drone；session 0 走缓存，其它 session 每次向 native 取。
     */
    public static synchronized Drone getDroneInstance(int sessionId, String sn) {
        try {
            if (sn == null || sn.isEmpty()) {
                Logger.error("FlightHubSdkManager", "getDroneInstance error: sn is null or empty");
                return null;
            }
            if (sessionId == 0 && droneRef.containsKey(sn)) {
                return droneRef.get(sn);
            }
            com.dji.flighthubsdk.generated.IDrone swigDrone =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getDroneInstance(sessionId, sn);
            if (swigDrone != null) {
                Drone drone = new Drone(swigDrone, sn);
                if (sessionId == 0) {
                    droneRef.put(sn, drone);
                }
                return drone;
            }
            Logger.warn("FlightHubSdkManager",
                    "getDroneInstance: drone not found, sessionId=" + sessionId + ", sn=" + sn);
            return null;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager",
                    "getDroneInstance error: sessionId=" + sessionId + ", " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通过设备 SN 获取 Dock 实例（session 0）
     */
    public static synchronized Dock getDockInstance(String sn) {
        return getDockInstance(0, sn);
    }

    /**
     * 按 session_id 获取 Dock；session 0 走缓存，其它 session 每次向 native 取。
     */
    public static synchronized Dock getDockInstance(int sessionId, String sn) {
        try {
            if (sn == null || sn.isEmpty()) {
                Logger.error("FlightHubSdkManager", "getDockInstance error: sn is null or empty");
                return null;
            }
            if (sessionId == 0 && dockRef.containsKey(sn)) {
                return dockRef.get(sn);
            }
            com.dji.flighthubsdk.generated.IDock swigDock =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getDockInstance(sessionId, sn);
            if (swigDock != null) {
                Dock dock = new Dock(swigDock, sn);
                if (sessionId == 0) {
                    dockRef.put(sn, dock);
                }
                return dock;
            }
            Logger.warn("FlightHubSdkManager",
                    "getDockInstance: dock not found, sessionId=" + sessionId + ", sn=" + sn);
            return null;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager",
                    "getDockInstance error: sessionId=" + sessionId + ", " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通过设备 SN 获取 RemoteController 实例
     */
    public static synchronized RemoteController getRemoteControllerInstance(String sn) {
        try {
            if (sn == null || sn.isEmpty()) {
                Logger.error("FlightHubSdkManager", "getRemoteControllerInstance error: sn is null or empty");
                return null;
            }
            if (remoteControllerRef.containsKey(sn)) {
                return remoteControllerRef.get(sn);
            }
            com.dji.flighthubsdk.generated.IRemoteController swigRc =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getRemoteControllerInstance(sn);
            if (swigRc != null) {
                RemoteController rc = new RemoteController(swigRc, sn);
                remoteControllerRef.put(sn, rc);
                return rc;
            }
            Logger.warn("FlightHubSdkManager", "getRemoteControllerInstance: rc not found, sn=" + sn);
            return null;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "getRemoteControllerInstance error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通过设备 SN 获取 BaseStation 实例
     */
    public static synchronized BaseStation getBaseStationInstance(String sn) {
        try {
            if (sn == null || sn.isEmpty()) {
                Logger.error("FlightHubSdkManager", "getBaseStationInstance error: sn is null or empty");
                return null;
            }
            if (baseStationRef.containsKey(sn)) {
                return baseStationRef.get(sn);
            }
            com.dji.flighthubsdk.generated.IBaseStation swigBs =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getBaseStationInstance(sn);
            if (swigBs != null) {
                BaseStation bs = new BaseStation(swigBs, sn);
                baseStationRef.put(sn, bs);
                return bs;
            }
            Logger.warn("FlightHubSdkManager", "getBaseStationInstance: bs not found, sn=" + sn);
            return null;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "getBaseStationInstance error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 通过设备 SN 获取 Relay 实例
     */
    public static synchronized Relay getRelayInstance(String sn) {
        try {
            if (sn == null || sn.isEmpty()) {
                Logger.error("FlightHubSdkManager", "getRelayInstance error: sn is null or empty");
                return null;
            }
            if (relayRef.containsKey(sn)) {
                return relayRef.get(sn);
            }
            com.dji.flighthubsdk.generated.IRelay swigRelay =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getRelayInstance(sn);
            if (swigRelay != null) {
                Relay relay = new Relay(swigRelay, sn);
                relayRef.put(sn, relay);
                return relay;
            }
            Logger.warn("FlightHubSdkManager", "getRelayInstance: relay not found, sn=" + sn);
            return null;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "getRelayInstance error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取流处理实例
     */
    public static synchronized Stream getStreamInstance() {
        try {
            if (stream == null) {
                stream = new Stream(com.dji.flighthubsdk.generated.FlightHubSdk.getStream());
            }
            return stream;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "GetStreamInstance error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取级联管理模块实例（workspace 级单例）。
     */
    public static synchronized CascadeModule getCascadeInstance() {
        try {
            if (cascade == null) {
                cascade = new CascadeModule(com.dji.flighthubsdk.generated.FlightHubSdk.getCascadeModule());
            }
            return cascade;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "GetCascadeInstance error: " + e.getMessage(), e);
            return null;
        }
    }

    public static SessionManager getSessionManager() {
        try {
            com.dji.flighthubsdk.generated.ISessionManager raw =
                    com.dji.flighthubsdk.generated.FlightHubSdk.getSessionManager();
            return raw != null ? new SessionManager(raw) : null;
        } catch (Exception e) {
            Logger.error("FlightHubSdkManager", "getSessionManager error: " + e.getMessage(), e);
            return null;
        }
    }
}
