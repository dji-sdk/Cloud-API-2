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

/**
 * 导入航线响应体。
 *
 * <p>对齐前端 {@code web/src/api/wayline.ts} 的响应契约：
 * {@code { "wayline_uuid": "xxx" }}
 *
 * @param waylineUuid 导入后生成的航线 UUID
 * @author DJI DevKit Team
 */
public record ImportWaylineResponse(
        @JsonProperty("wayline_uuid") String waylineUuid) {
}
