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

package com.dji.fh.kernel.realsdk;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

/**
 * RealSdkProperties 单元测试。
 *
 * @author DJI DevKit Team
 */
class RealSdkPropertiesTest {

    @Test
    void constructor_shouldBindAllFields_whenAllProvided() {
        RealSdkProperties props = new RealSdkProperties(
                "token-123",
                "api.example.com",
                "443",
                "wss://api.example.com/ws",
                "debug",
                Duration.ofSeconds(15),
                500L,
                Duration.ofSeconds(8),
                Duration.ofSeconds(25),
                5,
                "user_id",
                "your-auth-value",
                Duration.ofMinutes(3));

        assertThat(props.token()).isEqualTo("token-123");
        assertThat(props.address()).isEqualTo("api.example.com");
        assertThat(props.port()).isEqualTo("443");
        assertThat(props.notificationServerAddress()).isEqualTo("wss://api.example.com/ws");
        assertThat(props.logLevel()).isEqualTo("debug");
        assertThat(props.deviceListTimeout()).isEqualTo(Duration.ofSeconds(15));
        assertThat(props.osdIntervalMillis()).isEqualTo(500L);
        assertThat(props.osdSnapshotTtl()).isEqualTo(Duration.ofSeconds(8));
        assertThat(props.dockListenRetryDelay()).isEqualTo(Duration.ofSeconds(25));
        assertThat(props.dockListenMaxRetries()).isEqualTo(5);
        assertThat(props.httpAuthHeaderKey()).isEqualTo("user_id");
        assertThat(props.httpAuthHeaderValue()).isEqualTo("your-auth-value");
        assertThat(props.reconcileInterval()).isEqualTo(Duration.ofMinutes(3));
    }

    @Test
    void constructor_shouldFallbackDefault_whenLogLevelBlank() {
        RealSdkProperties props = new RealSdkProperties(
                "t", "h", "p", "wss://x", "  ", null, 0L, null, null, 0, null, null, null);

        assertThat(props.logLevel()).isEqualTo("info");
        assertThat(props.deviceListTimeout()).isEqualTo(Duration.ofSeconds(10));
        assertThat(props.osdIntervalMillis()).isEqualTo(1000L);
        assertThat(props.osdSnapshotTtl()).isEqualTo(Duration.ofSeconds(5));
        assertThat(props.dockListenRetryDelay()).isEqualTo(Duration.ofSeconds(20));
        assertThat(props.dockListenMaxRetries()).isEqualTo(3);
        assertThat(props.reconcileInterval()).isEqualTo(Duration.ofMinutes(2));
    }

    @Test
    void constructor_shouldFallbackDefault_whenLogLevelNull() {
        RealSdkProperties props = new RealSdkProperties(
                "t", "h", "p", "wss://x", null, null, -10L, null, null, -1, null, null, null);

        assertThat(props.logLevel()).isEqualTo("info");
        assertThat(props.osdIntervalMillis()).isEqualTo(1000L);
        assertThat(props.dockListenMaxRetries()).isEqualTo(3);
    }

    @Test
    void constructor_shouldFallbackOsdInterval_whenNonPositive() {
        RealSdkProperties props = new RealSdkProperties(
                "t", "h", "p", "wss://x", "info",
                Duration.ofSeconds(10), -5L, Duration.ofSeconds(5),
                Duration.ofSeconds(20), 3, null, null, null);

        assertThat(props.osdIntervalMillis()).isEqualTo(1000L);
    }
}
