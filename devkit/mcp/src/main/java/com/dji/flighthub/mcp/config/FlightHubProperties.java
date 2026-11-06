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

package com.dji.flighthub.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FlightHub SDK 连接配置。
 * 从环境变量（FH_*）或 application.yml 加载。
 */
@Component
@ConfigurationProperties(prefix = "flighthub")
public class FlightHubProperties {

    private Server server = new Server();
    private Auth auth = new Auth();
    private Security security = new Security();
    private Failsafe failsafe = new Failsafe();
    private Audit audit = new Audit();
    private Idempotency idempotency = new Idempotency();
    private RateLimit rateLimit = new RateLimit();
    private Transport transport = new Transport();
    private Cockpit cockpit = new Cockpit();
    private Output output = new Output();

    // --- 嵌套配置类 ---

    public static class Server {
        private String address;
        private int port = 443;
        /**
         * 是否校验 SDK 通知通道（WSS）的服务端 TLS 证书。
         *
         * <p>{@code null} 视为 {@code true}（安全默认）。私有化 FH2 常用自签名证书，
         * SDK 的 libwebsockets 通知通道默认开启证书校验（注意 SDK 的 HTTP 客户端并不校验），
         * 内网部署可设为 {@code false} 跳过校验，或保持 {@code true} 并通过
         * {@link #sslCaPath} 指定自签 CA。
         */
        private Boolean verifySsl;
        /**
         * 通知通道（WSS）的 CA 证书文件路径；为空则沿用 SDK 默认信任库。
         * 仅在 {@link #verifySsl} 为 {@code true} 时生效。
         */
        private String sslCaPath;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public Boolean getVerifySsl() {
            return verifySsl;
        }

        public void setVerifySsl(Boolean verifySsl) {
            this.verifySsl = verifySsl;
        }

        public String getSslCaPath() {
            return sslCaPath;
        }

        public void setSslCaPath(String sslCaPath) {
            this.sslCaPath = sslCaPath;
        }
    }

    public static class Auth {
        private String accessToken;
        private String workspaceUuid;
        /** 平台网关鉴权 header key（如 "user_id"），为空则不设置 */
        private String httpAuthKey;
        /** 平台网关鉴权 header value（如 "your-auth-value"，占位示例需按平台实际配置） */
        private String httpAuthValue;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getWorkspaceUuid() {
            return workspaceUuid;
        }

        public void setWorkspaceUuid(String workspaceUuid) {
            this.workspaceUuid = workspaceUuid;
        }

        public String getHttpAuthKey() {
            return httpAuthKey;
        }

        public void setHttpAuthKey(String httpAuthKey) {
            this.httpAuthKey = httpAuthKey;
        }

        public String getHttpAuthValue() {
            return httpAuthValue;
        }

        public void setHttpAuthValue(String httpAuthValue) {
            this.httpAuthValue = httpAuthValue;
        }
    }

    public static class Security {
        private int maxLevel = 3;
        private String operatorToken;
        private int sessionTimeout = 1800; // 超时时间（秒）

        public int getMaxLevel() {
            return maxLevel;
        }

        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        public String getOperatorToken() {
            return operatorToken;
        }

        public void setOperatorToken(String operatorToken) {
            this.operatorToken = operatorToken;
        }

        public int getSessionTimeout() {
            return sessionTimeout;
        }

