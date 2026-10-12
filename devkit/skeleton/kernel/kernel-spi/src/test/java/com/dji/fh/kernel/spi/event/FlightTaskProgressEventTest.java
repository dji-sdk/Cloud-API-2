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

class FlightTaskProgressEventTest {

    @Test
    void constructor_shouldStoreAllFields() {
        FlightTaskProgressEvent event = new FlightTaskProgressEvent(
                "test-source", "DRONE-001", "task-uuid-123",
                FlightTaskStatus.RUNNING, 50);

        assertThat(event.source()).isEqualTo("test-source");
        assertThat(event.deviceSn()).isEqualTo("DRONE-001");
        assertThat(event.taskId()).isEqualTo("task-uuid-123");
        assertThat(event.status()).isEqualTo(FlightTaskStatus.RUNNING);
        assertThat(event.progress()).isEqualTo(50);
    }

    @Test
    void constructor_shouldAcceptBoundaryProgress() {
        FlightTaskProgressEvent event = new FlightTaskProgressEvent(
                "kernel", "DRONE-002", "task-456",
                FlightTaskStatus.COMPLETED, 100);

        assertThat(event.progress()).isEqualTo(100);
        assertThat(event.status()).isEqualTo(FlightTaskStatus.COMPLETED);
    }
}
