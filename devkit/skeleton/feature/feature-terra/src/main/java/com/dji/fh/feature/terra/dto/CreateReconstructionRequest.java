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

import java.util.List;

/**
 * 创建重建任务请求体.
 *
 * <p>{@code folderIds} 与 {@code folderUuids} 二选一：优先使用数字文件夹 id
 * （对应 SDK {@code setFolderIds}），兼容旧版 UUID（{@code setFolderUuids}）。
 *
 * @param name               任务名称
 * @param folderIds          媒体文件夹 id 列表（数字，优先使用）
 * @param folderUuids        媒体文件夹 UUID 列表（兼容旧版，folderIds 为空时使用）
 * @param reconstructionType 重建类型：ortho_2d / mesh_3d / point_cloud
 * @param wkt                可选 WKT 范围
 */
public record CreateReconstructionRequest(
        String name,
        List<Integer> folderIds,
        List<String> folderUuids,
        String reconstructionType,
        String wkt
) {
    /**
     * 兼容旧版四参构造（folderIds 为空）.
     *
     * @param name               任务名称
     * @param folderUuids        媒体文件夹 UUID 列表
     * @param reconstructionType 重建类型
     * @param wkt                可选 WKT 范围
     */
    public CreateReconstructionRequest(
            final String name,
            final List<String> folderUuids,
            final String reconstructionType,
            final String wkt) {
        this(name, List.of(), folderUuids, reconstructionType, wkt);
    }
}
