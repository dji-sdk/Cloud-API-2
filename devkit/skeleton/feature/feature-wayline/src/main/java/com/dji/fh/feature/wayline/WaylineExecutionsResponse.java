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

import java.util.List;

/**
 * 航线关联的飞行执行列表响应 DTO。
 *
 * <p>对齐前端 {@code web/src/types/reconstruction.ts} 中 {@code WaylineFolder.executions} 的数据需求。
 *
 * @param waylineUuid 航线 UUID
 * @param waylineName 航线名称
 * @param executions  飞行执行列表
 * @author DJI DevKit Team
 */
public record WaylineExecutionsResponse(
        String waylineUuid,
        String waylineName,
        List<WaylineExecutionInfo> executions
) {}
