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

/**
 * 单次航线飞行执行信息 DTO。
 *
 * <p>每个执行对应一次实际飞行任务，包含该次飞行拍摄的照片数量。
 * 用于重建面板中按航线关联查看可用媒体数据。
 *
 * @param id         飞行执行 ID（任务 UUID，用于前端 :key 唯一标识）
 * @param name       执行显示名称
 * @param photoCount 本次执行拍摄的照片数量
 * @param createdAt  执行时间（Unix 秒）
 * @param folderId   媒体文件夹 id（数字，重建提交时使用；无媒体文件夹时为 null，前端禁用）
 * @author DJI DevKit Team
 */
public record WaylineExecutionInfo(
        String id,
        String name,
        int photoCount,
        long createdAt,
        Integer folderId
) {}
