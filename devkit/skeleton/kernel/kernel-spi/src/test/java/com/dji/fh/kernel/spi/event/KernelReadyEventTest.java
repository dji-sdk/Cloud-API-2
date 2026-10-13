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

class KernelReadyEventTest {

    @Test
    void constructor_shouldStoreAllFields() {
        KernelReadyEvent event = new KernelReadyEvent(
                "test-source", KernelReadyEvent.Mode.SDK, "1.0.0");

        assertThat(event.source()).isEqualTo("test-source");
        assertThat(event.mode()).isEqualTo(KernelReadyEvent.Mode.SDK);
        assertThat(event.version()).isEqualTo("1.0.0");
    }

    @Test
    void constructor_shouldWorkWithMockMode() {
        KernelReadyEvent event = new KernelReadyEvent(
                "kernel", KernelReadyEvent.Mode.MOCK, "0.1.0");

        assertThat(event.mode()).isEqualTo(KernelReadyEvent.Mode.MOCK);
        assertThat(event.version()).isEqualTo("0.1.0");
    }
}
