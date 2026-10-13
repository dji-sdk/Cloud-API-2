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

import java.util.List;

/**
 * 媒体文件列表分页响应。
 *
 * @param files      文件列表
 * @param totalCount 总数
 * @param startIndex 当前游标位置
 * @param pageSize   每页大小
 * @param hasMore    是否还有更多
 * @author DJI DevKit Team
 */
public record MediaListResponse(
        List<MediaFileInfo> files,
        int totalCount,
        int startIndex,
        int pageSize,
        boolean hasMore
) {}
