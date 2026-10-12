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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MediaServiceTest {

    private MediaService service;

    @BeforeEach
    void setUp() {
        MediaProperties props = new MediaProperties(50, 500, true, "");
        service = new MediaService(props);
    }

    @Test
    void listFiles_shouldReturnEmptyList_withDefaults() {
        MediaListResponse resp = service.listFiles(null, "all", null, null, 0, 50);
        assertThat(resp.files()).isEmpty();
        assertThat(resp.totalCount()).isZero();
        assertThat(resp.hasMore()).isFalse();
    }

    @Test
    void listFiles_shouldReturnEmptyList_withPhotoFilter() {
        MediaListResponse resp = service.listFiles(null, "photo", null, null, 0, 50);
        assertThat(resp.files()).isEmpty();
    }

    @Test
    void listFiles_shouldReturnEmptyList_withVideoFilter() {
        MediaListResponse resp = service.listFiles(null, "video", null, null, 0, 50);
        assertThat(resp.files()).isEmpty();
    }

    @Test
    void listFiles_shouldRespectPageSize() {
        MediaListResponse resp = service.listFiles(null, "all", null, null, 0, 2);
        assertThat(resp.pageSize()).isEqualTo(2);
        assertThat(resp.files()).isEmpty();
    }

    @Test
    void listFiles_shouldAcceptDeviceSn() {
        MediaListResponse resp = service.listFiles("DRONE-X", "all", null, null, 0, 50);
        assertThat(resp.files()).isEmpty();
    }

    @Test
    void getDownloadUrl_shouldReturnEmptyUrl() {
        DownloadUrlResponse resp = service.getDownloadUrl(10001L);
        assertThat(resp.fileId()).isEqualTo(10001L);
        assertThat(resp.downloadUrl()).isEmpty();
        assertThat(resp.expiresIn()).isZero();
    }

    @Test
    void batchDelete_shouldReturnZero() {
        BatchDeleteResponse resp = service.batchDelete(java.util.List.of(1L, 2L, 3L));
        assertThat(resp.deletedCount()).isZero();
    }

    @Test
    void clampPageSize_shouldUseDefault_whenZero() {
        MediaListResponse resp = service.listFiles(null, "all", null, null, 0, 0);
        assertThat(resp.pageSize()).isEqualTo(50);
    }

    @Test
    void clampPageSize_shouldCapAtMax() {
        MediaListResponse resp = service.listFiles(null, "all", null, null, 0, 9999);
        assertThat(resp.pageSize()).isEqualTo(500);
    }
}
