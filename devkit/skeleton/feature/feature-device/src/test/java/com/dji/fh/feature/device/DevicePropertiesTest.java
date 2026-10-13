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

package com.dji.fh.feature.device;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DevicePropertiesTest {

    /** 测试用显式存活超时，避免依赖默认值 */
    private static final Duration TEST_LIVENESS_TIMEOUT = Duration.ofMinutes(5);

    @Nested
    class HmsMaxPerDeviceTests {

        @Test
        void constructor_shouldDefault50_whenHmsMaxNotPositive() {
            DeviceProperties props = new DeviceProperties(0, true, TEST_LIVENESS_TIMEOUT, null);
            assertThat(props.hmsMaxPerDevice()).isEqualTo(50);
        }

        @Test
        void constructor_shouldUseProvidedValue_whenPositive() {
            DeviceProperties props = new DeviceProperties(100, true, TEST_LIVENESS_TIMEOUT, null);
            assertThat(props.hmsMaxPerDevice()).isEqualTo(100);
        }
    }

    @Nested
    class LivenessTimeoutTests {

        @Test
        void constructor_shouldUseProvidedValue_whenPositive() {
            DeviceProperties props = new DeviceProperties(50, true, Duration.ofSeconds(30), null);
            assertThat(props.livenessTimeout()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        void constructor_shouldDefault5Minutes_whenNull() {
            DeviceProperties props = new DeviceProperties(50, true, null, null);
            assertThat(props.livenessTimeout()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        void constructor_shouldDefault5Minutes_whenNegative() {
            DeviceProperties props = new DeviceProperties(50, true, Duration.ofMinutes(-1), null);
            assertThat(props.livenessTimeout()).isEqualTo(Duration.ofMinutes(5));
        }

        @Test
        void constructor_shouldKeepZero_whenZeroGiven() {
            // ZERO 是"关闭兜底"的合法取值，不得被兜底逻辑改写成默认值
            DeviceProperties props = new DeviceProperties(50, true, Duration.ZERO, null);
            assertThat(props.livenessTimeout()).isZero();
        }
    }

    @Nested
    class ReconcileConfirmWindowTests {

        @Test
        void constructor_shouldUseProvidedValue_whenPositive() {
            DeviceProperties props = new DeviceProperties(50, true, TEST_LIVENESS_TIMEOUT, Duration.ofSeconds(10));
            assertThat(props.reconcileConfirmWindow()).isEqualTo(Duration.ofSeconds(10));
        }

        @Test
        void constructor_shouldDefault30Seconds_whenNull() {
            DeviceProperties props = new DeviceProperties(50, true, TEST_LIVENESS_TIMEOUT, null);
            assertThat(props.reconcileConfirmWindow()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        void constructor_shouldDefault30Seconds_whenNegative() {
            DeviceProperties props = new DeviceProperties(50, true, TEST_LIVENESS_TIMEOUT, Duration.ofSeconds(-1));
            assertThat(props.reconcileConfirmWindow()).isEqualTo(Duration.ofSeconds(30));
        }

        @Test
        void constructor_shouldKeepZero_whenZeroGiven() {
            DeviceProperties props = new DeviceProperties(50, true, TEST_LIVENESS_TIMEOUT, Duration.ZERO);
            assertThat(props.reconcileConfirmWindow()).isZero();
        }
    }
}
