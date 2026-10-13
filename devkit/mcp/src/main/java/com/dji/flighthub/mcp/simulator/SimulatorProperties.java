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

package com.dji.flighthub.mcp.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 承载连接 pf-device-simulator-service 所需的配置项。
 *
 * <p>base-url 与 username 保留本地开发零配置默认值；password 无默认值
 * （安全审计 F-02 整改）：公知弱密码不得作为代码内 fallback，未显式配置
 * FH_SIM_PASSWORD 时 Sim* 工具将拒绝调用（fail-fast），由
 * {@link #hasUsableCredentials()} 统一判定。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
@ConfigurationProperties(prefix = "sim")
public class SimulatorProperties implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SimulatorProperties.class);

    private static final String DEFAULT_BASE_URL = "http://localhost:8000";

    private static final String DEFAULT_USERNAME = "admin";

    /** 公知弱密码黑名单：模拟器官方默认值（F-02 整改，禁止作为 fallback 使用）。 */
    private static final Set<String> WEAK_PASSWORDS = Set.of("Password@", "password", "admin", "123456");

    private String baseUrl = DEFAULT_BASE_URL;

    private String username = DEFAULT_USERNAME;

    /** 默认为空：未配置 FH_SIM_PASSWORD 时无可用凭据。 */
    private String password = "";

    /**
     * 获取模拟器服务基础 URL。
     *
     * @return 基础 URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 设置模拟器服务基础 URL。
     *
     * @param baseUrl 基础 URL
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 获取登录用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置登录用户名。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取登录密码（无默认值，未配置时为空字符串）。
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置登录密码。
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password == null ? "" : password;
    }

    /**
     * 判断当前凭据是否可用于登录模拟器。
     *
     * <p>判定规则（F-02 整改）：密码为空、或命中公知弱密码黑名单时，
     * 视为无可用凭据，Sim* 工具应拒绝调用并提示显式配置 FH_SIM_PASSWORD。
     *
     * @return true 表示凭据可用
     */
    public boolean hasUsableCredentials() {
        return password != null && !password.isBlank() && !WEAK_PASSWORDS.contains(password);
    }

    /**
     * 生成凭据不可用时的提示信息。
     *
     * @return 提示信息（含配置指引）
     */
    public String credentialsHint() {
        return "FH_SIM_PASSWORD is not set (or is a known weak value). "
                + "Configure it explicitly, e.g. FH_SIM_PASSWORD=$(openssl rand -hex 16), "
                + "matching the simulator's config.yaml auth.default_password. "
                + "See mcp/.env.example.";
    }

    /**
     * 配置绑定完成后记录凭据状态：空/弱密码仅提示，由 Sim* 工具在调用时 fail-fast。
     */
    @Override
    public void afterPropertiesSet() {
        if (!hasUsableCredentials()) {
            log.warn("【配置提醒】FH_SIM_PASSWORD 未配置或为公知弱密码，Sim* 模拟器工具将被拒绝调用。"
                    + "如需使用，请通过 FH_SIM_PASSWORD 显式配置与模拟器 config.yaml 一致的凭据。");
        }
    }
}
