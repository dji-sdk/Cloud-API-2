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

package com.dji.fh.kernel.spi.manifest;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleAiTest {

    @Test
    void constructor_shouldStoreFields() {
        ModuleAi ai = new ModuleAi(
                "device management",
                List.of("DeviceRepository", "EventListener"),
                List.of(new ModuleAi.Example("add new device type", "implement DeviceAdapter"))
        );

        assertThat(ai.capability()).isEqualTo("device management");
        assertThat(ai.typicalExtensionPoints()).containsExactly("DeviceRepository", "EventListener");
        assertThat(ai.examples()).hasSize(1);
        assertThat(ai.examples().get(0).description()).isEqualTo("add new device type");
        assertThat(ai.examples().get(0).mechanism()).isEqualTo("implement DeviceAdapter");
    }

    @Test
    void constructor_shouldDefaultNullListsToEmpty() {
        ModuleAi ai = new ModuleAi("capability", null, null);

        assertThat(ai.typicalExtensionPoints()).isEmpty();
        assertThat(ai.examples()).isEmpty();
    }
}
