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
 * 模型文件 DTO.
 *
 * @param fileId             文件 ID
 * @param uuid               文件 UUID
 * @param name               模型名称
 * @param fileType           文件类型
 * @param subFileType        子文件类型
 * @param state              状态（processing / success / failed）
 * @param reconstructionType 重建类型（ortho_2d / mesh_3d / point_cloud / panorama）
 * @param thumbnailUrl       缩略图 URL（SDK 返回，可能为 null）
 * @param createdAt          创建时间（Unix 秒）
 */
public record ModelFileDto(
        long fileId,
        String uuid,
        String name,
        String fileType,
        String subFileType,
        String state,
        String reconstructionType,
        String thumbnailUrl,
        long createdAt
) { }
