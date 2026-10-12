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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 批量下载航线响应体。
 *
 * <p>返回各航线的下载 URL，前端可据此逐个下载 KMZ 文件。
 *
 * @param downloads 各航线的下载信息列表
 * @author DJI DevKit Team
 */
public record BatchDownloadWaylinesResponse(
        List<DownloadItem> downloads) {

    /**
     * 单个航线的下载信息。
     *
     * @param waylineUuid 航线 UUID
     * @param waylineName 航线名称
     * @param url         KMZ 文件下载 URL
     * @param size        文件大小（字节）
     */
    public record DownloadItem(
            @JsonProperty("wayline_uuid") String waylineUuid,
            @JsonProperty("wayline_name") String waylineName,
            String url,
            int size) {
    }
}
