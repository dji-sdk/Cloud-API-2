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
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DockOsdUpdateEventTest {

    @Test
    void constructor_shouldThrowNPE_whenDeviceSnIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DockOsdUpdateEvent("test-source", null, new DockOsdData(Map.of())))
                .withMessageContaining("deviceSn");
    }

    @Test
    void constructor_shouldThrowNPE_whenDockOsdIsNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> new DockOsdUpdateEvent("test-source", "DOCK-1", null))
                .withMessageContaining("dockOsd");
    }
}
