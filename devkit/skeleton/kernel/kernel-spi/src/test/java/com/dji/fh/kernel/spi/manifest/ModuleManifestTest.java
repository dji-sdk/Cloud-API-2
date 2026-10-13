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

class ModuleManifestTest {

    @Test
    void manifest_shouldExposeRequiredFields_whenConstructed() {
        ModuleManifest m = new ModuleManifest(
                "1.0",
                "feature-device",
                "1.0.0",
                "设备管理",
                "设备查询与状态管理",
                new ModuleMetadata("feature", "must", List.of("DJI"), null, null),
                new ModuleRequires(">=1.0.0,<2.0.0", List.of()),
                ModuleProvides.empty(),
                null,
                new ModuleToggle("fh.skeleton.feature.device.enabled", true)
        );

        assertThat(m.schemaVersion()).isEqualTo("1.0");
        assertThat(m.id()).isEqualTo("feature-device");
        assertThat(m.metadata().category()).isEqualTo("feature");
        assertThat(m.toggle().defaultValue()).isTrue();
    }

    @Test
    void provides_empty_shouldReturnFullyEmptyInstance() {
        ModuleProvides empty = ModuleProvides.empty();

        assertThat(empty.spi()).isEmpty();
        assertThat(empty.events()).isEmpty();
        assertThat(empty.consumes()).isEmpty();
        assertThat(empty.rest()).isEmpty();
        assertThat(empty.mcpTools()).isEmpty();
        assertThat(empty.uiSlots()).isEmpty();
        assertThat(empty.configurations()).isEmpty();
    }
}
