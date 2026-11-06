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

        /**
         * 未被解析的占位符前缀。
         *
         * <p>环境变量缺失时，Spring 绑定不会把 application.yml 里的
         * {@code ${FH_JWT_SECRET}} 解析成空值，而是把这段字面量原样绑定进来
         * （16 字节），于是只报"长度不足"，现场无法判断是 compose 漏传变量
         * 还是密钥本身有问题。此处单独识别并给出明确指引。
         */
        private static final String UNRESOLVED_PLACEHOLDER_PREFIX = "${";

        /** 已知弱占位值黑名单（安全修复：拒绝开源后成为公知的默认 fallback）。 */
        private static final java.util.Set<String> WEAK_PLACEHOLDERS = java.util.Set.of(
                "change-me-in-production-must-be-32-chars!!",
                "change-me-in-production-must-be-32-chars",
                "your-secret-key-at-least-32-characters-long",
                "please-change-this-secret-key-32+chars"
        );

        /** 部署模板占位值前缀（不区分大小写），照抄模板必然不可用。 */
        private static final String TEMPLATE_PLACEHOLDER_PREFIX = "change_me";

        /** 部署模板占位值的尖括号定界符，如 {@code <replace-with-strong-random-key>}。 */
        private static final String TEMPLATE_PLACEHOLDER_START = "<";

        /** 部署模板占位值的尖括号定界符（结束）。 */
        private static final String TEMPLATE_PLACEHOLDER_END = ">";

        /** 提供默认值的紧凑构造函数。 */
        public Jwt {
            if (secret == null || secret.isBlank()) {
                throw new IllegalStateException(
                        "fh.user.jwt.secret 未配置。HS256 要求至少 " + MIN_SECRET_LENGTH
                                + " 字节密钥，请通过 FH_JWT_SECRET 环境变量注入。");
            }
            if (secret.startsWith(UNRESOLVED_PLACEHOLDER_PREFIX)) {
                throw new IllegalStateException(
                        "fh.user.jwt.secret 绑定到了未解析的占位符 " + secret
                                + "，说明环境变量 FH_JWT_SECRET 没有真正进入容器（常见原因："
                                + "docker-compose.yml 的 environment 段未透传该变量，"
                                + "--env-file 只影响 compose 插值，不会自动注入容器）。"
                                + "请确认 compose 的 environment 中含 FH_JWT_SECRET，"
                                + "或用 openssl rand -hex 32 生成密钥后显式传入。");
            }
            if (WEAK_PLACEHOLDERS.contains(secret)) {
                throw new IllegalStateException(
                        "fh.user.jwt.secret 检测到公知弱占位值，禁止启动。"
                                + "请通过 FH_JWT_SECRET 注入强随机值（openssl rand -hex 32）。");
            }
            if (isTemplatePlaceholder(secret)) {
                throw new IllegalStateException(
                        "fh.user.jwt.secret 仍是部署模板中的占位值 " + secret
                                + "，禁止启动。请用 openssl rand -hex 32 生成真实密钥后"
                                + "写入 deploy.env 的 FH_JWT_SECRET。");
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

        /**
         * 判断密钥是否仍是部署模板中的占位值。
         *
         * <p>覆盖两种模板写法：{@code CHANGE_ME_xxx} 环境变量风格，以及
         * {@code <replace-with-strong-random-key>} 尖括号风格
         * （见 {@code skeleton/deploy.env.example}）。后者长度有时超过 32 字节，
         * 只靠长度校验会被放过，必须单独拦截。
         *
         * @param secret 待校验密钥
         * @return 命中模板占位值时返回 true
         */
        private static boolean isTemplatePlaceholder(String secret) {
            String lowerCaseSecret = secret.toLowerCase(java.util.Locale.ROOT);
            if (lowerCaseSecret.startsWith(TEMPLATE_PLACEHOLDER_PREFIX)) {
                return true;
            }
            return lowerCaseSecret.startsWith(TEMPLATE_PLACEHOLDER_START)
                    && lowerCaseSecret.endsWith(TEMPLATE_PLACEHOLDER_END);
        }
    }

    /**
     * 注册配置。
     *
     * <p>安全发布专项修复：<b>默认关闭开放注册</b>（enabled=false）。
     * 此前默认 true + 首用户自动成为 admin，一旦公网可达即可被匿名抢占 admin 权限。
     * 集成商 需要开放注册时显式设为 {@code fh.user.registration.enabled=true}，
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
     * 由集成商自行实现 bootstrap 通道，本 Skeleton 不内置免注册建号端点。
     */
}
