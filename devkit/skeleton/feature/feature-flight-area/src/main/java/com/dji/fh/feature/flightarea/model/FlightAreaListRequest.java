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

package com.dji.fh.feature.flightarea.model;

/**
 * 飞行区域列表查询参数。
 *
 * @param type      类型筛选（可选）
 * @param status    状态筛选（可选）
 * @param name      名称模糊匹配（可选）
 * @param pageIndex 页号，从 1 开始（默认 1）
 * @param pageSize  每页大小（默认 50，最大 200）
 */
public record FlightAreaListRequest(
        FlightAreaType type,
        FlightAreaStatus status,
        String name,
        Integer pageIndex,
        Integer pageSize
) {
    /** 安全的 pageIndex，最小为 1。 */
    public int safePageIndex() {
        return (pageIndex == null || pageIndex < 1) ? 1 : pageIndex;
    }

    /** 安全的 pageSize，范围 1~200。 */
    public int safePageSize() {
        if (pageSize == null || pageSize < 1) {
            return 50;
        }
        return Math.min(pageSize, 200);
    }
}
