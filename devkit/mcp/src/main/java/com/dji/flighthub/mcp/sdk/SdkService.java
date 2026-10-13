/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.flighthub.mcp.sdk;

import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthubsdk.bean.HeaderKeyValue;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.FlightHubSdkManager;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * FlightHub SDK 服务封装层，为 MCP Tools 提供同步接口。
 * 负责 SDK 生命周期管理（初始化/关闭）及实例获取。
 *
 * <p>SDK 调用流程（2.0 新版，session 管理已移除）:
 * <ol>
 *   <li>FlightHubSDK.initialize(config)</li>
 *   <li>FlightHubSDK.setHttpHeaders / setWebSocketAuthHeaders</li>
 *   <li>FlightHubSdkManager.getWorkspaceInstance() → Workspace（无需 sessionId）</li>
 *   <li>FlightHubSdkManager.getDroneInstance(sn) → Drone</li>
 *   <li>FlightHubSdkManager.getDockInstance(sn) → Dock</li>
 * </ol>
 *
 * @author DJI FlightHub SDK Team
 */
@Service
public class SdkService {

    private static final Logger log = LoggerFactory.getLogger(SdkService.class);

    /** 自动探测相机 payload_index 的超时时间（秒） */
    private static final int PAYLOAD_INDEX_DETECT_TIMEOUT_SECONDS = 5;

    /**
     * Mock 模式环境变量名
     */
    private static final String ENV_MOCK_MODE = "FH_MOCK";

    /**
     * SDK 默认 session ID（无 workspace 绑定时使用）
     */
    private static final long DEFAULT_SESSION_ID = 1L;

    /**
     * MCP 期望的 SDK 主版本前缀（与 pom.xml 中声明的 flight-hub-sdk 版本一致）
     */
    private static final String EXPECTED_SDK_VERSION_PREFIX = "2.";

    private static final String AUTH_HEADER_KEY = "Authorization";
    private static final String AUTH_BEARER_PREFIX = "Bearer ";

    /** 平台网关 isv-webhook-auth 插件要求的元 header，值为逗号分隔的鉴权 header 名列表 */
    private static final String GATEWAY_AUTH_KEYS_HEADER = "auth_keys";

    /**
     * Mock 模式标记（启动时从环境变量读取，运行期间不变）
     */
    private final boolean mockMode = "true".equalsIgnoreCase(System.getenv(ENV_MOCK_MODE));

    private volatile boolean initialized = false;
    private volatile long sdkSessionId = -1;
    private volatile Workspace workspace;

    /**
     * 初始化 SDK 并注册 workspace session。
     * 若 FH_MOCK=true，跳过真实 SDK 初始化。
     *
     * @param serverAddress             服务器地址
     * @param port                      服务器端口
     * @param accessToken               认证 Token（可为 null）
     * @param workspaceUuid             工作空间 UUID（可为 null）
     * @param notificationServerAddress WebSocket 通知服务器地址，格式如 ws://host:port/path（可为 null，SDK 自动推断）
     * @return sessionId (>0 表示成功), -1 表示失败
     */
    public synchronized long initialize(String serverAddress, int port, String accessToken, String workspaceUuid,
                                        String notificationServerAddress) {
        return initialize(serverAddress, port, accessToken, workspaceUuid, notificationServerAddress, null, null);
    }

