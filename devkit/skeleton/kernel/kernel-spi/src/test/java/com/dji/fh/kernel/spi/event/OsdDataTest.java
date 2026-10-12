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
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class OsdDataTest {

    @Test
    void constructor_shouldAcceptMapWithNullValues_whenSparsePayload() {
        Map<String, Object> sparse = new HashMap<>();
        sparse.put("battery_percent", 85);
        sparse.put("altitude", null);

        assertThatCode(() -> new OsdData(sparse)).doesNotThrowAnyException();
    }

    @Test
    void get_shouldReturnEmpty_whenValueIsNull() {
        Map<String, Object> sparse = new HashMap<>();
        sparse.put("altitude", null);
        OsdData data = new OsdData(sparse);

        assertThat(data.get("altitude")).isEmpty();
    }

    @Test
    void constructor_shouldDefaultToEmptyMap_whenAttributesNull() {
        OsdData data = new OsdData(null);

        assertThat(data.attributes()).isEmpty();
    }
}
