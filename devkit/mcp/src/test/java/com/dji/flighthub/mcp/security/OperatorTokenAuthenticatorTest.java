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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OperatorTokenAuthenticator 单元测试。
 * 覆盖：启用判定（配置/未配置）、正确/错误/空 token 校验、脱敏日志。
 *
 * @author DJI FlightHub SDK Team
 */
class OperatorTokenAuthenticatorTest {

    private static final String OPERATOR_TOKEN = "op-secret-7f3a9c2e";

    /**
     * 构建配置了指定 operator token 的鉴权器。
     *
     * @param token 要配置的 token（null 表示不配置）
     * @return 鉴权器实例
     */
    private static OperatorTokenAuthenticator authenticatorWith(String token) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setOperatorToken(token);
        return new OperatorTokenAuthenticator(properties);
    }

    @Test
    void isEnabled_shouldReturnTrue_whenTokenConfigured() {
        OperatorTokenAuthenticator auth = authenticatorWith(OPERATOR_TOKEN);
        assertTrue(auth.isEnabled(), "配置了非空 token 时鉴权应启用");
    }

    @Test
    void isEnabled_shouldReturnFalse_whenTokenNotConfigured() {
        OperatorTokenAuthenticator auth = authenticatorWith(null);
        assertFalse(auth.isEnabled(), "未配置 token 时鉴权应禁用");

        OperatorTokenAuthenticator blank = authenticatorWith("  ");
        assertFalse(blank.isEnabled(), "空白 token 视为未配置");
    }

    @Test
    void verify_shouldReturnTrue_whenTokenMatches() {
        OperatorTokenAuthenticator auth = authenticatorWith(OPERATOR_TOKEN);
        assertTrue(auth.verify(OPERATOR_TOKEN), "正确 token 应校验通过");
    }

    @Test
    void verify_shouldReturnFalse_whenTokenMismatch() {
        OperatorTokenAuthenticator auth = authenticatorWith(OPERATOR_TOKEN);
        assertFalse(auth.verify("wrong-token"), "错误 token 应拒绝");
    }

    @Test
    void verify_shouldReturnFalse_whenTokenNull() {
        OperatorTokenAuthenticator auth = authenticatorWith(OPERATOR_TOKEN);
        assertFalse(auth.verify(null), "缺失 token 应拒绝");
    }

    @Test
    void verify_shouldReturnFalse_whenTokenBlank() {
        OperatorTokenAuthenticator auth = authenticatorWith(OPERATOR_TOKEN);
        assertFalse(auth.verify("   "), "空白 token 应拒绝");
    }

    @Test
    void verify_shouldReturnFalse_whenNotConfigured() {
        OperatorTokenAuthenticator auth = authenticatorWith(null);
        assertFalse(auth.verify(OPERATOR_TOKEN),
                "未配置 operator token 时任何调用方提供的 token 都应拒绝（fail-safe）");
    }

    @Test
    void maskToken_shouldMaskLongToken() {
        assertEquals("op-s****", OperatorTokenAuthenticator.maskToken(OPERATOR_TOKEN),
                "仅保留前 4 位，其余脱敏");
    }

    @Test
    void maskToken_shouldHandleShortToken() {
        assertEquals("****", OperatorTokenAuthenticator.maskToken("abc"),
                "长度不足 4 位时全部脱敏");
        assertEquals("****", OperatorTokenAuthenticator.maskToken(null),
                "null 输入安全返回全脱敏");
    }
}