        public void setSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }
    }

    public static class Failsafe {
        private String onCrash = "return_home"; // 失联策略：return_home | hover | none
        private int disconnectTimeout = 60;     // 断连超时（秒）

        public String getOnCrash() {
            return onCrash;
        }

        public void setOnCrash(String onCrash) {
            this.onCrash = onCrash;
        }

        public int getDisconnectTimeout() {
            return disconnectTimeout;
        }

        public void setDisconnectTimeout(int disconnectTimeout) {
            this.disconnectTimeout = disconnectTimeout;
        }
    }

    public static class Audit {
        private boolean enabled = true;
        private String endpoint;
        private int retentionDays = 30;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public int getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }
    }

    public static class Idempotency {
        private boolean enabled = true;
        private int ttlSeconds = 300;
        private int maxSize = 10000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getTtlSeconds() {
            return ttlSeconds;
        }

        public void setTtlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
    }

    /**
     * 工具级限流配置 — 按 SecurityLevel 分层的拒绝式漏桶。
     *
     * <p>配额为每分钟调用次数（rpm），{@code 0} 表示该档位不限流。
     * 单工具覆盖（{@link #perToolRpm}）优先级高于分层默认值。
     */
    public static class RateLimit {
        /** 工具级限流总开关 */
        private boolean enabled = true;
        /** L0 只读工具每分钟配额 */
        private int readOnlyRpm = 300;
        /** L1 可逆工具每分钟配额 */
        private int reversibleRpm = 30;
        /** L2 飞行工具每分钟配额 */
        private int flightRpm = 6;
        /** L3 紧急工具每分钟配额，0 = 不限流（紧急停止必须永远可执行） */
        private int criticalRpm = 0;
        /** 单工具覆盖：toolName -> rpm（0 表示不限流） */
        private Map<String, Integer> perToolRpm = new LinkedHashMap<>();
        /** 最大跟踪键数，超限 fail-open */
        private int maxKeys = 10000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getReadOnlyRpm() {
            return readOnlyRpm;
        }

        public void setReadOnlyRpm(int readOnlyRpm) {
            this.readOnlyRpm = readOnlyRpm;
        }

        public int getReversibleRpm() {
            return reversibleRpm;
        }

        public void setReversibleRpm(int reversibleRpm) {
            this.reversibleRpm = reversibleRpm;
        }

        public int getFlightRpm() {
            return flightRpm;
        }

        public void setFlightRpm(int flightRpm) {
            this.flightRpm = flightRpm;
        }

        public int getCriticalRpm() {
            return criticalRpm;
        }

        public void setCriticalRpm(int criticalRpm) {
            this.criticalRpm = criticalRpm;
        }

        public Map<String, Integer> getPerToolRpm() {
            return perToolRpm;
        }

        public void setPerToolRpm(Map<String, Integer> perToolRpm) {
            this.perToolRpm = perToolRpm;
        }

        public int getMaxKeys() {
            return maxKeys;
        }

        public void setMaxKeys(int maxKeys) {
            this.maxKeys = maxKeys;
        }
    }

    public static class Transport {
        private String mode = "stdio"; // 传输模式：stdio | sse
        private int ssePort = 8080;
        private String sseAuthToken;
        /** 认证模式：token（静态 token 比对）| jwt（JWT 签名验证，支持多用户） */
        private String authMode = "token";
        /** JWT HMAC 密钥（authMode=jwt 时必填，与 jwtPublicKey 互斥） */
        private String jwtSecret;
        /** JWT RSA 公钥路径（authMode=jwt 时可选，与 jwtSecret 互斥） */
        private String jwtPublicKey;
        /** 心跳发送间隔（毫秒），默认 30000 */
        private int heartbeatIntervalMs = 30000;
        /** 心跳超时阈值（秒），超过此时间无活动视为死连接，默认 300（5 分钟） */
        private int heartbeatTimeoutSeconds = 300;
        /** 断连 grace period（秒），默认 60 */
        private int gracePeriodSeconds = 60;
        /** grace period 过期前预警时间（秒），默认 10 */
        private int graceWarningBeforeSeconds = 10;
        /** 单次 tool 调用的最大执行时间（秒），超时后返回错误响应，默认 90（须大于 elicitation 超时 60s） */
        private int toolTimeoutSeconds = 90;

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public int getSsePort() {
            return ssePort;
        }

        public void setSsePort(int ssePort) {
            this.ssePort = ssePort;
        }

        public String getSseAuthToken() {
            return sseAuthToken;
        }

        public void setSseAuthToken(String sseAuthToken) {
            this.sseAuthToken = sseAuthToken;
        }

        public String getAuthMode() {
            return authMode;
        }

        public void setAuthMode(String authMode) {
            this.authMode = authMode;
        }

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public String getJwtPublicKey() {
            return jwtPublicKey;
        }

        public void setJwtPublicKey(String jwtPublicKey) {
            this.jwtPublicKey = jwtPublicKey;
        }

        public int getHeartbeatIntervalMs() {
            return heartbeatIntervalMs;
        }

        public void setHeartbeatIntervalMs(int heartbeatIntervalMs) {
            this.heartbeatIntervalMs = heartbeatIntervalMs;
        }

        public int getHeartbeatTimeoutSeconds() {
            return heartbeatTimeoutSeconds;
        }

        public void setHeartbeatTimeoutSeconds(int heartbeatTimeoutSeconds) {
            this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
        }

        public int getGracePeriodSeconds() {
            return gracePeriodSeconds;
        }

        public void setGracePeriodSeconds(int gracePeriodSeconds) {
            this.gracePeriodSeconds = gracePeriodSeconds;
        }

        public int getGraceWarningBeforeSeconds() {
            return graceWarningBeforeSeconds;
        }

        public void setGraceWarningBeforeSeconds(int graceWarningBeforeSeconds) {
            this.graceWarningBeforeSeconds = graceWarningBeforeSeconds;
        }

        public int getToolTimeoutSeconds() {
            return Math.max(10, toolTimeoutSeconds);
        }

        public void setToolTimeoutSeconds(int toolTimeoutSeconds) {
            this.toolTimeoutSeconds = toolTimeoutSeconds;
        }
    }

    public static class Cockpit {
        /** 驾驶舱前端地址，起飞成功后返回直播观察链接。为空则不返回。 */
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * 输出体积预算配置 — 防止超大 SDK 响应直接透传给 LLM。
     */
    public static class Output {
        /** 是否启用输出预算守卫 */
        private boolean enabled = true;
        /** 单次 tool 响应最大字节数（pretty-print 后），默认 256KB */
        private int maxBytes = 256 * 1024;
        /** 单个列表最多保留的条数，默认 500 */
        private int maxArrayItems = 500;
        /** 单个字符串最多保留的字符数，默认 8192 */
        private int maxStringChars = 8192;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxBytes() {
            return maxBytes;
        }

        public void setMaxBytes(int maxBytes) {
            this.maxBytes = maxBytes;
        }

        public int getMaxArrayItems() {
            return maxArrayItems;
        }

        public void setMaxArrayItems(int maxArrayItems) {
            this.maxArrayItems = maxArrayItems;
        }

        public int getMaxStringChars() {
            return maxStringChars;
        }

        public void setMaxStringChars(int maxStringChars) {
            this.maxStringChars = maxStringChars;
        }
    }

    // --- Getter/Setter ---

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Failsafe getFailsafe() {
        return failsafe;
    }

    public void setFailsafe(Failsafe failsafe) {
        this.failsafe = failsafe;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public Idempotency getIdempotency() {
        return idempotency;
    }

    public void setIdempotency(Idempotency idempotency) {
        this.idempotency = idempotency;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Cockpit getCockpit() {
        return cockpit;
    }

    public void setCockpit(Cockpit cockpit) {
        this.cockpit = cockpit;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }
}
