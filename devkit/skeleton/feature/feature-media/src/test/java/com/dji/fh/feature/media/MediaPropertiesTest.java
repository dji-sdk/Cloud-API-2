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

package com.dji.fh.feature.media;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class MediaPropertiesTest {

    @Test
    void defaults_shouldApply_whenValuesZeroOrNegative() {
        MediaProperties props = new MediaProperties(0, -1, true, "");
        assertThat(props.defaultPageSize()).isEqualTo(50);
        assertThat(props.maxPageSize()).isEqualTo(500);
    }

    @Test
    void customValues_shouldBePreserved() {
        MediaProperties props = new MediaProperties(20, 100, false, "");
        assertThat(props.defaultPageSize()).isEqualTo(20);
        assertThat(props.maxPageSize()).isEqualTo(100);
        assertThat(props.enabled()).isFalse();
    }

    @Test
    void proxyTrustedCertPath_shouldDefaultToEmpty_whenNull() {
        MediaProperties props = new MediaProperties(50, 500, true, null);
        assertThat(props.proxyTrustedCertPath()).isEmpty();
    }

    @Test
    void proxyTrustedCertPath_shouldBePreserved_whenExplicitlySet() {
        MediaProperties props = new MediaProperties(50, 500, true, "/etc/fh/trusted.pem");
        assertThat(props.proxyTrustedCertPath()).isEqualTo("/etc/fh/trusted.pem");
    }
}
