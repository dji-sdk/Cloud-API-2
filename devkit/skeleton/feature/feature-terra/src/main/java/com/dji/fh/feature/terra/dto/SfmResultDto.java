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
 * SfM 空三结果 DTO.
 *
 * @param outputPrefix       输出路径前缀
 * @param cameraCount        相机数量
 * @param pointCount         点数量
 * @param matchedPercentage  匹配百分比
 */
public record SfmResultDto(
        String outputPrefix,
        int cameraCount,
        long pointCount,
        double matchedPercentage
) { }
