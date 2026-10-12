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
    private Transport transport = new Transport();
    private Cockpit cockpit = new Cockpit();

    // --- 嵌套配置类 ---

    public static class Server {
        private String address;
        private int port = 443;

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
}
