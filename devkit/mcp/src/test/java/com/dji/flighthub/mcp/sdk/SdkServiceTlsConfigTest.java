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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import com.dji.flighthub.mcp.config.FlightHubProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SdkService} TLS 配置透传单元测试。
 *
 * <p>覆盖 {@code flighthub.server.verify-ssl} / {@code ssl-ca-path} 到 SDK
 * {@link FlightHubSdkConfig} 的映射语义（与 skeleton {@code RealSdkStarter#configureTls} 对齐）。
 *
 * @author DJI DevKit Team
 */
class SdkServiceTlsConfigTest {

    private static final String ADDRESS = "https://api.example.com";
    private static final int PORT = 443;
    private static final String WS_URL = "wss://api.example.com/ws";
    private static final String CA_PATH = "/certs/ca.pem";

    @Test
    @DisplayName("未配置 verify-ssl 时默认开启证书校验")
    void createSdkConfig_shouldEnableVerifySsl_whenNotConfigured() {
        SdkService service = new SdkService(new FlightHubProperties());

        FlightHubSdkConfig config = service.createSdkConfig(ADDRESS, PORT, WS_URL);

        assertTrue(config.isVerifySsl());
        assertNoCaPath(config);
    }

    @Test
    @DisplayName("显式 verify-ssl=false 时关闭证书校验")
    void createSdkConfig_shouldDisableVerifySsl_whenExplicitlyFalse() {
        SdkService service = new SdkService(propertiesWith(Boolean.FALSE, null));

        FlightHubSdkConfig config = service.createSdkConfig(ADDRESS, PORT, WS_URL);

        assertFalse(config.isVerifySsl());
        assertNoCaPath(config);
    }

    @Test
    @DisplayName("配置 ssl-ca-path 时应透传给 SDK")
    void createSdkConfig_shouldPassSslCaPath_whenConfigured() {
        SdkService service = new SdkService(propertiesWith(Boolean.TRUE, CA_PATH));

        FlightHubSdkConfig config = service.createSdkConfig(ADDRESS, PORT, WS_URL);

        assertTrue(config.isVerifySsl());
        assertEquals(CA_PATH, config.getSslCaPath());
    }

    @Test
    @DisplayName("ssl-ca-path 为空白时不设置")
    void createSdkConfig_shouldIgnoreSslCaPath_whenBlank() {
        SdkService service = new SdkService(propertiesWith(Boolean.TRUE, "   "));

        FlightHubSdkConfig config = service.createSdkConfig(ADDRESS, PORT, WS_URL);

        assertNoCaPath(config);
    }

    @Test
    @DisplayName("基础连接参数应正确写入 SDK 配置")
    void createSdkConfig_shouldMapConnectionFields() {
        SdkService service = new SdkService(new FlightHubProperties());

        FlightHubSdkConfig config = service.createSdkConfig(ADDRESS, PORT, WS_URL);

        assertEquals(ADDRESS, config.getAddress());
        assertEquals(String.valueOf(PORT), config.getPort());
        assertEquals(WS_URL, config.getNotificationServerAddress());
        assertEquals("INFO", config.getLogLevel());
    }

    private static FlightHubProperties propertiesWith(Boolean verifySsl, String caPath) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getServer().setVerifySsl(verifySsl);
        properties.getServer().setSslCaPath(caPath);
        return properties;
    }

    /**
     * 断言未设置自定义 CA 路径。
     *
     * <p>SDK 的 {@code FlightHubSdkConfig} 将未赋值的 {@code sslCaPath} 初始化为空串
     * （而非 {@code null}），因此按“空白”断言，语义上等同于“沿用默认信任库”。
     *
     * @param config SDK 配置
     */
    private static void assertNoCaPath(FlightHubSdkConfig config) {
        String caPath = config.getSslCaPath();
        assertTrue(caPath == null || caPath.isBlank(), "预期未设置 sslCaPath，实际: " + caPath);
    }
}
