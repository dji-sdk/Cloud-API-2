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
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 出站凭据（连接真实 FlightHub 服务端）弱占位值提醒 — 安全审计整改（P2）。
 *
 * <p>与 {@link StartupAuthValidator} 不同，本校验器只做 WARN 级提醒、不阻断启动：
 * {@code FH_SERVER_ADDRESS}/{@code FH_ACCESS_TOKEN}/{@code FH_WORKSPACE_UUID}
 * 在 Mock 模式下本就允许保持示例占位值，fail-fast 会破坏合法的本地/演示场景，
 * 因此仅在检测到 docker-compose.yml 默认占位值时输出提醒，帮助 ISV 在切换到
 * 真实环境前发现"忘记替换默认值"的配置疏漏。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class OutboundCredentialsValidator implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(OutboundCredentialsValidator.class);

    /** docker-compose.yml 与 .env.example 中的示例占位值黑名单（两处占位符写法不同，均需覆盖）。 */
    private static final Set<String> WEAK_SERVER_ADDRESSES = Set.of("https://your-server.com");
    private static final Set<String> WEAK_ACCESS_TOKENS = Set.of(
            "your-token", "your-workspace-access-token", "changeme", "change-me", "placeholder");
    private static final Set<String> WEAK_WORKSPACE_UUIDS = Set.of(
            "your-uuid", "your-workspace-binding-uuid", "changeme", "change-me", "placeholder");

    private final FlightHubProperties properties;

    /**
     * 构造函数。
     *
     * @param properties FlightHub 配置属性
     */
    public OutboundCredentialsValidator(FlightHubProperties properties) {
        this.properties = properties;
    }

    /**
     * 在所有单例 Bean 初始化完成后检查出站凭据是否仍为示例占位值，仅记录 WARN 日志。
     */
    @Override
    public void afterSingletonsInstantiated() {
        warnIfWeak("FH_SERVER_ADDRESS", properties.getServer().getAddress(), WEAK_SERVER_ADDRESSES);
        warnIfWeak("FH_ACCESS_TOKEN", properties.getAuth().getAccessToken(), WEAK_ACCESS_TOKENS);
        warnIfWeak("FH_WORKSPACE_UUID", properties.getAuth().getWorkspaceUuid(), WEAK_WORKSPACE_UUIDS);
    }

    private void warnIfWeak(String varName, String value, Set<String> weakValues) {
        if (value != null && weakValues.contains(value)) {
            log.warn("【配置提醒】event=weak_outbound_credential var={} value='{}' "
                    + "若已连接真实 FlightHub 服务端，请检查是否忘记替换；Mock 模式下可忽略此提醒。"
                    + "（该字段用于运维日志告警系统按 event=weak_outbound_credential 匹配主动监控）",
                    varName, value);
        }
    }
}
