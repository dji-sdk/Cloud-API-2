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

package com.dji.flighthub.mcp.tools.simulator;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.simulator.SimulatorApiException;
import com.dji.flighthub.mcp.simulator.SimulatorProperties;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Sim* Tool 公共基类：封装 SimulatorClient 调用的异常处理与错误提示映射。
 *
 * <p>与 {@code Fh*Tool} 不同，Sim* Tool 不调用 {@code requireSession()}——模拟器有独立的
 * cookie 鉴权体系，与 FlightHub PaaS SDK 的 access token session 无关。
 *
 * <p>凭据治理（安全审计 F-02 整改）：模拟器密码不再有公知弱密码 fallback。
 * 未显式配置 FH_SIM_PASSWORD（或仍为公知弱值）时，所有 Sim* 工具
 * 直接拒绝调用（fail-fast），不发起登录请求。
 *
 * @author DJI FlightHub SDK Team
 */
public abstract class AbstractSimTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(AbstractSimTool.class);

    private final FlightHubProperties flightHubProperties;

    private final SimulatorProperties simulatorProperties;

    /**
     * 构造 Sim* Tool 基类。
     *
     * @param sessionManager      session 管理器（仅用于满足 {@code AbstractMcpTool} 基类构造，
     *                             Sim* Tool 自身不调用 {@code requireSession()}）
     * @param flightHubProperties FlightHub SDK 全局配置，用于读取 FH_MAX_SECURITY_LEVEL 上限
     * @param simulatorProperties 模拟器连接配置（F-02：凭据 fail-fast 判定）
     */
    protected AbstractSimTool(SessionManager sessionManager, FlightHubProperties flightHubProperties,
                              SimulatorProperties simulatorProperties) {
        super(sessionManager);
        this.flightHubProperties = flightHubProperties;
        this.simulatorProperties = simulatorProperties;
    }

    /**
     * 禁止 Sim* Tool 调用基类的 session 版安全等级检查——Sim* Tool 没有 session，
     * 调用此方法必定是误用。应使用 {@link #enforceSimSecurityLevel(SecurityLevel)}。
     *
     * @param required 安全等级
     * @return 永不返回
     * @throws UnsupportedOperationException 始终抛出
     */
    @Override
    protected Map<String, Object> enforceSecurityLevel(SecurityLevel required) {
        throw new UnsupportedOperationException(
                "Sim* Tool 不使用 session，请调用 enforceSimSecurityLevel() 代替");
    }

    /**
     * 检查 FH_MAX_SECURITY_LEVEL 是否允许执行指定安全等级的操作（硬拦截）。
     * Sim* Tool 无 session，因此直接读取 {@link FlightHubProperties} 而非
     * session 的 max level（与 {@link AbstractMcpTool#enforceSecurityLevel} 的
     * session 版本对应，但校验源不同）。
     *
     * @param required 本次操作要求的安全等级
     * @return null=通过；非 null=硬拒绝响应
     */
    protected Map<String, Object> enforceSimSecurityLevel(SecurityLevel required) {
        int maxLevel = flightHubProperties.getSecurity().getMaxLevel();
        if (!required.isAllowedBy(maxLevel)) {
            return errorResult(
                    "Operation denied: insufficient security level",
                    String.format("This operation requires %s, but FH_MAX_SECURITY_LEVEL allows max %s.",
                            required.getLabel(), SecurityLevel.labelFor(maxLevel)));
        }
        return null;
    }

    /**
     * 从参数中提取必填的设备 SN。
     *
     * @param args 请求参数
     * @return SN，缺失或为空白时返回 null
     */
    protected String requireSn(Map<String, Object> args) {
        String sn = getStringArg(args, "sn", null);
        return (sn == null || sn.isBlank()) ? null : sn;
    }

    /**
     * 调用模拟器 API 并统一处理异常。
     *
     * <p>凭据 fail-fast（F-02 整改）：无可用凭据时不发起请求，直接返回配置错误。
     *
     * @param call           实际调用逻辑
     * @param successMessage 成功时的提示信息
     * @return 成功时的 {@code successResult}，失败时的 {@code errorResult}
     */
    protected Map<String, Object> callSimulator(SimulatorCall call, String successMessage) {
        if (!simulatorProperties.hasUsableCredentials()) {
            log.warn("Sim* tool call rejected: FH_SIM_PASSWORD not configured (security audit F-02)");
            return errorResult("Simulator credentials not configured", simulatorProperties.credentialsHint());
        }
        try {
            Map<String, Object> data = call.call();
            return successResult(data, successMessage);
        } catch (SimulatorApiException e) {
            return errorResult(e.getMessage(), buildSuggestion(e));
        }
    }

    private String buildSuggestion(SimulatorApiException e) {
        if (e.getHttpStatus() == 0) {
            return "Check that pf-device-simulator-service is running "
                    + "(e.g. `make run` in the simulator repo) and that FH_SIM_BASE_URL is correct.";
        }
        if (e.getHttpStatus() == 401) {
            return "Check that FH_SIM_USERNAME/FH_SIM_PASSWORD match the simulator's config.yaml "
                    + "auth.default_username/default_password.";
        }
        if (e.getHttpStatus() >= 500) {
            log.warn("Simulator returned a server error: {}", e.getMessage());
            return "The simulator returned a server error. Check the simulator process logs for details.";
        }
        return "Check the request parameters against the tool's schema. Simulator response: " + e.getMessage();
    }

    /**
     * 模拟器调用的函数式接口，供 lambda 传入具体的 SimulatorClient 方法调用。
     */
    @FunctionalInterface
    protected interface SimulatorCall {
        Map<String, Object> call() throws SimulatorApiException;
    }
}
