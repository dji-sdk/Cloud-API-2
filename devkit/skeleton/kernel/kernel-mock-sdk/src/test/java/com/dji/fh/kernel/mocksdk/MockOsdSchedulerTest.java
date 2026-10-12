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

import com.dji.fh.kernel.spi.event.DeviceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MockOsdSchedulerTest {

    private MockDeviceRegistry registry;
    private MockFlightHubSdk sdk;
    private MockOsdScheduler scheduler;

    @BeforeEach
    void setUp() {
        registry = new MockDeviceRegistry();
        sdk = new MockFlightHubSdk();
        scheduler = new MockOsdScheduler(sdk, registry);
    }

    @Test
    void tick_shouldPushOsd_toEachOnlineDevice() {
        registry.register("SN-1", DeviceType.DRONE, "drone");
        registry.markOnline("SN-1");
        AtomicInteger osdCount = new AtomicInteger();
        sdk.listenOsd("SN-1", (data, err) -> osdCount.incrementAndGet());

        scheduler.tick();

        assertThat(osdCount.get()).isEqualTo(1);
    }

    @Test
    void tick_shouldSkipOfflineDevices() {
        registry.register("SN-1", DeviceType.DRONE, "drone");
        // 不标记 online
        AtomicInteger osdCount = new AtomicInteger();
        sdk.listenOsd("SN-1", (data, err) -> osdCount.incrementAndGet());

        scheduler.tick();

        assertThat(osdCount.get()).isZero();
    }

    @Test
    void tick_shouldEmitOsdWithExpectedKeys() {
        registry.register("SN-1", DeviceType.DRONE, "drone");
        registry.markOnline("SN-1");
        AtomicInteger seen = new AtomicInteger();
        sdk.listenOsd("SN-1", (data, err) -> {
            assertThat(data).containsKeys("battery_percent", "altitude", "mode", "lat", "lng");
            seen.incrementAndGet();
        });

        scheduler.tick();

        assertThat(seen.get()).isEqualTo(1);
    }

    @Test
    void tick_shouldEmitLatLng_forDock() {
        registry.register("DOCK-1", DeviceType.DOCK, "dock");
        registry.markOnline("DOCK-1");
        AtomicInteger seen = new AtomicInteger();
        sdk.listenOsd("DOCK-1", (data, err) -> {
            assertThat(data).containsKeys("battery_percent", "lat", "lng");
            assertThat(((Number) data.get("lat")).doubleValue()).isNotZero();
            assertThat(((Number) data.get("lng")).doubleValue()).isNotZero();
            seen.incrementAndGet();
        });

        scheduler.tick();

        assertThat(seen.get()).isEqualTo(1);
    }

    @Test
    void tick_shouldAssignStableCity_forSameSn() {
        registry.register("SN-STABLE", DeviceType.DOCK, "dock");
        registry.markOnline("SN-STABLE");
        double[] lats = new double[2];
        double[] lngs = new double[2];
        int[] idx = {0};

        sdk.listenOsd("SN-STABLE", (data, err) -> {
            int i = Math.min(idx[0], 1);
            lats[i] = ((Number) data.get("lat")).doubleValue();
            lngs[i] = ((Number) data.get("lng")).doubleValue();
            idx[0]++;
        });

        scheduler.tick();
        scheduler.tick();

        assertThat(lats[0]).isNotZero();
        assertThat(lats[0]).isEqualTo(lats[1]);
        assertThat(lngs[0]).isEqualTo(lngs[1]);
    }
}
