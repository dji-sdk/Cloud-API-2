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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.dji.flighthub.mcp.config.FlightHubProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OutboundCredentialsValidator} 单元测试 — 安全审计整改（J-XX / CI-07，P2）。
 *
 * <p>覆盖出站凭据弱占位值 WARN 提醒（非阻断）：
 * <ul>
 *   <li>示例占位值 → 记录 WARN 日志，不抛异常</li>
 *   <li>正常值 → 不记录 WARN 日志</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
class OutboundCredentialsValidatorTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(OutboundCredentialsValidator.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    @DisplayName("三项凭据均为示例占位值时应各记录一条 WARN 且不抛异常")
    void afterSingletonsInstantiated_shouldWarn_whenAllPlaceholders() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getServer().setAddress("https://your-server.com");
        properties.getAuth().setAccessToken("your-token");
        properties.getAuth().setWorkspaceUuid("your-uuid");
        OutboundCredentialsValidator validator = new OutboundCredentialsValidator(properties);

        assertDoesNotThrow(validator::afterSingletonsInstantiated,
                "出站凭据校验必须是 WARN-only，不能中止启动");

        assertEquals(3, appender.list.size(), "三项均为占位值应各记录一条 WARN");
        assertTrue(appender.list.stream().allMatch(event -> event.getLevel() == Level.WARN));
        assertTrue(appender.list.stream().allMatch(event ->
                        event.getFormattedMessage().contains("event=weak_outbound_credential")),
                "WARN 日志必须包含结构化字段 event=weak_outbound_credential，便于运维告警系统匹配");
    }

    @Test
    @DisplayName(".env.example 实际占位符写法（your-workspace-access-token/your-workspace-binding-uuid）也应各记录一条 WARN")
    void afterSingletonsInstantiated_shouldWarn_whenEnvExamplePlaceholders() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getAuth().setAccessToken("your-workspace-access-token");
        properties.getAuth().setWorkspaceUuid("your-workspace-binding-uuid");
        OutboundCredentialsValidator validator = new OutboundCredentialsValidator(properties);

        assertDoesNotThrow(validator::afterSingletonsInstantiated);

        assertEquals(2, appender.list.size(), ".env.example 中的实际占位符也应被识别为弱值");
        assertTrue(appender.list.stream().allMatch(event -> event.getLevel() == Level.WARN));
    }

    @Test
    @DisplayName("三项凭据均为正常值时不应记录任何 WARN")
    void afterSingletonsInstantiated_shouldNotWarn_whenValuesAreReal() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getServer().setAddress("https://flighthub.example.com");
        properties.getAuth().setAccessToken("a1b2c3d4e5f6");
        properties.getAuth().setWorkspaceUuid("11111111-2222-3333-4444-555555555555");
        OutboundCredentialsValidator validator = new OutboundCredentialsValidator(properties);

        assertDoesNotThrow(validator::afterSingletonsInstantiated);

        assertEquals(0, appender.list.size(), "正常凭据不应触发任何提醒");
    }

    @Test
    @DisplayName("值为 null 时不应记录 WARN 也不应抛异常")
    void afterSingletonsInstantiated_shouldNotWarn_whenValuesNull() {
        FlightHubProperties properties = new FlightHubProperties();

        OutboundCredentialsValidator validator = new OutboundCredentialsValidator(properties);

        assertDoesNotThrow(validator::afterSingletonsInstantiated);
        assertEquals(0, appender.list.size(), "null 值不应触发提醒（Mock 模式常见场景）");
    }
}
