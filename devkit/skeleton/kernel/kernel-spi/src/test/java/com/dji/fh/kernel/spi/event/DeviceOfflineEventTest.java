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

package com.dji.fh.kernel.spi.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceOfflineEventTest {

    @Test
    void constructor_shouldStoreAllFields() {
        DeviceOfflineEvent event = new DeviceOfflineEvent(
                "test-source", "DRONE-SN-001", DeviceType.DRONE, "timeout");

        assertThat(event.source()).isEqualTo("test-source");
        assertThat(event.deviceSn()).isEqualTo("DRONE-SN-001");
        assertThat(event.deviceType()).isEqualTo(DeviceType.DRONE);
        assertThat(event.reason()).isEqualTo("timeout");
    }

    @Test
    void reason_shouldAllowNull() {
        DeviceOfflineEvent event = new DeviceOfflineEvent(
                "kernel", "DOCK-SN-002", DeviceType.DOCK, null);

        assertThat(event.reason()).isNull();
    }
}
