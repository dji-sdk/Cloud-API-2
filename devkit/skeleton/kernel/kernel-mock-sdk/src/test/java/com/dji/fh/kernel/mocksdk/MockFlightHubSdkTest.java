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

package com.dji.fh.kernel.mocksdk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MockFlightHubSdkTest {

    private MockFlightHubSdk sdk;

    @BeforeEach
    void setUp() {
        sdk = new MockFlightHubSdk();
    }

    @Test
    void listenDeviceOnline_shouldReturnPositiveId_whenRegistered() {
        long id = sdk.listenDeviceOnline((data, err) -> {
        });

        assertThat(id).isPositive();
    }

    @Test
    void fireDeviceOnline_shouldInvokeRegisteredCallback() {
        AtomicInteger count = new AtomicInteger();
        sdk.listenDeviceOnline((data, err) -> count.incrementAndGet());

        sdk.fireDeviceOnline("SN-1", Map.of("model", "drone"));

        assertThat(count.get()).isEqualTo(1);
    }

    @Test
    void cancelListenDeviceOnline_shouldStopCallback_whenCancelled() {
        AtomicInteger count = new AtomicInteger();
        long id = sdk.listenDeviceOnline((data, err) -> count.incrementAndGet());

        sdk.cancelListenDeviceOnline(id);
        sdk.fireDeviceOnline("SN-1", Map.of());

        assertThat(count.get()).isZero();
    }

    @Test
    void fireOsd_shouldOnlyInvokeMatchingSn_whenMultipleListenersRegistered() {
        Map<String, Integer> hits = new HashMap<>();
        sdk.listenOsd("SN-1", (data, err) -> hits.merge("SN-1", 1, Integer::sum));
        sdk.listenOsd("SN-2", (data, err) -> hits.merge("SN-2", 1, Integer::sum));

        sdk.fireOsd("SN-1", Map.of("battery_percent", 80));

        assertThat(hits).containsExactly(Map.entry("SN-1", 1));
    }

    @Test
    void fireDeviceOnline_shouldNotPropagateException_whenCallbackThrows() {
        sdk.listenDeviceOnline((data, err) -> {
            throw new RuntimeException("listener boom");
        });
        AtomicInteger laterCount = new AtomicInteger();
        sdk.listenDeviceOnline((data, err) -> laterCount.incrementAndGet());

        // 不抛 + 后续 listener 仍被调用
        sdk.fireDeviceOnline("SN-1", Map.of());

        assertThat(laterCount.get()).isEqualTo(1);
    }
}
