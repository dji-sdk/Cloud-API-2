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
import static org.assertj.core.api.Assertions.assertThatCode;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class WaylineServiceTest {

    private WaylineService service;

    @BeforeEach
    void setUp() {
        WaylineProperties props = new WaylineProperties(20, 100, true);
        service = new WaylineService(props);
    }

    @Test
    void listWaylines_shouldReturnEmptyList_withDefaults() {
        WaylineListResponse resp = service.listWaylines(1, 20, null, null, null);
        assertThat(resp.waylines()).isEmpty();
        assertThat(resp.total()).isZero();
        assertThat(resp.page()).isEqualTo(1);
        assertThat(resp.pageSize()).isEqualTo(20);
    }

    @Test
    void listWaylines_shouldAcceptFilters() {
        WaylineListResponse resp = service.listWaylines(1, 20, "Matrice 350 RTK", "Zenmuse H20T", "巡检");
        assertThat(resp.waylines()).isEmpty();
        assertThat(resp.total()).isZero();
    }

    @Test
    void listWaylines_shouldNormalizePage_whenZeroOrNegative() {
        WaylineListResponse resp = service.listWaylines(0, 20, null, null, null);
        assertThat(resp.page()).isEqualTo(1);
    }

    @Test
    void clampPageSize_shouldUseDefault_whenZero() {
        WaylineListResponse resp = service.listWaylines(1, 0, null, null, null);
        assertThat(resp.pageSize()).isEqualTo(20);
    }

    @Test
    void clampPageSize_shouldUseDefault_whenNegative() {
        WaylineListResponse resp = service.listWaylines(1, -1, null, null, null);
        assertThat(resp.pageSize()).isEqualTo(20);
    }

    @Test
    void clampPageSize_shouldCapAtMax() {
        WaylineListResponse resp = service.listWaylines(1, 9999, null, null, null);
        assertThat(resp.pageSize()).isEqualTo(100);
    }

    @Test
    void batchDeleteWaylines_shouldNotThrow_withDefaultImpl() {
        assertThatCode(() -> service.batchDeleteWaylines(List.of("uuid-1", "uuid-2")))
                .doesNotThrowAnyException();
    }

    @Test
    void batchDownloadWaylines_shouldReturnEmptyDownloads_withDefaultImpl() {
        BatchDownloadWaylinesResponse resp = service.batchDownloadWaylines(List.of("uuid-1"));
        assertThat(resp).isNotNull();
        assertThat(resp.downloads()).isEmpty();
    }

    @Test
    void importWayline_shouldReturnMockUuid_withDefaultImpl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.kmz", "application/octet-stream", new byte[]{1, 2, 3});
        ImportWaylineResponse resp = service.importWayline(file, "测试航线");
        assertThat(resp).isNotNull();
        assertThat(resp.waylineUuid()).startsWith("mock-");
    }

    @Test
    void waylineExists_shouldReturnTrue_withDefaultImpl() {
        assertThat(service.waylineExists("any-uuid")).isTrue();
    }

    @Test
    void waylineExists_shouldReturnTrue_forNull_withDefaultImpl() {
        assertThat(service.waylineExists(null)).isTrue();
    }
}