    /**
     * 初始化 SDK 并注册 workspace session（含平台网关鉴权头）。
     * 若 FH_MOCK=true，跳过真实 SDK 初始化。
     *
     * @param serverAddress             服务器地址
     * @param port                      服务器端口
     * @param accessToken               认证 Token（可为 null）
     * @param workspaceUuid             工作空间 UUID（可为 null）
     * @param notificationServerAddress WebSocket 通知服务器地址，格式如 ws://host:port/path（可为 null，SDK 自动推断）
     * @param httpAuthKey               平台网关鉴权 header key（如 "user_id"，可为 null）
     * @param httpAuthValue             平台网关鉴权 header value（如 "your-auth-value"，可为 null；占位示例需按平台实际配置）
     * @return sessionId (>0 表示成功), -1 表示失败
     */
    public synchronized long initialize(String serverAddress, int port, String accessToken, String workspaceUuid,
                                        String notificationServerAddress, String httpAuthKey, String httpAuthValue) {
        if (initialized) {
            log.warn("SDK already initialized, returning existing session");
            return sdkSessionId;
        }

        if (isMockMode()) {
            log.info("*** MOCK MODE *** Skipping real SDK initialization");
            this.sdkSessionId = DEFAULT_SESSION_ID;
            // Mock 模式不依赖真实 SDK，直接创建虚拟 Workspace 供 mock 路径工具使用
            this.workspace = new Workspace(null);
            this.initialized = true;
            return DEFAULT_SESSION_ID;
        }

        try {
            initializeSdkCore(serverAddress, port, notificationServerAddress);
            setAuthHeaders(accessToken, httpAuthKey, httpAuthValue);
            registerWorkspace(workspaceUuid, accessToken);

            this.initialized = true;
            logAndValidateSdkVersion();
            log.info("SDK initialized successfully: sessionId={}", sdkSessionId);
            return sdkSessionId > 0 ? sdkSessionId : DEFAULT_SESSION_ID;

        } catch (Exception e) {
            log.error("SDK initialization error: {}", e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 关闭 SDK 并释放所有资源。
     */
    public synchronized void shutdown() {
        if (!initialized) {
            return;
        }

        try {
            FlightHubSDK.shutdown();
            log.info("SDK shutdown complete");
        } catch (Exception e) {
            log.error("SDK shutdown error: {}", e.getMessage(), e);
        } finally {
            initialized = false;
            sdkSessionId = -1;
            workspace = null;
        }
    }

    /**
     * 判断当前是否为 Mock 模式。
     *
     * @return true 表示 Mock 模式
     */
    public boolean isMockMode() {
        return mockMode;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public long getSdkSessionId() {
        return sdkSessionId;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    /**
     * 根据 SN 获取 Drone 实例。
     *
     * @param sn 设备序列号
     * @return Drone 实例，未找到或 Mock 模式下返回 null
     */
    public Drone getDrone(String sn) {
        if (!initialized || sn == null) {
            return null;
        }
        if (isMockMode()) {
            return null;
        }
        return FlightHubSdkManager.getDroneInstance(sn);
    }

    /**
     * 根据 SN 获取 Dock 实例。
     *
     * @param sn 设备序列号
     * @return Dock 实例，未找到或 Mock 模式下返回 null
     */
    public Dock getDock(String sn) {
        if (!initialized || sn == null) {
            return null;
        }
        if (isMockMode()) {
            return null;
        }
        return FlightHubSdkManager.getDockInstance(sn);
    }

    /**
     * 自动发现无人机当前挂载的第一个相机 payload_index。
     * 通过 {@code DroneOsdModule.listenAircraftPayloadsCamerasList} 订阅一次获取相机列表，
     * 过滤出相机类型负载（排除喊话器、闪光灯等非相机负载）。
     *
     * @param drone 无人机实例
     * @return 发现到的相机 payloadIndex（如 "53-0-0"），超时或无相机时返回 null
     */
    public String resolvePayloadIndex(Drone drone) {
        if (drone == null) {
            return null;
        }
        com.dji.flighthubsdk.core.DroneOsdModule osdModule = drone.getDroneOsdModule();
        if (osdModule == null) {
            log.warn("resolvePayloadIndex: DroneOsdModule unavailable");
            return null;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>(null);

        int listenId = osdModule.listenAircraftPayloadsCamerasList(cameras -> {
            if (cameras != null) {
                List<com.dji.flighthubsdk.bean.AircraftPayloadCameraInfo> list = cameras.getCamerasList();
                if (list != null && !list.isEmpty()) {
                    String idx = list.get(0).getPayloadIndex();
                    if (idx != null && !idx.isBlank()) {
                        if (result.compareAndSet(null, idx)) {
                            latch.countDown();
                        }
                    }
                }
            }
        });

        if (listenId < 0) {
            log.warn("resolvePayloadIndex: listenAircraftPayloadsCamerasList failed, listenId={}", listenId);
            return null;
        }

        try {
            boolean received = latch.await(PAYLOAD_INDEX_DETECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!received) {
                log.warn("resolvePayloadIndex: timeout waiting for camera payloads list");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("resolvePayloadIndex: interrupted");
        } finally {
            osdModule.cancelListenAircraftPayloadsCamerasList(listenId);
        }

        log.info("resolvePayloadIndex: resolved={}", result.get());
        return result.get();
    }

    public String getSdkVersion() {
        return FlightHubSDK.getVersion();
    }

    // ==================== Private Methods ====================

    private void initializeSdkCore(String serverAddress, int port, String notificationServerAddress) {
        FlightHubSdkConfig config = new FlightHubSdkConfig();
        config.setAddress(serverAddress);
        config.setPort(String.valueOf(port));
        config.setLogLevel("INFO");

        // 显式设置 WebSocket 通知服务器地址
        // SDK 原生层（notification_sever_address，注：typo 来自 SDK）需要一个完整 ws:// URL；
        // 若调用方未传入，SDK 不会自动从 address+port 推断——留空会导致 WebSocket 连接失败。
        if (notificationServerAddress != null && !notificationServerAddress.isBlank()) {
            config.setNotificationServerAddress(notificationServerAddress);
            log.info("WebSocket 通知地址: {}", notificationServerAddress);
        } else {
            log.warn("notification_server_address 未设置，SDK 将尝试自动推断（可能失败）");
        }

        FlightHubSdkErrorCode initResult = FlightHubSDK.initialize(config);
        if (initResult != FlightHubSdkErrorCode.SUCCESS
                && initResult != FlightHubSdkErrorCode.ERROR_ALREADY_INITIALIZED) {
            throw new IllegalStateException("SDK initialize failed: " + initResult);
        }
    }

    private void setAuthHeaders(String accessToken, String httpAuthKey, String httpAuthValue) {
        // HTTP 和 WebSocket 共用同一组网关鉴权 header（与 SDK demo 一致）
        List<HeaderKeyValue> gatewayHeaders = new ArrayList<>();
        if (accessToken != null && !accessToken.isBlank()) {
            HeaderKeyValue authHeader = new HeaderKeyValue();
            authHeader.setKey(AUTH_HEADER_KEY);
            authHeader.setValue(AUTH_BEARER_PREFIX + accessToken);
            gatewayHeaders.add(authHeader);
        }
        if (httpAuthKey != null && !httpAuthKey.isBlank()) {
            // 平台网关 isv-webhook-auth 插件协议：
            // 1. auth_keys header 声明哪些 header 携带鉴权信息
            // 2. 实际鉴权 header（如 user_id: 平台实际分配值）
            HeaderKeyValue authKeysHeader = new HeaderKeyValue();
            authKeysHeader.setKey(GATEWAY_AUTH_KEYS_HEADER);
            authKeysHeader.setValue(httpAuthKey);
            gatewayHeaders.add(authKeysHeader);

            HeaderKeyValue gatewayHeader = new HeaderKeyValue();
            gatewayHeader.setKey(httpAuthKey);
            gatewayHeader.setValue(httpAuthValue != null ? httpAuthValue : "");
            gatewayHeaders.add(gatewayHeader);
            log.info("SDK 网关鉴权 header 已设置：auth_keys={}, {}=***", httpAuthKey, httpAuthKey);
        }
        if (!gatewayHeaders.isEmpty()) {
            FlightHubSDK.setWebSocketAuthHeaders(gatewayHeaders);
            FlightHubSDK.setHttpHeaders(gatewayHeaders);
            log.info("SDK 鉴权 header 已同步设置到 HTTP 和 WebSocket 通道");
        }
    }

    private void registerWorkspace(String workspaceUuid, String accessToken) {
        // 新版 SDK 移除了 registerWorkspaceToken / session 管理，直接通过 getWorkspaceInstance()
        // 获取全局默认 Workspace（SDK 内部通过配置文件或认证头自动绑定 workspace）。
        // workspaceUuid / accessToken 参数保留以便向后兼容调用方，不再传入 SDK。
        this.sdkSessionId = DEFAULT_SESSION_ID;

        Workspace ws = FlightHubSdkManager.getWorkspaceInstance();
        if (ws == null) {
            // workspace 为 null 说明 SDK handshake 尚未完成或认证失败，
            // 不能报告成功——后续所有工具调用都会因 workspace == null 而失败并输出误导性错误。
            throw new IllegalStateException(
                    "getWorkspaceInstance() returned null；SDK handshake 未完成或认证失败，请检查 FH_SERVER_ADDRESS / FH_ACCESS_TOKEN 配置");
        }
        this.workspace = ws;
        log.info("Workspace instance obtained successfully");
    }

    /**
     * 记录 SDK 版本并校验与 MCP 期望版本是否匹配。
     * 仅输出日志告警，不抛异常（避免版本小升级导致启动失败）。
     */
    private void logAndValidateSdkVersion() {
        try {
            String sdkVersion = getSdkVersion();
            log.info("SDK version: {}", sdkVersion);
            if (sdkVersion != null && !sdkVersion.startsWith(EXPECTED_SDK_VERSION_PREFIX)) {
                log.error("SDK version mismatch! Expected {}.x, actual={}. MCP may behave unexpectedly.",
                        EXPECTED_SDK_VERSION_PREFIX.replace(".", ""), sdkVersion);
            }
        } catch (Exception e) {
            log.warn("Unable to retrieve SDK version: {}", e.getMessage());
        }
    }
}
