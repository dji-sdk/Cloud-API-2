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

package com.dji.fh.feature.user;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用户模块配置属性。
 *
 * @param jwt          JWT 相关配置
 * @param registration 注册相关配置
 * @author DJI DevKit Team
 */
@ConfigurationProperties("fh.user")
public record UserProperties(
        Jwt jwt,
        Registration registration
) {

    /**
     * JWT 配置。
     *
     * @param secret 签名密钥（HS256 至少 32 字节，生产环境必须通过 FH_JWT_SECRET 环境变量注入）
     * @param expiry token 有效期（默认 24h）
     */
    public record Jwt(
            String secret,
            Duration expiry
    ) {
        /** HS256 要求最低密钥长度（32 字节）。 */
        private static final int MIN_SECRET_LENGTH = 32;

        /** 已知弱占位值黑名单（安全修复：拒绝开源后成为公知的默认 fallback）。 */
        private static final java.util.Set<String> WEAK_PLACEHOLDERS = java.util.Set.of(
                "change-me-in-production-must-be-32-chars!!",
                "change-me-in-production-must-be-32-chars",
                "your-secret-key-at-least-32-characters-long",
                "please-change-this-secret-key-32+chars"
        );

        /** 提供默认值的紧凑构造函数。 */
        public Jwt {
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException(
                        "fh.user.jwt.secret 未配置。HS256 要求至少 " + MIN_SECRET_LENGTH
                                + " 字节密钥，请通过 FH_JWT_SECRET 环境变量注入。");
            }
            if (WEAK_PLACEHOLDERS.contains(secret)) {
                throw new IllegalStateException(
                        "fh.user.jwt.secret 检测到公知弱占位值，禁止启动。"
                                + "请通过 FH_JWT_SECRET 注入强随机值（openssl rand -hex 32）。");
            }
            if (secret.length() < MIN_SECRET_LENGTH) {
                throw new IllegalStateException(
                        "fh.user.jwt.secret 长度不足（当前 " + secret.length()
                                + " 字节，最少 " + MIN_SECRET_LENGTH + " 字节）。");
            }
            if (expiry == null) {
                expiry = Duration.ofHours(24);
            }
        }
    }

    /**
     * 注册配置。
     *
     * <p>安全发布专项修复：<b>默认关闭开放注册</b>（enabled=false）。
     * 此前默认 true + 首用户自动成为 admin，一旦公网可达即可被匿名抢占 admin 权限。
     * ISV 需要开放注册时显式设为 {@code fh.user.registration.enabled=true}，
     * 并按部署文档准备初始 admin 引导通道（见 {@link #initialAdminBootstrap()} 说明）。
     *
     * @param enabled     是否开放注册（默认 false）
     * @param defaultRole 新用户默认角色（默认 user）
     */
    public record Registration(
            boolean enabled,
            String defaultRole
    ) {
        /** 提供默认值的紧凑构造函数。 */
        public Registration {
            if (defaultRole == null || defaultRole.isBlank()) {
                defaultRole = "user";
            }
        }
    }

    /** 提供默认值的紧凑构造函数。 */
    public UserProperties {
        if (jwt == null) {
            throw new IllegalStateException(
                    "fh.user.jwt 未配置。请设置 fh.user.jwt.secret（HS256 至少 32 字节），"
                            + "通过环境变量 FH_JWT_SECRET 或 application.yml 注入。");
        }
        if (registration == null) {
            registration = new Registration(false, null);
        }
    }

    /**
     * 初始管理员引导说明（配套部署文档用，非执行逻辑）。
     *
     * <p>开放注册（enabled=true）时，首个注册用户仍会自动成为 admin——这是特性
     * 而非漏洞：系统部署后首个初始化者必须获得初始 admin 入口。若部署场景要求
     * 更严格的引导（如注册关闭时通过环境变量/初始化 SQL 预置初始 admin），
     * 由 ISV 自行实现 bootstrap 通道，本 Skeleton 不内置免注册建号端点。
     */
}
