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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SimulatorProperties} 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class SimulatorPropertiesTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(SimulatorProperties.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    void defaults_shouldMatchSimulatorConfigYaml() {
        SimulatorProperties properties = new SimulatorProperties();

        assertEquals("http://localhost:8000", properties.getBaseUrl());
        assertEquals("admin", properties.getUsername());
        // F-02 整改：密码无默认值（为空），未配置时视为无可用凭据
        assertEquals("", properties.getPassword());
        assertTrue(!properties.hasUsableCredentials());
    }

    @Test
    @DisplayName("密码未配置时记录 WARN 且凭据不可用（F-02）")
    void afterPropertiesSet_shouldWarn_whenPasswordIsDefault() {
        SimulatorProperties properties = new SimulatorProperties();

        properties.afterPropertiesSet();

        assertEquals(1, appender.list.size());
        assertEquals(Level.WARN, appender.list.get(0).getLevel());
    }

    @Test
    @DisplayName("密码为公知弱密码（模拟器默认值）时凭据不可用（F-02）")
    void hasUsableCredentials_shouldBeFalse_whenPasswordIsWeak() {
        SimulatorProperties properties = new SimulatorProperties();

        properties.setPassword("Password@");

        assertTrue(!properties.hasUsableCredentials());
    }

    @Test
    @DisplayName("凭据不可用时提示信息包含 FH_SIM_PASSWORD 配置指引（F-02）")
    void credentialsHint_shouldMentionEnvVar() {
        SimulatorProperties properties = new SimulatorProperties();

        String hint = properties.credentialsHint();

        assertTrue(hint.contains("FH_SIM_PASSWORD"));
    }

    @Test
    @DisplayName("密码已显式配置为非默认值时不应记录 WARN")
    void afterPropertiesSet_shouldNotWarn_whenPasswordIsCustom() {
        SimulatorProperties properties = new SimulatorProperties();
        properties.setPassword("a-strong-custom-secret");

        properties.afterPropertiesSet();

        assertTrue(appender.list.isEmpty());
    }

    @Test
    void settersAndGetters_shouldRoundTrip() {
        SimulatorProperties properties = new SimulatorProperties();

        properties.setBaseUrl("http://sim.internal:9000");
        properties.setUsername("tester");
        properties.setPassword("secret");

        assertEquals("http://sim.internal:9000", properties.getBaseUrl());
        assertEquals("tester", properties.getUsername());
        assertEquals("secret", properties.getPassword());
    }
}
