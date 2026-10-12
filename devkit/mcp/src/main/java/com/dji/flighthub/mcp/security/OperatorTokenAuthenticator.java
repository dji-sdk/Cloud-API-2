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
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 运营商级二次鉴权 — 面向 L3（不可逆）飞行控制操作的独立 token 校验。
 *
 * <p>安全发布专项修复：此前 L3 紧急停止的确认
 * 仅依赖 {@code confirm} 参数，而该参数对标准 MCP 客户端（不支持自定义
 * elicitation 扩展）是可被 AI 直接填写的 {@code true}，缺少真实人类确认。
 * 本组件为 L3 操作提供与 session 凭据<b>分离</b>的独立二次鉴权：
 * 调用方必须额外提供 {@code FH_OPERATOR_TOKEN} 匹配的 token，即使 AI
 * 已持有 session 也无权独立触发紧急停止。
 *
 * <p>配置：{@code fh.security.operator-token}（环境变量 {@code FH_OPERATOR_TOKEN}）。
 * token 为空时 {@link #isEnabled()} 返回 false，表示"运营商鉴权未启用"——
 * 此时由调用方决定策略（L3 工具默认<b>拒绝执行</b>，宁可不可用不可裸奔）。
 *
 * <p>比对使用常量时间比较（{@link MessageDigest#isEqual}），防止时序侧信道。
 * token 在日志中一律脱敏，仅保留前 4 位用于识别。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class OperatorTokenAuthenticator {

    /** 日志脱敏后保留的 token 前缀长度 */
    private static final int MASK_KEEP_PREFIX_LENGTH = 4;

    private final FlightHubProperties properties;

    /**
     * 构造函数。
     *
     * @param properties FlightHub 配置（读取 operator-token）
     */
    public OperatorTokenAuthenticator(FlightHubProperties properties) {
        this.properties = properties;
    }

    /**
     * 判断运营商鉴权是否启用（配置了非空 token）。
     *
     * @return true 表示已配置，需校验；false 表示未配置
     */
    public boolean isEnabled() {
        String token = getConfiguredToken();
        return token != null && !token.isBlank();
    }

    /**
     * 校验调用方提供的运营商 token。
     *
     * @param providedToken 调用方提供的 token（可为 null/空）
     * @return true 表示校验通过；false 表示未配置或 token 不匹配
     */
    public boolean verify(String providedToken) {
        String configured = getConfiguredToken();
        if (configured == null || configured.isBlank()) {
            return false;
        }
        return providedToken != null
                && constantTimeEquals(configured, providedToken);
    }

    /**
     * 常量时间字符串比较，防止时序攻击。
     * 使用 JDK 标准库 {@link MessageDigest#isEqual} 确保无 JIT 优化风险。
     *
     * @param expected 期望值
     * @param actual   实际值
     * @return true 表示相等
     */
    private static boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对 token 做脱敏处理，仅保留前 4 位用于日志识别。
     *
     * @param token 原 token
     * @return 脱敏后的字符串
     */
    public static String maskToken(String token) {
        if (token == null || token.length() <= MASK_KEEP_PREFIX_LENGTH) {
            return "****";
        }
        return token.substring(0, MASK_KEEP_PREFIX_LENGTH) + "****";
    }

    private String getConfiguredToken() {
        return properties.getSecurity().getOperatorToken();
    }
}
