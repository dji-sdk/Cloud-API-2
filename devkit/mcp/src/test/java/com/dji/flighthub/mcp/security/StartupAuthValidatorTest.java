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

package com.dji.flighthub.mcp.security;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * StartupAuthValidator 单元测试 — 安全发布专项。
 *
 * <p>覆盖 SSE 模式启动鉴权硬校验：
 * <ul>
 *   <li>FH_SSE_AUTH_TOKEN 未配置 → 抛 IllegalStateException 中止启动</li>
 *   <li>弱占位值 → 抛 IllegalStateException 中止启动</li>
 *   <li>正常强随机值 → 校验通过</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
class StartupAuthValidatorTest {

    // ==================== 启动拦截 ====================

    @Test
    @DisplayName("token 为 null 时应抛异常中止启动")
    void afterSingletonsInstantiated_shouldThrow_whenTokenNull() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getTransport().setSseAuthToken(null);
        StartupAuthValidator validator = new StartupAuthValidator(properties, new MockEnvironment());

        assertThrows(IllegalStateException.class,
                validator::afterSingletonsInstantiated,
                "token 未配置时必须中止 SSE 模式启动");
    }

    @Test
    @DisplayName("token 为空字符串时应抛异常中止启动")
    void afterSingletonsInstantiated_shouldThrow_whenTokenEmpty() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getTransport().setSseAuthToken("");
        StartupAuthValidator validator = new StartupAuthValidator(properties, new MockEnvironment());

        assertThrows(IllegalStateException.class,
                validator::afterSingletonsInstantiated,
                "token 为空时必须中止 SSE 模式启动");
    }

    @Test
    @DisplayName("token 为纯空白字符时应抛异常中止启动")
    void afterSingletonsInstantiated_shouldThrow_whenTokenBlank() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getTransport().setSseAuthToken("   ");
        StartupAuthValidator validator = new StartupAuthValidator(properties, new MockEnvironment());

        assertThrows(IllegalStateException.class,
                validator::afterSingletonsInstantiated,
                "token 为空白字符时必须中止 SSE 模式启动");
    }

    // ==================== 正常路径 ====================

    @Test
    @DisplayName("token 为强随机值时应正常通过")
    void afterSingletonsInstantiated_shouldPass_whenTokenStrong() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getTransport().setSseAuthToken("a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6");
        StartupAuthValidator validator = new StartupAuthValidator(properties, new MockEnvironment());

        assertDoesNotThrow(validator::afterSingletonsInstantiated,
                "强随机 token 不应触发启动拦截");
    }

    @Test
    @DisplayName("token 为弱占位值时应抛异常中止启动")
    void afterSingletonsInstantiated_shouldThrow_whenTokenWeakPlaceholder() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getTransport().setSseAuthToken("change-me-to-a-strong-random-token");
        StartupAuthValidator validator = new StartupAuthValidator(properties, new MockEnvironment());

        assertThrows(IllegalStateException.class,
                validator::afterSingletonsInstantiated,
                "弱占位值必须中止启动，杜绝公知 token 调用飞行控制工具");
    }

    @Test
    @DisplayName("token 为 .env.example 中的占位值时应抛异常中止启动")
    void afterSingletonsInstantiated_shouldThrow_whenTokenEnvExamplePlaceholder() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getTransport().setSseAuthToken("REPLACE_WITH_openssl_rand_hex_32_OUTPUT");
        StartupAuthValidator validator = new StartupAuthValidator(properties, new MockEnvironment());

        assertThrows(IllegalStateException.class,
                validator::afterSingletonsInstantiated,
                ".env.example 占位值必须中止启动");
    }
}
