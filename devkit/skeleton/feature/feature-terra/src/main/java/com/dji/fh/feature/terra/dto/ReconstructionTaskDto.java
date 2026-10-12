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

package com.dji.fh.feature.terra.dto;

/**
 * 重建任务创建结果 DTO.
 *
 * @param id      任务 ID
 * @param uuid    任务 UUID
 * @param jobUuid 作业 UUID
 * @param status  初始状态（processing）
 */
public record ReconstructionTaskDto(
        long id,
        String uuid,
        String jobUuid,
        String status
) { }
