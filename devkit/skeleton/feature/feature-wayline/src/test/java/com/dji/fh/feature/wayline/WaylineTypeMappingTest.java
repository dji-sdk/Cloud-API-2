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

package com.dji.fh.feature.wayline;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WaylineTypeMappingTest {

    @Test
    void labelOf_shouldReturnWaypointLabel_whenCode0() {
        assertThat(WaylineTypeMapping.labelOf(0)).isEqualTo("航点航线");
    }

    @Test
    void labelOf_shouldReturnAreaLabel_whenCode1() {
        assertThat(WaylineTypeMapping.labelOf(1)).isEqualTo("面状航线");
    }

    @Test
    void labelOf_shouldReturnFacadeLabel_whenCode2() {
        assertThat(WaylineTypeMapping.labelOf(2)).isEqualTo("斜面航线");
    }

    @Test
    void labelOf_shouldReturnSolidLabel_whenCode3() {
        assertThat(WaylineTypeMapping.labelOf(3)).isEqualTo("几何体航线");
    }

    @Test
    void labelOf_shouldReturnMappingStripeLabel_whenCode4() {
        assertThat(WaylineTypeMapping.labelOf(4)).isEqualTo("带状航线");
    }

    @Test
    void labelOf_shouldReturnClosePhotographyLabel_whenCode7() {
        assertThat(WaylineTypeMapping.labelOf(7)).isEqualTo("贴近摄影航线");
    }

    @Test
    void labelOf_shouldReturnAiPatrolLabel_whenCode8() {
        assertThat(WaylineTypeMapping.labelOf(8)).isEqualTo("AI 巡逻航线");
    }

    @Test
    void labelOf_shouldReturnUnknownLabel_whenCodeUnknown() {
        assertThat(WaylineTypeMapping.labelOf(99)).isEqualTo(WaylineTypeMapping.UNKNOWN_LABEL);
    }

    @Test
    void labelOf_shouldReturnUnknownLabel_whenNull() {
        assertThat(WaylineTypeMapping.labelOf(null)).isEqualTo(WaylineTypeMapping.UNKNOWN_LABEL);
    }
}
