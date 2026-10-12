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
 * 质量报告 DTO.
 *
 * <p>SDK {@code TerraBuildQualityReportResult} 仅提供报告下载链接，
 * 不含评分和指标明细数据。
 *
 * @param qualityReportUrl 报告下载链接
 */
public record QualityReportDto(
        String qualityReportUrl
) { }
