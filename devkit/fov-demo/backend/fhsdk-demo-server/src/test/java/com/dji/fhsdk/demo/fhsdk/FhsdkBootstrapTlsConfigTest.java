// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

package com.dji.fhsdk.demo.fhsdk;

import com.dji.fhsdk.demo.config.FhsdkProperties;
import com.dji.flighthubsdk.bean.FlightHubSdkConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FhsdkBootstrap} WSS 证书校验透传单元测试。
 *
 * <p>覆盖 {@code fhsdk.verify-ssl} / {@code fhsdk.ssl-ca-path} 到 SDK
 * {@link FlightHubSdkConfig} 的映射语义（与 skeleton {@code RealSdkStarter#configureTls} 对齐）。
 */
class FhsdkBootstrapTlsConfigTest {

    private static final String CA_PATH = "/certs/ca.pem";

    @Test
    @DisplayName("未配置 verify-ssl 时默认开启证书校验")
    void buildSdkConfig_shouldEnableVerifySsl_whenNotConfigured() {
        FlightHubSdkConfig sdkConfig = bootstrapWith(null, null).buildSdkConfig();

        assertTrue(sdkConfig.isVerifySsl());
        assertNoCaPath(sdkConfig);
    }

    @Test
    @DisplayName("显式 verify-ssl=false 时关闭证书校验")
    void buildSdkConfig_shouldDisableVerifySsl_whenExplicitlyFalse() {
        FlightHubSdkConfig sdkConfig = bootstrapWith(Boolean.FALSE, null).buildSdkConfig();

        assertFalse(sdkConfig.isVerifySsl());
        assertNoCaPath(sdkConfig);
    }

    @Test
    @DisplayName("配置 ssl-ca-path 时应透传给 SDK")
    void buildSdkConfig_shouldPassSslCaPath_whenConfigured() {
        FlightHubSdkConfig sdkConfig = bootstrapWith(Boolean.TRUE, CA_PATH).buildSdkConfig();

        assertTrue(sdkConfig.isVerifySsl());
        assertEquals(CA_PATH, sdkConfig.getSslCaPath());
    }

    @Test
    @DisplayName("ssl-ca-path 为空白时不设置")
    void buildSdkConfig_shouldIgnoreSslCaPath_whenBlank() {
        FlightHubSdkConfig sdkConfig = bootstrapWith(Boolean.TRUE, "   ").buildSdkConfig();

        assertNoCaPath(sdkConfig);
    }

    private static FhsdkBootstrap bootstrapWith(Boolean verifySsl, String caPath) {
        FhsdkProperties properties = new FhsdkProperties();
        properties.setVerifySsl(verifySsl);
        properties.setSslCaPath(caPath);
        return new FhsdkBootstrap(properties);
    }

    /**
     * 断言未设置自定义 CA 路径。
     *
     * <p>SDK 的 {@code FlightHubSdkConfig} 将未赋值的 {@code sslCaPath} 初始化为空串
     * （而非 {@code null}），因此按“空白”断言，语义上等同于“沿用默认信任库”。
     *
     * @param sdkConfig SDK 配置
     */
    private static void assertNoCaPath(FlightHubSdkConfig sdkConfig) {
        String caPath = sdkConfig.getSslCaPath();
        assertTrue(caPath == null || caPath.isBlank(), "预期未设置 sslCaPath，实际: " + caPath);
    }
}
