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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MockDeviceRegistryTest {

    private MockDeviceRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new MockDeviceRegistry();
    }

    @Test
    void register_shouldStoreDevice_whenInvoked() {
        registry.register("SN-1", DeviceType.DRONE, "Mock Drone");

        Optional<MockDeviceRegistry.Device> got = registry.find("SN-1");

        assertThat(got).isPresent();
        assertThat(got.get().sn()).isEqualTo("SN-1");
        assertThat(got.get().type()).isEqualTo(DeviceType.DRONE);
        assertThat(got.get().model()).isEqualTo("Mock Drone");
        assertThat(got.get().online()).isFalse();
    }

    @Test
    void markOnline_shouldFlipFlag_whenDeviceRegistered() {
        registry.register("SN-1", DeviceType.DRONE, "Mock Drone");

        registry.markOnline("SN-1");

        assertThat(registry.find("SN-1").orElseThrow().online()).isTrue();
    }

    @Test
    void markOnline_shouldNoOp_whenDeviceUnknown() {
        // 不抛即可
        registry.markOnline("UNKNOWN");

        assertThat(registry.find("UNKNOWN")).isEmpty();
    }

    @Test
    void markOffline_shouldFlipFlag_whenDeviceOnline() {
        registry.register("SN-1", DeviceType.DRONE, "Mock Drone");
        registry.markOnline("SN-1");

        registry.markOffline("SN-1");

        assertThat(registry.find("SN-1").orElseThrow().online()).isFalse();
    }

    @Test
    void findAllOnline_shouldReturnOnlyOnlineDevices() {
        registry.register("SN-1", DeviceType.DRONE, "drone");
        registry.register("SN-2", DeviceType.DOCK, "dock");
        registry.markOnline("SN-1");

        assertThat(registry.findAllOnline())
                .extracting(MockDeviceRegistry.Device::sn)
                .containsExactly("SN-1");
    }
}
