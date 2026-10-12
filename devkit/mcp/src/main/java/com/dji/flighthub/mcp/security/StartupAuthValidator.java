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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * SSE 模式启动鉴权硬校验 — 安全发布专项修复。
 *
 * <p>问题背景：原 docker-compose.yml 中 FH_SSE_AUTH_TOKEN 默认为空字符串，
 * {@link BearerTokenAuthenticator#isEnabled()} 在 token 为空时返回 false，
 * {@link AuthenticationWebFilter} 据此放行所有请求，导致 SSE 模式下 51 个 SDK 工具
 * （含 fh_emergency_stop、fh_takeoff 等飞行控制指令）无鉴权暴露。
 *
 * <p>修复策略：实现 {@link SmartInitializingSingleton}，在所有 Bean 初始化完成后、
 * Netty 端口绑定之前执行校验。若 token 缺失或为弱占位值则抛出异常中止启动，
 * 确保不存在"端口已开放但鉴权未就绪"的 TOCTOU 窗口。
 *
 * <p>仅 SSE profile 激活（stdio 模式为本地 IDE 集成，不暴露网络端口，无需鉴权）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
@Profile("sse")
public class StartupAuthValidator implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(StartupAuthValidator.class);

    /** 弱占位值黑名单（开源后成为公知值，禁止用于生产）。 */
    private static final java.util.Set<String> WEAK_PLACEHOLDERS = java.util.Set.of(
            "change-me-to-a-strong-random-token",
            "your-token",
            "changeme",
            "change-me",
            "placeholder",
            "replace-me",
            "REPLACE_WITH_openssl_rand_hex_32_OUTPUT");

    private final FlightHubProperties properties;
    private final Environment environment;

    /**
     * 构造函数。
     *
     * @param properties  FlightHub 配置属性
     * @param environment Spring 环境（用于读取 FH_TRANSPORT 旁路校验）
     */
    public StartupAuthValidator(FlightHubProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    /**
     * 在所有单例 Bean 初始化完成后、Netty 端口绑定前执行鉴权校验。
     * 消除原 ApplicationReadyEvent 方案中端口已开放但鉴权未验证的 TOCTOU 窗口。
     */
    @Override
    public void afterSingletonsInstantiated() {
        String token = properties.getTransport().getSseAuthToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "【安全拦截】SSE 模式启动被拒绝：FH_SSE_AUTH_TOKEN 未配置。\n"
                            + "该变量为 SSE 模式必填项，留空会导致所有飞行控制工具无鉴权暴露。\n"
                            + "请执行：openssl rand -hex 32 生成强随机 token，\n"
                            + "并通过环境变量 FH_SSE_AUTH_TOKEN 注入后重启。\n"
                            + "（stdio 本地调试模式不受此限制）");
        }

        if (isWeakPlaceholder(token)) {
            throw new IllegalStateException(
                    "【安全拦截】SSE 模式启动被拒绝：FH_SSE_AUTH_TOKEN 检测到公知弱占位值。\n"
                            + "开源仓库中的示例 token 已被攻击者掌握，"
                            + "使用该值等于无鉴权暴露所有飞行控制工具。\n"
                            + "请执行：openssl rand -hex 32 生成强随机 token，\n"
                            + "并通过环境变量 FH_SSE_AUTH_TOKEN 注入后重启。");
        }

        log.info("SSE 鉴权已启用：FH_SSE_AUTH_TOKEN 配置正常（masked={}）", maskToken(token));
    }

    /**
     * 判断 token 是否为示例占位值（ISV 未替换）。
     *
     * @param token 待校验 token
     * @return true 表示为弱占位值
     */
    private boolean isWeakPlaceholder(String token) {
        return WEAK_PLACEHOLDERS.contains(token);
    }

    /**
     * 对 token 做脱敏处理，仅保留前 4 位用于日志识别。
     *
     * @param token 原 token
     * @return 脱敏后的字符串
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 4) {
            return "****";
        }
        return token.substring(0, 4) + "****";
    }
}
