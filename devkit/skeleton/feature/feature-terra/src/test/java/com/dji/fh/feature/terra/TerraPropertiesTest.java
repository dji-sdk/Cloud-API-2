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

package com.dji.fh.feature.terra;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TerraProperties record 测试。
 *
 * @author DJI DevKit Team
 */
class TerraPropertiesTest {

    @Test
    void shouldCreateWithDefaultValues() {
        TerraProperties properties = new TerraProperties(true, false);

        assertThat(properties.enabled()).isTrue();
        assertThat(properties.mock()).isFalse();
    }

    @Test
    void shouldCreateWithMockEnabled() {
        TerraProperties properties = new TerraProperties(true, true);

        assertThat(properties.enabled()).isTrue();
        assertThat(properties.mock()).isTrue();
    }

    @Test
    void shouldCreateWithFeatureDisabled() {
        TerraProperties properties = new TerraProperties(false, false);

        assertThat(properties.enabled()).isFalse();
        assertThat(properties.mock()).isFalse();
    }

    @Test
    void shouldSupportEquals() {
        TerraProperties a = new TerraProperties(true, true);
        TerraProperties b = new TerraProperties(true, true);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void shouldSupportToString() {
        TerraProperties properties = new TerraProperties(true, false);

        assertThat(properties.toString()).contains("enabled=true");
        assertThat(properties.toString()).contains("mock=false");
    }
}
