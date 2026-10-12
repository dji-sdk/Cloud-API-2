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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 航线库管理 feature 配置。
 *
 * @param defaultPageSize 默认每页数量（默认 20）
 * @param maxPageSize     最大每页数量（默认 100）
 * @param enabled         是否启用本 feature（默认 true）
 * @author DJI DevKit Team
 */
@ConfigurationProperties("fh.skeleton.feature.wayline")
public record WaylineProperties(
        int defaultPageSize,
        int maxPageSize,
        boolean enabled
) {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public WaylineProperties {
        if (defaultPageSize <= 0) {
            defaultPageSize = DEFAULT_PAGE_SIZE;
        }
        if (maxPageSize <= 0) {
            maxPageSize = MAX_PAGE_SIZE;
        }
    }
}
