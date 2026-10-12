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
 * 模型详情 DTO — 用于 3D 查看器加载模型.
 *
 * @param fileId             文件 ID
 * @param uuid               文件 UUID
 * @param name               模型名称
 * @param reconstructionType 重建类型
 * @param url                模型文件签名 URL（3D Tiles tileset.json 入口）
 * @param thumbnailUrl       缩略图 URL
 * @param previewUrl         预览图 URL
 * @param latitude           模型中心纬度（度）
 * @param longitude          模型中心经度（度）
 * @param altitude           模型中心高程（米）
 * @param transform          4x4 变换矩阵（行主序，16 元素）
 * @param createdAt          创建时间（Unix 秒）
 */
public record ModelDetailDto(
        long fileId,
        String uuid,
        String name,
        String reconstructionType,
        String url,
        String thumbnailUrl,
        String previewUrl,
        double latitude,
        double longitude,
        double altitude,
        List<Double> transform,
        long createdAt
) { }
