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
 * 媒体文件夹 DTO.
 *
 * <p>用于重建任务创建时选择数据源。文件夹 id 来自媒体文件 {@code filePath} 字段，
 * 名称通过 {@code getMediaFileDetailById} 获取。提交重建使用数字 {@code folderId}
 * （SDK {@code setFolderIds}），folderUuid 仅在建过模的文件夹上存在，可为 null。
 *
 * @param folderId   文件夹 ID（提交重建时使用）
 * @param folderUuid 文件夹 UUID（仅建过模的文件夹存在，可为 null）
 * @param folderName 文件夹名称
 */
public record FolderDto(
        int folderId,
        String folderUuid,
        String folderName
) { }
